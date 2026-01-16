/*
 *  Copyright 2024 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.meta.tree;

import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import elemental2.promise.Promise;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.logger.Level.DEBUG;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_SINGLETONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_TYPES_OPERATION;
import static org.jboss.hal.meta.tree.TraverseType.WILDCARD_RESOURCES;

/**
 * Represents a management model tree that provides functionality to traverse through various resources and apply actions or
 * filters based on specified parameters.
 * <p>
 * The {@code ModelTree} class relies on a dispatcher for executing operations and a statement context for resolving address
 * templates.
 */
@ApplicationScoped
public class ModelTree {

    private static final Logger logger = Logger.getLogger(ModelTree.class.getName());
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;

    @Inject
    public ModelTree(Dispatcher dispatcher, StatementContext statementContext) {
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
    }

    // ------------------------------------------------------ traverse

    /**
     * Traverses the management model tree starting from the specified address and performs the provided operation for each
     * traversed resource, while supporting exclusion and filtering by resource type.
     * <p>
     * The traversal process is controlled by a {@code TraverseContinuation} instance, allowing the operation to be aborted.
     * This method returns a promise resolving to a {@code TraverseContext} object that captures the state of the traversal.
     * <p>
     * This method manages the continuation's running state: it is set to {@code true} when traversal begins and reset to
     * {@code false} when the operation completes or is aborted.
     *
     * @param <T>          The type of result returned by the operation.
     * @param continuation The {@code TraverseContinuation} instance to control the traversal process. The traversal can be
     *                     aborted using this object.
     * @param start        The starting point for traversal, specified as an {@code AddressTemplate}. This can be a full address
     *                     or a wildcard address.
     * @param exclude      A set of strings representing resource addresses to exclude from traversal. Partial addresses (e.g.,
     *                     prefixes) can also be included to filter certain paths.
     * @param types        A set of {@code TraverseType} values specifying the resource types to include in the traversal.
     * @param operation    The {@code TraverseOperation} implementation to be performed on each resource during traversal. This
     *                     operation handles logic specific to the traversed resource.
     * @param consumer     A {@code TraverseConsumer} function that consumes the results of the operation and the traversal
     *                     context for each resource.
     * @return A {@code Promise} resolving to an instance of {@code TraverseContext} that encapsulates information about the
     * traversal, including its progress and outcomes.
     */
    public <T> Promise<TraverseContext> traverse(
            TraverseContinuation continuation,
            AddressTemplate start,
            Set<String> exclude,
            Set<TraverseType> types,
            TraverseOperation<T> operation,
            TraverseConsumer<T> consumer) {
        if (logger.isEnabled(DEBUG)) {
            logger.debug("Traverse %s, exclude: %s, type: %s", start, exclude,
                    types.stream().map(TraverseType::name).collect(toList()));
        }
        continuation.running = true;
        TraverseContext context = new TraverseContext();
        return read(continuation, context, start, exclude, types, operation, consumer)
                .finally_(() -> continuation.running = false);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> Promise<TraverseContext> read(TraverseContinuation continuation, TraverseContext context,
            AddressTemplate template, Set<String> excludes,
            Set<TraverseType> types, TraverseOperation<T> operation, TraverseConsumer<T> consumer) {
        if (continuation.running) {
            return operation.execute(template, statementContext)
                    .then(result -> {
                        if (types.contains(WILDCARD_RESOURCES) || template.fullyQualified()) {
                            logger.debug("✓ %s", template);
                            context.recordAccepted();
                            consumer.accept(template, result, context);
                        }
                        return readChildren(context, template, types);
                    })
                    .then(children -> {
                        context.recordProgress(children.size());
                        Promise[] promises = children.stream()
                                .filter(child -> !excluded(child, excludes))
                                .map(child -> read(continuation, context, child, excludes, types, operation, consumer))
                                .toArray(Promise[]::new);
                        return Promise.all(promises).then(__ -> Promise.resolve(context));
                    })
                    .catch_((error) -> {
                        ResourceAddress address = template.resolve(statementContext);
                        context.recordFailed(address.toString(), new Operation.Builder(address, "unknown").build());
                        return Promise.resolve(context);
                    });
        } else {
            logger.debug("Traversal aborted");
            return Promise.resolve(context);
        }
    }

    private Promise<List<AddressTemplate>> readChildren(TraverseContext context, AddressTemplate template,
            Set<TraverseType> traverseType) {
        if ("*".equals(template.last().value)) {

            // template:  /a=b/c=*
            // operation: /a=b:read-children-names(child-type=c)
            String resource = template.last().key;
            AddressTemplate parent = template.parent();
            ResourceAddress resourceAddress = parent.resolve(statementContext);
            Operation operation = new Operation.Builder(resourceAddress, READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, resource)
                    .param(INCLUDE_SINGLETONS, traverseType.contains(TraverseType.NON_EXISTING_SINGLETONS))
                    .build();
            logger.debug("⮑ %s", operation.asCli());
            return dispatcher.execute(operation, false)
                    .then(result -> Promise.resolve(result.asList().stream()
                            .map(modelNode -> parent.append(resource, modelNode.asString()))
                            .collect(toList())))
                    .catch_(__ -> {
                        context.recordFailed(resourceAddress.toString(), operation);
                        return Promise.resolve(emptyList());
                    });
        } else {

            // template:  /a=b/c=d
            // operation: /a=b/c=d:read-children-types()
            ResourceAddress resourceAddress = template.resolve(statementContext);
            Operation operation = new Operation.Builder(resourceAddress, READ_CHILDREN_TYPES_OPERATION)
                    .build();
            logger.debug("⮑ %s", operation.asCli());
            return dispatcher.execute(operation, false)
                    .then(result -> Promise.resolve(result.asList().stream()
                            .map(modelNode -> template.append(modelNode.asString(), "*"))
                            .collect(toList())))
                    .catch_(__ -> {
                        context.recordFailed(resourceAddress.toString(), operation);
                        return Promise.resolve(emptyList());
                    });
        }
    }

    private boolean excluded(AddressTemplate template, Set<String> excludes) {
        for (String exclude : excludes) {
            if (template.template.startsWith(exclude)) {
                return true;
            }
        }
        return false;
    }
}

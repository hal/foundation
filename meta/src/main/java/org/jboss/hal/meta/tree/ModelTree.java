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
import java.util.function.BiConsumer;

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
     * Traverses through the management model tree based on the specified parameters and invokes a consumer for each resource
     * address.
     * <p>
     * The method returns a promise that resolves to a {@link TraverseContext} when the model tree has been fully traversed. The
     * traversal can be controlled with an instance of {@link TraverseContinuation} that has to be passed as parameter. Call
     * {@link TraverseContinuation#stop()} to abort the traversal. In that case the promise is resolved with the current
     * context. DMR errors are caught by this method and won't cause the promise to be rejected. Other errors will reject the
     * promise, though.
     * <p>
     * The {@linkplain TraverseContinuation#running() running state} of the continuation is controlled by this method: It is
     * set to {@code true} when the traversal starts and to {@code false} if the traversal ends, fails, or has been aborted by
     * calling {@link TraverseContinuation#stop()}.
     *
     * @param continuation The continuation controlling the traversal process.
     * @param template     The address template used as the starting point for traversal. Can be a fully qualified resource
     *                     address or a wildcard address like {@code /subsystems=*}
     * @param exclude      A set of strings representing the resources to be excluded during the traversal. Can also be just the
     *                     beginning of a resource address such as {@code /core-service}
     * @param traverseType A set of TraverseType enums indicating the types of resources to be included in the traversal.
     * @param consumer     A function to be invoked for each traversed resource, receiving the resource's address template and
     *                     the {@linkplain TraverseContext traversal context}.
     * @return A Promise that resolves to a TraverseContext object after the traversal is completed.
     */
    public Promise<TraverseContext> traverse(TraverseContinuation continuation, AddressTemplate template,
            Set<String> exclude, Set<TraverseType> traverseType, BiConsumer<AddressTemplate, TraverseContext> consumer) {
        if (logger.isEnabled(DEBUG)) {
            logger.debug("Traverse %s, exclude: %s, type: %s", template, exclude,
                    traverseType.stream().map(TraverseType::name).collect(toList()));
        }
        continuation.running = true;
        TraverseContext context = new TraverseContext();
        return read(continuation, context, template, exclude, traverseType, consumer)
                .finally_(() -> continuation.running = false);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Promise<TraverseContext> read(TraverseContinuation continuation, TraverseContext context,
            AddressTemplate template, Set<String> excludes, Set<TraverseType> traverseType,
            BiConsumer<AddressTemplate, TraverseContext> addressConsumer) {
        if (continuation.running) {
            return readChildren(context, template, traverseType)
                    .then(children -> {
                        context.recordProgress(children.size());
                        Promise[] promises = children.stream()
                                .filter(child -> !excluded(child, excludes))
                                .peek(child -> {
                                    logger.debug("… %s", child);
                                    if (traverseType.contains(WILDCARD_RESOURCES) || child.fullyQualified()) {
                                        logger.debug("✓ %s", child);
                                        context.recordAccepted();
                                        addressConsumer.accept(child, context);
                                    }
                                })
                                .map(child -> read(continuation, context, child, excludes, traverseType, addressConsumer))
                                .toArray(Promise[]::new);
                        return Promise.all(promises).then(__ -> Promise.resolve(context));
                    });
        } else {
            logger.debug("Traversal aborted");
            return Promise.resolve(context);
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
}

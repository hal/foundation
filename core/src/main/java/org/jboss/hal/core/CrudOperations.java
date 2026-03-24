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
package org.jboss.hal.core;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRepository;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.StatementContextResolver;
import org.patternfly.core.Tuple;

import elemental2.promise.Promise;

import static java.util.Arrays.asList;
import static org.jboss.elemento.flow.Flow.parallel;
import static org.jboss.hal.core.LabelBuilder.labelBuilder;
import static org.jboss.hal.core.Notification.error;
import static org.jboss.hal.core.Notification.success;
import static org.jboss.hal.core.Notification.warning;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;
import static org.patternfly.core.Tuple.tuple;

/**
 * The CrudOperations class provides methods for performing create, update, and delete operations on resources represented by
 * AddressTemplate objects. It uses the {@link Dispatcher} to execute operations.
 * <p>
 * Other than emitting {@linkplain Notification notifications} for success and error, no UI is provided by this class. If you're
 * looking for modals for creating or deleting resources, refer to {@code org.jboss.hal.ui.resource.ResourceDialogs}.
 */
@ApplicationScoped
public class CrudOperations {

    private final Dispatcher dispatcher;
    private final MetadataRepository metadataRepository;
    private final StatementContext statementContext;
    private final Notifications notifications;

    @Inject
    public CrudOperations(Dispatcher dispatcher, MetadataRepository metadataRepository, StatementContext statementContext,
            Notifications notifications) {
        this.dispatcher = dispatcher;
        this.metadataRepository = metadataRepository;
        this.statementContext = statementContext;
        this.notifications = notifications;
    }

    // ------------------------------------------------------ create

    /**
     * Creates a new resource at the specified address template with the given resource data.
     *
     * @param template the address template that specifies where the resource should be created
     * @param resource the configuration data of the resource to be created
     * @return a promise that resolves with the created resource model node upon successful addition
     */
    public Promise<ModelNode> create(AddressTemplate template, ModelNode resource) {
        Operation operation = new Operation.Builder(template.resolve(statementContext), ADD)
                .payload(resource)
                .build();
        return dispatcher.execute(operation)
                .then(__ -> {
                    resource.get(NAME).set(template.last().value);
                    notifications.send(success("Resource added", typeName(template) + " has been successfully added."));
                    return Promise.resolve(resource);
                })
                .catch_(error -> {
                    notifications.send(error("Failed to add resource",
                            "An error occurred while adding " + typeName(template) + ".")
                            .details(String.valueOf(error), true));
                    return null;
                });
    }

    // ------------------------------------------------------ update

    public Promise<CompositeResult> update(AddressTemplate template, List<Operation> operations) {
        if (!operations.isEmpty()) {
            Composite composite = new Composite(operations);
            return dispatcher.execute(composite)
                    .then(result -> {
                        notifications.send(
                                success("Update successful", typeName(template) + " has been successfully updated."));
                        return Promise.resolve(result);
                    })
                    .catch_(error -> {
                        notifications.send(error("Failed to update resource",
                                "An error occurred while updating " + typeName(template) + ".")
                                .details(String.valueOf(error), true));
                        return null;
                    });
        } else {
            notifications.send(warning("Not modified", typeName(template) + " has not been modified."));
            return Promise.resolve(new CompositeResult(new ModelNode()));
        }
    }

    // ------------------------------------------------------ read

    public Promise<ModelNode> read(AddressTemplate template) {
        Operation operation = new Operation.Builder(template.resolve(statementContext), READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .build();
        return dispatcher.execute(operation)
                .then(Promise::resolve)
                .catch_(error -> {
                    notifications.send(error("Failed to read resource",
                            "An error occurred while reading " + typeName(template) + ".")
                            .details(String.valueOf(error), true));
                    return null;
                });
    }

    public Promise<Tuple<ModelNode, Metadata>> readWithMetadata(AddressTemplate template) {
        Task<FlowContext> resourceTask = context -> read(template).then(result -> {
            context.set("resource", result);
            return context.resolve();
        });
        Task<FlowContext> metadataTask = context -> metadataRepository.lookup(template).then(result -> {
            context.set("metadata", result);
            return context.resolve();
        });
        return parallel(new FlowContext(), asList(resourceTask, metadataTask))
                .then(context -> {
                    ModelNode resource = context.get("resource");
                    Metadata metadata = context.get("metadata");
                    return Promise.resolve(tuple(resource, metadata));
                })
                .catch_(error -> {
                    notifications.send(error("Failed to read resource",
                            "An error occurred while reading " + typeName(template) + ".")
                            .details(String.valueOf(error), true));
                    return null;
                });
    }

    // ------------------------------------------------------ delete

    public Promise<ModelNode> delete(AddressTemplate template) {
        Operation operation = new Operation.Builder(template.resolve(statementContext), REMOVE).build();
        return dispatcher.execute(operation)
                .then(result -> {
                    notifications.send(success("Resource deleted", typeName(template) + " has been successfully deleted."));
                    return Promise.resolve(result);
                })
                .catch_(error -> {
                    notifications.send(error("Failed to delete resource",
                            "An error occurred while deleting " + typeName(template) + ".")
                            .details(String.valueOf(error), true));
                    return null;
                });
    }

    // ------------------------------------------------------ internal

    private String typeName(AddressTemplate template) {
        AddressTemplate resolvedTemplate = new StatementContextResolver(statementContext).resolve(template);
        String type = resolvedTemplate.last().key;
        String failSafeType = type == null ? "Management model" : labelBuilder(type);
        String name = resolvedTemplate.last().value;
        return name != null ? failSafeType + " " + name : failSafeType;
    }
}

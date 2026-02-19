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

import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.StatementContextResolver;

import elemental2.promise.Promise;

import static org.jboss.hal.core.LabelBuilder.labelBuilder;
import static org.jboss.hal.core.Notification.error;
import static org.jboss.hal.core.Notification.success;
import static org.jboss.hal.core.Notification.warning;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;

/**
 * The CrudOperations class provides methods for performing create, update, and delete operations on resources represented by
 * AddressTemplate objects. It uses the {@link Dispatcher} to execute operations.
 * <p>
 * Each operation has built-in success messages. The caller must handle failures.
 */
@ApplicationScoped
public class CrudOperations {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Notifications notifications;

    @Inject
    public CrudOperations(Dispatcher dispatcher, StatementContext statementContext, Notifications notifications) {
        this.dispatcher = dispatcher;
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

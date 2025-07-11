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
import org.jboss.hal.env.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.MetadataRepository;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.StatementContextResolver;
import org.patternfly.component.alert.AlertDescription;
import org.patternfly.core.Tuple;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.core.Notifications.success;
import static org.jboss.hal.core.Notifications.warning;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;
import static org.jboss.hal.resources.Dataset.crudMessageName;
import static org.jboss.hal.resources.Dataset.crudMessageType;
import static org.patternfly.component.alert.AlertDescription.alertDescription;
import static org.patternfly.core.Tuple.tuple;

/**
 * The CrudOperations class provides methods for performing create, update, and delete operations on resources represented by
 * AddressTemplate objects. It uses the {@link Dispatcher} to execute operations.
 * <p>
 * Each operation has built-in success messages. The caller must handle failures.
 */
@ApplicationScoped
public class CrudOperations {

    private final Environment environment;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final MetadataRepository metadataRepository;

    @Inject
    public CrudOperations(Environment environment, Dispatcher dispatcher, StatementContext statementContext,
            MetadataRepository metadataRepository) {
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.metadataRepository = metadataRepository;
    }

    // ------------------------------------------------------ create

    public Promise<ModelNode> create(AddressTemplate template, ModelNode resource) {
        Tuple<String, String> typeName = typeName(template);
        Operation operation = new Operation.Builder(template.resolve(statementContext), ADD)
                .payload(resource)
                .build();
        return dispatcher.execute(operation)
                .then(result -> {
                    success("Resource added", description(typeName).add(" has been successfully added."));
                    return Promise.resolve(result);
                });
    }

    // ------------------------------------------------------ update

    public Promise<CompositeResult> update(AddressTemplate template, List<Operation> operations) {
        Tuple<String, String> typeName = typeName(template);
        if (!operations.isEmpty()) {
            Composite composite = new Composite(operations);
            return dispatcher.execute(composite)
                    .then(result -> {
                        success("Update successful", description(typeName).add(" has been successfully updated."));
                        return Promise.resolve(result);
                    });
        } else {
            warning("Not modified", description(typeName).add(" has not been modified."));
            return Promise.resolve(new CompositeResult(new ModelNode()));
        }
    }

    // ------------------------------------------------------ delete

    public Promise<ModelNode> delete(AddressTemplate template) {
        Tuple<String, String> typeName = typeName(template);
        Operation operation = new Operation.Builder(template.resolve(statementContext), REMOVE).build();
        return dispatcher.execute(operation)
                .then(result -> {
                    success("Resource deleted",
                            description(typeName).add(" has been successfully deleted."));
                    return Promise.resolve(result);
                });
    }

    // ------------------------------------------------------ internal

    private Tuple<String, String> typeName(AddressTemplate template) {
        AddressTemplate resolvedTemplate = new StatementContextResolver(statementContext).resolve(template);
        String type = resolvedTemplate.last().key;
        String name = resolvedTemplate.last().value;
        return tuple(type, name);
    }

    private AlertDescription description(Tuple<String, String> typeName) {
        return alertDescription()
                .add(typeElement(typeName.key))
                .add(" ")
                .add(nameElement(typeName.value));
    }

    private HTMLElement typeElement(String type) {
        String failSafeType = type == null ? "Management model" : new LabelBuilder().label(type);
        return span().data(crudMessageType, type).text(failSafeType).element();
    }

    private HTMLElement nameElement(String name) {
        String failSafeName = name == null ? "n/a" : name;
        return span().data(crudMessageName, name).text(failSafeName).element();
    }
}

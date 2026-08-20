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
package org.jboss.hal.op.configuration.datasource;

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.ui.resource.event.ResourceEvents;
import org.jboss.hal.ui.resource.shell.ResourceHeader;
import org.jboss.hal.ui.resource.spi.ResourceHeaderProvider;
import org.patternfly.component.label.Label;

import elemental2.promise.Promise;

import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.patternfly.component.Severity.success;
import static org.patternfly.component.label.Label.label;
import static org.patternfly.icon.IconSets.fas.database;
import static org.patternfly.icon.IconSets.rhUi.ban;
import static org.patternfly.style.Color.grey;

@ApplicationScoped
public class DataSourceHeader implements ResourceHeaderProvider {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private Label statusLabel;

    @Inject
    public DataSourceHeader(Dispatcher dispatcher, StatementContext statementContext) {
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
    }

    @Override
    public Set<AddressTemplate> scopes() {
        Set<AddressTemplate> scopes = new HashSet<>();
        scopes.add(AddressTemplate.ofTrusted("subsystem=datasources/data-source=*"));
        scopes.add(AddressTemplate.ofTrusted("subsystem=datasources/xa-data-source=*"));
        return scopes;
    }

    @Override
    public boolean appliesTo(Environment environment, AddressTemplate template) {
        return template.fullyQualified();
    }

    @Override
    public Promise<ResourceHeader> createHeader(AddressTemplate template, Metadata metadata,
            ResourceHeader defaultHeader) {
        return readDataSource(template).then(dataSource -> Promise.resolve(dataSourceHeader(dataSource, defaultHeader)));
    }

    private Promise<ModelNode> readDataSource(AddressTemplate template) {
        Operation operation = new Operation.Builder(template.resolve(statementContext), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        return dispatcher.execute(operation);
    }

    private ResourceHeader dataSourceHeader(ModelNode dataSource, ResourceHeader defaultHeader) {
        statusLabel = statusLabel(dataSource);
        ResourceHeader resourceHeader = defaultHeader
                .icon(database())
                .addLabel(statusLabel);

        ResourceEvents.Modified.listen(resourceHeader.element(), details ->
                readDataSource(details.template).then(modifiedDataSource -> {
                    failSafeRemoveFromParent(statusLabel);
                    statusLabel = statusLabel(modifiedDataSource);
                    resourceHeader.addLabel(statusLabel);
                    return null;
                }));

        return resourceHeader;
    }

    private Label statusLabel(ModelNode dataSource) {
        return dataSource.hasDefined(ENABLED) && dataSource.get(ENABLED).asBoolean()
                ? label("enabled").status(success).outline()
                : label("disabled", grey).outline().icon(ban());
    }
}

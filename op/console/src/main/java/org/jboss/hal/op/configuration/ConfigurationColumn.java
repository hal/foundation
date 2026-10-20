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
package org.jboss.hal.op.configuration;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.hal.op.configuration.socket.SocketBindingGroupColumn;
import org.jboss.hal.resources.OuiaIds;
import org.jboss.hal.ui.finder.ColumnProvider;
import org.jboss.hal.ui.finder.ColumnRegistry;
import org.patternfly.extension.finder.FinderColumn;

import static org.jboss.hal.op.configuration.ConfigurationDescriptions.INTERFACE_DESCRIPTION_1;
import static org.jboss.hal.op.configuration.ConfigurationDescriptions.INTERFACE_DESCRIPTION_2;
import static org.jboss.hal.op.configuration.ConfigurationDescriptions.PATH_DESCRIPTION_1;
import static org.jboss.hal.op.configuration.ConfigurationDescriptions.PATH_DESCRIPTION_2;
import static org.jboss.hal.op.configuration.ConfigurationDescriptions.SOCKET_BINDING_DESCRIPTION;
import static org.jboss.hal.op.configuration.ConfigurationDescriptions.SUBSYSTEM_DESCRIPTION;
import static org.jboss.hal.op.configuration.ConfigurationDescriptions.SYSTEM_PROPERTY_DESCRIPTION;
import static org.jboss.hal.ui.finder.FinderBricks.stackPreview;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.extension.finder.FinderColumn.finderColumn;
import static org.patternfly.extension.finder.FinderColumnHeader.finderColumnHeader;
import static org.patternfly.extension.finder.FinderItem.finderItem;
import static org.patternfly.layout.stack.StackItem.stackItem;

/** Top-level finder column listing the configuration categories (subsystems, interfaces, paths, etc.). */
@Dependent
public class ConfigurationColumn implements ColumnProvider {

    /** Column identifier used for registration and OUIA test IDs. */
    public static final String ID = "configuration-column";
    static final String INTERFACES_ITEM = "interfaces-item";
    static final String PATHS_ITEM = "paths-item";
    static final String SOCKET_BINDINGS_ITEM = "socket-bindings-item";
    static final String SUBSYSTEMS_ITEM = "subsystems-item";
    static final String SYSTEM_PROPERTIES_ITEM = "system-properties-item";

    private final Instance<ColumnRegistry> registry;

    @Inject
    public ConfigurationColumn(Instance<ColumnRegistry> registry) {
        this.registry = registry;
    }

    @Override
    public String identifier() {
        return ID;
    }

    @Override
    public FinderColumn get() {
        return finderColumn(ID).ouiaId(OuiaIds.ouia(ID))
                .addHeader(finderColumnHeader("Configuration"))
                .addItem(finderItem(SUBSYSTEMS_ITEM, "Subsystems").ouiaId(OuiaIds.ouia(SUBSYSTEMS_ITEM))
                        .nextColumn(registry.get().column(SubsystemColumn.ID))
                        .onPreview((item, preview) ->
                                stackPreview(preview, "Subsystems", stack -> stack
                                        .addItem(stackItem().add(content(p).editorial().html(SUBSYSTEM_DESCRIPTION))))))
                .addItem(finderItem(INTERFACES_ITEM, "Interfaces").ouiaId(OuiaIds.ouia(INTERFACES_ITEM))
                        .nextColumn(registry.get().column(InterfaceColumn.ID))
                        .onPreview((item, preview) ->
                                stackPreview(preview, "Interfaces", stack -> stack
                                        .addItem(stackItem()
                                                .add(content(p).editorial().html(INTERFACE_DESCRIPTION_1))
                                                .add(content(p).editorial().html(INTERFACE_DESCRIPTION_2))))))
                .addItem(finderItem(SOCKET_BINDINGS_ITEM, "Socket Bindings").ouiaId(OuiaIds.ouia(SOCKET_BINDINGS_ITEM))
                        .nextColumn(registry.get().column(SocketBindingGroupColumn.ID))
                        .onPreview((item, preview) ->
                                stackPreview(preview, "Socket Bindings", stack -> stack
                                        .addItem(stackItem().add(content(p).editorial().html(SOCKET_BINDING_DESCRIPTION))))))
                .addItem(finderItem(PATHS_ITEM, "Paths").ouiaId(OuiaIds.ouia(PATHS_ITEM))
                        .nextColumn(registry.get().column(PathColumn.ID))
                        .onPreview((item, preview) ->
                                stackPreview(preview, "Paths", stack -> stack
                                        .addItem(stackItem()
                                                .add(content(p).editorial().html(PATH_DESCRIPTION_1))
                                                .add(content(p).editorial().html(PATH_DESCRIPTION_2))))))
                .addItem(finderItem(SYSTEM_PROPERTIES_ITEM, "System Properties").ouiaId(OuiaIds.ouia(SYSTEM_PROPERTIES_ITEM))
                        .nextColumn(registry.get().column(SystemPropertyColumn.ID))
                        .onPreview((item, preview) ->
                                stackPreview(preview, "System Properties", stack -> stack
                                        .addItem(stackItem().add(content(p).editorial().html(SYSTEM_PROPERTY_DESCRIPTION))))));
    }
}

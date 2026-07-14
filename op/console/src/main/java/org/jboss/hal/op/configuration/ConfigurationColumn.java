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
import org.jboss.hal.op.finder.ColumnProvider;
import org.jboss.hal.op.finder.ColumnRegistry;
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
        return finderColumn(ID)
                .addHeader(finderColumnHeader("Configuration"))
                .addItem(finderItem("subsystems-item", "Subsystems")
                        .nextColumn(registry.get().column(SubsystemColumn.ID))
                        .onPreview((item, preview) ->
                                stackPreview(preview, "Subsystems", stack -> stack
                                        .addItem(stackItem().add(content(p).editorial().html(SUBSYSTEM_DESCRIPTION))))))
                .addItem(finderItem("interfaces-item", "Interfaces")
                        .nextColumn(registry.get().column(InterfaceColumn.ID))
                        .onPreview((item, preview) ->
                                stackPreview(preview, "Interfaces", stack -> stack
                                        .addItem(stackItem()
                                                .add(content(p).editorial().html(INTERFACE_DESCRIPTION_1))
                                                .add(content(p).editorial().html(INTERFACE_DESCRIPTION_2))))))
                .addItem(finderItem("socket-bindings-item", "Socket Bindings")
                        .nextColumn(registry.get().column(SocketBindingGroupColumn.ID))
                        .onPreview((item, preview) ->
                                stackPreview(preview, "Socket Bindings", stack -> stack
                                        .addItem(stackItem().add(content(p).editorial().html(SOCKET_BINDING_DESCRIPTION))))))
                .addItem(finderItem("paths-item", "Paths")
                        .nextColumn(registry.get().column(PathColumn.ID))
                        .onPreview((item, preview) ->
                                stackPreview(preview, "Paths", stack -> stack
                                        .addItem(stackItem()
                                                .add(content(p).editorial().html(PATH_DESCRIPTION_1))
                                                .add(content(p).editorial().html(PATH_DESCRIPTION_2))))))
                .addItem(finderItem("system-properties-item", "System Properties")
                        .nextColumn(registry.get().column(SystemPropertyColumn.ID))
                        .onPreview((item, preview) ->
                                stackPreview(preview, "System Properties", stack -> stack
                                        .addItem(stackItem().add(content(p).editorial().html(SYSTEM_PROPERTY_DESCRIPTION))))));
    }
}

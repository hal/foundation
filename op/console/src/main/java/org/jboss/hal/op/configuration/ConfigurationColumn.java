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

import org.gwtproject.safehtml.shared.SafeHtml;
import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.jboss.hal.op.configuration.socket.SocketBindingGroupColumn;
import org.jboss.hal.op.finder.ColumnProvider;
import org.jboss.hal.op.finder.ColumnRegistry;
import org.patternfly.extension.finder.FinderColumn;

import static org.jboss.hal.ui.BuildingBlocks.stackPreview;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.extension.finder.FinderColumn.finderColumn;
import static org.patternfly.extension.finder.FinderColumnHeader.finderColumnHeader;
import static org.patternfly.extension.finder.FinderItem.finderItem;
import static org.patternfly.layout.stack.StackItem.stackItem;

@Dependent
public class ConfigurationColumn implements ColumnProvider {

    public static final String ID = "configuration-column";

    // TODO Replace hard-coded previews with HTML templates based on https://handlebarsjs.com/
    // language=html
    private static final SafeHtml SUBSYSTEM_DESCRIPTION = SafeHtmlUtils.fromSafeConstant(
            "A set of subsystem configurations. A subsystem is an added set of capabilities added to the core server by an extension. As such a subsystem provides servlet handling capabilities, an EJB container, JTA support, etc.");
    // language=html
    private static final SafeHtml INTERFACE_DESCRIPTION_1 = SafeHtmlUtils.fromSafeConstant(
            "A logical name for a network interface / IP address / host name to which sockets can be bound. The <code>domain.xml</code>, <code>host.xml</code> and <code>standalone.xml</code> configurations all include a section where interfaces can be declared. Other sections of the configuration can then reference those interfaces by their logical name, rather than having to include the full details of the interface (which may vary on different machines).");
    // language=html
    private static final SafeHtml INTERFACE_DESCRIPTION_2 = SafeHtmlUtils.fromSafeConstant(
            "An interface configuration includes the logical name of the interface as well as information specifying the criteria to use for resolving the actual physical address to use.");
    // language=html
    private static final SafeHtml SOCKET_BINDING_DESCRIPTION = SafeHtmlUtils.fromSafeConstant(
            "A socket binding is a named configuration for a socket. The <code>domain.xml</code> and <code>standalone.xml</code> configurations both include a section where named socket configurations can be declared. Other sections of the configuration can then reference those sockets by their logical name, rather than having to include the full details of the socket configuration (which may vary on different machines).");
    // language=html
    private static final SafeHtml PATH_DESCRIPTION_1 = SafeHtmlUtils.fromSafeConstant(
            "A logical name for a filesystem path. The <code>domain.xml</code>, <code>host.xml</code> and <code>standalone.xml</code> configurations all include a section where paths can be declared. Other sections of the configuration can then reference those paths by their logical name, rather than having to include the full details of the path (which may vary on different machines).");
    // language=html
    private static final SafeHtml PATH_DESCRIPTION_2 = SafeHtmlUtils.fromSafeConstant(
            "For example, the logging subsystem configuration includes a reference to the <code>jboss.server.log.dir</code> path that points to the server&#39;s <code>log</code> directory.");
    // language=html
    private static final SafeHtml SYSTEM_PROPERTY_DESCRIPTION = SafeHtmlUtils.fromSafeConstant(
            "System property values can be set in a number of places in <code>domain.xml</code>, <code>host.xml</code> and <code>standalone.xml</code>. The values in <code>standalone.xml</code> are set as part of the server boot process. Values in <code>domain.xml</code> and <code>host.xml</code> are applied to servers when they are launched.");

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

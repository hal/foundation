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
package org.jboss.hal.op.configuration.socket;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.elemento.Id;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.op.finder.ColumnProvider;
import org.jboss.hal.op.finder.ColumnRegistry;
import org.patternfly.extension.finder.FinderColumn;

import static org.jboss.hal.op.finder.Columns.childResources;
import static org.jboss.hal.op.finder.Columns.metadataPreview;
import static org.jboss.hal.op.finder.Columns.resourceColumn;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.h1;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.extension.finder.FinderItem.finderItem;
import static org.patternfly.extension.finder.FinderItemActions.finderItemActions;

@Dependent
public class SocketBindingGroupColumn implements ColumnProvider {

    public static final String ID = "socket-binding-group-column";
    private final Instance<ColumnRegistry> registry;

    @Inject
    public SocketBindingGroupColumn(Instance<ColumnRegistry> registry) {
        this.registry = registry;
    }

    @Override
    public String identifier() {
        return ID;
    }

    @Override
    public FinderColumn get() {
        return resourceColumn(ID, "Socket Binding Group")
                .defaultSearch()
                .toggleSearch(column -> column.items().size() > 5)
                .addItems(childResources(__ -> AddressTemplate.of("socket-binding-group=*"),
                        node -> finderItem(Id.build(node.asString()))
                                .text(node.asString())
                                .addActions(finderItemActions()
                                        .addButton(button("View").control().small()))
                                .nextColumn(registry.get().column(SocketBindingTypeColumn.ID))))
                .onPreview(metadataPreview((name, metadata, preview) ->
                        preview.add(content(h1).text(name))
                                .add(content(p).editorial().text(metadata.resourceDescription().description()))));
    }
}

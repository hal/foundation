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

import org.jboss.elemento.Id;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.op.finder.ColumnProvider;
import org.jboss.hal.ui.resource.FinderSupport;
import org.patternfly.extension.finder.FinderColumn;
import org.patternfly.extension.finder.FinderItem;

import static org.jboss.hal.core.LabelBuilder.labelBuilderAllWords;
import static org.jboss.hal.core.Notification.nyi;
import static org.jboss.hal.ui.BuildingBlocks.stackPreview;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.resource.FinderSupport.childResources;
import static org.jboss.hal.ui.resource.FinderSupport.metadataPreview;
import static org.jboss.hal.ui.resource.ResourceDialogs.deleteResourceModal;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.extension.finder.FinderColumn.finderColumn;
import static org.patternfly.extension.finder.FinderColumnActions.finderColumnActions;
import static org.patternfly.extension.finder.FinderColumnHeader.finderColumnHeader;
import static org.patternfly.extension.finder.FinderItem.finderItem;
import static org.patternfly.extension.finder.FinderItemActions.finderItemActions;
import static org.patternfly.icon.IconSets.fas.externalLinkAlt;
import static org.patternfly.icon.IconSets.fas.plus;
import static org.patternfly.icon.IconSets.fas.redo;
import static org.patternfly.icon.IconSets.fas.trash;
import static org.patternfly.layout.stack.StackItem.stackItem;

@Dependent
public class SubsystemColumn implements ColumnProvider {

    public static final String ID = "subsystem-column";
    private static final AddressTemplate TEMPLATE = AddressTemplate.of("subsystem=*");

    @Override
    public String identifier() {
        return ID;
    }

    @Override
    public FinderColumn get() {
        FinderColumn column = finderColumn(ID);
        return column.addHeader(finderColumnHeader("Subsystem").addActions(finderColumnActions()
                        .addButton(button(plus()).plain().small().onClick((e, b) -> add(column)))
                        .addButton(button(redo()).plain().small().onClick((e, b) -> column.reload()))))
                .addItems(childResources(__ -> TEMPLATE, node -> finderItem(Id.build(node.asString()))
                        .text(labelBuilderAllWords(node.asString()))
                        .run(item -> item.addActions(finderItemActions()
                                .addButton(button(externalLinkAlt()).plain().small().onClick((e, b) -> view(item)))
                                .addButton(button(trash()).plain().small().onClick((e, b) -> remove(item)))))))
                .onPreview(metadataPreview((name, metadata, preview) ->
                        stackPreview(preview, labelBuilderAllWords(name), stack -> stack.addItem(stackItem()
                                .add(content(p).editorial().text(metadata.resourceDescription().description()))))));
    }

    private void add(FinderColumn column) {
        uic().notifications().send(nyi());
        column.reload();
    }

    private void view(FinderItem item) {
        uic().notifications().send(nyi());
    }

    private void remove(FinderItem item) {
        AddressTemplate template = item.get(FinderSupport.TEMPLATE_KEY);
        deleteResourceModal(template).then(node -> {
            if (node.isDefined()) { // undefined means canceled
                item.column().reload();
            }
            return null;
        });
    }
}

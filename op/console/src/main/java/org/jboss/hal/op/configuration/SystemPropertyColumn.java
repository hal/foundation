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
import jakarta.inject.Inject;

import org.jboss.elemento.Id;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.op.finder.ColumnProvider;
import org.jboss.hal.op.finder.Columns;
import org.patternfly.extension.finder.FinderColumn;
import org.patternfly.extension.finder.FinderItem;

import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.op.finder.Columns.childResources;
import static org.jboss.hal.ui.resource.ResourceDialogs.addResourceModal;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.h1;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;
import static org.patternfly.extension.finder.FinderColumnActions.finderColumnActions;
import static org.patternfly.extension.finder.FinderColumnHeader.finderColumnHeader;
import static org.patternfly.extension.finder.FinderItem.finderItem;
import static org.patternfly.extension.finder.FinderItemActions.finderItemActions;
import static org.patternfly.icon.IconSets.fas.plus;
import static org.patternfly.icon.IconSets.fas.redo;
import static org.patternfly.icon.IconSets.fas.trash;

@Dependent
public class SystemPropertyColumn implements ColumnProvider {

    public static final String ID = "system-property-column";
    private static final AddressTemplate TEMPLATE = AddressTemplate.of("system-property=*");

    private final CrudOperations crud;

    @Inject
    public SystemPropertyColumn(CrudOperations crud) {
        this.crud = crud;
    }

    @Override
    public String identifier() {
        return ID;
    }

    @Override
    public FinderColumn get() {
        return FinderColumn.finderColumn(ID)
                .run(column ->
                        column.addHeader(finderColumnHeader("System Property")
                                .addActions(finderColumnActions()
                                        .addButton(button(plus()).plain().small()
                                                .onClick((e, b) -> add(column)))
                                        .addButton(button(redo()).plain().small()
                                                .onClick((e, b) -> column.reload())))))
                .defaultSearch()
                .toggleSearch(column -> column.items().size() > 5)
                .addItems(childResources(__ -> TEMPLATE, this::item))
                .onPreview((item, preview) -> preview.add(content(h1).text(item.text()))
                        .add(descriptionList().horizontal()
                                .addItem(descriptionListGroup()
                                        .addTerm(descriptionListTerm("Value"))
                                        .addDescription(descriptionListDescription().run(dd -> {
                                            AddressTemplate template = item.get(Columns.TEMPLATE_KEY);
                                            crud.read(template).then(node -> {
                                                dd.text(node.get(VALUE).asString());
                                                return null;
                                            });
                                        })))));
    }

    private FinderItem item(ModelNode node) {
        FinderItem item = finderItem(Id.build(node.asString()));
        return item
                .text(node.asString())
                .addActions(finderItemActions()
                        .addButton(button(trash()).small()
                                .onClick((e, b) -> {
                                    AddressTemplate template = item.get(Columns.TEMPLATE_KEY);
                                    remove(item.column(), template);
                                })));
    }

    private void add(FinderColumn column) {
        addResourceModal(TEMPLATE, null, false).then(__ -> column.reload());
    }

    private void remove(FinderColumn column, AddressTemplate template) {
        crud.delete(template).then(__ -> column.reload());
    }
}

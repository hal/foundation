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
package org.jboss.hal.ui.modelbrowser;

import org.jboss.elemento.IsElement;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.model.filter.AccessTypeAttribute;
import org.jboss.hal.model.filter.DeprecatedAttribute;
import org.jboss.hal.model.filter.ExpressionAttribute;
import org.jboss.hal.model.filter.RequiredAttribute;
import org.jboss.hal.model.filter.StorageAttribute;
import org.jboss.hal.model.filter.TypesAttribute;
import org.jboss.hal.ui.filter.FilterLabels;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.core.ObservableValue;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.ui.filter.ItemCount.itemCount;
import static org.jboss.hal.ui.filter.NameTextInputGroup.nameFilterTextInputGroup;
import static org.jboss.hal.ui.filter.ReDeExMultiSelect.reDeExMultiSelect;
import static org.jboss.hal.ui.filter.StorageAccessTypeMultiSelect.storageAccessTypeMultiSelect;
import static org.jboss.hal.ui.filter.TypesMultiSelect.typesFilterMultiSelect;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarFilterContent.toolbarFilterContent;
import static org.patternfly.component.toolbar.ToolbarFilterLabelGroup.toolbarFilterLabelGroup;
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarGroupType.filterGroup;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.component.toolbar.ToolbarItemType.searchFilter;
import static org.patternfly.style.Classes.modifier;

class AttributesToolbar implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    static AttributesToolbar attributesToolbar(Filter<AttributeDescription> filter,
            ObservableValue<Integer> visible, ObservableValue<Integer> total) {
        return new AttributesToolbar(filter, visible, total);
    }

    // ------------------------------------------------------ instance

    private final Toolbar toolbar;

    private AttributesToolbar(Filter<AttributeDescription> filter,
            ObservableValue<Integer> visible, ObservableValue<Integer> total) {
        toolbar = toolbar().css(modifier("inset-none"))
                .addContent(toolbarContent()
                        .addItem(toolbarItem(searchFilter).add(nameFilterTextInputGroup(filter)))
                        .addGroup(toolbarGroup(filterGroup)
                                .addItem(toolbarItem().add(typesFilterMultiSelect(filter)))
                                .addItem(toolbarItem().add(reDeExMultiSelect(filter)))
                                .addItem(toolbarItem().add(storageAccessTypeMultiSelect(filter))))
                        .addItem(toolbarItem()
                                .style("align-self", "center")
                                .css(modifier("align-right"))
                                .add(itemCount(visible, total, "attribute", "attributes"))))
                .addFilterContent(toolbarFilterContent()
                        .bindVisibility(filter,
                                TypesAttribute.NAME,
                                RequiredAttribute.NAME,
                                DeprecatedAttribute.NAME,
                                StorageAttribute.NAME,
                                AccessTypeAttribute.NAME,
                                ExpressionAttribute.NAME)
                        .addGroup(toolbarGroup()
                                .add(toolbarFilterLabelGroup(filter, "Type")
                                        .filterAttributes(TypesAttribute.NAME)
                                        .filterToLabels(FilterLabels::typeLabels))
                                .add(toolbarFilterLabelGroup(filter, "Status")
                                        .filterAttributes(RequiredAttribute.NAME, DeprecatedAttribute.NAME,
                                                ExpressionAttribute.NAME)
                                        .filterToLabels(FilterLabels::reDeExLabels))
                                .add(toolbarFilterLabelGroup(filter, "Mode")
                                        .filterAttributes(StorageAttribute.NAME, AccessTypeAttribute.NAME)
                                        .filterToLabels(FilterLabels::storageAccessTypeLabels)))
                        .addItem(toolbarItem()
                                .add(button("Clear all filters").link().inline().onClick((e, c) -> filter.resetAll()))));
    }

    @Override
    public HTMLElement element() {
        return toolbar.element();
    }
}

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
package org.jboss.hal.ui.resource;

import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.meta.security.ElementGuard;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.model.filter.AccessTypeAttribute;
import org.jboss.hal.model.filter.DefinedAttribute;
import org.jboss.hal.model.filter.DeprecatedAttribute;
import org.jboss.hal.model.filter.ExpressionAttribute;
import org.jboss.hal.model.filter.RequiredAttribute;
import org.jboss.hal.model.filter.StorageAttribute;
import org.jboss.hal.model.filter.TypesAttribute;
import org.jboss.hal.ui.filter.FilterLabels;
import org.jboss.hal.ui.filter.NameSearchInput;
import org.jboss.hal.ui.resource.ResourceManager.State;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.component.toolbar.ToolbarContent;
import org.patternfly.component.toolbar.ToolbarGroup;
import org.patternfly.component.toolbar.ToolbarItem;
import org.patternfly.core.ObservableValue;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.hal.ui.filter.DeReDeExMultiSelect.deReDeExMultiSelect;
import static org.jboss.hal.ui.filter.ItemCount.itemCount;
import static org.jboss.hal.ui.filter.StorageAccessTypeMultiSelect.storageAccessTypeMultiSelect;
import static org.jboss.hal.ui.filter.TypesMultiSelect.typesFilterMultiSelect;
import static org.jboss.hal.ui.resource.ResourceManager.State.EDIT;
import static org.jboss.hal.ui.resource.ResourceManager.State.VIEW;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarFilterContent.toolbarFilterContent;
import static org.patternfly.component.toolbar.ToolbarFilterLabelGroup.toolbarFilterLabelGroup;
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarGroupType.actionGroupPlain;
import static org.patternfly.component.toolbar.ToolbarGroupType.buttonGroup;
import static org.patternfly.component.toolbar.ToolbarGroupType.filterGroup;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.component.toolbar.ToolbarItemType.searchFilter;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.icon.IconSets.fas.edit;
import static org.patternfly.icon.IconSets.fas.powerOff;
import static org.patternfly.icon.IconSets.fas.redo;
import static org.patternfly.popper.Placement.auto;
import static org.patternfly.style.Classes.modifier;

class ResourceToolbar implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    static ResourceToolbar resourceToolbar(ResourceManager resourceManager, Filter<ResourceAttribute> filter,
            ObservableValue<Integer> visible, ObservableValue<Integer> total) {
        return new ResourceToolbar(resourceManager, filter, visible, total);
    }

    // ------------------------------------------------------ instance

    private final Toolbar toolbar;
    private final ToolbarContent toolbarContent;
    private final ToolbarGroup viewActionGroup;
    private final ToolbarGroup editActionGroup;
    private final ToolbarItem resetItem;
    private final ToolbarItem editItem;

    private ResourceToolbar(ResourceManager resourceManager, Filter<ResourceAttribute> filter,
            ObservableValue<Integer> visible, ObservableValue<Integer> total) {
        String resetId = Id.unique("reset");
        String refreshId = Id.unique("refresh");
        String editId = Id.unique("edit");

        resetItem = toolbarItem()
                .add(button().id(resetId).plain().icon(powerOff()).onClick((e, b) -> resourceManager.reset()))
                .add(tooltip(By.id(resetId),
                        "Reset attributes to their initial or default value. Applied only to nillable attributes without relationships to other attributes.")
                        .placement(auto));
        ToolbarItem refreshItem = toolbarItem()
                .add(button().id(refreshId).plain().icon(redo()).onClick((e, b) -> resourceManager.refresh()))
                .add(tooltip(By.id(refreshId), "Refresh").placement(auto));
        editItem = toolbarItem()
                .add(button().id(editId).plain().icon(edit()).onClick((e, b) -> resourceManager.load(EDIT)))
                .add(tooltip(By.id(editId), "Edit resource").placement(auto));
        viewActionGroup = toolbarGroup(actionGroupPlain).css(modifier("align-right"))
                .addItem(refreshItem)
                .addItem(resetItem)
                .addItem(editItem);

        ToolbarItem saveItem = toolbarItem()
                .add(button("Save").primary().onClick((e, b) -> resourceManager.save()));
        ToolbarItem cancelItem = toolbarItem()
                .add(button("Cancel").secondary().onClick((e, b) -> resourceManager.cancel()));
        editActionGroup = toolbarGroup(buttonGroup).css(modifier("align-right"))
                .addItem(saveItem)
                .addItem(cancelItem);

        toolbar = toolbar().css(modifier("inset-none"))
                .addContent(toolbarContent = toolbarContent()
                        .addItem(toolbarItem(searchFilter).add(NameSearchInput.nameSearchInput(filter)))
                        .addGroup(toolbarGroup(filterGroup)
                                .addItem(toolbarItem().add(typesFilterMultiSelect(filter)))
                                .addItem(toolbarItem().add(deReDeExMultiSelect(filter)))
                                .addItem(toolbarItem().add(storageAccessTypeMultiSelect(filter))))
                        .addItem(toolbarItem()
                                .style("align-self", "center")
                                .add(itemCount(visible, total, "attribute", "attributes"))))
                .addFilterContent(toolbarFilterContent()
                        .bindVisibility(filter,
                                TypesAttribute.NAME,
                                DefinedAttribute.NAME,
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
                                        .filterAttributes(DefinedAttribute.NAME,
                                                RequiredAttribute.NAME,
                                                DeprecatedAttribute.NAME,
                                                ExpressionAttribute.NAME)
                                        .filterToLabels(FilterLabels::deReDeExLabels))
                                .add(toolbarFilterLabelGroup(filter, "Mode")
                                        .filterAttributes(StorageAttribute.NAME, AccessTypeAttribute.NAME)
                                        .filterToLabels(FilterLabels::storageAccessTypeLabels)))
                        .addItem(toolbarItem()
                                .add(button("Clear all filters").link().inline()
                                        .onClick((e, c) -> filter.resetAll()))));
    }

    @Override
    public HTMLElement element() {
        return toolbar.element();
    }

    void adjust(State state, SecurityContext securityContext) {
        if (state == VIEW) {
            failSafeRemoveFromParent(editActionGroup);
            ElementGuard.toggle(resetItem.element(), securityContext.writable());
            ElementGuard.toggle(editItem.element(), securityContext.writable());
            toolbarContent.addGroup(viewActionGroup);
        } else if (state == EDIT) {
            failSafeRemoveFromParent(viewActionGroup);
            toolbarContent.addGroup(editActionGroup);
        } else {
            failSafeRemoveFromParent(editActionGroup);
            failSafeRemoveFromParent(viewActionGroup);
        }
    }
}

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
package org.jboss.hal.ui.resource.manager;
import org.jboss.hal.ui.resource.ResourceAttribute;

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
import org.jboss.hal.resources.OuiaIds;
import org.jboss.hal.ui.filter.FilterLabels;
import org.jboss.hal.ui.filter.NameSearchInput;
import org.jboss.hal.ui.resource.manager.ResourceManager.State;

import static org.jboss.hal.ui.resource.manager.ResourceManager.State.EDIT;
import static org.jboss.hal.ui.resource.manager.ResourceManager.State.VIEW;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.component.toolbar.ToolbarContent;
import org.patternfly.component.toolbar.ToolbarGroup;
import org.patternfly.component.toolbar.ToolbarItem;
import org.patternfly.core.ObservableValue;
import org.patternfly.core.OuiaSupport;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.hal.ui.filter.DeReDeExMultiSelect.deReDeExMultiSelect;
import static org.jboss.hal.ui.filter.ItemCount.itemCount;
import static org.jboss.hal.ui.filter.StorageAccessTypeMultiSelect.storageAccessTypeMultiSelect;
import static org.jboss.hal.ui.filter.TypesMultiSelect.typesFilterMultiSelect;
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
import static org.patternfly.icon.IconSets.fas.penToSquare;
import static org.patternfly.icon.IconSets.fas.powerOff;
import static org.patternfly.icon.IconSets.fas.rotateRight;
import static org.patternfly.style.Classes.modifier;

/**
 * Action toolbar for the {@link ResourceManager}. Provides attribute filters (name, type, status, storage, access type) and
 * context-aware action buttons that change between view mode (refresh, reset, edit) and edit mode (save, cancel). Filter
 * chips and a clear-all action are shown when filter criteria are active.
 */
public class ResourceToolbar implements IsElement<HTMLElement>, OuiaSupport<HTMLElement, ResourceToolbar> {

    // ------------------------------------------------------ factory

    /** Creates a new toolbar bound to the given resource manager, filter, and item counters. */
    public static ResourceToolbar resourceToolbar(ResourceManager resourceManager,
            Filter<ResourceAttribute> filter, ObservableValue<Integer> visible, ObservableValue<Integer> total) {
        return new ResourceToolbar(resourceManager, filter, visible, total);
    }

    // ------------------------------------------------------ instance

    private final String resetId;
    private final String refreshId;
    private final String editId;
    private final Toolbar toolbar;
    private final ToolbarContent toolbarContent;
    private final ResourceManager resourceManager;
    private ToolbarGroup viewActionGroup;
    private ToolbarGroup editActionGroup;
    private ToolbarItem resetItem;
    private ToolbarItem editItem;

    private ResourceToolbar(ResourceManager resourceManager, Filter<ResourceAttribute> filter,
            ObservableValue<Integer> visible, ObservableValue<Integer> total) {
        this.resourceManager = resourceManager;

        this.resetId = Id.unique("reset");
        this.refreshId = Id.unique("refresh");
        this.editId = Id.unique("edit");
        this.toolbar = toolbar().css(modifier("inset-none"))
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
        initOuia();
    }

    @Override
    public String ouiaComponentType() {
        return "HalOP/ResourceToolbar";
    }

    @Override
    public ResourceToolbar that() {
        return this;
    }

    @Override
    public HTMLElement element() {
        return toolbar.element();
    }

    void adjust(State state, SecurityContext securityContext) {
        if (state == VIEW) {
            failSafeRemoveFromParent(editActionGroup);
            ElementGuard.toggle(resetItem, securityContext.writable());
            ElementGuard.toggle(editItem, securityContext.writable());
            toolbarContent.addGroup(viewActionGroup()); // recreate!
        } else if (state == EDIT) {
            failSafeRemoveFromParent(viewActionGroup);
            toolbarContent.addGroup(editActionGroup()); // recreate!
        } else {
            failSafeRemoveFromParent(editActionGroup);
            failSafeRemoveFromParent(viewActionGroup);
        }
    }

    // The toolbar groups, items and most important their tooltips are recreated each time so that the attach()
    // method on the tooltips is called and the overlay setup is done correctly.
    private ToolbarGroup viewActionGroup() {
        resetItem = toolbarItem()
                .add(button().id(resetId).plain().icon(powerOff())
                        .ouiaId(OuiaIds.RESET_BTN)
                        .onClick((e, b) -> resourceManager.reset()))
                .add(tooltip(By.id(resetId),
                        "Reset attributes to their initial or default value. Applied only to nillable attributes without relationships to other attributes."));
        ToolbarItem refreshItem = toolbarItem()
                .add(button().id(refreshId).plain().icon(rotateRight())
                        .ouiaId(OuiaIds.REFRESH_BTN)
                        .onClick((e, b) -> resourceManager.refresh()))
                .add(tooltip(By.id(refreshId), "Refresh"));
        editItem = toolbarItem()
                .add(button().id(editId).plain().icon(penToSquare())
                        .ouiaId(OuiaIds.EDIT_BTN)
                        .onClick((e, b) -> resourceManager.load(EDIT)))
                .add(tooltip(By.id(editId), "Edit resource"));
        viewActionGroup = toolbarGroup(actionGroupPlain).css(modifier("align-right"))
                .addItem(refreshItem)
                .addItem(resetItem)
                .addItem(editItem);
        return viewActionGroup;
    }

    private ToolbarGroup editActionGroup() {
        ToolbarItem saveItem = toolbarItem()
                .add(button("Save").primary()
                        .ouiaId(OuiaIds.SAVE_BTN)
                        .onClick((e, b) -> resourceManager.save()));
        ToolbarItem cancelItem = toolbarItem()
                .add(button("Cancel").secondary()
                        .ouiaId(OuiaIds.CANCEL_BTN)
                        .onClick((e, b) -> resourceManager.cancel()));
        editActionGroup = toolbarGroup(buttonGroup).css(modifier("align-right"))
                .addItem(saveItem)
                .addItem(cancelItem);
        return editActionGroup;
    }
}

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

import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.env.Settings;
import org.jboss.hal.meta.description.OperationDescription;
import org.jboss.hal.ui.UIContext;
import org.jboss.hal.ui.filter.DeprecatedFilterAttribute;
import org.jboss.hal.ui.filter.FilterChips;
import org.jboss.hal.ui.filter.GlobalOperationsFilterAttribute;
import org.jboss.hal.ui.filter.ParametersFilterAttribute;
import org.jboss.hal.ui.filter.ReturnValueFilterAttribute;
import org.patternfly.component.switch_.Switch;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.core.ObservableValue;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.ui.filter.DeprecatedFilterMultiSelect.deprecatedFilterMultiSelect;
import static org.jboss.hal.ui.filter.ItemCount.itemCount;
import static org.jboss.hal.ui.filter.NameFilterTextInputGroup.nameFilterTextInputGroup;
import static org.jboss.hal.ui.filter.SignatureFilterMultiSelect.signatureFilterMultiSelect;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.switch_.Switch.switch_;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarFilterChipGroup.toolbarFilterChipGroup;
import static org.patternfly.component.toolbar.ToolbarFilterContent.toolbarFilterContent;
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarGroupType.filterGroup;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.component.toolbar.ToolbarItemType.searchFilter;
import static org.patternfly.style.Classes.modifier;

class OperationsToolbar implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    static OperationsToolbar operationsToolbar(UIContext uic, Filter<OperationDescription> filter,
            ObservableValue<Integer> visible, ObservableValue<Integer> total) {
        return new OperationsToolbar(uic, filter, visible, total);
    }

    // ------------------------------------------------------ instance

    private static final String ORIGIN = "OperationsToolbar";
    private final Switch globalOperationsSwitch;
    private final Toolbar toolbar;

    private OperationsToolbar(UIContext uic, Filter<OperationDescription> filter,
            ObservableValue<Integer> visible, ObservableValue<Integer> total) {
        toolbar = toolbar()
                .addContent(toolbarContent()
                        .addItem(toolbarItem(searchFilter).add(nameFilterTextInputGroup(filter)))
                        .addGroup(toolbarGroup(filterGroup)
                                .addItem(toolbarItem().add(signatureFilterMultiSelect(filter)))
                                .addItem(toolbarItem().add(deprecatedFilterMultiSelect(filter, "Status"))))
                        .addItem(toolbarItem().style("align-self", "center")
                                .add(globalOperationsSwitch = switch_(Id.unique("global-operations"), "global-operations")
                                        .label("Show global operations", "Omit global operations")
                                        .onChange((e, c, v) -> {
                                            uic.settings().set(Settings.Key.SHOW_GLOBAL_OPERATIONS, v);
                                            filter.set(GlobalOperationsFilterAttribute.NAME, v, ORIGIN);
                                        })))
                        .addItem(toolbarItem()
                                .style("align-self", "center")
                                .css(modifier("align-right"))
                                .add(itemCount(visible, total, "operation", "operations"))))
                .addFilterContent(toolbarFilterContent()
                        .bindVisibility(filter, ParametersFilterAttribute.NAME, ReturnValueFilterAttribute.NAME,
                                DeprecatedFilterAttribute.NAME)
                        .addGroup(toolbarGroup()
                                .add(toolbarFilterChipGroup(filter, "Signature")
                                        .filterAttributes(ParametersFilterAttribute.NAME, ReturnValueFilterAttribute.NAME)
                                        .filterToChips(FilterChips::signatureChips))
                                .add(toolbarFilterChipGroup(filter, "Status")
                                        .filterAttributes(DeprecatedFilterAttribute.NAME)
                                        .filterToChips(FilterChips::deprecatedChips)))
                        .addItem(toolbarItem()
                                .add(button("Clear all filters").link().inline().onClick((e, c) -> filter.resetAll()))));

        filter.onChange((f, origin) -> {
            if (!origin.equals(ORIGIN)) {
                if (f.defined(GlobalOperationsFilterAttribute.NAME)) {
                    boolean showGlobalOperations = f.<Boolean>get(GlobalOperationsFilterAttribute.NAME).value();
                    globalOperationsSwitch.value(showGlobalOperations, false);
                }
            }
        });
    }

    @Override
    public HTMLElement element() {
        return toolbar.element();
    }
}
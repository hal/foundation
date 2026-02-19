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
import org.jboss.hal.model.filter.DeprecatedAttribute;
import org.jboss.hal.model.filter.GlobalOperationsAttribute;
import org.jboss.hal.model.filter.ParametersAttribute;
import org.jboss.hal.model.filter.ReturnValueAttribute;
import org.jboss.hal.ui.filter.FilterLabels;
import org.jboss.hal.ui.filter.NameSearchInput;
import org.patternfly.component.switch_.Switch;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.core.ObservableValue;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.filter.DeprecatedMultiSelect.deprecatedMultiSelect;
import static org.jboss.hal.ui.filter.ItemCount.itemCount;
import static org.jboss.hal.ui.filter.ParametersReturnValueMultiSelect.parametersReturnValueMultiSelect;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.switch_.Switch.switch_;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarFilterContent.toolbarFilterContent;
import static org.patternfly.component.toolbar.ToolbarFilterLabelGroup.toolbarFilterLabelGroup;
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarGroupType.filterGroup;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.component.toolbar.ToolbarItemType.searchFilter;
import static org.patternfly.style.Classes.modifier;

class OperationsToolbar implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    static OperationsToolbar operationsToolbar(Filter<OperationDescription> filter,
            ObservableValue<Integer> visible, ObservableValue<Integer> total) {
        return new OperationsToolbar(filter, visible, total);
    }

    // ------------------------------------------------------ instance

    private static final String ORIGIN = "OperationsToolbar";
    private final Switch globalOperationsSwitch;
    private final Toolbar toolbar;

    private OperationsToolbar(Filter<OperationDescription> filter,
            ObservableValue<Integer> visible, ObservableValue<Integer> total) {
        boolean showGlobalOperations = uic().settings().get(Settings.Key.SHOW_GLOBAL_OPERATIONS).asBoolean();
        toolbar = toolbar().css(modifier("inset-none"))
                .addContent(toolbarContent()
                        .addItem(toolbarItem(searchFilter).add(NameSearchInput.nameSearchInput(filter)))
                        .addGroup(toolbarGroup(filterGroup)
                                .addItem(toolbarItem().add(parametersReturnValueMultiSelect(filter)))
                                .addItem(toolbarItem().add(deprecatedMultiSelect(filter, "Status"))))
                        .addItem(toolbarItem().style("align-self", "center")
                                .add(globalOperationsSwitch = switch_(Id.unique("global-operations"), "global-operations")
                                        .label("Show global operations")
                                        .value(showGlobalOperations, false)
                                        .onChange((e, c, v) -> {
                                            uic().settings().set(Settings.Key.SHOW_GLOBAL_OPERATIONS, v);
                                            filter.set(GlobalOperationsAttribute.NAME, v, ORIGIN);
                                        })))
                        .addItem(toolbarItem()
                                .style("align-self", "center")
                                .css(modifier("align-right"))
                                .add(itemCount(visible, total, "operation", "operations"))))
                .addFilterContent(toolbarFilterContent()
                        .bindVisibility(filter,
                                ParametersAttribute.NAME,
                                ReturnValueAttribute.NAME,
                                DeprecatedAttribute.NAME)
                        .addGroup(toolbarGroup()
                                .add(toolbarFilterLabelGroup(filter, "Signature")
                                        .filterAttributes(ParametersAttribute.NAME, ReturnValueAttribute.NAME)
                                        .filterToLabels(FilterLabels::parametersReturnValueLabels))
                                .add(toolbarFilterLabelGroup(filter, "Status")
                                        .filterAttributes(DeprecatedAttribute.NAME)
                                        .filterToLabels(FilterLabels::deprecatedLabels)))
                        .addItem(toolbarItem()
                                .add(button("Clear all filters").link().inline().onClick((e, c) -> filter.resetAll()))));

        filter.onChange((f, origin) -> {
            if (!origin.equals(ORIGIN)) {
                if (f.defined(GlobalOperationsAttribute.NAME)) {
                    boolean value = f.<Boolean>get(GlobalOperationsAttribute.NAME).value();
                    globalOperationsSwitch.value(value, false);
                }
            }
        });
    }

    @Override
    public HTMLElement element() {
        return toolbar.element();
    }
}

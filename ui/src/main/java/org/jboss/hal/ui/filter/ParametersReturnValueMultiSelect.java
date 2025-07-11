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
package org.jboss.hal.ui.filter;

import java.util.ArrayList;
import java.util.List;

import org.jboss.elemento.IsElement;
import org.jboss.hal.model.filter.ParametersAttribute;
import org.jboss.hal.model.filter.ReturnValueAttribute;
import org.patternfly.component.menu.MultiSelect;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.ui.filter.MultiSelects.setBooleanFilter;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuGroup.menuGroup;
import static org.patternfly.component.menu.MenuItem.menuItem;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuToggle.menuToggle;
import static org.patternfly.component.menu.MultiSelect.multiSelect;
import static org.patternfly.component.menu.MultiSelectMenu.multiSelectGroupMenu;

public class ParametersReturnValueMultiSelect<T> implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static <T> ParametersReturnValueMultiSelect<T> parametersReturnValueMultiSelect(Filter<T> filter) {
        return new ParametersReturnValueMultiSelect<>(filter);
    }

    // ------------------------------------------------------ instance

    private static final String ORIGIN = "ParametersReturnValueMultiSelect";
    private final MultiSelect multiSelect;

    ParametersReturnValueMultiSelect(Filter<T> filter) {
        filter.onChange(this::onFilterChanged);
        this.multiSelect = multiSelect(menuToggle().text("Signature"))
                .stayOpen()
                .addMenu(multiSelectGroupMenu()
                        .onMultiSelect((e, c, menuItems) -> {
                            setBooleanFilter(filter, ParametersAttribute.NAME, menuItems, ORIGIN);
                            setBooleanFilter(filter, ReturnValueAttribute.NAME, menuItems, ORIGIN);
                        })
                        .addContent(menuContent()
                                .addGroup(menuGroup("Parameters")
                                        .addList(menuList()
                                                .addItem(menuItem(ParametersAttribute.NAME + "-true", "Parameters"))
                                                .addItem(menuItem(ParametersAttribute.NAME + "-false", "No parameters"))))
                                .addDivider()
                                .addGroup(menuGroup("Return value")
                                        .addList(menuList()
                                                .addItem(menuItem(ReturnValueAttribute.NAME + "-true", "Return value"))
                                                .addItem(menuItem(ReturnValueAttribute.NAME + "-false", "No return value"))))));
    }

    @Override
    public HTMLElement element() {
        return multiSelect.element();
    }

    // ------------------------------------------------------ internal

    private void onFilterChanged(Filter<T> filter, String origin) {
        if (!origin.equals(ORIGIN)) {
            multiSelect.clear(false);
            List<String> identifiers = new ArrayList<>();
            MultiSelects.<T, Boolean>collectIdentifiers(identifiers, filter, ParametersAttribute.NAME,
                    value -> ParametersAttribute.NAME + "-" + value);
            MultiSelects.<T, Boolean>collectIdentifiers(identifiers, filter, ReturnValueAttribute.NAME,
                    value -> ReturnValueAttribute.NAME + "-" + value);
            multiSelect.selectIdentifiers(identifiers, false);
        }
    }
}

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
import org.patternfly.component.menu.MultiSelect;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.ui.filter.MultiSelects.setBooleanFilter;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuGroup.menuGroup;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuToggle.menuToggle;
import static org.patternfly.component.menu.MultiSelect.multiSelect;
import static org.patternfly.component.menu.MultiSelectMenu.multiSelectGroupMenu;

public class DefinedRequiredDeprecatedMultiSelect<T> implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static <T> DefinedRequiredDeprecatedMultiSelect<T> definedRequiredDeprecatedMultiSelect(Filter<T> filter) {
        return new DefinedRequiredDeprecatedMultiSelect<>(filter);
    }

    // ------------------------------------------------------ instance

    private static final String ORIGIN = "DefinedRequiredDeprecatedMultiSelect";
    private final MultiSelect multiSelect;

    DefinedRequiredDeprecatedMultiSelect(Filter<T> filter) {
        filter.onChange(this::onFilterChanged);
        this.multiSelect = multiSelect(menuToggle().text("Status"))
                .stayOpen()
                .addMenu(multiSelectGroupMenu()
                        .onMultiSelect((e, c, menuItems) -> {
                            setBooleanFilter(filter, DefinedAttribute.NAME, menuItems, ORIGIN);
                            setBooleanFilter(filter, RequiredAttribute.NAME, menuItems, ORIGIN);
                            setBooleanFilter(filter, DeprecatedAttribute.NAME, menuItems, ORIGIN);
                        })
                        .addContent(menuContent()
                                .addGroup(menuGroup("Defined")
                                        .addList(menuList()
                                                .addItem(DefinedAttribute.NAME + "-true", "Defined")
                                                .addItem(DefinedAttribute.NAME + "-false", "Undefined")))
                                .addDivider()
                                .addGroup(menuGroup("Required")
                                        .addList(menuList()
                                                .addItem(RequiredAttribute.NAME + "-true", "Required")
                                                .addItem(RequiredAttribute.NAME + "-false", "Optional")))
                                .addDivider()
                                .addGroup(menuGroup("Deprecated")
                                        .addList(menuList()
                                                .addItem(DeprecatedAttribute.NAME + "-true", "Deprecated")
                                                .addItem(DeprecatedAttribute.NAME + "-false", "Not deprecated")))));
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
            MultiSelects.<T, Boolean>collectIdentifiers(identifiers, filter, DefinedAttribute.NAME,
                    value -> DefinedAttribute.NAME + "-" + value);
            MultiSelects.<T, Boolean>collectIdentifiers(identifiers, filter, RequiredAttribute.NAME,
                    value -> RequiredAttribute.NAME + "-" + value);
            MultiSelects.<T, Boolean>collectIdentifiers(identifiers, filter, DeprecatedAttribute.NAME,
                    value -> DeprecatedAttribute.NAME + "-" + value);
            multiSelect.selectIdentifiers(identifiers, false);
        }
    }
}
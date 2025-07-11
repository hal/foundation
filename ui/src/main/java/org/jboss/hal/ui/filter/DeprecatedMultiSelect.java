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
import org.jboss.hal.model.filter.DeprecatedAttribute;
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

public class DeprecatedMultiSelect<T> implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static <T> DeprecatedMultiSelect<T> deprecatedMultiSelect(Filter<T> filter, String text) {
        return new DeprecatedMultiSelect<>(filter, text);
    }

    // ------------------------------------------------------ instance

    private static final String ORIGIN = "DeprecatedMultiSelect";
    private final MultiSelect multiSelect;

    DeprecatedMultiSelect(Filter<T> filter, String text) {
        filter.onChange(this::onFilterChanged);
        this.multiSelect = multiSelect(menuToggle().text(text))
                .stayOpen()
                .addMenu(multiSelectGroupMenu()
                        .onMultiSelect((e, c, menuItems) -> setBooleanFilter(filter, DeprecatedAttribute.NAME, menuItems,
                                ORIGIN))
                        .addContent(menuContent()
                                .addGroup(menuGroup()
                                        .addList(menuList()
                                                .addItem(menuItem(DeprecatedAttribute.NAME + "-true", "Deprecated"))
                                                .addItem(menuItem(DeprecatedAttribute.NAME + "-false", "Not deprecated"))))));
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
            MultiSelects.<T, Boolean>collectIdentifiers(identifiers, filter, DeprecatedAttribute.NAME,
                    value -> DeprecatedAttribute.NAME + "-" + value);
            multiSelect.selectIdentifiers(identifiers, false);
        }
    }
}

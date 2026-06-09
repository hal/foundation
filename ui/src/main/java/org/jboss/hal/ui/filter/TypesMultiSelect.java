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
import org.jboss.hal.model.filter.TypeValues;
import org.jboss.hal.model.filter.TypesAttribute;
import org.jboss.hal.resources.Keys;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MultiSelect;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.model.filter.TypeValues.typeValues;
import static org.patternfly.component.badge.Badge.badge;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuItem.checkboxMenuItem;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuToggle.menuToggle;
import static org.patternfly.component.menu.MultiSelect.multiSelect;
import static org.patternfly.component.menu.MultiSelectMenu.multiSelectCheckboxMenu;

/**
 * Multi-select filter for the value types of management model attributes.
 * <p>
 * Allows filtering by data types such as STRING, INT, BOOLEAN, LONG, LIST, OBJECT, etc. Multiple types can be selected
 * simultaneously.
 */
public class TypesMultiSelect<T> implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    /** Creates a new types multi-select filter. */
    public static <T> TypesMultiSelect<T> typesFilterMultiSelect(Filter<T> filter) {
        return new TypesMultiSelect<>(filter);
    }

    // ------------------------------------------------------ instance

    private static final String ORIGIN = "TypesMultiSelect";
    private final MultiSelect multiSelect;

    TypesMultiSelect(Filter<T> filter) {
        filter.onChange(this::onFilterChanged);
        this.multiSelect = multiSelect(menuToggle("Type").addBadge(badge(0).read()))
                .addMenu(multiSelectCheckboxMenu()
                        .onMultiSelect((event, menuItem, selected) -> setFilter(filter, selected))
                        .addContent(menuContent()
                                .addList(menuList()
                                        .addItems(typeValues(), tv -> checkboxMenuItem(tv.identifier, tv.name)
                                                .store(Keys.TYPE_VALUES, tv)))));
    }

    @Override
    public HTMLElement element() {
        return multiSelect.element();
    }

    // ------------------------------------------------------ internal

    private void setFilter(Filter<T> filter, List<MenuItem> selected) {
        if (selected.isEmpty()) {
            filter.reset(TypesAttribute.NAME, ORIGIN);
        } else {
            List<TypeValues> types = new ArrayList<>();
            for (MenuItem item : selected) {
                TypeValues type = item.get(Keys.TYPE_VALUES);
                types.add(type);
            }
            filter.set(TypesAttribute.NAME, types, ORIGIN);
        }
    }

    private void onFilterChanged(Filter<T> filter, String origin) {
        if (!origin.equals(ORIGIN)) {
            multiSelect.clear(false);
            if (filter.defined(TypesAttribute.NAME)) {
                List<String> identifiers = filter.<List<TypeValues>>get(TypesAttribute.NAME).value().stream()
                        .map(tv -> tv.identifier)
                        .collect(toList());
                multiSelect.selectIdentifiers(identifiers, false);
            }
        }
    }
}

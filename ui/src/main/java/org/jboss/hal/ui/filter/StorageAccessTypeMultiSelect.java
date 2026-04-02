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
import java.util.Optional;

import org.jboss.elemento.IsElement;
import org.jboss.hal.model.filter.AccessTypeAttribute;
import org.jboss.hal.model.filter.AccessTypeValue;
import org.jboss.hal.model.filter.StorageAttribute;
import org.jboss.hal.model.filter.StorageValue;
import org.jboss.hal.resources.Keys;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MultiSelect;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.model.filter.AccessTypeValue.accessTypeValues;
import static org.jboss.hal.model.filter.StorageValue.storageValues;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuGroup.menuGroup;
import static org.patternfly.component.menu.MenuItem.menuItem;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuToggle.menuToggle;
import static org.patternfly.component.menu.MultiSelect.multiSelect;
import static org.patternfly.component.menu.MultiSelectMenu.multiSelectGroupMenu;

public class StorageAccessTypeMultiSelect<T> implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static <T> StorageAccessTypeMultiSelect<T> storageAccessTypeMultiSelect(Filter<T> filter) {
        return new StorageAccessTypeMultiSelect<>(filter);
    }

    // ------------------------------------------------------ instance

    private static final String ORIGIN = "StorageAccessTypeMultiSelect";
    private final MultiSelect multiSelect;

    StorageAccessTypeMultiSelect(Filter<T> filter) {
        filter.onChange(this::onFilterChanged);
        this.multiSelect = multiSelect(menuToggle().text("Mode"))
                .addMenu(multiSelectGroupMenu()
                        .onMultiSelect((e, c, menuItems) -> setFilter(filter, menuItems))
                        .addContent(menuContent()
                                .addGroup(menuGroup("Storage")
                                        .addList(menuList()
                                                .addItems(storageValues(), sv -> menuItem(sv.identifier, sv.text)
                                                        .store(Keys.STORAGE_VALUE, sv))))
                                .addDivider()
                                .addGroup(menuGroup("Access type")
                                        .addList(menuList()
                                                .addItems(accessTypeValues(), atv -> menuItem(atv.identifier, atv.text)
                                                        .store(Keys.ACCESS_TYPE_VALUE, atv))))));
    }

    @Override
    public HTMLElement element() {
        return multiSelect.element();
    }

    // ------------------------------------------------------ internal

    private void setFilter(Filter<T> filter, List<MenuItem> menuItems) {
        Optional<StorageValue> storageValue = menuItems.stream()
                .filter(menuItem -> menuItem.has(Keys.STORAGE_VALUE))
                .map(menuItem -> menuItem.<StorageValue>get(Keys.STORAGE_VALUE))
                .findFirst();
        if (storageValue.isPresent()) {
            filter.set(StorageAttribute.NAME, storageValue.get(), ORIGIN);
        } else {
            filter.reset(StorageAttribute.NAME, ORIGIN);
        }
        Optional<AccessTypeValue> accessTypeValue = menuItems.stream()
                .filter(menuItem -> menuItem.has(Keys.ACCESS_TYPE_VALUE))
                .map(menuItem -> menuItem.<AccessTypeValue>get(Keys.ACCESS_TYPE_VALUE))
                .findFirst();
        if (accessTypeValue.isPresent()) {
            filter.set(AccessTypeAttribute.NAME, accessTypeValue.get(), ORIGIN);
        } else {
            filter.reset(AccessTypeAttribute.NAME, ORIGIN);
        }
    }

    private void onFilterChanged(Filter<T> filter, String origin) {
        if (!origin.equals(ORIGIN)) {
            multiSelect.clear(false);
            List<String> identifiers = new ArrayList<>();
            MultiSelects.<T, StorageValue>collectIdentifiers(identifiers, filter, StorageAttribute.NAME,
                    value -> value.identifier);
            MultiSelects.<T, AccessTypeValue>collectIdentifiers(identifiers, filter, AccessTypeAttribute.NAME,
                    value -> value.identifier);
            multiSelect.selectIdentifiers(identifiers, false);
        }
    }
}

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
package org.jboss.hal.op.task.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jboss.elemento.IsElement;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.ui.filter.MultiSelects;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MultiSelect;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static org.jboss.hal.core.LabelBuilder.labelBuilder;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuGroup.menuGroup;
import static org.patternfly.component.menu.MenuItem.menuItem;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuToggle.menuToggle;
import static org.patternfly.component.menu.MultiSelect.multiSelect;
import static org.patternfly.component.menu.MultiSelectMenu.multiSelectGroupMenu;

class StatisticsEnabledMultiSelect implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    static StatisticsEnabledMultiSelect statisticsEnabledMultiSelect(Filter<ModelNode> filter) {
        return new StatisticsEnabledMultiSelect(filter);
    }

    // ------------------------------------------------------ instance

    private static final String ORIGIN = "StatisticsEnabledMultiSelect";
    private static final String STATISTICS_ENABLED_KEY = STATISTICS_ENABLED + "-key";
    private final MultiSelect multiSelect;

    StatisticsEnabledMultiSelect(Filter<ModelNode> filter) {
        filter.onChange(this::onFilterChanged);
        this.multiSelect = multiSelect(menuToggle().text(labelBuilder(STATISTICS_ENABLED)))
                .addMenu(multiSelectGroupMenu()
                        .onMultiSelect((e, c, menuItems) -> setFilter(filter, menuItems))
                        .addContent(menuContent()
                                .addGroup(menuGroup()
                                        .addList(menuList()
                                                .addItems(asList(StatisticsEnabledValue.values()),
                                                        sev -> menuItem(sev.identifier, sev.text)
                                                                .store(STATISTICS_ENABLED_KEY, sev))))));
    }

    @Override
    public HTMLElement element() {
        return multiSelect.element();
    }

    // ------------------------------------------------------ internal

    private void setFilter(Filter<ModelNode> filter, List<MenuItem> menuItems) {
        Optional<StatisticsEnabledValue> sev = menuItems.stream()
                .filter(menuItem -> menuItem.has(STATISTICS_ENABLED_KEY))
                .map(menuItem -> menuItem.<StatisticsEnabledValue>get(STATISTICS_ENABLED_KEY))
                .findFirst();
        if (sev.isPresent()) {
            filter.set(StatisticsEnabledAttribute.NAME, sev.get(), ORIGIN);
        } else {
            filter.reset(StatisticsEnabledAttribute.NAME, ORIGIN);
        }
    }

    private void onFilterChanged(Filter<ModelNode> filter, String origin) {
        if (!origin.equals(ORIGIN)) {
            multiSelect.clear(false);
            List<String> identifiers = new ArrayList<>();
            MultiSelects.<ModelNode, StatisticsEnabledValue>collectIdentifiers(identifiers, filter,
                    StatisticsEnabledAttribute.NAME, value -> value.identifier);
            multiSelect.selectIdentifiers(identifiers, false);
        }
    }
}

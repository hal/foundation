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

import java.util.List;
import java.util.function.Function;

import org.patternfly.component.menu.MenuItem;
import org.patternfly.filter.Filter;
import org.patternfly.filter.FilterAttribute;

import static java.util.stream.Collectors.toList;

public class MultiSelects {

    public static <T> void setBooleanFilter(Filter<T> filter, String filterAttribute, List<MenuItem> menuItems, String origin) {
        String prefix = filterAttribute + "-";
        List<MenuItem> selected = menuItems.stream()
                .filter(menuItem -> menuItem.identifier().startsWith(prefix))
                .collect(toList());
        if (selected.isEmpty()) {
            filter.reset(filterAttribute, origin);
        } else {
            for (MenuItem menuItem : selected) {
                String selection = menuItem.identifier().substring(prefix.length());
                filter.set(filterAttribute, Boolean.parseBoolean(selection), origin);
            }
        }
    }

    public static <T, V> void collectIdentifiers(List<String> identifiers, Filter<T> filter, String filterAttribute,
            Function<V, String> valueToIdentifier) {
        if (filter.defined(filterAttribute)) {
            FilterAttribute<T, V> value = filter.get(filterAttribute);
            identifiers.add(valueToIdentifier.apply(value.value()));
        }
    }
}

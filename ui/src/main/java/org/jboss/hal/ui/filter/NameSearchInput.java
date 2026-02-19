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

import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.model.filter.NameAttribute;
import org.patternfly.component.textinputgroup.SearchInput;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static org.patternfly.component.textinputgroup.SearchInput.searchInput;
import static org.patternfly.icon.IconSets.fas.search;

public class NameSearchInput<T> implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static <T> NameSearchInput<T> nameSearchInput(Filter<T> filter) {
        return new NameSearchInput<>(filter, "Filter by name");
    }

    public static <T> NameSearchInput<T> nameSearchInput(Filter<T> filter, String placeholder) {
        return new NameSearchInput<>(filter, placeholder);
    }

    // ------------------------------------------------------ instance

    private final SearchInput searchInput;

    NameSearchInput(Filter<T> filter, String placeholder) {
        searchInput = searchInput(Id.unique()).placeholder(placeholder).icon(search())
                .onKeyup((event, textInputGroup, value) -> filter.set(NameAttribute.NAME, value))
                .onClear((event, textInputGroup) -> filter.reset(NameAttribute.NAME));
        searchInput.input().apply(input -> input.autocomplete = "off");
        filter.onChange((f, origin) -> {
            if (!f.defined(NameAttribute.NAME)) {
                searchInput.value("", false);
            }
        });
    }

    @Override
    public HTMLElement element() {
        return searchInput.element();
    }
}

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
package org.jboss.hal.op.search;

import java.util.ArrayList;
import java.util.List;

import org.jboss.elemento.Id;
import org.jboss.hal.op.mgt.ModelGraphTools;
import org.jboss.hal.op.mgt.SearchResult;
import org.patternfly.async.AsyncItems;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MenuList;
import org.patternfly.component.textinputgroup.SearchInput;

import elemental2.promise.Promise;

import static java.util.Collections.emptyList;
import static org.patternfly.component.menu.MenuItem.menuItem;

/**
 * Async items implementation that queries the model graph tools (MGT) REST API for resources, attributes, operations, and
 * capabilities matching the current search input value.
 *
 * <p>Each {@link SearchResult} is mapped to a {@link MenuItem} displaying the result name. The full {@link SearchResult} is
 * stored on the menu item for later retrieval on selection.
 *
 * @see UniversalSearchAsyncItems
 */
class MgtSearchAsyncItems implements AsyncItems<MenuList, MenuItem> {

    private final SearchInput searchInput;
    private final ModelGraphTools modelGraphTools;

    MgtSearchAsyncItems(SearchInput searchInput, ModelGraphTools modelGraphTools) {
        this.searchInput = searchInput;
        this.modelGraphTools = modelGraphTools;
    }

    @Override
    public Promise<Iterable<MenuItem>> apply(MenuList menuList) {
        String value = searchInput.value();
        if (value == null || value.trim().isEmpty()) {
            return Promise.resolve(emptyList());
        }

        return modelGraphTools.search(value.trim())
                .then(results -> {
                    List<MenuItem> items = new ArrayList<>();
                    for (SearchResult result : results) {
                        String text = result.name;
                        if (result.address != null) {
                            text = result.name + "  " + result.address;
                        }
                        MenuItem item = menuItem(Id.build(result.type, result.name), text)
                                .store("searchResult", result);
                        items.add(item);
                    }
                    return Promise.resolve((Iterable<MenuItem>) items);
                })
                .catch_(__ -> Promise.resolve(emptyList()));
    }
}

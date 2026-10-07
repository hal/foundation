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
import org.patternfly.core.HasValue;
import org.patternfly.style.Color;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Collections.emptyList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.small;
import static org.jboss.elemento.Elements.span;
import static org.patternfly.component.label.Label.label;
import static org.patternfly.component.menu.MenuItem.menuItem;
import static org.patternfly.style.Classes.util;

/**
 * Async items implementation that queries the model graph tools (MGT) REST API for resources, attributes, operations, and
 * capabilities matching the current search input value.
 *
 * <p>Each {@link SearchResult} is mapped to a {@link MenuItem} with a colored type label, description, and address. The full
 * {@link SearchResult} is stored on the menu item for later retrieval on selection.
 *
 * @see UniversalSearchAsyncItems
 */
class MgtSearchAsyncItems implements AsyncItems<MenuList, MenuItem> {

    private final HasValue<String> valueProvider;
    private final ModelGraphTools modelGraphTools;

    MgtSearchAsyncItems(HasValue<String> valueProvider, ModelGraphTools modelGraphTools) {
        this.valueProvider = valueProvider;
        this.modelGraphTools = modelGraphTools;
    }

    @Override
    public Promise<Iterable<MenuItem>> apply(MenuList menuList) {
        String value = valueProvider.value();
        if (value == null || value.trim().isEmpty()) {
            return Promise.resolve(emptyList());
        }

        return modelGraphTools.search(value.trim())
                .then(results -> {
                    List<MenuItem> items = new ArrayList<>();
                    for (SearchResult result : results) {
                        String identifier = result.address != null
                                ? Id.build(result.type, result.name, result.address)
                                : Id.build(result.type, result.name);
                        MenuItem item = menuItem(identifier, result.name)
                                .text(nameWithTypeLabel(result))
                                .store("searchResult", result);
                        HTMLElement descriptionElement = descriptionWithAddress(result);
                        if (descriptionElement != null) {
                            item.description(descriptionElement);
                        }
                        items.add(item);
                    }
                    return Promise.resolve((Iterable<MenuItem>) items);
                })
                .catch_(__ -> Promise.resolve(emptyList()));
    }

    private static HTMLElement nameWithTypeLabel(SearchResult result) {
        return span()
                .add(result.name)
                .add(label(result.type, typeColor(result.type)).compact()
                        .css(util("ml-sm")))
                .element();
    }

    private static HTMLElement descriptionWithAddress(SearchResult result) {
        boolean hasDescription = result.description != null && !result.description.isEmpty();
        boolean hasAddress = result.address != null && !result.address.isEmpty();
        if (!hasDescription && !hasAddress) {
            return null;
        }
        return div()
                .run(d -> {
                    if (hasDescription) {
                        d.add(span().text(result.description));
                    }
                    if (hasAddress) {
                        d.add(small().css(util("mt-xs"), util("display-block"), util("color-200"))
                                .text(result.address));
                    }
                })
                .element();
    }

    private static Color typeColor(String type) {
        if (type == null) {
            return Color.grey;
        }
        return switch (type) {
            case "Attribute" -> Color.blue;
            case "Resource" -> Color.green;
            case "Operation" -> Color.purple;
            case "Capability" -> Color.teal;
            default -> Color.grey;
        };
    }
}

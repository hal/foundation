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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jboss.elemento.Id;
import org.jboss.hal.core.mgt.ModelGraphTools;
import org.jboss.hal.core.mgt.ResourceRef;
import org.jboss.hal.core.mgt.SearchResult;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.resources.Keys;
import org.patternfly.async.AsyncItems;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MenuList;
import org.patternfly.core.HasValue;
import org.patternfly.style.Color;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Collections.emptyList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.mgt;
import static org.jboss.hal.resources.HalClasses.universalSearch;
import static org.patternfly.component.label.Label.label;
import static org.patternfly.component.menu.MenuItem.menuItem;
import static org.patternfly.style.Classes.util;

/**
 * Async items implementation that queries the model graph tools (MGT) REST API for resources, attributes, operations, and
 * capabilities matching the current search input value.
 *
 * <p>Each {@link SearchResult} is mapped to a {@link MenuItem} with a colored type label, description, and address. The full
 * {@link SearchResult} is stored on the menu item for later retrieval using the {@link Keys#SEARCH_RESULT} key.
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

        String trimmed = value.trim();
        String typeFilter = typeFilter(trimmed);
        String searchTerm = typeFilter != null ? trimmed.substring(2).trim() : trimmed;
        if (searchTerm.isEmpty()) {
            return Promise.resolve(emptyList());
        }

        return modelGraphTools.search(searchTerm)
                .then(results -> {
                    List<SearchResult> sorted = new ArrayList<>();
                    for (SearchResult result : results) {
                        if (typeFilter == null || typeFilter.equals(result.type)) {
                            sorted.add(result);
                        }
                    }
                    sorted.sort((a, b) -> {
                        int cmp = Integer.compare(categoryOrder(a.type), categoryOrder(b.type));
                        if (cmp != 0) {
                            return cmp;
                        }
                        String nameA = a.name != null ? a.name : "";
                        String nameB = b.name != null ? b.name : "";
                        return nameA.compareTo(nameB);
                    });

                    Set<String> seen = new LinkedHashSet<>();
                    List<MenuItem> items = new ArrayList<>();
                    for (SearchResult result : sorted) {
                        String identifier = result.address != null
                                ? Id.build(result.type, result.name, result.address)
                                : Id.build(result.type, result.name);
                        if (!seen.add(identifier)) {
                            continue;
                        }
                        MenuItem item = menuItem(identifier, result.name)
                                .css(halComponent(universalSearch, mgt, HalClasses.result))
                                .icon(label(result.type, typeColor(result.type)).compact().element())
                                .store(Keys.SEARCH_RESULT, result);
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

    private static HTMLElement descriptionWithAddress(SearchResult result) {
        boolean hasDescription = result.description != null && !result.description.isEmpty();
        boolean hasAddress = result.address != null && !result.address.isEmpty();
        boolean hasProviders = result.providedBy != null && result.providedBy.length > 0;
        if (!hasDescription && !hasAddress && !hasProviders) {
            return null;
        }
        return div()
                .run(d -> {
                    if (hasDescription) {
                        d.add(span().text(result.description));
                    }
                    if (hasAddress) {
                        d.add(span().css(util("mt-xs"), util("display-block"), util("font-size-sm"), util("color-200"))
                                .text(result.address));
                    }
                    if (hasProviders) {
                        for (ResourceRef ref : result.providedBy) {
                            if (ref.address != null) {
                                d.add(span().css(util("mt-xs"), util("display-block"), util("font-size-sm"), util("color-200"))
                                        .text(ref.address));
                            }
                        }
                    }
                })
                .element();
    }

    private static int categoryOrder(String type) {
        if (type == null) {
            return 99;
        }
        return switch (type) {
            case "Resource" -> 0;
            case "Attribute" -> 1;
            case "Operation" -> 2;
            case "Capability" -> 3;
            default -> 99;
        };
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

    static boolean hasTypeFilter(String value) {
        if (value != null && value.length() >= 2 && value.charAt(1) == ':') {
            char prefix = value.charAt(0);
            return prefix == 'a' || prefix == 'r' || prefix == 'o' || prefix == 'c';
        }
        return false;
    }

    private static String typeFilter(String value) {
        if (hasTypeFilter(value)) {
            return switch (value.charAt(0)) {
                case 'a' -> "Attribute";
                case 'r' -> "Resource";
                case 'o' -> "Operation";
                case 'c' -> "Capability";
                default -> null;
            };
        }
        return null;
    }
}

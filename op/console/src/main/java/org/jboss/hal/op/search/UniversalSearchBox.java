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

import java.util.List;

import org.jboss.elemento.Id;
import org.jboss.elemento.Key;
import org.jboss.hal.core.mgt.ModelGraphTools;
import org.jboss.hal.core.mgt.SearchResult;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.resources.Keys;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MenuList;
import org.patternfly.component.modal.Modal;
import org.patternfly.component.textinputgroup.SearchInputGroupTypeahead;

import static org.jboss.elemento.EventType.keydown;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.op.search.MgtSearchAsyncItems.hasTypeFilter;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.universalSearch;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.component.ResourceAddressTypeahead.addressStructureChanged;
import static org.patternfly.component.SelectionMode.click;
import static org.patternfly.component.menu.Menu.menu;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuItem.menuItem;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuType.menu;
import static org.patternfly.component.menu.SearchFilter.lastSegment;
import static org.patternfly.component.modal.Modal.modal;
import static org.patternfly.component.modal.ModalBody.modalBody;
import static org.patternfly.component.textinputgroup.SearchInputGroupTypeahead.searchInputGroupTypeahead;
import static org.patternfly.component.typeahead.Decision.debounce;
import static org.patternfly.component.typeahead.Decision.filter;
import static org.patternfly.component.typeahead.Decision.refresh;

/**
 * Modal search box for the universal search feature. A new instance is created each time the search is opened, avoiding
 * stale-state issues from the {@link SearchInputGroupTypeahead} typeahead lifecycle inside a modal.
 *
 * <p>The search data flow is handled by the {@link UniversalSearchAsyncItems} compound delegator, which routes search requests
 * to either {@link org.jboss.hal.ui.component.ResourceAddressAsyncItems} (for resource address typeahead when the input starts
 * with {@code /}) or {@link MgtSearchAsyncItems} (for model graph tools queries when MGT is available). This class contains no
 * search logic — it is purely a view component.
 */
public class UniversalSearchBox {

    // ------------------------------------------------------ factory

    public static UniversalSearchBox universalSearchBox() {
        return new UniversalSearchBox();
    }

    // ------------------------------------------------------ instance

    private static final int DEBOUNCE_MS = 300;
    private static final String PLACEHOLDER_ADDRESS_ONLY = "Go to a resource…";
    private static final String PLACEHOLDER_MGT = "Search or filter by a: r: o: c: — start with / for addresses…";

    private final Modal modal;
    private final MenuList menuList;
    private final SearchInputGroupTypeahead searchInput;

    UniversalSearchBox() {
        ModelGraphTools modelGraphTools = uic().modelGraphTools();
        boolean mgtAvailable = modelGraphTools.available();

        searchInput = searchInputGroupTypeahead("universal-search")
                .placeholder(mgtAvailable ? PLACEHOLDER_MGT : PLACEHOLDER_ADDRESS_ONLY)
                .filter(lastSegment('/'))
                .refreshOn((previous, current) -> {
                    if (hasTypeFilter(current)) {
                        return debounce(DEBOUNCE_MS);
                    }
                    if (current != null && current.startsWith("/")) {
                        if (addressStructureChanged(previous, current)) {
                            return refresh();
                        }
                        return filter();
                    }
                    return debounce(DEBOUNCE_MS);
                });

        UniversalSearchAsyncItems asyncItems = new UniversalSearchAsyncItems(searchInput, modelGraphTools);
        asyncItems.mgtAvailable(mgtAvailable);

        searchInput.addMenu(menu(menu, click)
                .scrollable()
                .addContent(menuContent()
                        .addList(menuList = menuList()
                                .addItems(asyncItems))));

        modal = modal().css(halComponent(universalSearch))
                .top()
                .width("80%")
                .hideClose()
                .closeOnEsc(true)
                .addBody(modalBody().add(searchInput));

        searchInput.on(keydown, e -> {
            if (Key.Escape.match(e)) {
                if (searchInput.expanded()) {
                    e.stopPropagation();
                }
            }
            if (Key.Enter.match(e)) {
                String value = searchInput.value();
                if (!searchInput.expanded() && value != null && value.startsWith("/")) {
                    gotoAddress(value);
                }
            }
        });

        searchInput.menu().onSingleSelect((event, item, selected) -> {
            SearchResult searchResult = item.get(Keys.MGT_SEARCH_RESULT);
            if (searchResult != null) {
                evaluateSearchResult(searchResult);
            } else if (Boolean.TRUE.equals(item.get(Keys.RESOLVED_ADDRESS))) {
                modal.close();
                AddressTemplate.ofUntrusted(item.text()).ifPresent(t -> uic().routeRegistry().goTo(t));
            }
        });
    }

    // ------------------------------------------------------ api

    /** Opens the search modal and focuses the search input. */
    public void show() {
        modal.open();
        searchInput.input().element().focus();
    }

    // ------------------------------------------------------ navigation

    private void evaluateSearchResult(SearchResult searchResult) {
        String address = searchResult.address != null ? searchResult.address : searchResult.name;
        AddressTemplate.ofUntrusted(address).ifPresent(t -> resolveAndNavigate(t, searchResult));
    }

    private void gotoAddress(String value) {
        if (value != null && !value.trim().isEmpty()) {
            AddressTemplate.ofUntrusted(value).ifPresent(t -> resolveAndNavigate(t, null));
        }
    }

    private void resolveAndNavigate(AddressTemplate template, SearchResult searchResult) {
        if (template.fullyQualified()) {
            verifyAndNavigate(template, searchResult);
        } else {
            uic().modelTree().resolveWildcards(template).then(resolved -> {
                if (resolved.size() == 1) {
                    verifyAndNavigate(resolved.get(0), searchResult);
                } else {
                    showResolvedAddresses(resolved, searchResult);
                }
                return null;
            });
        }
    }

    private void verifyAndNavigate(AddressTemplate template, SearchResult searchResult) {
        ResourceAddress address = template.resolve(uic().statementContext());
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION).build();
        uic().dispatcher().execute(operation)
                .then(__ -> {
                    modal.close();
                    uic().routeRegistry().goTo(template, highlight(searchResult));
                    return null;
                })
                .catch_(__ -> {
                    menuList.clear();
                    menuList.addItem(menuItem(Id.unique("not-found"),
                            "Resource not found: " + template).disabled());
                    searchInput.expand();
                    searchInput.menu().clearSearch();
                    return null;
                });
    }

    private void showResolvedAddresses(List<AddressTemplate> templates, SearchResult searchResult) {
        menuList.clear();
        if (templates.isEmpty()) {
            menuList.addItem(menuItem(Id.unique("no-results"), "No matching resources found").disabled());
        } else {
            menuList.addItem(menuItem(Id.unique("pick-address"),
                    "Address resolves to multiple resources. Pick one:").disabled());
            for (AddressTemplate template : templates) {
                MenuItem item = menuItem(Id.build(template.template), template.template)
                        .store(Keys.RESOLVED_ADDRESS, Boolean.TRUE);
                if (searchResult != null) {
                    item.store(Keys.MGT_SEARCH_RESULT, searchResult);
                }
                menuList.addItem(item);
            }
        }
        searchInput.expand();
        searchInput.menu().clearSearch();
    }

    private String highlight(SearchResult searchResult) {
        if (searchResult == null || searchResult.type == null || searchResult.name == null) {
            return null;
        }
        String prefix = switch (searchResult.type) {
            case "Attribute" -> "a:";
            case "Operation" -> "o:";
            default -> null;
        };
        return prefix != null ? prefix + searchResult.name : null;
    }
}

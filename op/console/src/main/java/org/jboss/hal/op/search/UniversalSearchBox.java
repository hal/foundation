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

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.op.mgt.ModelGraphTools;
import org.jboss.hal.op.mgt.SearchResult;
import org.jboss.hal.ui.component.ResourceAddressTypeahead;
import org.patternfly.component.menu.SearchFilter;
import org.patternfly.component.modal.Modal;
import org.patternfly.component.textinputgroup.SearchInputGroupTypeahead;
import org.patternfly.component.typeahead.Decision;
import org.patternfly.component.typeahead.RefreshStrategy;

import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.universalSearch;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.SelectionMode.click;
import static org.patternfly.component.menu.Menu.menu;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuType.menu;
import static org.patternfly.component.modal.Modal.modal;
import static org.patternfly.component.modal.ModalBody.modalBody;
import static org.patternfly.component.textinputgroup.SearchInputGroupTypeahead.searchInputGroupTypeahead;

/**
 * Modal search box for the universal search feature. A new instance is created each time the search is opened, avoiding
 * stale-state issues from the {@link SearchInputGroupTypeahead} typeahead lifecycle inside a modal.
 *
 * <p>The search data flow is handled by the {@link UniversalSearchAsyncItems} compound delegator, which routes search requests
 * to either {@link org.jboss.hal.ui.component.ResourceAddressAsyncItems} (for resource address typeahead when the input starts
 * with {@code /}) or {@link MgtSearchAsyncItems} (for model graph tools queries when MGT is available). This class contains no
 * search logic — it is purely a view component.
 *
 * @see UniversalSearch
 */
public class UniversalSearchBox {

    // ------------------------------------------------------ instance

    private static final int DEBOUNCE_MS = 300;
    private static final String PLACEHOLDER_ADDRESS_ONLY = "Go to a resource…";
    private static final String PLACEHOLDER_MGT = "Search resources, attributes, operations or go to a resource…";

    private final Modal modal;
    private final SearchInputGroupTypeahead searchInput;

    UniversalSearchBox(ModelGraphTools modelGraphTools, boolean mgtAvailable) {
        searchInput = searchInputGroupTypeahead("universal-search")
                .placeholder(mgtAvailable ? PLACEHOLDER_MGT : PLACEHOLDER_ADDRESS_ONLY)
                .refreshOn(universalSearchStrategy())
                .filter(SearchFilter.lastSegment('/'));

        searchInput.onClear((e, si) -> searchInput.menu().reset());
        searchInput.onInput((e, si, value) -> {
            if (value == null || value.isEmpty()) {
                searchInput.menu().reset();
            }
        });

        UniversalSearchAsyncItems asyncItems = new UniversalSearchAsyncItems(searchInput, modelGraphTools);
        asyncItems.mgtAvailable(mgtAvailable);

        searchInput.addMenu(menu(menu, click)
                .scrollable()
                .addContent(menuContent()
                        .addList(menuList()
                                .addItems(asyncItems))));

        modal = modal().css(halComponent(universalSearch))
                .top()
                .width("80%")
                .hideClose()
                .closeOnEsc(true)
                .addBody(modalBody().add(searchInput));

        searchInput.menu().onSingleSelect((event, item, selected) -> {
            modal.close();
            SearchResult searchResult = item.get("searchResult");
            String address = searchResult != null && searchResult.address != null
                    ? searchResult.address
                    : item.text();
            AddressTemplate.ofUntrusted(address).ifPresent(t -> uic().routeRegistry().goTo(t));
        });
    }

    // ------------------------------------------------------ api

    /** Opens the search modal and focuses the search input. */
    void show() {
        modal.open();
        searchInput.input().element().focus();
    }

    // ------------------------------------------------------ internal

    private static RefreshStrategy universalSearchStrategy() {
        return (previous, current) -> {
            if (current != null && current.startsWith("/")) {
                if (ResourceAddressTypeahead.addressStructureChanged(previous, current)) {
                    return Decision.refresh();
                }
                return Decision.filter();
            }
            return Decision.debounce(DEBOUNCE_MS);
        };
    }
}

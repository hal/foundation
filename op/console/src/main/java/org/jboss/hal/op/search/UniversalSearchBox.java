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

import org.jboss.elemento.Callback;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.op.mgt.ModelGraphTools;
import org.jboss.hal.op.mgt.ModelGraphToolsEvents.Availability;
import org.jboss.hal.op.mgt.ModelGraphToolsEvents.Ping;
import org.jboss.hal.op.mgt.SearchResult;
import org.patternfly.component.menu.MenuList;
import org.patternfly.component.modal.Modal;
import org.patternfly.component.textinputgroup.SearchInput;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Scheduler.debounce;
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
import static org.patternfly.component.textinputgroup.SearchInput.searchInput;

/**
 * Modal search box for the universal search feature. Displays a top-positioned modal with a search input backed by a
 * {@link org.patternfly.component.menu.Menu} for typeahead results.
 *
 * <p>The search data flow is handled by the {@link UniversalSearchAsyncItems} compound delegator, which routes search requests
 * to either {@link org.jboss.hal.ui.component.ResourceAddressAsyncItems} (for resource address typeahead when the input starts
 * with {@code /}) or {@link MgtSearchAsyncItems} (for model graph tools queries when MGT is available). This class contains no
 * search logic — it is purely a view component.
 *
 * <p>This class is a singleton; use {@link #universalSearchBox(ModelGraphTools)} to obtain the instance.
 */
public class UniversalSearchBox {

    // ------------------------------------------------------ factory

    private static final String PLACEHOLDER_ADDRESS_ONLY = "Go to a resource…";
    private static final String PLACEHOLDER_MGT = "Search resources, attributes, operations or go to a resource…";

    private static UniversalSearchBox instance;

    /** Returns the singleton instance, creating it on first access with the given {@link ModelGraphTools} service. */
    public static UniversalSearchBox universalSearchBox(ModelGraphTools modelGraphTools) {
        if (instance == null) {
            instance = new UniversalSearchBox(modelGraphTools);
        }
        return instance;
    }

    // ------------------------------------------------------ instance

    private final Modal modal;
    private final SearchInput searchInput;
    private final UniversalSearchAsyncItems asyncItems;
    private final MenuList menuList;
    private int lastSlashCount;
    private int lastEqualsCount;

    UniversalSearchBox(ModelGraphTools modelGraphTools) {
        lastSlashCount = 0;
        lastEqualsCount = 0;

        searchInput = searchInput("universal-search")
                .placeholder(PLACEHOLDER_ADDRESS_ONLY);
        asyncItems = new UniversalSearchAsyncItems(searchInput, modelGraphTools);

        searchInput.addMenu(menu(menu, click)
                .scrollable()
                .addContent(menuContent()
                        .addList(menuList = menuList()
                                .addItems(asyncItems))));

        Callback debouncedMgtSearch = debounce(300, menuList::reset);
        searchInput.onInput((event, si, value) -> {
            if (value == null || value.trim().isEmpty()) {
                return;
            }
            if (value.startsWith("/")) {
                int slashes = countChar(value, '/');
                int equals = countChar(value, '=');
                if (slashes != lastSlashCount || equals != lastEqualsCount) {
                    lastSlashCount = slashes;
                    lastEqualsCount = equals;
                    menuList.reset();
                }
            } else if (asyncItems.isMgtAvailable()) {
                debouncedMgtSearch.call();
            }
        });

        modal = modal().css(halComponent(universalSearch))
                .top()
                .width("80%")
                .hideClose()
                .closeOnEsc(true)
                .addBody(modalBody().add(searchInput))
                .onClose((event, component) -> {
                    searchInput.value("");
                    searchInput.collapse(false);
                    lastSlashCount = 0;
                    lastEqualsCount = 0;
                });

        searchInput.menu().onSingleSelect((event, item, selected) -> {
            modal.close();
            SearchResult searchResult = item.get("searchResult");
            String address = searchResult != null && searchResult.address != null
                    ? searchResult.address
                    : item.text();
            AddressTemplate.ofUntrusted(address).ifPresent(t -> uic().routeRegistry().goTo(t));
        });

        Availability.listen(document.body, details -> {
            asyncItems.mgtAvailable(details.available);
            searchInput.input().element().placeholder =
                    details.available ? PLACEHOLDER_MGT : PLACEHOLDER_ADDRESS_ONLY;
        });
        Ping.dispatch(document.body);
    }

    // ------------------------------------------------------ api

    /** Opens the search modal and focuses the search input. */
    public void show() {
        modal.open();
        searchInput.input().element().focus();
    }

    // ------------------------------------------------------ internal

    private int countChar(String str, char c) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }
}

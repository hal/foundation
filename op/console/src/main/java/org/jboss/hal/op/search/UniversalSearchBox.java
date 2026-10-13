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
import org.jboss.elemento.Key;
import org.jboss.elemento.flow.FlowContext;
import org.jboss.hal.core.mgt.ModelGraphTools;
import org.jboss.hal.core.mgt.ResourceRef;
import org.jboss.hal.core.mgt.SearchResult;
import org.jboss.hal.core.mgt.SearchResultType;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.ResourceCheck;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.resources.Keys;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MenuList;
import org.patternfly.component.modal.Modal;
import org.patternfly.component.textinputgroup.SearchInputGroupTypeahead;

import static java.util.stream.Collectors.toList;

import static org.jboss.elemento.EventType.keyup;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUBSYSTEM;
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
    private boolean selectionJustMade;

    UniversalSearchBox() {
        ModelGraphTools modelGraphTools = uic().modelGraphTools();
        boolean mgtAvailable = modelGraphTools.available();

        searchInput = searchInputGroupTypeahead("universal-search")
                .placeholder(mgtAvailable ? PLACEHOLDER_MGT : PLACEHOLDER_ADDRESS_ONLY)
                // the search workflow is complex: we need to decide when to close the dropdown menu
                .stayOpen((e, c, m) -> true)
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

        searchInput.on(keyup, e -> {
            if (Key.Enter.match(e) && e.target == searchInput.input().element()) {
                if (selectionJustMade) {
                    selectionJustMade = false;
                } else {
                    String value = searchInput.value();
                    if (value != null && value.startsWith("/")) {
                        resolveUntrustedTemplate(value);
                    }
                }
            }
        });

        searchInput.menu().onSingleSelect((event, item, selected) -> {
            selectionJustMade = true;
            SearchResult searchResult = item.get(Keys.MGT_SEARCH_RESULT);
            if (searchResult != null) {
                evaluateSearchResult(searchResult);
            } else if (Boolean.TRUE.equals(item.get(Keys.RESOLVED_ADDRESS))) {
                resolveUntrustedTemplate(item.text());
            } else {
                searchInput.collapse(false);
            }
            searchInput.input().element().focus();
        });
    }

    public void show() {
        modal.open();
        searchInput.input().element().focus();
    }

    // ------------------------------------------------------ evaluate

    private void evaluateSearchResult(SearchResult searchResult) {
        List<AddressTemplate> templates = new ArrayList<>();
        if (searchResult.type() == SearchResultType.CAPABILITY) {
            if (searchResult.providedBy != null) {
                for (ResourceRef ref : searchResult.providedBy) {
                    if (ref.address != null) {
                        AddressTemplate.ofUntrusted(ref.address).ifPresent(templates::add);
                    }
                }
            }
        } else {
            String address = searchResult.address != null ? searchResult.address : searchResult.name;
            AddressTemplate.ofUntrusted(address).ifPresent(templates::add);
        }
        resolveTemplates(adjustTemplatesForDomainMode(templates), searchResult);
    }

    private List<AddressTemplate> adjustTemplatesForDomainMode(List<AddressTemplate> templates) {
        if (uic().environment().domain()) {
            // Domain mode: map standalone addresses for domain:
            // /subsystem=*            → /profile=*/subsystem=*  (resolve profiles via ModelTree.resolveWildcards())
            // /core-service=*         → not at domain root (lives under /host=*/server=*), skip or ignore
            // /deployment=*           → same address, but different semantics (assignment to server groups)
            // /deployment-overlay=*   → same address, but different semantics (assignment to server groups)
            // /extension=*            → same address, no mapping needed
            // /interface=*            → same address, no mapping needed
            // /path=*                 → same address, no mapping needed
            // /socket-binding-group=* → same address, no mapping needed
            // /system-property=*      → same address, no mapping needed
            return templates.stream()
                    .map(t -> {
                        if (SUBSYSTEM.equals(t.first().key)) {
                            return AddressTemplate.of(PROFILE, "*").append(t);
                        } else {
                            return t;
                        }
                    })
                    .collect(toList());
        } else {
            return templates;
        }
    }

    // ------------------------------------------------------ resolve

    private void resolveUntrustedTemplate(String value) {
        if (value != null) {
            AddressTemplate.ofUntrusted(value)
                    .map(List::of)
                    .ifPresent(templates -> resolveTemplates(templates, null));
        }
    }

    private void resolveTemplates(List<AddressTemplate> templates, SearchResult searchResult) {
        if (templates.isEmpty()) {
            headerMenuItem("no-results", "No matching resources found");
        } else {
            uic().modelTree().resolveWildcards(templates).then(resolved -> {
                if (resolved.isEmpty()) {
                    headerMenuItem("no-results", "No matching resources found");
                } else if (resolved.size() == 1) {
                    verifyAndGo(resolved.get(0), searchResult);
                } else {
                    showPickList(resolved);
                }
                return null;
            });
        }
    }

    // ------------------------------------------------------ pick

    private void showPickList(List<AddressTemplate> templates) {
        menuList.clear();
        if (templates.isEmpty()) {
            menuList.addItem(menuItem(Id.unique("no-results"), "No matching resources found").disabled());
        } else {
            menuList.addItem(menuItem(Id.unique("pick-address"),
                    "Multiple resources found. Pick one:").disabled());
            for (AddressTemplate template : templates) {
                MenuItem item = menuItem(Id.build(template.template), template.template)
                        .store(Keys.RESOLVED_ADDRESS, Boolean.TRUE);
                menuList.addItem(item);
            }
        }
        searchInput.expand();
        searchInput.menu().clearSearch();
    }

    // ------------------------------------------------------ go

    private void verifyAndGo(AddressTemplate template, SearchResult searchResult) {
        ResourceAddress address = template.resolve(uic().statementContext());
        new ResourceCheck(uic().dispatcher(), address).apply(new FlowContext()).then(context -> {
            int status = context.pop(404);
            if (status == 200) {
                closeAll();
                uic().routeRegistry().goTo(template, highlight(searchResult));
            } else {
                headerMenuItem("not-found", "Resource not found: " + template);
            }
            return null;
        });
    }

    private String highlight(SearchResult searchResult) {
        if (searchResult == null || searchResult.type == null || searchResult.name == null) {
            return null;
        }
        String prefix = switch (searchResult.type) {
            case "Attribute" -> "a:";
            case "Operation" -> "o:";
            case "Capability" -> "c:";
            default -> null;
        };
        return prefix != null ? prefix + searchResult.name : null;
    }

    // ------------------------------------------------------ helper methods

    private void headerMenuItem(String id, String text) {
        menuList.clear();
        menuList.addItem(menuItem(id, text).disabled());
        searchInput.expand();
        searchInput.menu().clearSearch();
    }

    private void closeAll() {
        searchInput.collapse(false);
        modal.close();
    }
}

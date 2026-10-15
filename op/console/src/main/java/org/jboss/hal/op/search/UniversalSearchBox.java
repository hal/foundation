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

import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.flow.FlowContext;
import org.jboss.hal.core.mgt.ModelGraphTools;
import org.jboss.hal.core.mgt.ResourceRef;
import org.jboss.hal.core.mgt.SearchResult;
import org.jboss.hal.core.mgt.SearchResultType;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.ResourceCheck;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Segment;
import org.jboss.hal.resources.Keys;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MenuList;
import org.patternfly.component.modal.Modal;
import org.patternfly.component.textinputgroup.SearchInputGroupTypeahead;
import org.patternfly.component.typeahead.Decision;
import org.patternfly.style.Classes;

import elemental2.dom.Event;
import elemental2.dom.HTMLElement;

import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.elemento.Elements.querySelector;
import static org.jboss.elemento.Key.Enter;
import static org.jboss.elemento.Key.Escape;
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
import static org.patternfly.icon.IconSets.fas.circleExclamation;
import static org.patternfly.style.Classes.component;

/**
 * Modal search box for the universal search feature. A new instance is created each time the search is opened, avoiding
 * stale-state issues from the {@link SearchInputGroupTypeahead} typeahead lifecycle inside a modal.
 *
 * <h2>Modes</h2>
 * <ul>
 *     <li><strong>Resource address mode</strong> — input starts with {@code /}. The typeahead fetches child types/names
 *         segment-by-segment via DMR. Selecting a segment appends it to the input (the menu closes via {@code stayOpen}
 *         returning {@code false}). Pressing Enter resolves the address: wildcards are expanded, the resource is verified,
 *         and on success the console navigates to it.</li>
 *     <li><strong>MGT mode</strong> — free text or type-filtered queries ({@code a:}, {@code r:}, {@code o:}, {@code c:}).
 *         The typeahead queries the model graph tools REST API. Selecting a result extracts its address, resolves wildcards,
 *         verifies the resource, and navigates with a highlight hint so the target view can scroll to the attribute/operation.
 *         Capabilities collect addresses from their {@code providedBy} list, which may yield a pick list.</li>
 * </ul>
 *
 * <h2>Data flow</h2>
 * <p>Search item fetching is handled by {@link UniversalSearchAsyncItems}, which routes to
 * {@link org.jboss.hal.ui.component.ResourceAddressAsyncItems} or {@link MgtSearchAsyncItems} based on the input.
 * This class handles everything after: event dispatch → use case selection → resolution pipeline → menu state → navigation.
 *
 * <h2>Menu state</h2>
 * <p>The menu can show three kinds of content:
 * <ol>
 *     <li><strong>Normal items</strong> — async items from the typeahead (address segments or MGT results). Managed entirely
 *         by the PatternFly typeahead framework.</li>
 *     <li><strong>Pick list</strong> — a header item + selectable address items, shown when wildcard resolution yields multiple
 *         results. The header is tracked as {@code headerItem}.</li>
 *     <li><strong>Error</strong> — a danger-styled disabled item, shown for invalid addresses, missing resources, or empty
 *         results. Tracked as {@code errorItem}.</li>
 * </ol>
 * <p>Pick list and error items are "special items" — they live outside the typeahead's async lifecycle and must be explicitly
 * cleaned up via {@link #clearSpecialItems()} before the menu returns to normal typeahead operation. The {@code stayOpen}
 * predicate keeps the menu open while special items are showing; once cleared, the menu closes naturally on selection.
 *
 * <h2>ESC handling</h2>
 * <p>ESC is layered: first press closes the menu (keeping the modal and input value), second press closes the modal entirely.
 * The modal's built-in {@code closeOnEsc} is disabled; ESC is handled manually in {@link #onKeydown(Event)}.
 *
 * <h2>Expand/collapse</h2>
 * <p>The typeahead framework handles expand/collapse for normal search flow. Explicit {@code expand()} calls appear only in
 * {@link #showPickList} and {@link #showError} (we're replacing content outside the typeahead lifecycle). Explicit
 * {@code collapse()} calls appear only in {@link #reset()}, {@link #closeAll()}, and the ESC handler.
 */
public class UniversalSearchBox {

    // ------------------------------------------------------ factory

    public static UniversalSearchBox universalSearchBox() {
        return new UniversalSearchBox();
    }

    // ------------------------------------------------------ instance

    private static final String ANCHOR_NAME = "--hal-universal-search";
    private static final int DEBOUNCE_MS = 300;
    private static final String PLACEHOLDER_ADDRESS_ONLY = "Go to a resource…";
    private static final String PLACEHOLDER_MGT = "Search or filter by a: r: o: c: — start with / for addresses…";

    private final Modal modal;
    private final MenuList menuList;
    private final SearchInputGroupTypeahead searchInput;
    /** Tracked so we can remove it from the DOM when transitioning back to normal search. */
    private MenuItem errorItem;
    /** Tracked so we can remove it from the DOM when transitioning back to normal search. */
    private MenuItem headerItem;

    UniversalSearchBox() {
        searchInput = searchInputGroupTypeahead("universal-search");
        ModelGraphTools modelGraphTools = uic().modelGraphTools();
        UniversalSearchAsyncItems asyncItems = new UniversalSearchAsyncItems(searchInput, modelGraphTools);

        searchInput.placeholder(modelGraphTools.available() ? PLACEHOLDER_MGT : PLACEHOLDER_ADDRESS_ONLY)
                .addMenu(menu(menu, click).scrollable()
                        .addContent(menuContent()
                                .addList(menuList = menuList()
                                        .addItems(asyncItems)))
                        .onSingleSelect((event, item, selected) -> onItemSelected(item)))
                .filter(lastSegment('/'))
                .refreshOn(this::refreshOrFilter)
                .stayOpen((e, c, m) -> hasSpecialItems())
                .onKeydown((e, si, value) -> onKeydown(e))
                .onKeyup((e, si, value) -> onKeyup(e))
                .onInput((e, si, value) -> onInput(value))
                .onToggle((e, c, expanded) -> anchorOverlayToModal(expanded))
                .noDefaultOnClear().onClear((e, si) -> onClear());

        modal = modal().css(halComponent(universalSearch))
                .top()
                .width("80%")
                .hideClose()
                .closeOnEsc(false)
                .addBody(modalBody().add(searchInput));
        modal.element().style.setProperty("anchor-name", ANCHOR_NAME);
    }

    public void show() {
        modal.open();
        searchInput.input().element().focus();
    }

    // ------------------------------------------------------ event handlers

    /**
     * Layered ESC: if the menu is open (showing results, pick list, or error), the first ESC closes just the menu — the modal
     * and input value are preserved so the user can edit and try again. If the menu is already closed, ESC exits the universal
     * search entirely.
     */
    private void onKeydown(Event e) {
        if (Escape.match(e)) {
            e.preventDefault();
            if (searchInput.expanded()) {
                clearSpecialItems();
                searchInput.collapse(false);
            } else {
                closeAll();
            }
        }
    }

    /**
     * Handles Enter on the input. Only acts when the menu is closed — if the menu is open, Enter selects a menu item via
     * {@link #onItemSelected}, and we must not double-process. The menu's expanded state is the single source of truth for this
     * coordination (no boolean flags needed). Only addresses (starting with {@code /}) are resolved; Enter on free-text MGT
     * queries is intentionally a no-op — the user must select a result from the menu.
     */
    private void onKeyup(Event e) {
        if (Enter.match(e) && e.target == searchInput.input().element()) {
            if (!searchInput.expanded()) {
                String value = searchInput.value();
                if (value != null && value.startsWith("/")) {
                    resolveAddress(value);
                }
            }
        }
    }

    /**
     * Fires on every keystroke. Clears special items (error, pick list header) so they don't linger when the user edits the
     * input after an error or pick list was shown. If the input becomes empty (backspace to empty), performs a full reset.
     */
    private void onInput(String value) {
        clearSpecialItems();
        if (value == null || value.isEmpty()) {
            reset();
        }
    }

    /**
     * Handles the clear button click. Since {@code noDefaultOnClear()} disables the framework's default value clearing, we
     * clear the value ourselves and reset all menu states.
     */
    private void onClear() {
        searchInput.value("");
        reset();
    }

    /**
     * Decides whether to re-fetch items from the server or filter the existing items client-side. Type-filtered queries
     * ({@code a:}, {@code r:}, etc.) and free-text MGT queries use a 300ms debounce to avoid hammering the API on every
     * keystroke. Address mode uses a structural check: if the number of {@code /} or {@code =} changed (the user moved to a new
     * segment), a full refresh fetches new child types/names. Otherwise, the existing items are filtered client-side using only
     * the text after the last {@code /}.
     */
    private Decision refreshOrFilter(String previous, String current) {
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
    }

    /**
     * Dispatches a menu item selection based on its metadata. Items carry either a {@link Keys#SEARCH_RESULT} (MGT result) or
     * an {@link Keys#ADDRESS_TEMPLATE} (pick list entry). Address typeahead segments carry neither — for those, the typeahead
     * framework handles the value update, and the {@code stayOpen} predicate (returning {@code false} when no special items
     * exist) lets the menu close naturally so the user can continue building the address.
     */
    private void onItemSelected(MenuItem item) {
        SearchResult searchResult = item.get(Keys.SEARCH_RESULT);
        AddressTemplate template = item.get(Keys.ADDRESS_TEMPLATE);
        if (searchResult != null) {
            evaluateSearchResult(searchResult);
        } else if (template != null) {
            pickAddress(template);
        }
        searchInput.input().element().focus();
    }

    // ------------------------------------------------------ use cases

    /**
     * Resource address mode: parses the user's input as an address template and feeds it into the resolution pipeline. Invalid
     * addresses (malformed segments, trailing {@code =} with no value) produce an error in the menu rather than a silent
     * no-op.
     */
    private void resolveAddress(String value) {
        if (value != null) {
            AddressTemplate.ofUntrusted(value)
                    .map(List::of)
                    .ifPresentOrElse(
                            templates -> resolveTemplates(templates, null),
                            () -> showError("Invalid address: " + value));
        }
    }

    /**
     * MGT mode: extracts address templates from a search result. For capabilities, the addresses come from the
     * {@code providedBy} list (one per provider, potentially yielding a pick list). For all other types, the address comes
     * directly from the result. Templates are adjusted for domain mode and fed into the resolution pipeline. The original
     * search result is carried through so {@link #highlight} can produce a hint for the target view.
     */
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

    /** Re-enters the resolution pipeline for an address the user picked from the pick list. */
    private void pickAddress(AddressTemplate template) {
        resolveTemplates(singletonList(template), null);
    }

    // ------------------------------------------------------ resolution pipeline
    //
    // All three use cases (resolveAddress, evaluateSearchResult, pickAddress) converge here.
    // The pipeline expands wildcards, verifies the resource exists, and either navigates or shows
    // an error/pick list. The searchResult is threaded through for highlight generation.

    /**
     * Takes address templates (possibly with wildcards) and resolves them to concrete resources via the model tree. Single
     * result → verify and navigate. Multiple results → show a pick list. Empty → show error.
     */
    private void resolveTemplates(List<AddressTemplate> templates, SearchResult searchResult) {
        if (templates.isEmpty()) {
            showError(noMatchingResources(searchResult));
        } else {
            uic().modelTree().resolveWildcards(templates).then(resolved -> {
                if (resolved.isEmpty()) {
                    showError(noMatchingResources(searchResult));
                } else if (resolved.size() == 1) {
                    verifyAndGo(resolved.get(0), searchResult);
                } else {
                    showPickList(resolved, searchResult);
                }
                return null;
            });
        }
    }

    /**
     * Domain mode adjustment: standalone addresses starting with {@code /subsystem=*} are prefixed with {@code /profile=*} so
     * that wildcard resolution can enumerate available profiles. All other addresses pass through unchanged. In standalone
     * mode, this is a no-op.
     */
    private List<AddressTemplate> adjustTemplatesForDomainMode(List<AddressTemplate> templates) {
        if (uic().environment().domain()) {
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

    /**
     * Final gate before navigation: resolves the template to a concrete address and checks if the resource exists on the
     * server. HTTP 200 → close the search and navigate. Anything else → show an error in the menu so the user can try a
     * different address.
     */
    private void verifyAndGo(AddressTemplate template, SearchResult searchResult) {
        ResourceAddress address = template.resolve(uic().statementContext());
        new ResourceCheck(uic().dispatcher(), address).apply(new FlowContext()).then(context -> {
            int status = context.pop(404);
            if (status == 200) {
                goTo(template, highlight(searchResult));
            } else {
                showError(resourceNotFound(template, searchResult));
            }
            return null;
        });
    }

    /**
     * Produces a highlight hint from the search result so the target view can scroll to the matching attribute, operation, or
     * capability. Resources don't produce a highlight (the view is already showing the resource). Returns {@code null} when the
     * search was address-only (no MGT result) or for resource results.
     */
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

    // ------------------------------------------------------ menu state
    //
    // These methods manage transitions between normal typeahead content and special items (pick list, error).
    // showPickList() and showError() replace the menu content and expand the menu explicitly (since we're
    // operating outside the typeahead's async lifecycle). reset() clears everything and collapses.
    // clearSpecialItems() removes just the tracked special items without touching the async items.

    /**
     * Full reset: clears special items, clears the menu list, and collapses the menu. Uses {@code clear()} rather than
     * {@code reset()} on the menu list to keep the async status as {@code resolved} — this avoids a redundant {@code load()}
     * cycle when the menu is next expanded (only {@code refresh()} from the debounce path should fetch items).
     */
    private void reset() {
        clearSpecialItems();
        menuList.clear();
        searchInput.collapse(false);
    }

    /**
     * Replaces the menu with a pick list when wildcard resolution yields multiple addresses. The header shows context from the
     * original search result (e.g., "3 resources for attribute connection-url") when available, or a generic message for
     * address-only searches. Each address is stored as an {@link Keys#ADDRESS_TEMPLATE} on the menu item so
     * {@link #onItemSelected} can dispatch to {@link #pickAddress} without reparsing.
     */
    private void showPickList(List<AddressTemplate> templates, SearchResult searchResult) {
        clearSpecialItems();
        menuList.clear();
        if (templates.isEmpty()) {
            showError("No matching resources found");
        } else {
            String header = pickListHeader(templates.size(), searchResult);
            headerItem = menuItem(Id.unique("pick"), header).disabled();
            menuList.addItem(headerItem);
            templates.stream()
                    .sorted(comparing(t -> t.template))
                    .forEach(template -> {
                        Segment last = template.last();
                        String text = last != null ? last.value : template.template;
                        menuList.addItem(menuItem(Id.build(template.template), text)
                                .description(template.template)
                                .store(Keys.ADDRESS_TEMPLATE, template));
                    });
            searchInput.expand();
        }
    }

    private String pickListHeader(int count, SearchResult searchResult) {
        if (searchResult != null && searchResult.type != null && searchResult.name != null) {
            return count + " resources for " + searchResult.type.toLowerCase() + " " + searchResult.name + ":";
        }
        return count + " resources found:";
    }

    private String noMatchingResources(SearchResult searchResult) {
        if (searchResult != null && searchResult.type != null && searchResult.name != null) {
            return "No resources for " + searchResult.type.toLowerCase() + " " + searchResult.name;
        }
        return "No matching resources found";
    }

    private String resourceNotFound(AddressTemplate template, SearchResult searchResult) {
        if (searchResult != null && searchResult.type != null && searchResult.name != null) {
            return "Resource not found for " + searchResult.type.toLowerCase() + " " + searchResult.name
                    + ": " + template.template;
        }
        return "Resource not found: " + template.template;
    }

    /**
     * Replaces the menu with a danger-styled error message. The error item is tracked as a special item so it can be cleaned up
     * when the user edits the input. Used for invalid addresses, non-existent resources, and empty wildcard resolution.
     */
    private void showError(String message) {
        clearSpecialItems();
        menuList.clear();
        errorItem = menuItem(Id.unique("error"), message)
                .icon(circleExclamation().element())
                .danger()
                .disabled();
        menuList.addItem(errorItem);
        searchInput.expand();
    }

    /** Returns {@code true} when the menu is showing a pick list or error — used by the {@code stayOpen} predicate. */
    private boolean hasSpecialItems() {
        return errorItem != null || headerItem != null;
    }

    /**
     * Removes tracked special items (error, header) from the DOM and nulls the references. Called on every input change to
     * ensure stale pick list or error items don't linger when the user edits the input. Safe to call when no special items
     * exist — {@code failSafeRemoveFromParent} handles null elements.
     */
    private void clearSpecialItems() {
        if (errorItem != null) {
            failSafeRemoveFromParent(errorItem);
            errorItem = null;
        }
        if (headerItem != null) {
            failSafeRemoveFromParent(headerItem);
            headerItem = null;
        }
    }

    /**
     * Repoints the overlay's CSS anchor from the text input to the modal on first expand. {@code Overlay.attach()} sets
     * {@code position-anchor} to the text input, so we must override after attach — which has already run by the time the
     * toggle handler fires. The anchor name on the modal is set once in the constructor.
     */
    private boolean overlayAnchored;

    private void anchorOverlayToModal(boolean expanded) {
        if (expanded) {
            HTMLElement overlay = querySelector(searchInput.element(), By.classname(component(Classes.overlay)));
            if (!overlayAnchored) {
                overlay.style.setProperty("position-anchor", ANCHOR_NAME);
                overlayAnchored = true;
            }
            String modalWidth = modal.element().getBoundingClientRect().width + "px";
            overlay.style.setProperty("min-width", modalWidth);
            overlay.style.setProperty("max-width", modalWidth);
        }
    }

    // ------------------------------------------------------ navigation

    /** Terminal action: closes the universal search and navigates to the resource with an optional highlight hint. */
    private void goTo(AddressTemplate template, String highlight) {
        closeAll();
        uic().routeRegistry().goTo(template, highlight);
    }

    private void closeAll() {
        searchInput.collapse(false);
        menuList.reset();
        modal.close();
    }
}

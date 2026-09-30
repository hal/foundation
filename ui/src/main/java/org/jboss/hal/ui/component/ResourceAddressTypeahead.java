package org.jboss.hal.ui.component;

import org.jboss.elemento.ElementClassListMethods;
import org.jboss.elemento.ElementEventMethods;
import org.jboss.elemento.HTMLElementStyleMethods;
import org.jboss.hal.meta.AddressTemplate;
import org.patternfly.component.ComponentIcon;
import org.patternfly.component.HasValue;
import org.patternfly.component.menu.MenuList;
import org.patternfly.component.textinputgroup.SearchInput;
import org.patternfly.style.Modifiers.Disabled;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;

import static org.patternfly.component.SelectionMode.single;
import static org.patternfly.component.menu.Menu.menu;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuType.menu;

/**
 * A typeahead component for navigating and selecting WildFly management resource addresses. As the user types an address (e.g.,
 * {@code /subsystem=datasources/data-source=}), the component queries the running server for matching child types and child
 * names and presents them as suggestions.
 * <p>
 * Structural characters ({@code /} and {@code =}) trigger a new server query, while regular characters are filtered client-side
 * from the already-loaded results.
 */
public class ResourceAddressTypeahead implements
        ComponentIcon<HTMLElement, ResourceAddressTypeahead>,
        Disabled<HTMLElement, ResourceAddressTypeahead>,
        ElementClassListMethods<HTMLElement, ResourceAddressTypeahead>,
        ElementEventMethods<HTMLElement, ResourceAddressTypeahead>,
        HasValue<String>,
        HTMLElementStyleMethods<HTMLElement, ResourceAddressTypeahead> {

    // ------------------------------------------------------ factory

    public static ResourceAddressTypeahead resourceAddressTypeahead(String id) {
        return new ResourceAddressTypeahead(id);
    }

    // ------------------------------------------------------ instance

    private final MenuList menuList;
    private final SearchInput searchInput;
    private final ResourceAddressAsyncItems asyncItems;
    private int lastSlashCount;
    private int lastEqualsCount;

    ResourceAddressTypeahead(String id) {
        this.lastSlashCount = 0;
        this.lastEqualsCount = 0;
        this.searchInput = SearchInput.searchInput(id);
        this.asyncItems = new ResourceAddressAsyncItems(searchInput);

        searchInput.addMenu(menu(menu, single)
                .scrollable()
                .addContent(menuContent()
                        .addList(menuList = menuList()
                                .addItems(asyncItems))));
        searchInput.onInput((e, si, value) -> {
            if (value == null || !value.startsWith("/")) {
                searchInput.collapse(false);
                return;
            }
            int slashes = countChar(value, '/');
            int equals = countChar(value, '=');
            if (slashes != lastSlashCount || equals != lastEqualsCount) {
                lastSlashCount = slashes;
                lastEqualsCount = equals;
                searchInput.collapse(false);
                menuList.reset();
            }
        });
    }

    @Override
    public HTMLElement element() {
        return searchInput.element();
    }

    // ------------------------------------------------------ builder

    @Override
    public ResourceAddressTypeahead icon(Element icon) {
        searchInput.icon(icon);
        return this;
    }

    @Override
    public ResourceAddressTypeahead removeIcon() {
        searchInput.removeIcon();
        return this;
    }

    @Override
    public ResourceAddressTypeahead that() {
        return this;
    }

    // ------------------------------------------------------ api

    public AddressTemplate addressTemplate() {
        return AddressTemplate.ofUntrusted(searchInput.value()).orElse(null);
    }

    public boolean expanded() {
        return searchInput.expanded();
    }

    public SearchInput searchInput() {
        return searchInput;
    }

    @Override
    public String value() {
        return searchInput.value();
    }

    public ResourceAddressTypeahead value(String value) {
        searchInput.value(value);
        return this;
    }

    public ResourceAddressTypeahead value(String value, boolean fireEvent) {
        searchInput.value(value, fireEvent);
        return this;
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

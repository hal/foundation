package org.jboss.hal.ui.component;

import org.jboss.elemento.ElementClassListMethods;
import org.jboss.elemento.ElementEventMethods;
import org.jboss.elemento.HTMLElementStyleMethods;
import org.jboss.elemento.Id;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.patternfly.component.AsyncItems;
import org.patternfly.component.ComponentIcon;
import org.patternfly.component.HasValue;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MenuList;
import org.patternfly.component.textinputgroup.SearchInput;
import org.patternfly.style.Modifiers.Disabled;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_TYPES_OPERATION;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.SelectionMode.single;
import static org.patternfly.component.menu.Menu.menu;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuItem.menuItem;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuType.menu;
import static org.patternfly.component.textinputgroup.SearchInput.searchInput;

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
    private int lastSlashCount;
    private int lastEqualsCount;

    ResourceAddressTypeahead(String id) {
        this.lastSlashCount = 0;
        this.lastEqualsCount = 0;
        this.searchInput = searchInput(id)
                .addMenu(menu(menu, single)
                        .scrollable()
                        .addContent(menuContent()
                                .addList(menuList = menuList()
                                        .addItems(resources()))));

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

    private AsyncItems<MenuList, MenuItem> resources() {
        return list -> {
            String value = searchInput.value();
            if (value == null || value.isEmpty() || !value.startsWith("/")) {
                return Promise.resolve(emptyList());
            }

            String input = value;
            int lastSlash = input.lastIndexOf('/');
            String parentPart = input.substring(0, lastSlash);
            String activePart = input.substring(lastSlash + 1);

            ResourceAddress parentAddress = buildAddress(parentPart);
            if (parentAddress == null) {
                return Promise.resolve(emptyList());
            }
            int equalsIndex = activePart.indexOf('=');

            if (equalsIndex >= 0) {
                String childType = activePart.substring(0, equalsIndex);
                return readChildrenNames(parentAddress, parentPart, childType);
            } else {
                return readChildrenTypes(parentAddress, parentPart);
            }
        };
    }

    private Promise<Iterable<MenuItem>> readChildrenTypes(ResourceAddress address, String parentPart) {
        Operation operation = new Operation.Builder(address, READ_CHILDREN_TYPES_OPERATION)
                .build();
        String prefix = parentPart.isEmpty() ? "/" : parentPart + "/";
        return uic().dispatcher().execute(operation, false)
                .then(result -> Promise.resolve(result.asList().stream()
                        .map(ModelNode::asString)
                        .sorted()
                        .map(type -> menuItem(Id.build(type), prefix + type))
                        .collect(toList())))
                .catch_(__ -> Promise.resolve(emptyList()));
    }

    private Promise<Iterable<MenuItem>> readChildrenNames(ResourceAddress address, String parentPart, String childType) {
        Operation operation = new Operation.Builder(address, READ_CHILDREN_NAMES_OPERATION)
                .param(CHILD_TYPE, childType)
                .build();
        String prefix = (parentPart.isEmpty() ? "/" : parentPart + "/") + childType + "=";
        return uic().dispatcher().execute(operation, false)
                .then(result -> Promise.resolve(result.asList().stream()
                        .map(ModelNode::asString)
                        .sorted()
                        .map(name -> menuItem(Id.build(name), prefix + name))
                        .collect(toList())))
                .catch_(__ -> Promise.resolve(emptyList()));
    }

    private ResourceAddress buildAddress(String addressString) {
        if (addressString == null || addressString.isEmpty()) {
            return ResourceAddress.root();
        }
        ResourceAddress address = ResourceAddress.root();
        String clean = addressString.startsWith("/") ? addressString.substring(1) : addressString;
        if (!clean.isEmpty()) {
            for (String segment : clean.split("/")) {
                int eq = segment.indexOf('=');
                if (eq <= 0) {
                    return null;
                }
                address.add(segment.substring(0, eq), segment.substring(eq + 1));
            }
        }
        return address;
    }

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

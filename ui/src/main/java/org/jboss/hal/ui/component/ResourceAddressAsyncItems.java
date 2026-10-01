package org.jboss.hal.ui.component;

import org.jboss.elemento.Id;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.patternfly.async.AsyncItems;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MenuList;
import org.patternfly.component.textinputgroup.SearchInput;

import elemental2.promise.Promise;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_TYPES_OPERATION;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.menu.MenuItem.menuItem;

public class ResourceAddressAsyncItems implements AsyncItems<MenuList, MenuItem> {

    private final SearchInput searchInput;

    public ResourceAddressAsyncItems(SearchInput searchInput) {
        this.searchInput = searchInput;
    }

    @Override
    public Promise<Iterable<MenuItem>> apply(MenuList menuItems) {
        String value = searchInput.value();
        if (value == null || !value.startsWith("/")) {
            return Promise.resolve(emptyList());
        }

        int lastSlash = value.lastIndexOf('/');
        String parentPart = value.substring(0, lastSlash);
        String activePart = value.substring(lastSlash + 1);

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
}

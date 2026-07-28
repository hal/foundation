package org.jboss.hal.ui.resource.form;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.patternfly.component.AsyncItems;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MenuList;

import elemental2.promise.IThenable;
import elemental2.promise.Promise;

import static elemental2.promise.Promise.reject;
import static elemental2.promise.Promise.resolve;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.resource.dialog.DialogBricks.addResourceModal;
import static org.patternfly.component.menu.MenuItem.menuItem;

class PathSupport {

    static AsyncItems<MenuList, MenuItem> paths() {
        Operation operation = new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                .param(CHILD_TYPE, PATH)
                .build();
        return menuList -> uic().dispatcher().execute(operation)
                .then(result -> resolve(result.asList().stream()
                        .map(ModelNode::asString)
                        .sorted()
                        // make sure identifier == text/value
                        // this is important because we need to select menu items based on values
                        .map(c -> menuItem(c, c))
                        .collect(toList())));
    }

    static Promise<MenuItem> newPath(String value) {
        IThenable.ThenOnFulfilledCallbackFn<ModelNode, MenuItem> processNewResource = modelNode -> {
            if (modelNode.isDefined()) {
                String name = modelNode.get(NAME).asString();
                return resolve(menuItem(name, name));
            } else {
                return reject("Add operation canceled");
            }
        };
        return addResourceModal(AddressTemplate.of(PATH, "*"), value, false).then(processNewResource);
    }
}

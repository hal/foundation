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
package org.jboss.hal.op.endpoint;

import org.jboss.elemento.IsElement;
import org.jboss.hal.resources.Keys;
import org.patternfly.component.menu.MenuList;
import org.patternfly.component.menu.SingleSelect;

import elemental2.dom.HTMLElement;
import elemental2.dom.URL;

import static elemental2.dom.DomGlobal.location;
import static org.jboss.hal.op.endpoint.EndpointModal.endpointModal;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuFooter.menuFooter;
import static org.patternfly.component.menu.MenuGroup.menuGroup;
import static org.patternfly.component.menu.MenuItem.menuItem;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.SingleSelect.singleSelect;
import static org.patternfly.component.menu.SingleSelectMenu.singleSelectMenu;

/**
 * The EndpointSelector class is responsible for managing and presenting a list of available endpoints in the user interface. It
 * allows users to select an endpoint, connect to it, and dynamically refresh the list of endpoints when necessary.
 * <p>
 * The class is implemented as a singleton to ensure a single instance is shared across the application. It uses components such
 * as SingleSelect, MenuList, and other utility methods to build an interactive UI for endpoint selection.
 */
public class EndpointSelector implements IsElement<HTMLElement> {

    private static EndpointSelector instance;

    public static EndpointSelector endpointSelector(EndpointStorage storage) {
        if (instance == null) {
            instance = new EndpointSelector(storage);
        }
        return instance;
    }

    private final EndpointStorage storage;
    private final SingleSelect singleSelect;
    private final MenuList menuList;

    EndpointSelector(EndpointStorage storage) {
        this.storage = storage;
        this.singleSelect = singleSelect(storage.current().name)
                .add(singleSelectMenu()
                        .onSingleSelect((event, component, selected) -> {
                            Endpoint endpoint = component.get(Keys.ENDPOINT);
                            if (endpoint != null) {
                                connect(endpoint);
                            }
                        })
                        .addContent(menuContent()
                                .addGroup(menuGroup("Management interfaces")
                                        .addList(menuList = menuList()
                                                .addItems(storage.endpoints(), endpoint ->
                                                        menuItem(endpoint.id, endpoint.name)
                                                                .store(Keys.ENDPOINT, endpoint)
                                                                .description(endpoint.url)))))
                        .addFooter(menuFooter()
                                .add(button("Choose another management interface")
                                        .link()
                                        .inline()
                                        .onClick((event, component) ->
                                                endpointModal(storage, true).open().then(endpoint -> {
                                                    connect(endpoint);
                                                    return null;
                                                })))));
        singleSelect.select(storage.current().id, false);
    }

    @Override
    public HTMLElement element() {
        return singleSelect.element();
    }

    public void refresh() {
        menuList.clear();
        menuList.addItems(storage.endpoints(), endpoint ->
                menuItem(endpoint.id, endpoint.name)
                        .store(Keys.ENDPOINT, endpoint)
                        .description(endpoint.url));
    }

    private void connect(Endpoint endpoint) {
        URL url = new URL(location.href);
        url.searchParams.set(Endpoint.CONNECT_PARAMETER, endpoint.name);
        location.assign(url.toString());
    }
}

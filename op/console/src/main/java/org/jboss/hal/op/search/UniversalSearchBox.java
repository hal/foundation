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

import org.patternfly.component.modal.Modal;
import org.patternfly.component.textinputgroup.SearchInput;

import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.universalSearch;
import static org.patternfly.component.modal.Modal.modal;
import static org.patternfly.component.modal.ModalBody.modalBody;
import static org.patternfly.component.textinputgroup.SearchInput.searchInput;

public class UniversalSearchBox {

    // ------------------------------------------------------ factory

    private static UniversalSearchBox instance;

    public static UniversalSearchBox universalSearchBox() {
        if (instance == null) {
            instance = new UniversalSearchBox();
        }
        return instance;
    }

    // ------------------------------------------------------ instance

    private final Modal modal;
    private final SearchInput searchInput;

    UniversalSearchBox() {
        searchInput = searchInput("universal-search")
                .placeholder("Search resources, attributes, operations…");
        searchInput.onChange((event, si, value) -> UniversalSearch.search(value));

        modal = modal().css(halComponent(universalSearch))
                .top()
                .hideClose()
                .addBody(modalBody().add(searchInput));
        modal.onClose((event, component) -> searchInput.value(""));
    }

    // ------------------------------------------------------ api

    public void show() {
        modal.open();
        searchInput.input().element().focus();
    }
}

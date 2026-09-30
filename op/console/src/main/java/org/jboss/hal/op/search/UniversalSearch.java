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

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.hal.op.mgt.ModelGraphTools;
import org.jboss.hal.op.mgt.ModelGraphToolsEvents;

import elemental2.dom.KeyboardEvent;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.EventType.keydown;

/**
 * Controller for the universal search feature. Registers the {@code Cmd+K} / {@code Ctrl+K} keyboard shortcut and listens for
 * {@link UniversalSearchEvents.Open} events to show the search modal. Tracks model graph tools (MGT) availability and passes it
 * to each new {@link UniversalSearchBox} instance.
 *
 * <p>The actual search logic is not in this class. It is handled by the {@link UniversalSearchAsyncItems} compound delegator,
 * which routes search requests to either {@link org.jboss.hal.ui.component.ResourceAddressAsyncItems} (for resource address
 * typeahead) or {@link MgtSearchAsyncItems} (for model graph tools queries).
 */
@Startup
@ApplicationScoped
public class UniversalSearch {

    private final ModelGraphTools modelGraphTools;
    private boolean mgtAvailable;

    @Inject
    public UniversalSearch(ModelGraphTools modelGraphTools) {
        this.modelGraphTools = modelGraphTools;
    }

    @PostConstruct
    void init() {
        ModelGraphToolsEvents.Availability.listen(document.body, details -> mgtAvailable = details.available);
        UniversalSearchEvents.Open.listen(document.body, this::openSearchBox);

        document.addEventListener(keydown.name, event -> {
            KeyboardEvent keyboardEvent = (KeyboardEvent) event;
            if ("k".equals(keyboardEvent.key) && (keyboardEvent.metaKey || keyboardEvent.ctrlKey)) {
                String tagName = document.activeElement != null ? document.activeElement.tagName : "";
                if (!"INPUT".equals(tagName) && !"TEXTAREA".equals(tagName)) {
                    keyboardEvent.preventDefault();
                    openSearchBox();
                }
            }
        });
    }

    private void openSearchBox() {
        new UniversalSearchBox(modelGraphTools, mgtAvailable).show();
    }
}

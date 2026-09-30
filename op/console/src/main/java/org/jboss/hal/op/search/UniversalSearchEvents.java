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
import org.jboss.hal.event.UIEvent;

import elemental2.dom.HTMLElement;

/**
 * UI events for the universal search feature. Currently, contains only the {@link Open} event, which is dispatched to open the
 * search modal from external sources (e.g., the masthead search button or the {@code Cmd+K} / {@code Ctrl+K} keyboard
 * shortcut).
 *
 * <p>The actual search data flow is handled by the {@link UniversalSearchAsyncItems} compound delegator using the
 * {@link org.patternfly.component.AsyncItems} pattern, not through events. This keeps the search input's built-in menu and
 * typeahead behavior intact while preserving separation of concerns through the {@code AsyncItems} interface.
 *
 * @see UniversalSearchAsyncItems
 */
public interface UniversalSearchEvents {

    /** Event dispatched to open the universal search modal. */
    interface Open extends UIEvent {

        String TYPE = UIEvent.type("universal-search", "open");

        /** Dispatches an open event from the given source element. */
        static void dispatch(HTMLElement source) {
            source.dispatchEvent(UIEvent.create(TYPE));
        }

        /** Registers a listener for open events on the given element. */
        static void listen(HTMLElement element, Callback listener) {
            element.addEventListener(TYPE, event -> listener.call());
        }
    }
}

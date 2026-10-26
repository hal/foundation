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
package org.jboss.hal.mgt;

import java.util.function.Consumer;

import org.jboss.hal.event.UIEvent;

import elemental2.dom.CustomEvent;

import static elemental2.dom.DomGlobal.document;

/**
 * UI event dispatched on {@code document.body} when MGT availability changes. Carries a boolean payload indicating
 * whether the MGT sidecar container is available.
 */
public interface ModelGraphAvailabilityEvent extends UIEvent {

    String TYPE = UIEvent.type("model-graph", "availability");

    class Details {

        public boolean available;
    }

    /** Dispatches an availability event on {@code document.body}. */
    static void dispatch(boolean available) {
        Details details = new Details();
        details.available = available;
        document.body.dispatchEvent(UIEvent.create(TYPE, details));
    }

    /** Registers a listener for availability events on {@code document.body}. */
    @SuppressWarnings("unchecked")
    static void listen(Consumer<Details> listener) {
        document.body.addEventListener(TYPE, event -> {
            CustomEvent<Details> customEvent = (CustomEvent<Details>) event;
            listener.accept(customEvent.detail);
        });
    }
}

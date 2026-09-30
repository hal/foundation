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
package org.jboss.hal.op.mgt;

import java.util.function.Consumer;

import org.jboss.elemento.Callback;
import org.jboss.hal.event.UIEvent;

import elemental2.dom.CustomEvent;
import elemental2.dom.HTMLElement;

/**
 * Umbrella interface for model graph tools (MGT) UI events. These events coordinate between the {@link ModelGraphTools} service
 * and the {@link ModelGraphToolsIndicator} without direct coupling.
 *
 * <p>The event flow is: {@link Ping} is dispatched to request an availability check, and {@link Availability} is dispatched in
 * response with the result.
 */
public interface ModelGraphToolsEvents {

    /** Event dispatched to request an MGT availability check. */
    interface Ping extends UIEvent {

        String TYPE = UIEvent.type("model-graph-tools", "ping");

        /** Dispatches a ping event from the given source element. */
        static void dispatch(HTMLElement source) {
            source.dispatchEvent(UIEvent.create(TYPE));
        }

        /** Registers a listener for ping events on the given element. */
        static void listen(HTMLElement element, Callback listener) {
            element.addEventListener(TYPE, event -> listener.call());
        }
    }

    /** Event dispatched in response to a {@link Ping}, carrying the MGT availability status. */
    interface Availability extends UIEvent {

        String TYPE = UIEvent.type("model-graph-tools", "availability");

        /** Event payload carrying the availability status. */
        class Details {

            public boolean available;
        }

        /** Dispatches an availability event with the given status from the source element. */
        static void dispatch(HTMLElement source, boolean available) {
            Details details = new Details();
            details.available = available;
            source.dispatchEvent(UIEvent.create(TYPE, details));
        }

        /** Registers a listener for availability events on the given element. */
        @SuppressWarnings("unchecked")
        static void listen(HTMLElement element, Consumer<Details> listener) {
            element.addEventListener(TYPE, event -> {
                CustomEvent<Details> customEvent = (CustomEvent<Details>) event;
                listener.accept(customEvent.detail);
            });
        }
    }
}

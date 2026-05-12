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
package org.jboss.hal.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Marker interface for HAL UI events dispatched as browser {@link elemental2.dom.CustomEvent}s on HTML elements.
 *
 * <p>
 * Implementations should provide static factory methods that create and return {@link elemental2.dom.CustomEvent} instances.
 * Use {@link #type(String, String...)} to build namespaced event type strings (e.g., {@code "hal::model-browser::add"}).
 *
 * @see <a
 *         href="https://developer.mozilla.org/en-US/docs/Web/Events/Creating_and_triggering_events">Creating and triggering
 *         events (MDN)</a>
 */
public interface UIEvent {

    /**
     * Builds a namespaced event type string by joining {@code "hal"}, the given identifier, and any additional identifiers with
     * {@code "::"} as separator. For example, {@code type("model-browser", "add")} produces {@code "hal::model-browser::add"}.
     *
     * @param identifier  the primary event identifier
     * @param identifiers optional additional identifiers for further namespacing
     * @return the namespaced event type string
     */
    static String type(String identifier, String... identifiers) {
        List<String> allIdentifiers = new ArrayList<>();
        allIdentifiers.add("hal");
        allIdentifiers.add(identifier);
        allIdentifiers.addAll(List.of(identifiers));
        return String.join("::", allIdentifiers);
    }
}

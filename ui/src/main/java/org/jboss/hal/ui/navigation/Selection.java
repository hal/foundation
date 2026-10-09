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
package org.jboss.hal.ui.navigation;

import java.util.Optional;

import org.jboss.hal.meta.AddressTemplate;

/**
 * Represents a parsed selection from a route parameter. A selection can contain an address template and/or a highlight
 * specification, separated by {@code @}.
 *
 * <h2>Format</h2>
 * <pre>
 * selection       = [address] ["@" highlight]
 * address         = address-template (e.g., "subsystem=datasources/data-source=ExampleDS")
 * highlight       = type ":" name
 * type            = "a" | "o"          // attribute or operation
 * name            = identifier         // e.g., "max-pool-size", "suspend"
 * </pre>
 *
 * <h2>Examples</h2>
 * <ul>
 *     <li>{@code "/subsystem=datasources"} — address only</li>
 *     <li>{@code "/subsystem=datasources@a:max-pool-size"} — address + attribute highlight</li>
 *     <li>{@code "/subsystem=datasources@o:suspend"} — address + operation highlight</li>
 *     <li>{@code "@a:max-pool-size"} — highlight only (no address)</li>
 * </ul>
 *
 * @param address   the address template, or empty if only a highlight was specified
 * @param highlight the highlight specification, or empty if no highlight was given
 */
public record Selection(Optional<AddressTemplate> address, Optional<Highlight> highlight) {

    static final char SEPARATOR = '@';

    /**
     * Parses a selection string into its address and highlight components.
     *
     * @param value the raw selection string from the route parameter, may be {@code null}
     * @return a parsed selection, never {@code null}
     */
    public static Selection parse(String value) {
        if (value == null || value.isEmpty()) {
            return new Selection(Optional.empty(), Optional.empty());
        }

        int separatorIndex = value.lastIndexOf(SEPARATOR);
        if (separatorIndex < 0) {
            return new Selection(AddressTemplate.ofUntrusted(value), Optional.empty());
        }

        String addressPart = value.substring(0, separatorIndex);
        String highlightPart = value.substring(separatorIndex + 1);

        Optional<AddressTemplate> address = addressPart.isEmpty()
                ? Optional.empty()
                : AddressTemplate.ofUntrusted(addressPart);
        Optional<Highlight> highlight = Highlight.parse(highlightPart);

        return new Selection(address, highlight);
    }

    /**
     * Encodes an address template and highlight into a selection string suitable for a route parameter.
     *
     * @param template  the address template - may be {@code null}
     * @param highlight the highlight string (e.g., "a:max-pool-size"), may be {@code null}
     * @return the encoded selection string, or {@code null} if both arguments are {@code null}
     */
    public static String encode(AddressTemplate template, String highlight) {
        if (template == null && highlight == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (template != null) {
            sb.append(template.template);
        }
        if (highlight != null && !highlight.isEmpty()) {
            sb.append(SEPARATOR).append(highlight);
        }
        return sb.toString();
    }
}

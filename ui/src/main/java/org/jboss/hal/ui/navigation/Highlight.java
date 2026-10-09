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

/**
 * A highlight specification for a management model element. Used to visually emphasize a specific attribute or operation after
 * navigating from the universal search.
 *
 * <h2>Format</h2>
 * <pre>
 * highlight = type ":" name
 * type      = "a"           // attribute
 *           | "o"           // operation
 * name      = identifier    // e.g., "max-pool-size", "suspend"
 * </pre>
 *
 * @param type the highlight type
 * @param name the name of the attribute or operation to highlight
 */
public record Highlight(Type type, String name) {

    public enum Type {
        ATTRIBUTE,
        OPERATION;

        static Optional<Type> fromPrefix(char prefix) {
            return switch (prefix) {
                case 'a' -> Optional.of(ATTRIBUTE);
                case 'o' -> Optional.of(OPERATION);
                default -> Optional.empty();
            };
        }

        public char prefix() {
            return switch (this) {
                case ATTRIBUTE -> 'a';
                case OPERATION -> 'o';
            };
        }
    }

    /**
     * Parses a highlight string like "a:max-pool-size" or "o:suspend".
     *
     * @param value the highlight string, may be {@code null} or empty
     * @return the parsed highlight, or empty if the string is invalid
     */
    static Optional<Highlight> parse(String value) {
        if (value == null || value.length() < 3 || value.charAt(1) != ':') {
            return Optional.empty();
        }
        return Type.fromPrefix(value.charAt(0))
                .map(type -> new Highlight(type, value.substring(2)));
    }

    @Override
    public String toString() {
        return type.prefix() + ":" + name;
    }
}

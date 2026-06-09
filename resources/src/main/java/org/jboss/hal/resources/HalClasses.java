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
package org.jboss.hal.resources;

/**
 * CSS class name constants following a BEM-like naming convention. Component classes use the {@code hal-c-} prefix, while
 * modifier classes use the {@code hal-m-} prefix.
 */
public interface HalClasses {

    // ------------------------------------------------------ constants (a-z)

    String body = "body";
    String capabilityReference = "capability-reference";
    String colon = "colon";
    String content = "content";
    String copy = "copy";
    String curlyBraces = "curly-braces";
    String deprecated = "deprecated";
    String detail = "detail";
    String defaultValue = "default-value";
    String form = "form";
    String dollar = "dollar";
    String expression = "expression";
    String goto_ = "goto";
    String modelBrowser = "model-browser";
    String name = "name";
    String nestedLabel = "nested-label";
    String providedBy = "provided-by";
    String rbacHidden = "rbac-hidden";
    String resource = "resource";
    String restricted = "restricted";
    String results = "results";
    String stabilityLevel = "stability-level";
    String status = "status";
    String tree = "tree";
    String unit = "unit";
    String undefined = "undefined";
    String value = "value";
    String view = "view";

    // ------------------------------------------------------ api

    /**
     * Builds a BEM-style component class name with optional elements.
     *
     * @param component the component name
     * @param elements  optional element names to append with double-underscore separator
     * @return a CSS class name in the format {@code hal-c-<component>} or {@code hal-c-<component>__<element1>-<element2>}
     */
    static String halComponent(String component, String... elements) {
        return compose('c', component, elements);
    }

    /**
     * Builds a BEM-style modifier class name.
     *
     * @param modifier the modifier name
     * @return a CSS class name in the format {@code hal-m-<modifier>}, or an empty string if the modifier is {@code null} or empty
     */
    static String halModifier(String modifier) {
        return modifier != null && !modifier.isEmpty() ? "hal-m-" + modifier : "";
    }

    // ------------------------------------------------------ internal

    /**
     * Internal helper that assembles a prefixed CSS class name from an abbreviation character, a type segment, and optional
     * element segments joined with hyphens.
     */
    static String compose(char abbreviation, String type, String... elements) {
        StringBuilder builder = new StringBuilder();
        if (type != null && !type.isEmpty()) {
            builder.append("hal").append("-").append(abbreviation).append("-").append(type);
            if (elements != null && elements.length != 0) {
                builder.append("__");
                for (int i = 0; i < elements.length; i++) {
                    builder.append(elements[i]);
                    if (i < elements.length - 1) {
                        builder.append("-");
                    }
                }
            }
        }
        return builder.toString();
    }
}

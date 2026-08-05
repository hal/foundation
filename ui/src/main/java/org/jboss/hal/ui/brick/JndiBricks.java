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
package org.jboss.hal.ui.brick;

import org.jboss.elemento.HTMLContainerBuilder;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.resources.HalClasses.colon;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.jndi;
import static org.jboss.hal.resources.HalClasses.name;
import static org.jboss.hal.resources.HalClasses.scheme;
import static org.jboss.hal.resources.HalClasses.segment;
import static org.jboss.hal.resources.HalClasses.slash;

/**
 * Factory methods for rendering JNDI names (e.g. {@code java:jboss/datasources/ExampleDS}) as colour-coded HTML elements.
 * <p>
 * Each structural part receives a distinct CSS class: the scheme ({@code java}), the colon separator, slash separators,
 * intermediate path segments, and the final resource name.
 */
public final class JndiBricks {

    /**
     * Renders a JNDI name as a colour-coded element. If the value contains a colon, it is parsed into scheme, path segments,
     * and name. Otherwise it is rendered as plain text.
     *
     * @param value the JNDI name string
     * @return a span element with the rendered content
     */
    public static HTMLElement renderJndiName(String value) {
        if (value == null || value.isEmpty()) {
            return span().element();
        }

        int colonIndex = value.indexOf(':');
        if (colonIndex < 0) {
            return span().text(value).element();
        }

        HTMLContainerBuilder<HTMLElement> container = span().css(halComponent(jndi));
        String schemePart = value.substring(0, colonIndex);
        String rest = value.substring(colonIndex + 1);

        container.add(span().css(halComponent(jndi, scheme)).text(schemePart));
        container.add(span().css(halComponent(jndi, colon)));

        if (rest.startsWith("/")) {
            container.add(span().css(halComponent(jndi, slash)));
            rest = rest.substring(1);
        }

        if (!rest.isEmpty()) {
            String[] parts = rest.split("/");
            for (int i = 0; i < parts.length; i++) {
                if (i == parts.length - 1) {
                    container.add(span().css(halComponent(jndi, name)).text(parts[i]));
                } else {
                    container.add(span().css(halComponent(jndi, segment)).text(parts[i]));
                    container.add(span().css(halComponent(jndi, slash)));
                }
            }
        }

        return container.element();
    }

    private JndiBricks() {
    }
}

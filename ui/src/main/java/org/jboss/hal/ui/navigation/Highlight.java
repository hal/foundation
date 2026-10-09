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

import org.jboss.hal.resources.Dataset;

import elemental2.dom.AddEventListenerOptions;
import elemental2.dom.HTMLElement;
import elemental2.dom.ScrollIntoViewOptions;

import static elemental2.dom.DomGlobal.clearTimeout;
import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.requestAnimationFrame;
import static elemental2.dom.DomGlobal.setTimeout;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.resources.HalClasses.highlight;

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
     * @param value the highlight string - may be {@code null} or empty
     * @return the parsed highlight, or empty if the string is invalid
     */
    public static Optional<Highlight> parse(String value) {
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

    public void flash(HTMLElement ancestor, HTMLElement element) {
        ancestor.dataset.delete(Dataset.highlight);
        requestAnimationFrame(__ -> requestAnimationFrame(___ -> {
            double[] fallback = {0};
            // scrollend doesn't bubble, but capture phase sees it from any scrollable ancestor
            AddEventListenerOptions onceCapture = AddEventListenerOptions.create();
            onceCapture.setOnce(true);
            onceCapture.setCapture(true);
            elemental2.dom.EventListener listener = ____ -> {
                clearTimeout(fallback[0]);
                applyFlash(element);
            };
            document.addEventListener("scrollend", listener, onceCapture);
            // Fallback if the element is already in view (no scroll → no scrollend)
            fallback[0] = setTimeout(_____ -> {
                document.removeEventListener("scrollend", listener, onceCapture);
                applyFlash(element);
            }, 150);
            ScrollIntoViewOptions options = ScrollIntoViewOptions.create();
            options.setBehavior("smooth");
            options.setBlock("center");
            element.scrollIntoView(options);
        }));
    }

    private void applyFlash(HTMLElement element) {
        element.classList.add(halModifier(highlight));
        setTimeout(__ -> element.classList.remove(halModifier(highlight)), 3000);
    }
}

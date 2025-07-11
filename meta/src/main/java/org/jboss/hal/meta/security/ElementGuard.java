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
package org.jboss.hal.meta.security;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jboss.elemento.By;
import org.jboss.elemento.Elements;
import org.jboss.hal.resources.HalClasses;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.document;
import static java.util.stream.StreamSupport.stream;
import static org.jboss.elemento.Elements.querySelectorAll;
import static org.jboss.hal.resources.HalClasses.rbacHidden;
import static org.patternfly.style.Classes.util;

/**
 * Helper class to process elements with constraints in their {@code data-constraint} attributes. Toggles the element's
 * visibility depending on the {@link AuthorisationDecision} result.
 */
public class ElementGuard {

    public static final String constraint = "constraint";

    /**
     * Adds the {@link HalClasses#rbacHidden} CSS class if {@code condition == true}, removes it otherwise.
     */
    public static void toggle(HTMLElement element, boolean condition) {
        if (new Visible().test(element)) {
            Elements.toggle(element, rbacHidden, condition);
        }
    }

    public static void processElements(AuthorisationDecision authorisationDecision, String selector) {
        processElements(authorisationDecision, querySelectorAll(document, By.selector(selector)));
    }

    public static void processElements(AuthorisationDecision authorisationDecision, HTMLElement element) {
        processElements(authorisationDecision, querySelectorAll(element, By.data(constraint)));
    }

    private static void processElements(AuthorisationDecision authorisationDecision, Iterable<HTMLElement> elements) {
        stream(elements.spliterator(), false)
                .filter(new Visible()) // prevents that hidden elements become visible by Toggle()
                .forEach(new Toggle(authorisationDecision));
    }

    /**
     * Predicate which returns only visible elements.
     * <p>
     * Use this filter to find elements which can be processed by other security related functions such as {@link Toggle}.
     */
    public static class Visible implements Predicate<HTMLElement> {

        @Override
        public boolean test(HTMLElement element) {
            return Elements.isVisible(element) && !element.classList.contains(util("display-none"));
        }
    }


    /** Toggle the CSS class {@link HalClasses#rbacHidden} based on the element's constraints. */
    public static class Toggle implements Consumer<HTMLElement> {

        private final AuthorisationDecision authorisationDecision;

        public Toggle(AuthorisationDecision authorisationDecision) {
            this.authorisationDecision = authorisationDecision;
        }

        @Override
        public void accept(HTMLElement element) {
            String data = String.valueOf(element.dataset.get(constraint));
            if (data != null) {
                Constraints constraints = Constraints.parse(data);
                Elements.toggle(element, rbacHidden, !authorisationDecision.allowed(constraints));
            }
        }
    }

    private ElementGuard() {
    }
}

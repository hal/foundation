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
package org.jboss.hal.ui.filter;

import org.jboss.elemento.IsElement;
import org.patternfly.core.ObservableValue;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.span;
import static org.patternfly.token.Token.globalFontSizeSm;
import static org.patternfly.token.Token.globalTextColorDisabled;

public class ItemCount implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static ItemCount itemCount(ObservableValue<Integer> visible, ObservableValue<Integer> total, String singular,
            String plural) {
        return new ItemCount(visible, total, singular, plural);
    }

    // ------------------------------------------------------ instance

    private final String singular;
    private final String plural;
    private final HTMLElement root;

    ItemCount(ObservableValue<Integer> visible, ObservableValue<Integer> total, String singular, String plural) {
        this.singular = singular;
        this.plural = plural;
        this.root = span()
                .style("color", globalTextColorDisabled.var)
                .style("font-size", globalFontSizeSm.var)
                .element();
        visible.subscribe((v, __) -> root.textContent = text(v, total.get()));
        total.subscribe((t, __) -> root.textContent = text(visible.get(), t));
        visible.publish();
        total.publish();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    String text(int v, int t) {
        if (v == t) {
            return t + " " + (t == 1 ? singular : plural);
        } else {
            return v + " / " + t + " " + plural;
        }
    }
}

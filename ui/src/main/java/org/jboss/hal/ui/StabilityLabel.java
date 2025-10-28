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
package org.jboss.hal.ui;

import org.jboss.elemento.ElementClassListMethods;
import org.jboss.elemento.ElementTextDelegate;
import org.jboss.elemento.HTMLElementStyleMethods;
import org.jboss.hal.env.Stability;
import org.patternfly.component.label.Label;
import org.patternfly.core.Aria;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;

import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.stabilityLevel;
import static org.jboss.hal.ui.BuildingBlocks.stabilityColor;
import static org.jboss.hal.ui.BuildingBlocks.stabilityIcon;
import static org.patternfly.component.label.Label.label;

public class StabilityLabel implements
        ElementClassListMethods<HTMLElement, StabilityLabel>,
        ElementTextDelegate<HTMLElement, StabilityLabel>,
        HTMLElementStyleMethods<HTMLElement, StabilityLabel> {

    // ------------------------------------------------------ factory

    public static StabilityLabel stabilityLabel(Stability stability) {
        return new StabilityLabel(stability);
    }

    // ------------------------------------------------------ instance

    private final Label label;

    StabilityLabel(Stability stability) {
        label = label(stability.label, stabilityColor(stability))
                .css(halComponent(stabilityLevel))
                .aria(Aria.label, stability.label)
                .icon(stabilityIcon(stability));
    }

    @Override
    public Element textDelegate() {
        return label.textDelegate();
    }

    @Override
    public StabilityLabel that() {
        return this;
    }

    @Override
    public HTMLElement element() {
        return label.element();
    }

    // ------------------------------------------------------ modifier

    public StabilityLabel compact() {
        return compact(false);
    }

    public StabilityLabel compact(boolean compact) {
        if (compact) {
            label.compact().removeIcon();
        }
        return this;
    }
}

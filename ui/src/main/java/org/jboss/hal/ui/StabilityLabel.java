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
import static org.jboss.hal.ui.brick.StabilityBricks.stabilityIcon;
import static org.jboss.hal.ui.brick.StabilityBricks.stabilityStatus;
import static org.patternfly.component.label.Label.label;

/**
 * A PatternFly {@link Label} component that displays a WildFly {@link Stability} level with an appropriate icon and
 * color.
 * <p>
 * The label uses PatternFly status colors and icons to visually represent the stability level (e.g., experimental,
 * preview, community, default).
 */
public class StabilityLabel implements
        ElementClassListMethods<HTMLElement, StabilityLabel>,
        ElementTextDelegate<HTMLElement, StabilityLabel>,
        HTMLElementStyleMethods<HTMLElement, StabilityLabel> {

    // ------------------------------------------------------ factory

    /** Creates a new stability label for the given stability level. */
    public static StabilityLabel stabilityLabel(Stability stability) {
        return new StabilityLabel(stability);
    }

    // ------------------------------------------------------ instance

    private final Label label;

    /** Creates a new stability label displaying the given stability level with appropriate icon and color. */
    StabilityLabel(Stability stability) {
        label = label(stability.label)
                .css(halComponent(stabilityLevel), stabilityStatus(stability).modifier())
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

    /** Makes the label compact by removing the icon and reducing its size. */
    public StabilityLabel compact() {
        return compact(false);
    }

    /** Conditionally makes the label compact. When compact, the icon is removed and the label size is reduced. */
    public StabilityLabel compact(boolean compact) {
        if (compact) {
            label.compact().removeIcon();
        }
        return this;
    }
}

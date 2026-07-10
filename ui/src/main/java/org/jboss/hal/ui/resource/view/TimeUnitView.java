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
package org.jboss.hal.ui.resource.view;

import org.jboss.elemento.IsElement;
import org.jboss.hal.ui.resource.ResourceAttribute;
import org.jboss.hal.ui.resource.composite.TimeUnitAttribute;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.resources.HalClasses.timeUnit;
import static org.jboss.hal.resources.HalClasses.undefined;
import static org.jboss.hal.resources.HalClasses.view;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.Gap.sm;

/**
 * Read-only view component for keepalive-time attributes. Renders a consolidated single-line display:
 * <ul>
 *     <li><b>Defined</b> — shows the time and unit as a single value, e.g. "100 MILLISECONDS"</li>
 *     <li><b>Undefined</b> — gray "undefined" text</li>
 * </ul>
 */
class TimeUnitView implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    static TimeUnitView timeUnitValue(ResourceAttribute ra) {
        return new TimeUnitView(ra);
    }

    // ------------------------------------------------------ instance

    private final HTMLElement root;

    TimeUnitView(ResourceAttribute ra) {
        long time = TimeUnitAttribute.time(ra.value);
        String unit = TimeUnitAttribute.unit(ra.value);

        this.root = flex().css(halComponent(resource, view, timeUnit))
                .alignItems(center).columnGap(sm)
                .element();

        if (time >= 0 && unit != null) {
            root.appendChild(span().text(String.valueOf(time)).element());
            root.appendChild(span().text(unit).element());
        } else {
            root.classList.add(halComponent(resource, view, undefined));
            root.appendChild(span().text("undefined").element());
        }
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}

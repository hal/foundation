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

import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;

import org.jboss.hal.ui.resource.pipeline.TimeUnitProvider;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.resources.HalClasses.timeUnit;
import static org.jboss.hal.resources.HalClasses.view;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.Gap.sm;

/** View item for time-unit composite attributes. Shows time value + unit (e.g. "100 MILLISECONDS"). */
public class TimeUnitViewItem extends AbstractViewItem {

    public TimeUnitViewItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        super(identifier, attribute, context);
    }

    @Override
    protected HTMLElement definedValue() {
        long time = TimeUnitProvider.time(attribute.value());
        String unit = TimeUnitProvider.unit(attribute.value());

        HTMLElement root = flex().css(halComponent(resource, view, timeUnit))
                .alignItems(center).columnGap(sm)
                .element();

        if (time >= 0 && unit != null) {
            root.appendChild(span().text(String.valueOf(time)).element());
            root.appendChild(span().text(unit).element());
        } else {
            root.appendChild(span().text(attribute.value().asString()).element());
        }
        return root;
    }
}

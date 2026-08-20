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

import org.jboss.hal.ui.resource.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.Pipeline;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNIT;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.Gap.sm;

/** View item for time-unit composite attributes. Shows time value + unit (e.g. "100 MILLISECONDS"). */
public class TimeUnitViewItem extends AbstractViewItem {

    private final HTMLElement valueElement;
    private final HTMLElement root;

    public TimeUnitViewItem(PipelineContext context, String identifier, ResolvedAttribute attribute) {
        super(identifier, attribute);
        this.valueElement = ViewItemBricks.valueElement(context, attribute, this::definedValue);
        this.root = descriptionListGroup(identifier)
                .addTerm(ViewItemBricks.label(context, attribute.description()))
                .addDescription(descriptionListDescription().add(valueElement))
                .element();
    }

    private HTMLElement definedValue(PipelineContext context, ResolvedAttribute attribute) {
        ViewItem timeItem = Pipeline.instance().viewItem(context, attribute.child(TIME));
        ViewItem unitItem = Pipeline.instance().viewItem(context, attribute.child(UNIT));

        return flex().gap(sm)
                .addItem(flexItem()
                        .add(timeItem.valueElement()))
                .addItem(flexItem()
                        .add(unitItem.valueElement()))
                .element();
    }

    @Override
    public HTMLElement valueElement() {
        return valueElement;
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}

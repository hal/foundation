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

import org.jboss.hal.dmr.Property;

import elemental2.dom.HTMLElement;

import static org.patternfly.component.label.Label.label;
import static org.patternfly.component.label.LabelGroup.labelGroup;
import static org.patternfly.style.Color.grey;

/**
 * View item for free-form key-value map attributes. Renders the map entries as a compact PatternFly Label Group
 * with {@code key=value} labels.
 */
public class MapViewItem extends AbstractViewItem {

    public MapViewItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        super(identifier, attribute, context);
    }

    @Override
    protected HTMLElement definedValue() {
        return labelGroup()
                .numLabels(5)
                .addItems(attribute.value().asPropertyList(),
                        (Property entry) -> label(entry.getName() + "=" + entry.getValue().asString(), grey)
                                .compact())
                .element();
    }
}

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
package org.jboss.hal.ui.resource.pipeline;

import org.jboss.hal.meta.description.AttributeDescription;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.span;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;

/**
 * Placeholder view item used during the pipeline build-out. Renders a simple label + text value display. Will be replaced with
 * proper type-specific implementations during migration.
 */
class PlaceholderViewItem implements ViewItem {

    private final String identifier;
    private final HTMLElement root;

    PlaceholderViewItem(String identifier, AttributeGroup group, PipelineContext context) {
        this.identifier = identifier;
        AttributeDescription primary = group.primary();
        String value = context.value(primary).isDefined()
                ? context.value(primary).asString()
                : "undefined";
        this.root = descriptionListGroup(identifier)
                .addTerm(descriptionListTerm(primary.name()))
                .addDescription(descriptionListDescription().add(span().text(value)))
                .element();
    }

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}

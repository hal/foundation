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

import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;

import elemental2.dom.HTMLElement;

import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;

/**
 * Default view item for single attributes. Handles all standard types: BOOLEAN (switch), simple types (plain text, unit,
 * allowed values), LIST (inline list or JSON), and OBJECT (JSON). Used by
 * {@code org.jboss.hal.ui.resource.pipeline.DefaultItemProvider} and
 * {@code org.jboss.hal.ui.resource.pipeline.FlatteningProvider}.
 */
public class StandardViewItem implements ViewItem {

    private final String identifier;
    private final ResolvedAttribute attribute;
    private final HTMLElement valueElement;
    private final HTMLElement root;

    public StandardViewItem(PipelineContext context, String identifier, ResolvedAttribute attribute) {
        this.identifier = identifier;
        this.attribute = attribute;
        this.valueElement = ViewItemBricks.valueElement(context, attribute, new StandardDefinedValue());
        this.root = descriptionListGroup(identifier)
                .addTerm(ViewItemBricks.label(context, attribute.description()))
                .addDescription(descriptionListDescription().add(valueElement))
                .element();
    }

    // ------------------------------------------------------ api

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public ResolvedAttribute attribute() {
        return attribute;
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

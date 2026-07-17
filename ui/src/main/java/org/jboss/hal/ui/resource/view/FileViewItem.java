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

import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;

/** View item for file composite attributes ({@code {path, relative-to}}). Shows "path relative to dir" or just "path". */
public class FileViewItem extends AbstractViewItem {

    private final HTMLElement valueElement;
    private final HTMLElement root;

    public FileViewItem(PipelineContext context, String identifier, ResolvedAttribute attribute) {
        super(identifier, attribute);
        this.valueElement = ViewItemBricks.valueElement(context, attribute,
                (ctx, attr) -> ViewItemBricks.fileValue(ctx, attr.child(PATH), attr.child(RELATIVE_TO)));
        this.root = descriptionListGroup(identifier)
                .addTerm(ViewItemBricks.label(context, attribute.description()))
                .addDescription(descriptionListDescription().add(valueElement))
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

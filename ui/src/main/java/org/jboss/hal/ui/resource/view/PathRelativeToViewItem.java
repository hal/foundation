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
import org.jboss.hal.ui.resource.pipeline.ResolvedAttribute;

import java.util.List;

import org.jboss.hal.meta.description.AttributeDescription;
import org.patternfly.component.list.DescriptionListTerm;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.core.Humanize.sentenceCase;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.resources.HalClasses.undefined;
import static org.jboss.hal.resources.HalClasses.view;
import static org.jboss.hal.ui.brick.AttributeBricks.attributeDescriptionPopover;
import static org.jboss.hal.ui.brick.DescriptionBricks.AttributeDescriptionContent.all;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;

/**
 * View item for sibling path + relative-to attribute groups. Shows "path relative to dir" as a single visual unit. Implements
 * {@link ViewItem} directly (not via {@link AbstractViewItem}) because it renders two attributes in a single group.
 */
public class PathRelativeToViewItem implements ViewItem {

    private final String identifier;
    private final ResolvedAttribute primaryAttribute;
    private final HTMLElement root;

    public PathRelativeToViewItem(String identifier, List<ResolvedAttribute> attributes, PipelineContext context) {
        this.identifier = identifier;

        ResolvedAttribute pathAttr = findPath(attributes);
        ResolvedAttribute relativeToAttr = findRelativeTo(attributes);
        this.primaryAttribute = pathAttr != null ? pathAttr : attributes.get(0);
        AttributeDescription primaryDesc = primaryAttribute.description();

        String label = sentenceCase(primaryDesc.name());
        DescriptionListTerm term = descriptionListTerm(label)
                .help(attributeDescriptionPopover(label, primaryDesc, all));

        HTMLElement valueElement;
        if (pathAttr != null && !pathAttr.readable()) {
            valueElement = AbstractViewItem.restrictedValue();
        } else {
            valueElement = buildValue(pathAttr, relativeToAttr);
        }

        this.root = descriptionListGroup(identifier)
                .addTerm(term)
                .addDescription(descriptionListDescription().add(valueElement))
                .element();
    }

    private HTMLElement buildValue(ResolvedAttribute pathAttr, ResolvedAttribute relativeToAttr) {
        String path = pathAttr != null && pathAttr.value().isDefined() ? pathAttr.value().asString() : null;
        String relativeTo = relativeToAttr != null && relativeToAttr.value().isDefined()
                ? relativeToAttr.value().asString() : null;

        if (path != null && relativeTo != null) {
            return span()
                    .add(span().text(path))
                    .add(span().text(" relative to ").style("color", "var(--pf-t--global--color--subtle)"))
                    .add(span().text(relativeTo))
                    .element();
        } else if (path != null) {
            return span().text(path).element();
        } else {
            HTMLElement el = span().text("undefined").element();
            el.classList.add(halComponent(resource, view, undefined));
            return el;
        }
    }

    private static ResolvedAttribute findPath(List<ResolvedAttribute> attributes) {
        return attributes.stream()
                .filter(ra -> !ra.name().endsWith(RELATIVE_TO))
                .findFirst().orElse(null);
    }

    private static ResolvedAttribute findRelativeTo(List<ResolvedAttribute> attributes) {
        return attributes.stream()
                .filter(ra -> ra.name().endsWith(RELATIVE_TO))
                .findFirst().orElse(null);
    }

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public ResolvedAttribute attribute() {
        return primaryAttribute;
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}

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

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.elemento.Id;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.ui.resource.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.ui.resource.view.ViewItemDefaults.NUM_LABELS;
import static org.patternfly.component.label.Label.label;
import static org.patternfly.component.label.LabelGroup.labelGroup;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.style.Color.purple;

/**
 * View item for LIST attributes with simple-record value-type. Renders each list entry as a compact label showing the record's
 * key values joined by commas.
 */
public class ListSimpleRecordViewItem extends AbstractViewItem {

    private final HTMLElement valueElement;
    private final HTMLElement root;

    public ListSimpleRecordViewItem(PipelineContext context, String identifier, ResolvedAttribute attribute) {
        super(identifier, attribute);
        this.valueElement = ViewItemBricks.valueElement(context, attribute, this::definedValue);
        this.root = descriptionListGroup(identifier)
                .addTerm(ViewItemBricks.label(context, attribute.description()))
                .addDescription(descriptionListDescription().add(valueElement))
                .element();
    }

    private HTMLElement definedValue(PipelineContext context, ResolvedAttribute attribute) {
        List<ModelNode> items = attribute.value().asList();
        return labelGroup()
                .numLabels(NUM_LABELS)
                .addItems(items, item -> {
                    String summary = recordSummary(item);
                    return label(Id.build(identifier(), summary), summary, purple);
                })
                .element();
    }

    private String recordSummary(ModelNode record) {
        if (record.isDefined() && record.getType() == org.jboss.hal.dmr.ModelType.OBJECT) {
            return record.asPropertyList().stream()
                    .filter(p -> p.getValue().isDefined())
                    .map(p -> p.getValue().asString())
                    .collect(Collectors.joining(", "));
        }
        return record.asString();
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

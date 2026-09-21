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

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.AttributeDescriptions;
import org.jboss.hal.ui.resource.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.Pipeline;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.core.Humanize.sentenceCase;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.resources.HalClasses.view;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.table.Tbody.tbody;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.Th.th;
import static org.patternfly.component.table.Thead.thead;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.style.Classes.fitContent;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.table;

/**
 * View item for LIST attributes with simple-record value-type. Renders the list entries as a compact PatternFly table with
 * column headers derived from the value-type sub-attribute names. Each cell delegates to the attribute pipeline for
 * type-appropriate rendering.
 */
public class ListSimpleRecordViewItem extends AbstractViewItem {

    private final HTMLElement valueElement;
    private final HTMLElement root;

    public ListSimpleRecordViewItem(PipelineContext context, String identifier, ResolvedAttribute attribute) {
        super(identifier, attribute);
        this.valueElement = ViewItemBricks.valueElement(context, attribute, this::definedValue);
        this.root = descriptionListGroup(identifier).css(halComponent(resource, view, table))
                .addTerm(ViewItemBricks.label(context, attribute.description()))
                .addDescription(descriptionListDescription().add(valueElement))
                .element();
    }

    private HTMLElement definedValue(PipelineContext context, ResolvedAttribute attribute) {
        AttributeDescriptions descriptions = attribute.description().valueTypeAttributeDescriptions();
        List<AttributeDescription> columns = new ArrayList<>();
        for (AttributeDescription ad : descriptions) {
            columns.add(ad);
        }

        List<ModelNode> items = attribute.value().asList();
        var headRow = tr(identifier() + "-head");
        for (AttributeDescription col : columns) {
            headRow.add(th(col.name()).css(modifier(fitContent)).text(sentenceCase(col.name())));
        }

        var body = tbody();
        int index = 0;
        for (ModelNode item : items) {
            var row = tr(identifier() + "-" + index);
            ResolvedAttribute entry = attribute.listEntry(item);
            for (AttributeDescription col : columns) {
                ResolvedAttribute child = entry.child(col.name());
                ViewItem cellViewItem = Pipeline.instance().viewItem(context, child.detachFromParent());
                if (cellViewItem != null) {
                    row.add(td(col.name()).add(cellViewItem.valueElement()));
                } else {
                    row.add(td(col.name()));
                }
            }
            body.addRow(row);
            index++;
        }

        return table().compact()
                .addHead(thead().addRow(headRow))
                .addBody(body)
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

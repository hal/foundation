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
package org.jboss.hal.ui.modelbrowser;

import java.util.function.Function;

import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.ResourceDescription;
import org.patternfly.component.table.Td;
import org.patternfly.component.table.TitleCell;
import org.patternfly.component.table.Tr;
import org.patternfly.style.Variable;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ACCESS_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONFIGURATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.METRIC;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_WRITE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORAGE;
import static org.jboss.hal.ui.BuildingBlocks.attributeDescription;
import static org.jboss.hal.ui.BuildingBlocks.attributeName;
import static org.jboss.hal.ui.BuildingBlocks.AttributeDescriptionContent.allButReadOnly;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.TitleCell.titleCell;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.icon.IconSets.fas.database;
import static org.patternfly.icon.IconSets.fas.edit;
import static org.patternfly.icon.IconSets.fas.lock;
import static org.patternfly.icon.IconSets.fas.memory;
import static org.patternfly.icon.IconSets.patternfly.trendUp;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Variable.utilVar;

class AttributeRow implements Function<AttributeDescription, Tr> {

    private final ResourceDescription resource;
    private final boolean anyComplexAttributes;

    AttributeRow(ResourceDescription resource, boolean anyComplexAttributes) {
        this.resource = resource;
        this.anyComplexAttributes = anyComplexAttributes;
    }

    @Override
    public Tr apply(AttributeDescription attribute) {
        return tr(attribute.name())
                .run(tr -> {
                    if (anyComplexAttributes) {
                        TitleCell titleCell = titleCell();
                        tr.addTitleCell(titleCell
                                .add(attributeName(attribute, () -> uic().environment().highlightStability(resource.stability(),
                                        attribute.stability())))
                                .add(attributeDescription(attribute, allButReadOnly)
                                        .style("cursor", "initial")
                                        .css(util("mt-sm"))));
                        if (attribute.listOrObjectValueType()) {
                            tr.addChildren(attribute.valueTypeAttributeDescriptions(),
                                    new AttributeRow(resource, true));
                        } else {
                            // TODO
                            ((HTMLElement) titleCell.containerDelegate()).style.setProperty("cursor", "initial");
                        }
                    } else {
                        tr.addItem(td("Name")
                                .add(attributeName(attribute, () -> uic().environment().highlightStability(resource.stability(),
                                        attribute.stability())))
                                .add(attributeDescription(attribute, allButReadOnly).css(util("mt-sm"))));
                    }
                })
                .addItem(td("Type").text(attribute.formatType()))
                .addItem(td("Storage").run(td -> storage(td, attribute)))
                .addItem(td("Access type").run(td -> accessType(td, attribute)));
    }

    private void storage(Td td, AttributeDescription attribute) {
        if (attribute.hasDefined(STORAGE)) {
            String storageId = Id.unique(attribute.name(), STORAGE);
            String storage = attribute.get(STORAGE).asString();
            Variable minWidth = utilVar("min-width", "MinWidth");
            if (CONFIGURATION.equals(storage)) {
                td.add(span().id(storageId)
                        .add(database())
                        .add(tooltip(By.id(storageId))
                                .css(util("min-width"))
                                .style(minWidth.name, "10ch")
                                .text("Configuration")));
            } else if (RUNTIME.equals(storage)) {
                td.add(span().id(storageId)
                        .add(memory())
                        .add(tooltip(By.id(storageId))
                                .css(util("min-width"))
                                .style(minWidth.name, "10ch")
                                .text("Memory")));
            } else {
                // TODO
                td.element().innerHTML = SafeHtmlUtils.fromSafeConstant("&nbsp;").asString();
            }
        } else {
            // TODO
            td.element().innerHTML = SafeHtmlUtils.fromSafeConstant("&nbsp;").asString();
        }
    }

    private void accessType(Td td, AttributeDescription attribute) {
        if (attribute.hasDefined(ACCESS_TYPE)) {
            String accessTypeId = Id.unique(attribute.name(), ACCESS_TYPE);
            String accessType = attribute.get(ACCESS_TYPE).asString();
            switch (accessType) {
                case READ_WRITE:
                    td.add(span().id(accessTypeId)
                            .add(edit())
                            .add(tooltip(By.id(accessTypeId))
                                    .style("min-width", "7ch")
                                    .text("read-write")));
                    break;
                case READ_ONLY:
                    td.add(span().id(accessTypeId)
                            .add(lock())
                            .add(tooltip(By.id(accessTypeId))
                                    .style("min-width", "7ch")
                                    .text("read-only")));
                    break;
                case METRIC:
                    td.add(span().id(accessTypeId)
                            .add(trendUp())
                            .add(tooltip(By.id(accessTypeId))
                                    .style("min-width", "7ch")
                                    .text("metric")));
                    break;
                default:
                    // TODO
                    td.element().innerHTML = SafeHtmlUtils.fromSafeConstant("&nbsp;").asString();
                    break;
            }
        } else {
            // TODO
            td.element().innerHTML = SafeHtmlUtils.fromSafeConstant("&nbsp;").asString();
        }
    }
}

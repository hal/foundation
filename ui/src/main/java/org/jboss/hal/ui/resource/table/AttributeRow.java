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
package org.jboss.hal.ui.resource.table;

import java.util.function.Function;

import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.ResourceDescription;
import org.patternfly.component.table.TitleCell;
import org.patternfly.component.table.Tr;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ACCESS_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORAGE;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.brick.AttributeBricks.accessTypeTd;
import static org.jboss.hal.ui.brick.AttributeBricks.attributeDescription;
import static org.jboss.hal.ui.brick.AttributeBricks.attributeName;
import static org.jboss.hal.ui.brick.AttributeBricks.storageTd;
import static org.jboss.hal.ui.brick.DescriptionBricks.AttributeDescriptionContent.allButReadOnly;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.TitleCell.titleCell;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.style.Classes.util;

/**
 * Function that maps an {@link AttributeDescription} to a table row ({@link Tr}) for the attributes table.
 * <p>
 * Each row displays the attribute name, type, storage (configuration vs. runtime), and access type (read-write, read-only,
 * metric). Complex attributes (LIST/OBJECT) are rendered as expandable tree table rows with nested child attributes.
 */
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
                .addItem(storageTd("Storage", attribute.hasDefined(STORAGE) ? attribute.get(STORAGE).asString() : null))
                .addItem(accessTypeTd("Access", attribute.hasDefined(ACCESS_TYPE) ? attribute.get(ACCESS_TYPE).asString() : null));
    }
}

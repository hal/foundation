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

import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.CapabilityDescription;
import org.jboss.hal.meta.description.ResourceDescription;
import org.patternfly.component.list.List;
import org.patternfly.component.table.Table;
import org.patternfly.layout.flex.Flex;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.table.Tbody.tbody;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.Th.th;
import static org.patternfly.component.table.Thead.thead;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.icon.IconSets.fas.ban;
import static org.patternfly.layout.bullseye.Bullseye.bullseye;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.SpaceItems.sm;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Width.width10;
import static org.patternfly.style.Width.width30;
import static org.patternfly.style.Width.width60;

class CapabilitiesTable implements IsElement<HTMLElement> {

    private final Table table;

    CapabilitiesTable(Metadata metadata) {
        this.table = table()
                .addHead(thead().css(util("mt-sm"))
                        .addRow(tr("capabilities-head")
                                .addItem(th("name").width(width60).text("Name"))
                                .addItem(th("dynamic").width(width10).text("Dynamic"))
                                .addItem(th("dynamic-elements").width(width30).text("Dynamic elements"))))
                .addBody(tbody()
                        .run(tbody -> {
                            if (metadata.resourceDescription().capabilities().isEmpty()) {
                                tbody.addRow(tr(Id.unique("empty"))
                                        .addItem(td().colSpan(3)
                                                .add(bullseye()
                                                        .add(emptyState()
                                                                .icon(ban())
                                                                .text("No capabilities")
                                                                .addBody(emptyStateBody()
                                                                        .text("This resource provides no capabilities."))))));
                            } else {
                                tbody.addRows(metadata.resourceDescription().capabilities(), capability -> tr(capability.name())
                                        .addItem(td("Name")
                                                .add(capabilityName(metadata.resourceDescription(), capability)))
                                        .addItem(td("Dynamic").text(String.valueOf(capability.dynamic())))
                                        .addItem(td("Dynamic elements").add(dynamicElements(capability))));
                            }
                        }));
    }

    @Override
    public HTMLElement element() {
        return table.element();
    }

    private Flex capabilityName(ResourceDescription resource, CapabilityDescription capability) {
        if (uic().environment().highlightStability(resource.stability(), capability.stability())) {
            return flex().spaceItems(sm)
                    .addItem(flexItem().text(capability.name()))
                    .add(flexItem().add(stabilityLabel(capability.stability())));
        } else {
            return flex().add(capability.name());
        }
    }

    private List dynamicElements(CapabilityDescription capability) {
        return list().plain()
                .addItems(capability.dynamicElements(), dynamicElement -> listItem().text(dynamicElement));
    }
}

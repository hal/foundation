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
package org.jboss.hal.op.endpoint;

import java.util.List;
import java.util.function.Consumer;

import org.jboss.elemento.Callback;
import org.jboss.elemento.IsElement;
import org.patternfly.component.table.Table;
import org.patternfly.component.table.Tr;
import org.patternfly.handler.SelectHandler;
import org.patternfly.icon.IconSets;

import elemental2.dom.HTMLElement;

import static org.patternfly.component.SelectionMode.single;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.table.TableText.tableText;
import static org.patternfly.component.table.Tbody.tbody;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.Th.th;
import static org.patternfly.component.table.Thead.thead;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.component.table.Wrap.fitContent;
import static org.patternfly.icon.IconSets.fas.pencilAlt;
import static org.patternfly.icon.IconSets.fas.trash;
import static org.patternfly.layout.bullseye.Bullseye.bullseye;

class EndpointTable implements IsElement<HTMLElement> {

    private final Table table;
    private final Tr emptyRow;
    private final Consumer<Endpoint> update;
    private final Consumer<Endpoint> delete;

    EndpointTable(Callback create, Consumer<Endpoint> update, Consumer<Endpoint> delete) {
        emptyRow = tr("endpoint-empty")
                .addItem(td().colSpan(4)
                        .add(bullseye()
                                .add(emptyState()
                                        .icon(IconSets.fas.ban())
                                        .text("No management interfaces found")
                                        .headingLevel(2)
                                        .addFooter(emptyStateFooter()
                                                .add(button().link().text("Add management interface")
                                                        .onClick((event, component) -> create.call()))))));
        this.update = update;
        this.delete = delete;
        table = table()
                .selectionMode(single)
                .addHead(thead()
                        .addRow(tr("endpoint-head")
                                .addItem(th().text("Name"))
                                .addItem(th().text("URL"))
                                .addItem(th().screenReader("Edit"))
                                .addItem(th().screenReader("Remove"))))
                .addBody(tbody());
    }

    @Override
    public HTMLElement element() {
        return table.element();
    }

    EndpointTable onSelect(SelectHandler<Tr> handler) {
        table.onSingleSelect(handler);
        return this;
    }

    void select(String key) {
        table.select(key);
    }

    void show(List<Endpoint> endpoints) {
        if (endpoints.isEmpty()) {
            table.tbody().clear();
            table.tbody().addRow(emptyRow);
        } else {
            table.tbody().clear();
            table.tbody().addRows(endpoints, endpoint -> tr(endpoint.id)
                    .clickable()
                    .addItem(td("Name").text(endpoint.name))
                    .addItem(td("URL").text(endpoint.url))
                    .addItem(td("Edit").action().wrap(fitContent)
                            .add(tableText()
                                    .add(button().plain().icon(pencilAlt())
                                            .onClick((event, component) -> update.accept(endpoint)))))
                    .addItem(td("Remove").action().wrap(fitContent)
                            .add(tableText()
                                    .add(button().plain().icon(trash())
                                            .onClick((event, component) -> delete.accept(endpoint))))));
        }
    }
}

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

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.env.Settings;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.OperationDescription;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.model.filter.GlobalOperationsAttribute;
import org.jboss.hal.resources.Keys;
import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.component.list.List;
import org.patternfly.component.table.Tbody;
import org.patternfly.component.table.Tr;
import org.patternfly.core.ObservableValue;
import org.patternfly.filter.Filter;
import org.patternfly.layout.flex.Flex;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.isAttached;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.strong;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;
import static org.jboss.hal.resources.HalClasses.deprecated;
import static org.jboss.hal.resources.HalClasses.filtered;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.ui.BuildingBlocks.attributeDescription;
import static org.jboss.hal.ui.BuildingBlocks.attributeName;
import static org.jboss.hal.ui.BuildingBlocks.emptyRow;
import static org.jboss.hal.ui.BuildingBlocks.operationDescription;
import static org.jboss.hal.ui.BuildingBlocks.AttributeDescriptionContent.allButReadOnly;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.modelbrowser.OperationsToolbar.operationsToolbar;
import static org.jboss.hal.ui.resource.ResourceDialogs.executeOperationModal;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.label.Label.label;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.table.Tbody.tbody;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.Th.th;
import static org.patternfly.component.table.Thead.thead;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.core.ObservableValue.ov;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.SpaceItems.sm;
import static org.patternfly.layout.flex.SpaceItems.xs;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.fitContent;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.screenReader;
import static org.patternfly.style.Classes.table;
import static org.patternfly.style.Classes.text;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Color.blue;
import static org.patternfly.style.Width.width20;
import static org.patternfly.style.Width.width35;
import static org.patternfly.style.Width.width45;

class OperationsTable implements IsElement<HTMLElement> {

    private static final Logger logger = Logger.getLogger(OperationsTable.class.getName());
    private final AddressTemplate template;
    private final Filter<OperationDescription> filter;
    private final ObservableValue<Integer> visible;
    private final ObservableValue<Integer> total;
    private final Tbody tbody;
    private final HTMLElement root;
    private EmptyState noAttributes;

    OperationsTable(AddressTemplate template, Metadata metadata) {
        boolean showGlobalOperations = uic().settings().get(Settings.Key.SHOW_GLOBAL_OPERATIONS).asBoolean();
        this.template = template;
        this.filter = new OperationsFilter(showGlobalOperations).onChange(this::onFilterChanged);
        this.visible = ov(metadata.resourceDescription().operations().size());
        this.total = ov(metadata.resourceDescription().operations().size());
        this.root = div()
                .add(operationsToolbar(filter, visible, total))
                .add(table()
                        .addHead(thead()
                                .addRow(tr("operations-head")
                                        .addItem(th("name").width(width35).text("Name"))
                                        .addItem(th("parameters").width(width45).text("Parameters"))
                                        .addItem(th("return-value").width(width20).text("Return value"))
                                        .addItem(th("execute")
                                                .add(span().css(screenReader).text("Execute operation")))))
                        .addBody(tbody = tbody()
                                .addRows(metadata.resourceDescription().operations(), operation -> {
                                    boolean executable = metadata.securityContext().executable(operation.name());
                                    AttributeDescription returnValue = operation.returnValue();
                                    return tr(operation.name())
                                            .store(Keys.OPERATION_DESCRIPTION, operation)
                                            .addItem(td("Name")
                                                    .add(operationName(metadata.resourceDescription(), operation))
                                                    .add(operationDescription(operation)))
                                            .run(tableRow -> {
                                                if (returnValue.isDefined()) {
                                                    tableRow.addItem(td("Parameters")
                                                                    .add(parameters(metadata.resourceDescription(), operation)))
                                                            .addItem(td("Return value")
                                                                    .add(returnValue(returnValue)));
                                                } else {
                                                    tableRow.addItem(td("Parameters")
                                                            .colSpan(2)
                                                            .add(parameters(metadata.resourceDescription(), operation)));
                                                }
                                            })
                                            .addItem(td("Execute operation").css(modifier(fitContent))
                                                    .run(td -> {
                                                        // :add() and :remove() are special and can be executed otherwise
                                                        if (executable && !ADD.equals(operation.name()) && !REMOVE.equals(
                                                                operation.name())) {
                                                            td.add(span().css(component(table, text))
                                                                    .add(button("Execute").tertiary()
                                                                            .onClick((e, c) -> execute(operation))));
                                                        }
                                                    }));
                                })))
                .element();
        filter.set(GlobalOperationsAttribute.NAME, showGlobalOperations);
    }

    private void execute(OperationDescription operation) {
        executeOperationModal(template, operation.name());
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    private Flex operationName(ResourceDescription resource, OperationDescription operation) {
        HTMLContainerBuilder<HTMLElement> name = strong()
                .text(operation.name())
                .run(strong -> {
                    if (operation.deprecation().isDefined()) {
                        strong.css(halModifier(deprecated));
                    }
                });
        // I guess it's safe to say that an operation is either global *or* preview or experimental
        if (operation.global()) {
            return flex().spaceItems(sm)
                    .addItem(flexItem().add(name))
                    .add(flexItem().add(label("global", blue)));
        } else if (uic().environment().highlightStability(resource.stability(), operation.stability())) {
            return flex().spaceItems(sm)
                    .addItem(flexItem().add(name))
                    .add(flexItem().add(stabilityLabel(operation.stability())));
        } else {
            return flex().add(name);
        }
    }

    private List parameters(ResourceDescription resource, OperationDescription operation) {
        return list().plain().bordered()
                .addItems(operation.parameters(), parameter -> listItem()
                        .add(attributeName(parameter, true, () -> uic().environment()
                                .highlightStability(resource.stability(), operation.stability(),
                                        parameter.stability()))
                                .alignItems(center).spaceItems(xs)
                                .add(span().text(":"))
                                .add(span().text(parameter.formatType())))
                        .add(attributeDescription(parameter, allButReadOnly).css(util("mt-sm"))));
    }

    private HTMLContainerBuilder<HTMLDivElement> returnValue(AttributeDescription returnValue) {
        return div()
                .add(returnValue.formatType())
                .run(div -> {
                    if (returnValue.hasDefined(DESCRIPTION)) {
                        div.add(div().css(util("mt-sm")).add(returnValue.description()));
                    }
                });
    }

    private void noOperations() {
        if (noAttributes == null) {
            noAttributes = emptyRow(filter);
        }
        if (!isAttached(noAttributes)) {
            tbody.empty(5, noAttributes);
        }
    }

    private void onFilterChanged(Filter<OperationDescription> filter, String origin) {
        logger.debug("Filter operations: %s", filter);
        int matchingItems;
        if (filter.defined()) {
            matchingItems = 0;
            for (Tr tr : tbody.items()) {
                OperationDescription od = tr.get(Keys.OPERATION_DESCRIPTION);
                if (od != null) {
                    boolean match = filter.match(od);
                    tr.classList().toggle(halModifier(filtered), !match);
                    if (match) {
                        matchingItems++;
                    }
                }
            }
            if (matchingItems == 0) {
                noOperations();
            } else {
                tbody.clearEmpty();
            }
        } else {
            matchingItems = total.get();
            tbody.clearEmpty();
            tbody.items().forEach(dlg -> dlg.classList().remove(halModifier(filtered)));
        }
        visible.set(matchingItems);
    }
}

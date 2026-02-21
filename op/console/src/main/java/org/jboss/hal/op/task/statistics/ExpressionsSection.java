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
package org.jboss.hal.op.task.statistics;

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.patternfly.component.button.Button;
import org.patternfly.component.page.PageGroup;
import org.patternfly.component.table.Tbody;
import org.patternfly.component.table.Td;
import org.patternfly.component.title.Title;

import elemental2.dom.HTMLElement;

import static java.util.Collections.singletonList;
import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ValueEncoder.encode;
import static org.jboss.hal.ui.resource.ResourceDialogs.addResourceModal;
import static org.patternfly.component.Ordered.DATA_ORDER;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.component.page.PageGroup.pageGroup;
import static org.patternfly.component.page.PageSection.pageSection;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.table.TableText.tableText;
import static org.patternfly.component.table.Tbody.tbody;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.Th.th;
import static org.patternfly.component.table.Thead.thead;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.component.table.Wrap.fitContent;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.style.Classes.screenReader;
import static org.patternfly.style.Size._2xl;
import static org.patternfly.style.Width.width40;
import static org.patternfly.style.Width.width60;
import static org.patternfly.token.Token.globalTextColorDisabled;

class ExpressionsSection implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    static ExpressionsSection expressionsSection(StatisticsTask task, Dispatcher dispatcher, CrudOperations crud) {
        return new ExpressionsSection(task, dispatcher, crud);
    }

    // ------------------------------------------------------ instance

    private static final String EXPRESSION_COLUMN = "Expression";
    private static final String SYSTEM_PROPERTY_COLUMN = "System property";
    private static final String FIRST_ACTION_COLUMN = "First action";
    private static final String SECOND_ACTION_COLUMN = "Second action";

    private final StatisticsTask task;
    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final PageGroup pageGroup;
    private final Tbody expressionsTbody;

    ExpressionsSection(StatisticsTask task, Dispatcher dispatcher, CrudOperations crud) {
        this.task = task;
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.expressionsTbody = tbody().ordered();

        this.pageGroup = pageGroup()
                .addSection(pageSection().limitWidth()
                        .add(content().add(Title.title(2, _2xl, "Expressions")))
                        .add(content(p).editorial()
                                .add("Expressions found in the ")
                                .add(code(STATISTICS_ENABLED))
                                .add(" attributes. The table also shows whether a system property has been defined " +
                                        "for the expression and, if so, what its value is. Use the buttons to " +
                                        "add, remove, and change the values of the system properties.")))
                .addSection(pageSection().limitWidth()
                        .add(toolbar()
                                .addContent(toolbarContent()
                                        .addItem(toolbarItem()
                                                .add(button("Add").primary()
                                                        .onClick((e, c) -> newExpression())))))
                        .add(table()
                                .addHead(thead()
                                        .addRow(tr("expressions-head")
                                                .addItem(th("expression").width(width60).text(EXPRESSION_COLUMN))
                                                .addItem(th("system-property").width(width40).text(SYSTEM_PROPERTY_COLUMN))
                                                .addItem(th("first-action")
                                                        .add(span().css(screenReader).text(FIRST_ACTION_COLUMN)))
                                                .addItem(th("second-action")
                                                        .add(span().css(screenReader).text(SECOND_ACTION_COLUMN)))))
                                .addBody(expressionsTbody)));
    }

    @Override
    public HTMLElement element() {
        return pageGroup.element();
    }

    // ------------------------------------------------------ api

    void addExpression(String expression) {
        String expressionId = Id.build(expression);
        Td spTd = td(SYSTEM_PROPERTY_COLUMN);
        Td faTd = td(FIRST_ACTION_COLUMN).action().wrap(fitContent);
        Td saTd = td(SECOND_ACTION_COLUMN).action().wrap(fitContent);
        expressionsTbody.addItem(tr(expressionId)
                .data(DATA_ORDER, expression)
                .addItem(td(EXPRESSION_COLUMN).text(expression))
                .addItem(spTd)
                .addItem(faTd)
                .addItem(saTd)
                .run(tr -> updateExpressionRow(expression, spTd, faTd, saTd)));
    }

    // ------------------------------------------------------ internal

    private void newExpression() {
        addResourceModal(AddressTemplate.of("system-property=*"), null, false).then(modelNode -> {
            String expression = modelNode.get(NAME).asString();
            task.addExpression(expression, true);
            return null;
        });
    }

    private void updateExpressionRow(String expression, Td spTd, Td faTd, Td saTd) {
        removeChildrenFrom(spTd);
        removeChildrenFrom(faTd);
        removeChildrenFrom(saTd);
        dispatcher.execute(new Operation.Builder(systemPropertyAddress(expression).resolve(), READ_RESOURCE_OPERATION).build(),
                        false)
                .then(result -> {
                    if (result.hasDefined(VALUE)) {
                        boolean value = result.get(VALUE).asBoolean();
                        spTd.add(span().text(String.valueOf(value)));
                        faTd.addText(tableText().add(remove(expression, spTd, faTd, saTd)));
                        if (value) {
                            saTd.addText(tableText().add(false_(expression, false, spTd, faTd, saTd)));
                        } else {
                            saTd.addText(tableText().add(true_(expression, false, spTd, faTd, saTd)));
                        }
                    } else {
                        spTd.add(undefined());
                        faTd.addText(tableText().add(true_(expression, false, spTd, faTd, saTd)));
                        saTd.addText(tableText().add(false_(expression, false, spTd, faTd, saTd)));
                    }
                    return null;
                })
                .catch_(error -> {
                    spTd.add(undefined());
                    faTd.addText(tableText().add(true_(expression, true, spTd, faTd, saTd)));
                    saTd.addText(tableText().add(false_(expression, true, spTd, faTd, saTd)));
                    return null;
                });
    }

    private AddressTemplate systemPropertyAddress(String expression) {
        return AddressTemplate.of("system-property=" + encode(expression));
    }

    private static HTMLContainerBuilder<HTMLElement> undefined() {
        return span().style("color", globalTextColorDisabled.var).text("undefined");
    }

    private Button remove(String expression, Td spTd, Td faTd, Td saTd) {
        return button("Remove").secondary().onClick((e, c) ->
                crud.delete(systemPropertyAddress(expression)).then(__ -> {
                    updateExpressionRow(expression, spTd, faTd, saTd);
                    return null;
                }));
    }

    private Button true_(String expression, boolean create, Td spTd, Td faTd, Td saTd) {
        return button("True").secondary().onClick((e, c) -> update(expression, create, true, spTd, faTd, saTd));
    }

    private Button false_(String expression, boolean create, Td spTd, Td faTd, Td saTd) {
        return button("False").secondary().onClick((e, c) -> update(expression, create, false, spTd, faTd, saTd));
    }

    private void update(String expression, boolean create, boolean value, Td spTd, Td faTd, Td saTd) {
        if (create) {
            ModelNode payload = new ModelNode();
            payload.get(VALUE).set(value);
            crud.create(systemPropertyAddress(expression), payload).then(result -> {
                updateExpressionRow(expression, spTd, faTd, saTd);
                return null;
            });
        } else {
            crud.update(systemPropertyAddress(expression), singletonList(
                            new Operation.Builder(systemPropertyAddress(expression).resolve(), WRITE_ATTRIBUTE_OPERATION)
                                    .param(NAME, VALUE)
                                    .param(VALUE, value)
                                    .build()))
                    .then(result -> {
                        updateExpressionRow(expression, spTd, faTd, saTd);
                        return null;
                    });
        }
    }
}

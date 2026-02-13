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
package org.jboss.hal.op.task;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.Id;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.tree.ModelTree;
import org.jboss.hal.meta.tree.TraverseContinuation;
import org.jboss.hal.meta.tree.TraverseOperation;
import org.jboss.hal.meta.tree.TraverseType;
import org.jboss.hal.model.filter.NameAttribute;
import org.jboss.hal.task.Task;
import org.jboss.hal.ui.BuildingBlocks;
import org.patternfly.component.button.Button;
import org.patternfly.component.menu.MenuList;
import org.patternfly.component.table.Tbody;
import org.patternfly.component.table.Td;
import org.patternfly.component.table.Tr;
import org.patternfly.component.title.Title;
import org.patternfly.core.ObservableValue;
import org.patternfly.filter.Filter;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.dmr.Expression.extractExpressions;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelType.EXPRESSION;
import static org.jboss.hal.dmr.ValueEncoder.encode;
import static org.jboss.hal.resources.HalClasses.filtered;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.ui.BuildingBlocks.renderExpression;
import static org.jboss.hal.ui.filter.ItemCount.itemCount;
import static org.jboss.hal.ui.filter.NameTextInputGroup.nameFilterTextInputGroup;
import static org.patternfly.component.Ordered.DATA_ORDER;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.component.menu.Dropdown.dropdown;
import static org.patternfly.component.menu.DropdownMenu.dropdownMenu;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuItem.menuItem;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuToggle.menuToggle;
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
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarGroupType.actionGroup;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.component.toolbar.ToolbarItemType.searchFilter;
import static org.patternfly.core.ObservableValue.ov;
import static org.patternfly.filter.FilterOperator.AND;
import static org.patternfly.icon.IconSets.fas.chartLine;
import static org.patternfly.style.Classes.screenReader;
import static org.patternfly.style.Size._2xl;
import static org.patternfly.style.Width.width33;
import static org.patternfly.style.Width.width66;
import static org.patternfly.token.Token.globalTextColorDisabled;

@Dependent
public class StatisticsTask implements Task {

    private static final Logger logger = Logger.getLogger(StatisticsTask.class.getName());
    public static final String TASK_ID = StatisticsTask.class.getName();

    private final Dispatcher dispatcher;
    private final ModelTree modelTree;
    private final CrudOperations crud;
    private final Filter<AddressTemplate> filter;
    private final ObservableValue<Integer> visible;
    private final ObservableValue<Integer> total;
    private final Set<String> distinctExpressions;
    private final Set<String> distinctResources;
    private final Tbody expressionsTbody;
    private final Tbody resourcesTBody;
    private final MenuList expressionsMenuList;

    @Inject
    public StatisticsTask(Dispatcher dispatcher, ModelTree modelTree, CrudOperations crud) {
        this.dispatcher = dispatcher;
        this.modelTree = modelTree;
        this.crud = crud;
        this.visible = ov(0);
        this.total = ov(0);
        this.distinctExpressions = new HashSet<>();
        this.distinctResources = new HashSet<>();
        this.expressionsTbody = tbody().ordered();
        this.resourcesTBody = tbody().ordered();
        this.expressionsMenuList = menuList().ordered();
        this.filter = new Filter<AddressTemplate>(AND).onChange(this::onFilterChanged);
        filter.add(new NameAttribute<>(AddressTemplate::toString));
    }

    @Override
    public String id() {
        return TASK_ID;
    }

    @Override
    public String title() {
        return "Statistics";
    }

    @Override
    public Element icon() {
        return chartLine().element();
    }

    @Override
    public HTMLElement summary() {
        return content(p)
                .add("Enable / disable statistics for all or a selection of subsystems.")
                .element();
    }

    @Override
    public Iterable<HTMLElement> elements() {
        return asList(
                header().element(),

                // Expressions header
                pageSection().limitWidth()
                        .add(content().add(Title.title(2, _2xl, "Expressions")))
                        .add(content(p).editorial()
                                .add("Expressions found in the ")
                                .add(code(STATISTICS_ENABLED))
                                .add(" attributes. The table also shows whether a system property has been defined " +
                                        "for the expression and, if so, what its value is. Use the buttons to " +
                                        "add, remove, and change the values of the system properties."))
                        .element(),

                // Expressions table
                pageSection().limitWidth()
                        .add(toolbar()
                                .addContent(toolbarContent()
                                        .addGroup(toolbarGroup()
                                                .addItem(toolbarItem()
                                                        .add(button("Add").primary())))))
                        .add(table()
                                .addHead(thead()
                                        .addRow(tr("expressions-head")
                                                .addItem(th("expression").width(width66).text("Expression"))
                                                .addItem(th("system-property").width(width33).text("System property"))
                                                .addItem(th("primary-action").add(
                                                        span().css(screenReader).text("Primary action")))
                                                .addItem(
                                                        th("secondary-action").add(
                                                                span().css(screenReader).text("Secondary action")))))
                                .addBody(expressionsTbody))
                        .element(),

                // Resources header
                pageSection().limitWidth()
                        .add(content().add(Title.title(2, _2xl, "Resources")))
                        .add(content(p).editorial()
                                .add("Resources with a ")
                                .add(code(STATISTICS_ENABLED))
                                .add(" attribute and its current value. Use the toolbar to filter the resources and " +
                                        "change the value of the ")
                                .add(code(STATISTICS_ENABLED))
                                .add(" attributes for the selected resources. Alternatively, you can also modify the " +
                                        "attribute for each resource individually."))
                        .element(),

                // Resources table
                pageSection().limitWidth()
                        .add(toolbar()
                                .addContent(toolbarContent()
                                        .addItem(toolbarItem(searchFilter)
                                                .add(nameFilterTextInputGroup(filter, "Filter by resource")))
                                        .addItem(toolbarItem()
                                                .style("align-self", "center")
                                                .add(itemCount(visible, total, "resource", "resources")))
                                        .addGroup(toolbarGroup(actionGroup)
                                                .addItem(toolbarItem().add(button("True").secondary()))
                                                .addItem(toolbarItem().add(button("False").secondary()))
                                                .addItem(toolbarItem()
                                                        .add(dropdown(menuToggle("Expression").secondary())
                                                                .addMenu(dropdownMenu()
                                                                        .addContent(menuContent()
                                                                                .addList(expressionsMenuList))))))))
                        .add(table()
                                .addHead(thead()
                                        .addRow(tr("resources-head")
                                                .addItem(th("resource").width(width66).text("Resource"))
                                                .addItem(th(STATISTICS_ENABLED).width(width33).text("Statistics enabled"))
                                                .addItem(th("actions").add(span().css(screenReader).text("Actions")))))
                                .addBody(resourcesTBody))
                        .element()
        );
    }

    @Override
    public void run() {
        clear();
        TraverseOperation<ModelNode> operation = (template, context) -> {
            if (template.fullyQualified()) {
                return dispatcher.execute(new Operation.Builder(template.resolve(context), READ_RESOURCE_OPERATION)
                                .param(ATTRIBUTES_ONLY, true)
                                .param(INCLUDE_RUNTIME, true)
                                .build())
                        .then(result -> Promise.resolve(result.asPropertyList().stream()
                                .filter(property -> STATISTICS_ENABLED.equals(property.getName()))
                                .map(Property::getValue)
                                .findFirst()
                                .orElse(new ModelNode())))
                        .catch_(error -> {
                            logger.error("Failed to read attributes of %s: %s", template, error);
                            return Promise.resolve(new ModelNode());
                        });
            } else {
                return Promise.resolve(new ModelNode());
            }
        };
        // TODO Support domain mode
        modelTree.traverse(new TraverseContinuation(), AddressTemplate.root(), singleton("/core-service"),
                        EnumSet.noneOf(TraverseType.class), operation,
                        (template, statisticsEnabled, context) -> {
                            if (template.fullyQualified() && statisticsEnabled.isDefined()) {
                                addExpression(statisticsEnabled);
                                addResource(template, statisticsEnabled);
                            }
                        })
                .then(context -> {
                    expressionsMenuList.addItems(new TreeSet<>(distinctExpressions), expression ->
                            menuItem(Id.build(expression), "")
                                    .text(BuildingBlocks.renderExpression("${" + expression + ":false}"))
                                    .store("expression", expression)
                                    .data(DATA_ORDER, expression));
                    expressionsMenuList
                            .addDivider()
                            .addItem(menuItem("new-expression", "New expression")
                                    .data(DATA_ORDER, "zzz-new-expression"));
                    visible.set(distinctResources.size());
                    total.set(distinctResources.size());
                    return null;
                });
    }

    private void clear() {
        visible.set(0);
        total.set(0);
        distinctExpressions.clear();
        expressionsTbody.clear();
        expressionsMenuList.clear();
        distinctResources.clear();
        resourcesTBody.clear();
    }

    // ------------------------------------------------------ expressions

    private void addExpression(ModelNode statisticsEnabled) {
        if (statisticsEnabled.getType() == EXPRESSION) {
            String[] expressions = extractExpressions(statisticsEnabled.asString());
            if (expressions != null) {
                for (String expression : expressions) {
                    if (!distinctExpressions.contains(expression)) {
                        distinctExpressions.add(expression);
                        Td spTd = td("System property");
                        Td paTd = td("Primary action").action().wrap(fitContent);
                        Td saTd = td("Secondary action").action().wrap(fitContent);
                        String expressionId = Id.build(expression);
                        expressionsTbody.addItem(tr(expressionId)
                                .store("expression", expression)
                                .data(DATA_ORDER, expression)
                                .addItem(td("Expression").text(expression))
                                .addItem(spTd)
                                .addItem(paTd)
                                .addItem(saTd)
                                .run(tr -> updateExpressionRow(expression, spTd, paTd, saTd)));
                    }
                }
            }
        }
    }

    private void updateExpressionRow(String expression, Td spTd, Td paTd, Td saTd) {
        // TODO Support domain mode
        removeChildrenFrom(spTd);
        removeChildrenFrom(paTd);
        removeChildrenFrom(saTd);
        dispatcher.execute(new Operation.Builder(systemPropertyAddress(expression).resolve(), READ_RESOURCE_OPERATION).build(),
                        false)
                .then(result -> {
                    if (result.hasDefined(VALUE)) {
                        boolean value = result.get(VALUE).asBoolean();
                        spTd.add(span().text(String.valueOf(value)));
                        paTd.add(tableText().add(remove(expression, spTd, paTd, saTd)));
                        if (value) {
                            saTd.add(tableText().add(false_(expression, false, spTd, paTd, saTd)));
                        } else {
                            saTd.add(tableText().add(true_(expression, false, spTd, paTd, saTd)));
                        }
                    } else {
                        spTd.add(undefined());
                        paTd.add(tableText().add(true_(expression, false, spTd, paTd, saTd)));
                        saTd.add(tableText().add(false_(expression, false, spTd, paTd, saTd)));
                    }
                    return null;
                })
                .catch_(error -> {
                    spTd.add(undefined());
                    paTd.add(tableText().add(true_(expression, true, spTd, paTd, saTd)));
                    saTd.add(tableText().add(false_(expression, true, spTd, paTd, saTd)));
                    return null;
                });
    }

    private AddressTemplate systemPropertyAddress(String expression) {
        return AddressTemplate.of("system-property=" + encode(expression));
    }

    private static HTMLContainerBuilder<HTMLElement> undefined() {
        return span().style("color", globalTextColorDisabled.var).text("undefined");
    }

    private Button remove(String expression, Td spTd, Td paTd, Td saTd) {
        return button("Remove").secondary().onClick((e, c) ->
                crud.delete(systemPropertyAddress(expression)).then(__ -> {
                    updateExpressionRow(expression, spTd, paTd, saTd);
                    return null;
                }));
    }

    private Button true_(String expression, boolean create, Td spTd, Td paTd, Td saTd) {
        return button("True").secondary().onClick((e, c) -> update(expression, create, true, spTd, paTd, saTd));
    }

    private Button false_(String expression, boolean create, Td spTd, Td paTd, Td saTd) {
        return button("False").secondary().onClick((e, c) -> update(expression, create, false, spTd, paTd, saTd));
    }

    private void update(String expression, boolean create, boolean value, Td spTd, Td paTd, Td saTd) {
        if (create) {
            ModelNode payload = new ModelNode();
            payload.get(VALUE).set(value);
            crud.create(systemPropertyAddress(expression), payload).then(result -> {
                updateExpressionRow(expression, spTd, paTd, saTd);
                return null;
            });
        } else {
            crud.update(systemPropertyAddress(expression), singletonList(
                            new Operation.Builder(systemPropertyAddress(expression).resolve(), WRITE_ATTRIBUTE_OPERATION)
                                    .param(NAME, VALUE)
                                    .param(VALUE, value)
                                    .build()))
                    .then(result -> {
                        updateExpressionRow(expression, spTd, paTd, saTd);
                        return null;
                    });
        }
    }

    // ------------------------------------------------------ resources

    private void addResource(AddressTemplate template, ModelNode statisticsEnabled) {
        if (!distinctResources.contains(template.toString())) {
            distinctResources.add(template.toString());
            Td actionsTd = td("Actions");
            String templateId = Id.build(template.toString());
            resourcesTBody.addItem(tr(templateId)
                    .store("template", template)
                    .data(DATA_ORDER, template.toString())
                    .addItem(td("Resource").text(template.toString()))
                    .addItem(td("Statistics enabled").run(td -> {
                        if (statisticsEnabled.getType() == ModelType.EXPRESSION) {
                            td.add(renderExpression(statisticsEnabled.asString()));
                        } else {
                            td.text(statisticsEnabled.asString());
                        }
                    }))
                    .addItem(actionsTd));
        }
    }

    private void onFilterChanged(Filter<AddressTemplate> filter, String origin) {
        int matchingItems;
        if (filter.defined()) {
            matchingItems = 0;
            for (Tr row : resourcesTBody.items()) {
                AddressTemplate template = row.get("template");
                boolean match = filter.match(template);
                row.element().classList.toggle(halModifier(filtered), !match);
                if (match) {
                    matchingItems++;
                }
            }
        } else {
            matchingItems = total.get();
            resourcesTBody.items().forEach(row -> row.element().classList.remove(halModifier(filtered)));
        }
        visible.set(matchingItems);
    }
}

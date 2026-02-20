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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.core.Notification;
import org.jboss.hal.core.Notifications;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.model.filter.NameAttribute;
import org.jboss.hal.resources.HalClasses;
import org.patternfly.component.button.Button;
import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.component.form.Checkbox;
import org.patternfly.component.menu.Dropdown;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MenuList;
import org.patternfly.component.menu.MenuToggle;
import org.patternfly.component.page.PageGroup;
import org.patternfly.component.table.Table;
import org.patternfly.component.table.Tbody;
import org.patternfly.component.table.Td;
import org.patternfly.component.table.Tr;
import org.patternfly.component.title.Title;
import org.patternfly.core.ObservableValue;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.isAttached;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.core.Notification.error;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WILDFLY_STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.op.task.statistics.StatisticsEnabledMultiSelect.statisticsEnabledMultiSelect;
import static org.jboss.hal.resources.HalClasses.filtered;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.ui.BuildingBlocks.emptyRow;
import static org.jboss.hal.ui.BuildingBlocks.renderExpression;
import static org.jboss.hal.ui.filter.ItemCount.itemCount;
import static org.jboss.hal.ui.filter.NameSearchInput.nameSearchInput;
import static org.patternfly.component.Ordered.DATA_ORDER;
import static org.patternfly.component.SelectionMode.multi;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.component.form.Checkbox.checkboxWrapped;
import static org.patternfly.component.menu.Dropdown.dropdown;
import static org.patternfly.component.menu.DropdownMenu.dropdownMenu;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuGroup.menuGroup;
import static org.patternfly.component.menu.MenuItem.menuItem;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuToggle.menuToggle;
import static org.patternfly.component.menu.MenuToggleType.split;
import static org.patternfly.component.page.PageGroup.pageGroup;
import static org.patternfly.component.page.PageSection.pageSection;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.table.TableText.tableText;
import static org.patternfly.component.table.Tbody.tbody;
import static org.patternfly.component.table.Td.checkboxTd;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.Th.th;
import static org.patternfly.component.table.Thead.thead;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.component.table.Wrap.fitContent;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarGroupType.actionGroup;
import static org.patternfly.component.toolbar.ToolbarGroupType.filterGroup;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.component.toolbar.ToolbarItemType.searchFilter;
import static org.patternfly.core.ObservableValue.ov;
import static org.patternfly.filter.FilterOperator.AND;
import static org.patternfly.icon.IconSets.fas.ellipsisV;
import static org.patternfly.style.Classes.screenReader;
import static org.patternfly.style.Size._2xl;
import static org.patternfly.style.Width.width40;
import static org.patternfly.style.Width.width60;

class ResourcesSection implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    static ResourcesSection resourcesSection(StatisticsTask task, Dispatcher dispatcher, Notifications notifications) {
        return new ResourcesSection(task, dispatcher, notifications);
    }

    // ------------------------------------------------------ instance

    private static final String RESOURCE_COLUMN = "Resource";
    private static final String STATISTICS_ENABLED_COLUMN = "Statistics enabled";
    private static final String EXPRESSION_ACTION_COLUMN = "Expression action";
    private static final String TRUE_ACTION_COLUMN = "True action";
    private static final String FALSE_ACTION_COLUMN = "False action";

    private static final String RESOURCE = "resource";
    private static final String TEMPLATE = "template";

    private final StatisticsTask task;
    private final Dispatcher dispatcher;
    private final Notifications notifications;
    private final Filter<ModelNode> filter;
    private final ObservableValue<Integer> visible;
    private final ObservableValue<Integer> total;
    private final Map<AddressTemplate, MenuList> expressionMenuLists;
    private final Map<AddressTemplate, MenuList> nestedExpressionMenuLists;
    private final PageGroup pageGroup;
    private final Checkbox bulkSelectCheckbox;
    private final MenuToggle bulkSelectToggle;
    private final MenuItem selectFilteredMenuItem;
    private final MenuItem selectAllMenuItem;
    private final Button bulkTrueButton;
    private final Button bulkFalseButton;
    private final Dropdown bulkExpressionDropdown;
    private final MenuList bulkExpressionMenuList;
    private final MenuList bulkNestedExpressionMenuList;
    private final Table resourcesTable;
    private final Tbody resourcesTBody;
    private EmptyState noResources;

    ResourcesSection(StatisticsTask task, Dispatcher dispatcher, Notifications notifications) {
        this.task = task;
        this.dispatcher = dispatcher;
        this.notifications = notifications;

        this.filter = new Filter<ModelNode>(AND).onChange(this::onFilterChanged);
        this.filter.add(new NameAttribute<>(modelNode -> modelNode.get(TEMPLATE).asString()));
        this.filter.add(new StatisticsEnabledAttribute());

        this.expressionMenuLists = new HashMap<>();
        this.nestedExpressionMenuLists = new HashMap<>();
        this.selectFilteredMenuItem = menuItem("select-filtered", "Select filtered")
                .disabled()
                .onClick((e, c) -> selectFiltered());
        this.selectAllMenuItem = menuItem("select-all", "Select all")
                .onClick((e, c) -> selectAll());
        String bulkSelectId = Id.build(StatisticsTask.TASK_ID, "bulk-select");
        this.bulkSelectCheckbox = checkboxWrapped(bulkSelectId, bulkSelectId).standalone()
                .onChange((e, c, value) -> {
                    if (value) {
                        selectAll();
                    } else {
                        selectNone();
                    }
                });
        this.bulkSelectToggle = menuToggle(split).addCheckbox(bulkSelectCheckbox);
        this.bulkTrueButton = button("True").secondary().disabled()
                .onClick((e, c) -> bulkUpdate(true, null));
        this.bulkFalseButton = button("False").secondary().disabled()
                .onClick((e, c) -> bulkUpdate(false, null));
        this.bulkExpressionMenuList = menuList().ordered();
        this.bulkNestedExpressionMenuList = menuList().ordered();
        this.bulkExpressionDropdown = expressionDropdown(null, bulkExpressionMenuList,
                bulkNestedExpressionMenuList).disabled();
        this.resourcesTable = table().selectionMode(multi);
        this.resourcesTBody = tbody().ordered();

        this.visible = ov(0).subscribe((current, __) ->
                selectFilteredMenuItem.text("Select filtered (" + current + ")"));
        this.total = ov(0).subscribe((current, __) ->
                selectAllMenuItem.text("Select all (" + current + ")"));

        this.pageGroup = pageGroup()
                .addSection(pageSection().limitWidth()
                        .add(content().add(Title.title(2, _2xl, "Resources")))
                        .add(content(p).editorial()
                                .add("Resources with a ")
                                .add(code(STATISTICS_ENABLED))
                                .add(" attribute and its current value. Use the toolbar to filter the resources and " +
                                        "change the value of the ")
                                .add(code(STATISTICS_ENABLED))
                                .add(" attributes for the selected resources. Alternatively, you can also modify the " +
                                        "attribute for each resource individually.")))
                .addSection(pageSection().limitWidth()
                        .add(toolbar()
                                .addContent(toolbarContent()
                                        .addItem(toolbarItem()
                                                .add(dropdown(bulkSelectToggle)
                                                        .stayOpen()
                                                        .addMenu(dropdownMenu()
                                                                .addContent(menuContent()
                                                                        .addList(menuList()
                                                                                .addItem(menuItem("select-none", "Select none")
                                                                                        .onClick((e, c) -> selectNone()))
                                                                                .addItem(selectFilteredMenuItem)
                                                                                .addItem(selectAllMenuItem))))))
                                        .addGroup(toolbarGroup(filterGroup)
                                                .addItem(toolbarItem(searchFilter)
                                                        .add(nameSearchInput(filter, "Filter by resource")))
                                                .addItem(toolbarItem().add(statisticsEnabledMultiSelect(filter))))
                                        .addItem(toolbarItem()
                                                .style("align-self", "center")
                                                .add(itemCount(visible, total, "resource", "resources")))
                                        .addGroup(toolbarGroup(actionGroup)
                                                .addItem(toolbarItem().add(bulkTrueButton))
                                                .addItem(toolbarItem().add(bulkFalseButton))
                                                .addItem(toolbarItem().add(bulkExpressionDropdown)))))
                        .add(resourcesTable
                                .onMultiSelect((e, c, rows) -> onSelection(rows))
                                .addHead(thead()
                                        .addRow(tr("resources-head")
                                                .addItem(th().screenReader("Row select"))
                                                .addItem(th("resource").width(width60).text(RESOURCE_COLUMN))
                                                .addItem(th(STATISTICS_ENABLED).width(width40).text(STATISTICS_ENABLED_COLUMN))
                                                .addItem(th("true-action")
                                                        .add(span().css(screenReader).text(TRUE_ACTION_COLUMN)))
                                                .addItem(th("false-action")
                                                        .add(span().css(screenReader).text(FALSE_ACTION_COLUMN)))
                                                .addItem(th("expression-action")
                                                        .add(span().css(screenReader).text(EXPRESSION_ACTION_COLUMN)))))
                                .addBody(resourcesTBody)));
    }

    @Override
    public HTMLElement element() {
        return pageGroup.element();
    }

    // ------------------------------------------------------ api

    void clear() {
        visible.set(0);
        total.set(0);
        bulkExpressionMenuList.clear();
        bulkNestedExpressionMenuList.clear();
        expressionMenuLists.clear();
        nestedExpressionMenuLists.clear();
        resourcesTBody.clear();
    }

    void count() {
        visible.set(task.distinctResources.size());
        total.set(task.distinctResources.size());
    }

    void addResource(AddressTemplate template, ModelNode statisticsEnabled) {
        if (!task.distinctResources.contains(template.toString())) {
            task.distinctResources.add(template.toString());
            resourcesTBody.addItem(resourceRow(template, statisticsEnabled));
        }
    }

    void updateExpressionMenus(String expression) {
        if (!WILDFLY_STATISTICS_ENABLED.equals(expression)) {
            bulkExpressionMenuList.addItem(expressionItem(null, expression));
            bulkNestedExpressionMenuList.addItem(nestedExpressionItem(null, expression));
            for (Map.Entry<AddressTemplate, MenuList> entry : expressionMenuLists.entrySet()) {
                entry.getValue().addItem(expressionItem(entry.getKey(), expression));
            }
            for (Map.Entry<AddressTemplate, MenuList> entry : nestedExpressionMenuLists.entrySet()) {
                entry.getValue().addItem(nestedExpressionItem(entry.getKey(), expression));
            }
        }
    }

    // ------------------------------------------------------ filter

    private void onFilterChanged(Filter<ModelNode> filter, String origin) {
        int matchingItems;
        selectFilteredMenuItem.disabled(!filter.defined());
        if (filter.defined()) {
            matchingItems = 0;
            for (Tr row : resourcesTBody.items()) {
                ModelNode modelNode = row.get(RESOURCE);
                if (modelNode != null) {
                    boolean match = filter.match(modelNode);
                    row.classList().toggle(halModifier(filtered), !match);
                    if (match) {
                        matchingItems++;
                    }
                }
            }
            if (matchingItems == 0) {
                noResources();
            } else {
                resourcesTBody.clearEmpty();
            }
        } else {
            matchingItems = total.get();
            resourcesTBody.clearEmpty();
            resourcesTBody.items().forEach(row -> row.element().classList.remove(halModifier(filtered)));
        }
        visible.set(matchingItems);
    }

    // ------------------------------------------------------ selection

    private void selectNone() {
        resourcesTable.selectNone();
        // the rest happens in onSelection()
    }

    private void selectFiltered() {
        // Instead of selecting each filtered item and firing the selection event,
        // which in turn calls onSelection(...), we do it manually.
        List<Tr> filtered = resourcesTBody.items().stream()
                .filter(item -> !item.element().classList.contains(halModifier(HalClasses.filtered)))
                .collect(toList());
        for (Tr item : filtered) {
            resourcesTable.select(item, true, false);
        }
        onSelection(filtered);
    }

    private void selectAll() {
        resourcesTable.selectAll();
        // the rest happens in onSelection()
    }

    private void onSelection(List<Tr> rows) {
        if (rows.isEmpty()) {
            bulkSelectCheckbox.inputElement().checked(false);
            bulkSelectCheckbox.inputElement().indeterminate(false);
            bulkSelectToggle.text("");
        } else {
            bulkSelectToggle.text(rows.size() + " selected");
            if (rows.size() == task.distinctResources.size()) {
                bulkSelectCheckbox.inputElement().checked(true);
                bulkSelectCheckbox.inputElement().indeterminate(false);
            } else {
                bulkSelectCheckbox.inputElement().indeterminate(true);
            }
        }
        bulkTrueButton.disabled(rows.isEmpty());
        bulkFalseButton.disabled(rows.isEmpty());
        bulkExpressionDropdown.disabled(rows.isEmpty());
    }

    // ------------------------------------------------------ (bulk) update

    private void bulkUpdate(Boolean value, String expression) {
        List<Tr> selectedItems = resourcesTable.selectedItems();
        List<AddressTemplate> templates = selectedItems.stream()
                .map(item -> item.<AddressTemplate>get(TEMPLATE))
                .collect(toList());
        List<Operation> updateOperations = templates.stream()
                .map(template -> writeAttributeOperation(template, value, expression))
                .collect(toList());
        dispatcher.execute(new Composite(updateOperations))
                .then(__ -> {
                    resourcesTable.selectNone();
                    notifications.send(Notification.success("Resources updated",
                            updateOperations.size() + " resources have been successfully updated."));
                    for (AddressTemplate template : templates) {
                        readAndUpdateRow(template);
                    }
                    return null;
                })
                .catch_(error -> {
                    notifications.send(error("Failed to update resources",
                            "An error occurred while updating " + updateOperations.size() + " resources.")
                            .details(String.valueOf(error), true));
                    return null;
                });
    }

    private void singleUpdate(AddressTemplate template, Boolean value, String expression) {
        dispatcher.execute(writeAttributeOperation(template, value, expression))
                .then(__ -> {
                    notifications.send(Notification.success("Resources updated",
                            template + " has been successfully updated."));
                    readAndUpdateRow(template);
                    return null;
                })
                .catch_(error -> {
                    notifications.send(error("Failed to update resources",
                            "An error occurred while updating " + template + ".")
                            .details(String.valueOf(error), true));
                    return null;
                });
    }

    private void readAndUpdateRow(AddressTemplate template) {
        dispatcher.execute(readAttributeOperation(template)).then(modelNode -> {
            resourcesTBody.updateItem(resourceRow(template, modelNode));
            for (String expression : task.distinctExpressions) {
                if (!WILDFLY_STATISTICS_ENABLED.equals(expression)) {
                    MenuList expressionMenuList = expressionMenuLists.get(template);
                    if (expressionMenuList != null) {
                        expressionMenuList.addItem(expressionItem(template, expression));

                    }
                    MenuList nestedExpressionMenuList = nestedExpressionMenuLists.get(template);
                    if (nestedExpressionMenuList != null) {
                        nestedExpressionMenuList.addItem(nestedExpressionItem(template, expression));
                    }
                }
            }
            return null;
        });
    }

    private Operation writeAttributeOperation(AddressTemplate template, Boolean value, String expression) {
        Operation.Builder builder = new Operation.Builder(template.resolve(), WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, STATISTICS_ENABLED);
        if (value != null) {
            builder.param(VALUE, value);
        } else if (expression != null) {
            builder.param(VALUE, expression);
        }
        return builder.build();
    }

    private Operation readAttributeOperation(AddressTemplate template) {
        return new Operation.Builder(template.resolve(), READ_ATTRIBUTE_OPERATION)
                .param(NAME, STATISTICS_ENABLED)
                .build();
    }

    // ------------------------------------------------------ helper methods

    private void noResources() {
        if (noResources == null) {
            noResources = emptyRow(filter);
        }
        if (!isAttached(noResources)) {
            resourcesTBody.empty(6, noResources);
        }
    }

    private Tr resourceRow(AddressTemplate template, ModelNode statisticsEnabled) {
        ModelNode modelNode = new ModelNode();
        modelNode.get(TEMPLATE).set(template.toString());
        modelNode.get(STATISTICS_ENABLED).set(statisticsEnabled);

        String templateId = Id.build(template.toString());
        Td seTd = td(STATISTICS_ENABLED_COLUMN);
        Td taTd = td(TRUE_ACTION_COLUMN).action().wrap(fitContent);
        Td faTd = td(FALSE_ACTION_COLUMN).action().wrap(fitContent);
        MenuList expressionList = menuList().ordered();
        MenuList nestedExpressionList = menuList().ordered();
        expressionMenuLists.put(template, expressionList);
        nestedExpressionMenuLists.put(template, nestedExpressionList);

        return tr(templateId)
                .store(RESOURCE, modelNode)
                .store(TEMPLATE, template)
                .data(DATA_ORDER, template.toString())
                .addItem(checkboxTd())
                .addItem(td(RESOURCE_COLUMN).text(template.toString()))
                .addItem(seTd)
                .addItem(taTd)
                .addItem(faTd)
                .addItem(td(EXPRESSION_ACTION_COLUMN).actions()
                        .add(expressionDropdown(template, expressionList, nestedExpressionList)))
                .run(tr -> updateResourceRow(template, statisticsEnabled, seTd, taTd, faTd));
    }

    private void updateResourceRow(AddressTemplate template, ModelNode statisticsEnabled, Td seTd, Td taTd, Td faTd) {
        removeChildrenFrom(seTd);
        removeChildrenFrom(taTd);
        removeChildrenFrom(faTd);
        if (statisticsEnabled.getType() == ModelType.EXPRESSION) {
            seTd.add(renderExpression(statisticsEnabled.asString()));
            taTd.addText(tableText().add(button("True").secondary()
                    .onClick((e, c) -> singleUpdate(template, true, null))));
            faTd.addText(tableText().add(button("False").secondary()
                    .onClick((e, c) -> singleUpdate(template, false, null))));
        } else {
            seTd.add(span().text(statisticsEnabled.asString()));
            if (statisticsEnabled.asBoolean()) {
                taTd.addText(tableText().add(" "));
                faTd.addText(tableText().add(button("False").secondary()
                        .onClick((e, c) -> singleUpdate(template, false, null))));
            } else {
                taTd.addText(tableText().add(button("True").secondary()
                        .onClick((e, c) -> singleUpdate(template, true, null))));
                faTd.addText(tableText().add(" "));
            }
        }
    }

    private Dropdown expressionDropdown(AddressTemplate template, MenuList expressionList, MenuList nestedExpressionList) {
        Dropdown dropdown = template == null
                ? dropdown(menuToggle("Expression").secondary())
                : dropdown(ellipsisV(), "Expressions for " + template);
        dropdown.addMenu(dropdownMenu()
                .addContent(menuContent()
                        .addGroup(menuGroup().addList(menuList().addItem(statisticsEnabledItem(template))))
                        .addDivider()
                        // the menu list will be filled later in updateExpressionMenus() and readAndUpdateRow()
                        .addGroup(menuGroup().addList(expressionList))
                        .addDivider()
                        // the menu list will be filled later in updateExpressionMenus() and readAndUpdateRow()
                        .addGroup(menuGroup().addList(nestedExpressionList))
                        .addDivider()
                        .addGroup(menuGroup().addList(menuList().addItem(newExpressionItem(template))))));
        return dropdown;
    }

    private MenuItem statisticsEnabledItem(AddressTemplate template) {
        String identifier = template == null
                ? Id.build(WILDFLY_STATISTICS_ENABLED)
                : Id.build(template.toString(), WILDFLY_STATISTICS_ENABLED);
        String $expression = "${" + WILDFLY_STATISTICS_ENABLED + ":false}";
        return menuItem(identifier)
                .text(renderExpression($expression))
                .onClick((e, c) -> {
                    if (template == null) {
                        bulkUpdate(null, $expression);
                    } else {
                        singleUpdate(template, null, $expression);
                    }
                });
    }

    private MenuItem expressionItem(AddressTemplate template, String expression) {
        String identifier = template == null
                ? Id.build(expression)
                : Id.build(template.toString(), expression);
        String $expression = "${" + expression + ":false}";
        return menuItem(identifier)
                .text(renderExpression($expression))
                .data(DATA_ORDER, expression)
                .onClick((e, c) -> {
                    if (template == null) {
                        bulkUpdate(null, $expression);
                    } else {
                        singleUpdate(template, null, $expression);
                    }
                });
    }

    private MenuItem nestedExpressionItem(AddressTemplate template, String expression) {
        String identifier = template == null
                ? Id.build(WILDFLY_STATISTICS_ENABLED, expression)
                : Id.build(template.toString(), WILDFLY_STATISTICS_ENABLED, expression);
        String $expression = "${" + expression + ":${" + WILDFLY_STATISTICS_ENABLED + ":false}}";
        return menuItem(identifier)
                .text(renderExpression($expression))
                .data(DATA_ORDER, expression)
                .onClick((e, c) -> {
                    if (template == null) {
                        bulkUpdate(null, $expression);
                    } else {
                        singleUpdate(template, null, $expression);
                    }
                });
    }

    private MenuItem newExpressionItem(AddressTemplate template) {
        String identifier = template == null
                ? Id.build("new-expression")
                : Id.build(template.toString(), "new-expression");
        return menuItem(identifier, "New expression")
                .onClick((e, c) -> {
                    String newExpression = ""; // TODO Open modal dialog to enter new expression
                    if (template == null) {
                        // bulkUpdate(null, newExpression);
                    } else {
                        // singleUpdate(template, null, newExpression);
                    }
                });
    }
}

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
import java.util.Set;

import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.core.Notifications;
import org.jboss.hal.dmr.Composite;
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
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.core.Notification.error;
import static org.jboss.hal.core.Notification.success;
import static org.jboss.hal.core.Notification.warning;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WILDFLY_STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.op.task.statistics.NewExpressionModal.newExpressionModal;
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
    private static final String TRUE_ACTION_COLUMN = "True action";
    private static final String FALSE_ACTION_COLUMN = "False action";
    private static final String EXPRESSION_DROPDOWN_COLUMN = "Expression dropdown";
    private static final String EXPRESSION_DROPDOWN_DATA = "expressionDropdown";
    private static final String RESOURCE_DATA = "resource-data";

    private final StatisticsTask task;
    private final Dispatcher dispatcher;
    private final Notifications notifications;
    private final Filter<ResourceData> filter;
    private final ObservableValue<Integer> visible;
    private final ObservableValue<Integer> total;
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
    private final Map<ResourceData, MenuList[]> expressionMenuLists;
    private EmptyState noResources;

    ResourcesSection(StatisticsTask task, Dispatcher dispatcher, Notifications notifications) {
        this.task = task;
        this.dispatcher = dispatcher;
        this.notifications = notifications;

        this.filter = new Filter<ResourceData>(AND).onChange(this::onFilterChanged);
        this.filter.add(new NameAttribute<>(rd -> rd.template.toString()));
        this.filter.add(new StatisticsEnabledAttribute());

        this.expressionMenuLists = new HashMap<>();
        this.selectFilteredMenuItem = menuItem("select-filtered", "Select filtered").disabled()
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
                                .add(" attribute and its current value. Use the toolbar to select and filter the resources. " +
                                        "You can change the values of all selected resources using the buttons in the toolbar. " +
                                        "Alternatively, you can also modify the attribute for each resource individually. " +
                                        "If the ")
                                .add(code(STATISTICS_ENABLED))
                                .add(" attribute does not support expressions, the expression dropdown is omitted.")))
                .addSection(pageSection().limitWidth()
                        .add(toolbar()
                                .addContent(toolbarContent()
                                        .addItem(toolbarItem()
                                                .add(dropdown(bulkSelectToggle)
                                                        .stayOpen()
                                                        .addMenu(dropdownMenu()
                                                                .addContent(menuContent()
                                                                        .addList(menuList()
                                                                                .addItem(menuItem("select-none",
                                                                                        "Select none")
                                                                                        .onClick(
                                                                                                (e, c) -> selectNone()))
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
                                                .addItem(th(STATISTICS_ENABLED).width(width40)
                                                        .text(STATISTICS_ENABLED_COLUMN))
                                                .addItem(th("true-action")
                                                        .add(span().css(screenReader).text(TRUE_ACTION_COLUMN)))
                                                .addItem(th("false-action")
                                                        .add(span().css(screenReader).text(FALSE_ACTION_COLUMN)))
                                                .addItem(th("expression-dropdown")
                                                        .add(span().css(screenReader)
                                                                .text(EXPRESSION_DROPDOWN_COLUMN)))))
                                .addBody(resourcesTBody)));
    }

    @Override
    public HTMLElement element() {
        return pageGroup.element();
    }

    // ------------------------------------------------------ api

    void addResource(ResourceData sed) {
        resourcesTBody.addItem(resourceRow(sed));
    }

    void count(int resources) {
        visible.set(resources);
        total.set(resources);
    }

    void updateBulkExpressionDropdown(Set<String> expressions) {
        for (String expression : expressions) {
            if (!WILDFLY_STATISTICS_ENABLED.equals(expression)) {
                bulkExpressionMenuList.addItem(expressionItem(null, expression));
                bulkNestedExpressionMenuList.addItem(nestedExpressionItem(null, expression));
            }
        }
    }

    void addExpressionDropdown(ResourceData rd, Set<String> expressions) {
        HTMLElement td = resourcesTBody.querySelector(By.data(EXPRESSION_DROPDOWN_DATA, rd.template.toString()));
        if (td != null) {
            MenuList expressionMenuList = menuList().ordered();
            MenuList nestedExpressionMenuList = menuList().ordered();
            for (String expression : expressions) {
                if (!WILDFLY_STATISTICS_ENABLED.equals(expression)) {
                    expressionMenuList.addItem(expressionItem(rd, expression));
                    nestedExpressionMenuList.addItem(nestedExpressionItem(rd, expression));
                }
            }
            td.appendChild(expressionDropdown(rd, expressionMenuList,
                    nestedExpressionMenuList).element());
            expressionMenuLists.put(rd, new MenuList[]{expressionMenuList, nestedExpressionMenuList});
        }
    }

    void updateExpressionDropdowns(String expression) {
        if (!WILDFLY_STATISTICS_ENABLED.equals(expression)) {
            bulkExpressionMenuList.addItem(expressionItem(null, expression));
            bulkNestedExpressionMenuList.addItem(nestedExpressionItem(null, expression));
        }
        expressionMenuLists.forEach((rd, menuLists) -> {
            if (!WILDFLY_STATISTICS_ENABLED.equals(expression)) {
                menuLists[0].addItem(expressionItem(rd, expression));
                menuLists[1].addItem(nestedExpressionItem(rd, expression));
            }
        });
    }

    // ------------------------------------------------------ filter

    private void onFilterChanged(Filter<ResourceData> filter, String origin) {
        int matchingItems;
        selectFilteredMenuItem.disabled(!filter.defined());
        if (filter.defined()) {
            matchingItems = 0;
            for (Tr row : resourcesTBody.items()) {
                ResourceData rd = row.get(RESOURCE_DATA);
                if (rd != null) {
                    boolean match = filter.match(rd);
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
            if (rows.size() == task.resources.size()) {
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
        List<ResourceData> selected = resourcesTable.selectedItems().stream()
                .map(tr -> tr.<ResourceData>get(RESOURCE_DATA))
                .collect(toList());
        List<ResourceData> allowed = expression != null
                ? selected.stream().filter(rd -> rd.expressionsAllowed).collect(toList())
                : selected;
        List<Operation> operations = allowed.stream()
                .map(rd -> writeAttributeOperation(rd.template, value, expression))
                .collect(toList());
        dispatcher.execute(new Composite(operations))
                .then(__ -> {
                    resourcesTable.selectNone();
                    if (selected.size() == allowed.size()) {
                        String title = allowed.size() + " resources updated";
                        String description = allowed.size() + " resources have been successfully updated.";
                        notifications.send(success(title, description));
                    } else {
                        String title = allowed.size() + " / " + selected.size() + " resources updated";
                        String description = allowed.size() + " resources have been updated. " +
                                selected.size() + " resources have not been updated because they do not support expressions.";
                        notifications.send(warning(title, description));
                    }
                    for (ResourceData rd : allowed) {
                        readAndUpdateRow(rd);
                    }
                    return null;
                })
                .catch_(error -> {
                    notifications.send(error("Failed to update resources",
                            "An error occurred while updating " + operations.size() + " resources.")
                            .details(String.valueOf(error), true));
                    return null;
                });
    }

    private void singleUpdate(ResourceData rd, Boolean value, String expression) {
        dispatcher.execute(writeAttributeOperation(rd.template, value, expression))
                .then(__ -> {
                    notifications.send(success("Resources updated",
                            rd.template + " has been successfully updated."));
                    readAndUpdateRow(rd);
                    return null;
                })
                .catch_(error -> {
                    notifications.send(error("Failed to update resources",
                            "An error occurred while updating " + rd.template + ".")
                            .details(String.valueOf(error), true));
                    return null;
                });
    }

    private void readAndUpdateRow(ResourceData rd) {
        dispatcher.execute(readAttributeOperation(rd.template)).then(modelNode -> {
            ResourceData copy = rd.copy(modelNode);
            resourcesTBody.updateItem(resourceRow(copy));
            if (copy.expressionsAllowed) {
                addExpressionDropdown(copy, task.expressions);
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

    private Tr resourceRow(ResourceData rd) {
        String templateId = Id.build(rd.template.toString());
        Td seTd = td(STATISTICS_ENABLED_COLUMN);
        Td taTd = td(TRUE_ACTION_COLUMN).action().wrap(fitContent);
        Td faTd = td(FALSE_ACTION_COLUMN).action().wrap(fitContent);
        Td edTd = td(EXPRESSION_DROPDOWN_COLUMN).actions().data(EXPRESSION_DROPDOWN_DATA, rd.template.toString());
        Tr tr = tr(templateId)
                .store(RESOURCE_DATA, rd)
                .data(DATA_ORDER, rd.template.toString())
                .addItem(checkboxTd())
                .addItem(td(RESOURCE_COLUMN).text(rd.template.toString()))
                .addItem(seTd)
                .addItem(taTd)
                .addItem(faTd)
                .addItem(edTd);

        if (rd.value.getType() == ModelType.EXPRESSION) {
            seTd.add(renderExpression(rd.value.asString()));
            taTd.addText(tableText().add(button("True").secondary().onClick((e, c) ->
                    singleUpdate(rd, true, null))));
            faTd.addText(tableText().add(button("False").secondary().onClick((e, c) ->
                    singleUpdate(rd, false, null))));
        } else {
            seTd.text(rd.value.asString());
            if (rd.value.asBoolean()) {
                faTd.addText(tableText().add(button("False").secondary().onClick((e, c) ->
                        singleUpdate(rd, false, null))));
            } else {
                taTd.addText(tableText().add(button("True").secondary().onClick((e, c) ->
                        singleUpdate(rd, true, null))));
            }
        }
        return tr;
    }

    private Dropdown expressionDropdown(ResourceData rd, MenuList expressionList, MenuList nestedExpressionList) {
        Dropdown dropdown = rd == null
                ? dropdown(menuToggle("Expression").secondary())
                : dropdown(ellipsisV(), "Expressions for " + rd.template);
        dropdown.addMenu(dropdownMenu()
                .addContent(menuContent()
                        .addGroup(menuGroup().addList(menuList().addItem(statisticsEnabledItem(rd))))
                        .addDivider()
                        // this menu list will be filled in updateBulkExpressionDropdown() and addExpressionDropdown()
                        .addGroup(menuGroup().addList(expressionList))
                        .addDivider()
                        // this menu list will be filled in updateBulkExpressionDropdown() and addExpressionDropdown()
                        .addGroup(menuGroup().addList(nestedExpressionList))
                        .addDivider()
                        .addGroup(menuGroup().addList(menuList().addItem(newExpressionItem(rd))))));
        return dropdown;
    }

    private MenuItem statisticsEnabledItem(ResourceData rd) {
        String identifier = rd == null
                ? Id.build(WILDFLY_STATISTICS_ENABLED)
                : Id.build(rd.template.toString(), WILDFLY_STATISTICS_ENABLED);
        String $expression = "${" + WILDFLY_STATISTICS_ENABLED + ":false}";
        return menuItem(identifier)
                .text(renderExpression($expression))
                .onClick((e, c) -> {
                    if (rd == null) {
                        bulkUpdate(null, $expression);
                    } else {
                        singleUpdate(rd, null, $expression);
                    }
                });
    }

    private MenuItem expressionItem(ResourceData rd, String expression) {
        String identifier = rd == null
                ? Id.build(expression)
                : Id.build(rd.template.toString(), expression);
        String $expression = "${" + expression + ":false}";
        return menuItem(identifier)
                .text(renderExpression($expression))
                .data(DATA_ORDER, expression)
                .onClick((e, c) -> {
                    if (rd == null) {
                        bulkUpdate(null, $expression);
                    } else {
                        singleUpdate(rd, null, $expression);
                    }
                });
    }

    private MenuItem nestedExpressionItem(ResourceData rd, String expression) {
        String identifier = rd == null
                ? Id.build(WILDFLY_STATISTICS_ENABLED, expression)
                : Id.build(rd.template.toString(), WILDFLY_STATISTICS_ENABLED, expression);
        String $expression = "${" + expression + ":${" + WILDFLY_STATISTICS_ENABLED + ":false}}";
        return menuItem(identifier)
                .text(renderExpression($expression))
                .data(DATA_ORDER, expression)
                .onClick((e, c) -> {
                    if (rd == null) {
                        bulkUpdate(null, $expression);
                    } else {
                        singleUpdate(rd, null, $expression);
                    }
                });
    }

    private MenuItem newExpressionItem(ResourceData rd) {
        String identifier = rd == null
                ? Id.build("new-expression")
                : Id.build(rd.template.toString(), "new-expression");
        return menuItem(identifier, "New expression").onClick((e, c) ->
                newExpressionModal(newExpression -> {
                    if (rd == null) {
                        bulkUpdate(null, newExpression);
                    } else {
                        singleUpdate(rd, null, newExpression);
                    }
                }).open());
    }
}

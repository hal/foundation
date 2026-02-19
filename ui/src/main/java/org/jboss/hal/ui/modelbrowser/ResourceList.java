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

import java.util.List;

import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.env.Stability;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.model.filter.NameAttribute;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.resources.Keys;
import org.jboss.hal.ui.filter.NameSearchInput;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.AddResource;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.DeleteResource;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.SelectInTree;
import org.patternfly.component.emptystate.EmptyStateActions;
import org.patternfly.component.list.DataList;
import org.patternfly.component.list.DataListCell;
import org.patternfly.component.list.DataListItem;
import org.patternfly.component.menu.Menu;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.component.toolbar.ToolbarItem;
import org.patternfly.component.tooltip.Tooltip;
import org.patternfly.core.ObservableValue;
import org.patternfly.filter.Filter;
import org.patternfly.filter.FilterOperator;
import org.patternfly.layout.flex.Flex;
import org.patternfly.style.Classes;
import org.patternfly.style.Variable;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.isAttached;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.small;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.filter.ItemCount.itemCount;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserEngine.parseChildren;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.FOLDER;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.SINGLETON_FOLDER;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateActions.emptyStateActions;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;
import static org.patternfly.component.list.DataList.dataList;
import static org.patternfly.component.list.DataListAction.dataListAction;
import static org.patternfly.component.list.DataListCell.dataListCell;
import static org.patternfly.component.list.DataListItem.dataListItem;
import static org.patternfly.component.menu.Dropdown.dropdown;
import static org.patternfly.component.menu.DropdownMenu.dropdownMenu;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuItem.menuItem;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuToggle.menuToggle;
import static org.patternfly.component.menu.MenuToggleType.plainText;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarGroupType.actionGroupPlain;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.component.toolbar.ToolbarItemType.searchFilter;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.core.ObservableValue.ov;
import static org.patternfly.icon.IconSets.fas.ban;
import static org.patternfly.icon.IconSets.fas.plus;
import static org.patternfly.icon.IconSets.fas.redo;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Direction.column;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.Gap.md;
import static org.patternfly.popper.Placement.auto;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Variable.componentVar;

class ResourceList implements IsElement<HTMLElement> {

    private static final Logger logger = Logger.getLogger(ResourceList.class.getName());

    private final ModelBrowserNode parent;
    private final Metadata metadata;
    private final ObservableValue<Integer> visible;
    private final ObservableValue<Integer> total;
    private final Filter<ModelBrowserNode> filter;
    private final NoMatch<ModelBrowserNode> noMatch;
    private final Operation operation;
    private final ToolbarItem addItem;
    private final Toolbar toolbar;
    private final HTMLElement listContainer;
    private final HTMLElement root;
    private DataList dataList;

    ResourceList(ModelBrowserNode parent, Metadata metadata) {
        this.parent = parent;
        this.metadata = metadata;
        this.visible = ov(0);
        this.total = ov(0);
        this.filter = new Filter<ModelBrowserNode>(FilterOperator.AND)
                .add(new NameAttribute<>(mbn -> mbn.name))
                .onChange(this::onFilterChanged);
        this.noMatch = new NoMatch<>(filter);
        this.operation = new Operation.Builder(parent.template.parent().resolve(), READ_CHILDREN_NAMES_OPERATION)
                .param(CHILD_TYPE, parent.name)
                .build();

        addItem = toolbarItem();
        Tooltip.tooltip(addItem.element(), "Add").appendToBody();
        String refreshId = Id.unique("refresh");
        ToolbarItem refreshItem = toolbarItem()
                .add(button().id(refreshId).plain().icon(redo()).onClick((e, b) -> refresh()))
                .add(tooltip(By.id(refreshId), "Refresh").placement(auto));

        Variable spacer = componentVar(component(Classes.toolbar), "spacer");
        Variable filterGroupSpacer = componentVar(component(Classes.toolbar, Classes.group), "m-filter-group", "spacer");
        toolbar = toolbar().css(util("pt-xs"))
                .addContent(toolbarContent()
                        .addItem(toolbarItem(searchFilter)
                                .style(spacer.name, filterGroupSpacer.asVar()) // override spacing
                                .add(NameSearchInput.nameSearchInput(filter)))
                        .addItem(toolbarItem()
                                .style("align-self", "center")
                                .add(itemCount(visible, total, "resource", "resources")))
                        .addGroup(toolbarGroup(actionGroupPlain).css(modifier("align-right"))
                                .addItem(addItem)
                                .addItem(refreshItem)));
        setVisible(toolbar, false); // to avoid visual noise

        root = div()
                .add(toolbar)
                .add(listContainer = div().element())
                .element();
        load();
    }

    private Promise<List<ModelBrowserNode>> load() {
        return uic().dispatcher().execute(operation).then(result -> {
            List<ModelBrowserNode> allChildren = parseChildren(parent, result, true);
            List<ModelBrowserNode> existingChildren = allChildren.stream().filter(child -> child.exists).collect(toList());
            List<ModelBrowserNode> missingChildren = allChildren.stream().filter(child -> !child.exists).collect(toList());

            if (existingChildren.isEmpty()) {
                empty(missingChildren);
                return Promise.resolve(emptyList());

            } else {
                removeChildrenFrom(addItem);
                if (supportsAdd(parent, missingChildren)) {
                    boolean singleton = parent.type == SINGLETON_FOLDER;
                    if (missingChildren.isEmpty()) {
                        addItem.add(button().plain()
                                .icon(plus())
                                .onClick((e, b) -> AddResource.dispatch(element(),
                                        parent.template, null, singleton)));
                    } else if (missingChildren.size() == 1) {
                        addItem.add(button().plain()
                                .icon(plus())
                                .onClick((e, b) -> AddResource.dispatch(element(),
                                        parent.template, missingChildren.get(0).name, singleton)));
                    } else {
                        addItem.add(dropdown(plus(), "Add")
                                .addMenu(missingChildrenMenu(missingChildren)));
                    }
                    setVisible(addItem, true);
                } else {
                    setVisible(addItem, false);
                }
                visible.set(existingChildren.size());
                total.set(existingChildren.size());
                children(existingChildren);
                return Promise.resolve(existingChildren);
            }
        });
    }

    private boolean supportsAdd(ModelBrowserNode parent, List<ModelBrowserNode> children) {
        // TODO RBAC
        if (parent.type == ModelBrowserNode.Type.FOLDER) {
            return metadata.resourceDescription().operations().supports(ADD);
        } else if (parent.type == ModelBrowserNode.Type.SINGLETON_FOLDER) {
            return !children.isEmpty();
        } else {
            return false;
        }
    }

    private Menu missingChildrenMenu(List<ModelBrowserNode> missingChildren) {
        return dropdownMenu().scrollable()
                .addContent(menuContent()
                        .addList(menuList()
                                .addItems(missingChildren, mbn -> menuItem(mbn.template.identifier(), mbn.name)
                                        .onClick((e, mi) -> AddResource.dispatch(element(), parent.template, mbn.name,
                                                parent.type == SINGLETON_FOLDER)))));
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ status

    private void empty(List<ModelBrowserNode> missingChildren) {
        setVisible(toolbar, false);
        removeChildrenFrom(listContainer);

        EmptyStateActions actions = emptyStateActions();
        if (supportsAdd(parent, missingChildren)) {
            boolean singleton = parent.type == SINGLETON_FOLDER;
            if (missingChildren.isEmpty()) {
                actions.add(button("Add").link().onClick((e, b) -> AddResource.dispatch(element(),
                        parent.template, null, singleton)));
            } else if (missingChildren.size() == 1) {
                actions.add(button("Add").link().onClick((e, b) -> AddResource.dispatch(element(),
                        parent.template, missingChildren.get(0).name, singleton)));
            } else {
                actions.add(dropdown(menuToggle(plainText).text("Add"))
                        .addMenu(missingChildrenMenu(missingChildren)));
            }
        }
        actions.add(button("Refresh").link().onClick((e, b) -> refresh()));

        listContainer.appendChild(emptyState()
                .icon(ban())
                .text("No child resources")
                .addBody(emptyStateBody()
                        .text("This resource has no child resources."))
                .addFooter(emptyStateFooter()
                        .addActions(actions))
                .element());
    }

    private void children(List<ModelBrowserNode> children) {
        setVisible(toolbar, true);
        if (dataList == null) {
            dataList = dataList();
        }
        dataList.clear();
        dataList.addItems(children, child -> {
            String childId = Id.build(child.name);
            Metadata childMetadata = parent.type == SINGLETON_FOLDER
                    ? uic().metadataRepository().get(child.template)
                    : metadata;
            return dataListItem(childId)
                    .store(Keys.MODEL_BROWSER_NODE, child)
                    .addCell(nameCell(childId, child, childMetadata))
                    .addAction(dataListAction()
                            .run(dataListAction -> {
                                if (parent.type == FOLDER) {
                                    // There are no individual descriptions, so center the button horizontally
                                    dataListAction.style("align-items", "center");
                                }
                            })
                            .add(button("View") // TODO RBAC
                                    .tertiary()
                                    .onClick((e, b) -> SelectInTree.dispatch(b.element(),
                                            parent.identifier, child.identifier)))
                            .run(dataListAction -> {
                                // TODO RBAC
                                if (childMetadata.resourceDescription().operations().supports(REMOVE)) {
                                    dataListAction.add(button("Remove")
                                            .tertiary()
                                            .onClick((e, b) -> DeleteResource.dispatch(b.element(), child.template)));
                                }
                            }));
        });
        if (!isAttached(dataList)) {
            listContainer.appendChild(dataList.element());
        }
    }

    private DataListCell nameCell(String childId, ModelBrowserNode child, Metadata metadata) {
        Flex flex = flex().direction(column);
        if (parent.type == SINGLETON_FOLDER) {
            Stability stability = metadata.resourceDescription().stability();
            if (uic().environment().highlightStability(stability)) {
                flex.add(flex().alignItems(center).columnGap(md)
                        .add(flexItem().id(childId).text(child.name))
                        .add(flexItem().add(stabilityLabel(stability))));
            } else {
                flex.addItem(flexItem().id(childId).text(child.name));
            }
            flex.add(small().text(metadata.resourceDescription().description()));
        } else {
            flex.addItem(flexItem().id(childId).text(child.name));
        }
        return dataListCell()
                .run(cell -> {
                    if (parent.type == FOLDER) {
                        cell.style("align-self", "center");
                    }
                })
                .add(flex);
    }

    // ------------------------------------------------------ filter

    private void onFilterChanged(Filter<ModelBrowserNode> filter, String origin) {
        if (dataList != null) {
            logger.debug("Filter resources: %s", filter);
            int matchingItems;
            if (filter.defined()) {
                matchingItems = 0;
                for (DataListItem item : dataList.items()) {
                    ModelBrowserNode mbn = item.get(Keys.MODEL_BROWSER_NODE);
                    if (mbn != null) {
                        boolean match = filter.match(mbn);
                        item.classList().toggle(halModifier(HalClasses.filtered), !match);
                        if (match) {
                            matchingItems++;
                        }
                    }
                }
                noMatch.toggle(listContainer, matchingItems == 0);
            } else {
                matchingItems = total.get();
                noMatch.toggle(listContainer, false);
                dataList.items().forEach(dlg -> dlg.classList().remove(halModifier(HalClasses.filtered)));
            }
            visible.set(matchingItems);
        }
    }

    // ------------------------------------------------------ action handlers

    void refresh() {
        removeChildrenFrom(listContainer);
        load().then(children -> {
            if (!children.isEmpty() && filter.defined()) {
                onFilterChanged(filter, null);
            }
            return null;
        });
    }
}

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
package org.jboss.hal.ui.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jboss.elemento.Attachable;
import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.env.Stability;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.model.filter.NameAttribute;
import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.component.list.DataList;
import org.patternfly.component.list.DataListCell;
import org.patternfly.component.list.DataListItem;
import org.patternfly.component.menu.Menu;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.component.toolbar.ToolbarItem;
import org.patternfly.component.tooltip.Tooltip;
import org.patternfly.core.ObservableValue;
import org.patternfly.core.OuiaSupport;
import org.patternfly.filter.Filter;
import org.patternfly.filter.FilterOperator;
import org.patternfly.layout.flex.Flex;
import org.patternfly.style.Classes;
import org.patternfly.style.Variable;

import elemental2.dom.HTMLElement;
import elemental2.dom.MutationRecord;

import static java.util.Collections.emptyList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.isAttached;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.small;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_SINGLETONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_TYPES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.brick.EmptyStateBricks.noItems;
import static org.jboss.hal.ui.brick.EmptyStateBricks.noMatch;
import static org.jboss.hal.ui.brick.EmptyStateBricks.toggle;
import static org.jboss.hal.ui.filter.ItemCount.itemCount;
import static org.jboss.hal.ui.filter.NameSearchInput.nameSearchInput;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.emptystate.EmptyStateActions.emptyStateActions;
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
import static org.patternfly.core.ObservableValue.ov;
import static org.patternfly.icon.IconSets.fas.plus;
import static org.patternfly.icon.IconSets.fas.rotateRight;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Direction.column;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.Gap.md;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.filtered;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Variable.componentVar;

/**
 * Filterable list of child resources for a WildFly management resource.
 * <p>
 * Automatically detects the loading strategy from the template:
 * <ul>
 * <li>Wildcard templates (ending with {@code =*}): uses {@code read-children-names} on the parent address with the child type
 * as parameter. This lists instances of a specific child type (e.g., all {@code core-service} singletons).</li>
 * <li>Fully qualified templates: uses {@code read-children-types} with {@code include-singletons=true} on the template address.
 * This lists all child types of a resource.</li>
 * </ul>
 * For wildcard (singleton folder) templates, call sites can provide additional {@linkplain #missingChildren(List) missing
 * children} — singletons that could exist but don't yet — so they appear as "Add" options.
 * <p>
 * Each child has "View" and optional "Remove" action buttons. New children can be added via the toolbar.
 * <p>
 * Communication uses callbacks:
 * <ul>
 * <li>{@link #onSelect(Consumer)} — invoked when a child's "View" button is clicked</li>
 * <li>{@link #onAdd(AddCallback)} — invoked when the "Add" button is clicked</li>
 * <li>{@link #onDelete(Consumer)} — invoked when a child's "Remove" button is clicked</li>
 * </ul>
 */
public class ResourceList implements IsElement<HTMLElement>, Attachable,
        OuiaSupport<HTMLElement, ResourceList> {

    // ------------------------------------------------------ factory

    /** Creates a new resource list for the given parent template and metadata. */
    public static ResourceList resourceList(AddressTemplate template, Metadata metadata) {
        return new ResourceList(template, metadata);
    }

    // ------------------------------------------------------ callback

    /** Callback for add-resource actions. */
    @FunctionalInterface
    public interface AddCallback {

        void onAdd(AddressTemplate parent, String childName, boolean singleton);
    }

    // ------------------------------------------------------ child resource

    /** Describes a child resource in the list. */
    public static class ChildResource {

        public final String name;
        public final AddressTemplate template;
        public final boolean singleton;
        public final boolean exists;

        public ChildResource(String name, AddressTemplate template, boolean singleton, boolean exists) {
            this.name = name;
            this.template = template;
            this.singleton = singleton;
            this.exists = exists;
        }
    }

    // ------------------------------------------------------ instance

    private final AddressTemplate template;
    private final Metadata metadata;
    private final boolean wildcardTemplate;
    private final ObservableValue<Integer> visible;
    private final ObservableValue<Integer> total;
    private final EmptyState noMatch;
    private final ToolbarItem addItem;
    private final Toolbar toolbar;
    private final HTMLElement listContainer;
    private final HTMLElement root;
    private Consumer<AddressTemplate> onSelect;
    private AddCallback onAdd;
    private Consumer<AddressTemplate> onDelete;
    private List<ChildResource> extraMissingChildren;
    private DataList dataList;

    ResourceList(AddressTemplate template, Metadata metadata) {
        this.template = template;
        this.metadata = metadata;
        this.wildcardTemplate = !template.isEmpty() && "*".equals(template.last().value);
        this.visible = ov(0);
        this.total = ov(0);
        this.extraMissingChildren = emptyList();
        Filter<ChildResource> filter = new Filter<ChildResource>(FilterOperator.AND)
                .add(new NameAttribute<>(cr -> cr.name))
                .onChange(this::onFilterChanged);
        this.noMatch = noMatch(filter);

        String addId = Id.unique("add");
        addItem = toolbarItem().id(addId)
                .add(Tooltip.tooltip(By.id(addId), "Add"));
        String refreshId = Id.unique("refresh");
        ToolbarItem refreshItem = toolbarItem()
                .add(button().id(refreshId).plain().icon(rotateRight()).onClick((e, b) -> refresh()))
                .add(Tooltip.tooltip(By.id(refreshId), "Refresh"));

        Variable spacer = componentVar(component(Classes.toolbar), "spacer");
        Variable filterGroupSpacer = componentVar(component(Classes.toolbar, Classes.group), "m-filter-group", "spacer");
        toolbar = toolbar().css(util("pt-xs"))
                .addContent(toolbarContent()
                        .addItem(toolbarItem(searchFilter)
                                .style(spacer.name, filterGroupSpacer.asVar())
                                .add(nameSearchInput(filter)))
                        .addItem(toolbarItem()
                                .style("align-self", "center")
                                .add(itemCount(visible, total, "resource", "resources")))
                        .addGroup(toolbarGroup(actionGroupPlain).css(modifier("align-right"))
                                .addItem(addItem)
                                .addItem(refreshItem)));
        setVisible(toolbar, false);

        root = div()
                .add(toolbar)
                .add(listContainer = div().element())
                .element();
        Attachable.register(this, this);
        initOuia();
    }

    @Override
    public void attach(MutationRecord mutationRecord) {
        load();
    }

    @Override
    public String ouiaComponentType() {
        return "halOP/ResourceList";
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ builder

    /**
     * Provides additional children that could exist but don't yet (e.g., missing singletons known from the tree). These are
     * merged with the loaded children and shown as "Add" options.
     */
    public ResourceList missingChildren(List<ChildResource> missingChildren) {
        this.extraMissingChildren = missingChildren != null ? missingChildren : emptyList();
        return this;
    }

    @Override
    public ResourceList that() {
        return this;
    }

    // ------------------------------------------------------ events

    /** Registers a callback invoked when a child resource's "View" button is clicked. */
    public ResourceList onSelect(Consumer<AddressTemplate> onSelect) {
        this.onSelect = onSelect;
        return this;
    }

    /** Registers a callback invoked when the "Add" button is clicked. */
    public ResourceList onAdd(AddCallback onAdd) {
        this.onAdd = onAdd;
        return this;
    }

    /** Registers a callback invoked when a child resource's "Remove" button is clicked. */
    public ResourceList onDelete(Consumer<AddressTemplate> onDelete) {
        this.onDelete = onDelete;
        return this;
    }

    // ------------------------------------------------------ internal

    // The DMR operation selection and result parsing below mirrors the logic in
    // ModelBrowserEngine.readChildrenOperation() / parseChildren().
    // Both use the same two-mode pattern based on the template shape:
    //   - wildcard (=*): read-children-names on the parent address
    //   - non-wildcard:  read-children-types with include-singletons on the template address
    // The duplication is intentional: ModelBrowserEngine builds a hierarchical ModelBrowserNode
    // tree for the TreeView, while this class builds a flat ChildResource list for a DataList.
    // The shared logic is small (~40 lines) and unlikely to diverge, so extracting it into a
    // shared utility would add indirection without meaningful benefit.

    private void load() {
        if (wildcardTemplate) {
            loadByChildNames();
        } else {
            loadByChildTypes();
        }
    }

    private void loadByChildNames() {
        AddressTemplate parentAddress = template.parent();
        String childType = template.last().key;
        Operation operation = new Operation.Builder(parentAddress.resolve(), READ_CHILDREN_NAMES_OPERATION)
                .param(CHILD_TYPE, childType)
                .build();
        uic().dispatcher().execute(operation, result -> {
            List<ChildResource> children = parseChildNames(result);
            addMissingChildren(children);
            processChildren(children);
        });
    }

    private void loadByChildTypes() {
        Operation operation = new Operation.Builder(template.resolve(), READ_CHILDREN_TYPES_OPERATION)
                .param(INCLUDE_SINGLETONS, true)
                .build();
        uic().dispatcher().execute(operation, result -> processChildren(parseChildTypes(result)));
    }

    private List<ChildResource> parseChildNames(ModelNode result) {
        List<ChildResource> children = new ArrayList<>();
        if (result.isDefined()) {
            String key = template.last().key;
            for (ModelNode node : result.asList()) {
                String name = node.asString();
                AddressTemplate childTemplate = template.parent().append(key, name);
                children.add(new ChildResource(name, childTemplate, true, true));
            }
        }
        return children;
    }

    // Singleton detection: read-children-types with include-singletons returns "key=value"
    // for singletons (e.g. "core-service=management") and plain names for regular children
    // (e.g. "extension"). The "=" in the result is the DMR convention that distinguishes them.
    private List<ChildResource> parseChildTypes(ModelNode result) {
        List<ChildResource> children = new ArrayList<>();
        if (result.isDefined()) {
            for (ModelNode node : result.asList()) {
                String name = node.asString();
                boolean singleton = name.contains("=");
                String childName = singleton ? name.substring(name.indexOf('=') + 1) : name;
                String key = singleton ? name.substring(0, name.indexOf('=')) : name;
                AddressTemplate childTemplate = template.append(key, singleton ? childName : "*");
                children.add(new ChildResource(childName, childTemplate, singleton, true));
            }
        }
        return children;
    }

    private void addMissingChildren(List<ChildResource> children) {
        for (ChildResource missing : extraMissingChildren) {
            boolean found = children.stream().anyMatch(cr -> cr.name.equals(missing.name));
            if (!found) {
                children.add(missing);
            }
        }
    }

    private void processChildren(List<ChildResource> children) {
        List<ChildResource> existing = new ArrayList<>();
        List<ChildResource> missing = new ArrayList<>();
        for (ChildResource child : children) {
            if (child.exists) {
                existing.add(child);
            } else {
                missing.add(child);
            }
        }

        if (existing.isEmpty()) {
            empty(missing);
        } else {
            setupAddButton(missing);
            visible.set(existing.size());
            total.set(existing.size());
            showChildren(existing);
        }
    }

    private void empty(List<ChildResource> missing) {
        setVisible(toolbar, false);
        removeChildrenFrom(listContainer);

        org.patternfly.component.emptystate.EmptyStateActions actions = emptyStateActions();
        if (!missing.isEmpty()) {
            if (missing.size() == 1) {
                ChildResource m = missing.get(0);
                actions.add(button("Add").link().onClick((e, b) -> fireAdd(m)));
            } else {
                actions.add(dropdown(menuToggle(plainText).text("Add"))
                        .addMenu(missingMenu(missing)));
            }
        } else if (metadata.resourceDescription().operations().supports(ADD)) {
            actions.add(button("Add").link().onClick((e, b) -> fireAdd(null)));
        }
        actions.add(button("Refresh").link().onClick((e, b) -> refresh()));

        listContainer.appendChild(noItems("No child resources", "This resource has no child resources.")
                .addFooter(emptyStateFooter()
                        .addActions(actions))
                .element());
    }

    private void showChildren(List<ChildResource> children) {
        setVisible(toolbar, true);
        if (dataList == null) {
            dataList = dataList();
        }
        dataList.clear();
        for (ChildResource child : children) {
            String childId = Id.build(child.name);
            dataList.addItem(dataListItem(childId)
                    .addCell(nameCell(childId, child))
                    .addAction(dataListAction()
                            .run(dataListAction -> {
                                if (!child.singleton) {
                                    dataListAction.style("align-items", "center");
                                }
                            })
                            .add(button("View").tertiary()
                                    .onClick((e, b) -> {
                                        if (onSelect != null) {
                                            onSelect.accept(child.template);
                                        }
                                    }))
                            .run(action -> {
                                Metadata childMeta = child.singleton
                                        ? uic().metadataRepository().get(child.template)
                                        : metadata;
                                if (childMeta.resourceDescription().operations().supports(REMOVE)) {
                                    action.add(button("Remove").tertiary()
                                            .onClick((e, b) -> {
                                                if (onDelete != null) {
                                                    onDelete.accept(child.template);
                                                }
                                            }));
                                }
                            })));
        }
        if (!isAttached(dataList)) {
            listContainer.appendChild(dataList.element());
        }
    }

    private DataListCell nameCell(String childId, ChildResource child) {
        Flex f = flex().direction(column);
        if (child.singleton) {
            Metadata childMeta = uic().metadataRepository().get(child.template);
            Stability stability = childMeta.resourceDescription().stability();
            if (uic().environment().highlightStability(stability)) {
                f.add(flex().alignItems(center).columnGap(md)
                        .add(flexItem().id(childId).text(child.name))
                        .add(flexItem().add(stabilityLabel(stability))));
            } else {
                f.addItem(flexItem().id(childId).text(child.name));
            }
            f.add(small().text(childMeta.resourceDescription().description()));
        } else {
            f.addItem(flexItem().id(childId).text(child.name));
        }
        return dataListCell().add(f);
    }

    private void setupAddButton(List<ChildResource> missing) {
        removeChildrenFrom(addItem);
        boolean supportsAdd = metadata.resourceDescription().operations().supports(ADD) || !missing.isEmpty();
        if (supportsAdd) {
            if (missing.isEmpty()) {
                addItem.add(button().plain().icon(plus())
                        .onClick((e, b) -> fireAdd(null)));
            } else if (missing.size() == 1) {
                addItem.add(button().plain().icon(plus())
                        .onClick((e, b) -> fireAdd(missing.get(0))));
            } else {
                addItem.add(dropdown(plus(), "Add")
                        .addMenu(missingMenu(missing)));
            }
            setVisible(addItem, true);
        } else {
            setVisible(addItem, false);
        }
    }

    private Menu missingMenu(List<ChildResource> missing) {
        return dropdownMenu().scrollable()
                .addContent(menuContent()
                        .addList(menuList()
                                .addItems(missing, m -> menuItem(m.template.identifier(), m.name)
                                        .onClick((e, mi) -> fireAdd(m)))));
    }

    private void fireAdd(ChildResource child) {
        if (onAdd != null) {
            onAdd.onAdd(template, child != null ? child.name : null, child != null && child.singleton);
        }
    }

    // ------------------------------------------------------ filter

    private void onFilterChanged(Filter<ChildResource> filter, String origin) {
        if (dataList != null) {
            int matchingItems;
            if (filter.defined()) {
                matchingItems = 0;
                for (DataListItem item : dataList.items()) {
                    String text = item.element().textContent;
                    boolean match = filter.match(new ChildResource(text, null, false, true));
                    item.classList().toggle(modifier(filtered), !match);
                    if (match) {
                        matchingItems++;
                    }
                }
                toggle(noMatch, listContainer, matchingItems == 0);
            } else {
                matchingItems = total.get();
                toggle(noMatch, listContainer, false);
                dataList.items().forEach(dli -> dli.classList().remove(modifier(filtered)));
            }
            visible.set(matchingItems);
        }
    }

    // ------------------------------------------------------ actions

    /** Reloads the child resource list from the management endpoint. */
    public void refresh() {
        removeChildrenFrom(listContainer);
        load();
    }
}

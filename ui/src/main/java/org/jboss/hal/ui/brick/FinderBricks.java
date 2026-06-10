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
package org.jboss.hal.ui.brick;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jboss.elemento.Id;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.resources.OuiaIds;
import org.jboss.hal.ui.resource.finder.FinderSupport;
import org.patternfly.component.content.ContentType;
import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.extension.finder.Finder;
import org.patternfly.extension.finder.FinderColumn;
import org.patternfly.extension.finder.FinderItem;
import org.patternfly.extension.finder.ResolvedFinderPath;
import org.patternfly.extension.finder.FinderPreview;
import org.patternfly.filter.Filter;
import org.patternfly.layout.stack.Stack;

import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.resource.dialog.ResourceDialogs.addResourceModal;
import static org.jboss.hal.ui.resource.dialog.ResourceDialogs.deleteResourceModal;
import static org.jboss.hal.ui.resource.finder.FinderSupport.childResources;
import static org.jboss.hal.ui.resource.finder.FinderSupport.itemAddress;
import static org.jboss.hal.ui.resource.view.ResourceView.resourceView;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateActions.emptyStateActions;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;
import static org.patternfly.extension.finder.FinderColumn.finderColumn;
import static org.patternfly.extension.finder.FinderColumnActions.finderColumnActions;
import static org.patternfly.extension.finder.FinderColumnHeader.finderColumnHeader;
import static org.patternfly.extension.finder.FinderItem.finderItem;
import static org.patternfly.extension.finder.FinderItemActions.finderItemActions;
import static org.patternfly.icon.IconSets.fas.magnifyingGlass;
import static org.patternfly.icon.IconSets.fas.plus;
import static org.patternfly.icon.IconSets.fas.rotateRight;
import static org.patternfly.icon.IconSets.fas.trash;
import static org.patternfly.icon.IconSets.fas.upRightFromSquare;
import static org.patternfly.layout.stack.Stack.stack;
import static org.patternfly.layout.stack.StackItem.stackItem;

/**
 * Factory methods for constructing finder columns, preview layouts, and empty states used in the HAL console's finder-based
 * navigation.
 */
public final class FinderBricks {

    public static Finder topLevelFinder() {
        return Finder.finder();
    }

    /**
     * Creates an empty state shown when no finder items match the current filter. Displays a "No results found" message with a
     * "Clear all filters" action that resets the filter.
     *
     * @param filter the active filter whose state will be reset when the user clicks "Clear all filters"
     * @param <T>    the type of items being filtered
     * @return an empty state component
     */
    public static <T> EmptyState emptyRow(Filter<T> filter) {
        return emptyState()
                .icon(magnifyingGlass())
                .text("No results found")
                .addBody(emptyStateBody()
                        .text(
                                "No results match the filter criteria. Clear all filters and try again."))
                .addFooter(emptyStateFooter()
                        .addActions(emptyStateActions()
                                .add(button("Clear all filters").link()
                                        .onClick((event, component) -> filter.resetAll()))));
    }

    /**
     * Creates a finder column with full CRUD support: an "add" button that opens an add-resource dialog, a "refresh" button,
     * per-item "open" and "delete" actions, a search bar (shown when there are 5+ items), and an optional preview panel
     * displaying selected resource attributes.
     *
     * @param id                a unique identifier for the column and its OUIA test IDs
     * @param header            the column header text
     * @param resourceRoute     route template for the resource page (e.g. {@code "/configuration/resource/:address"})
     * @param previewAttributes attribute names to display in the preview panel; if empty, no preview is shown
     * @param templateFn        resolves the current finder path to an {@link AddressTemplate} for the resource
     * @param nextColumn        supplier for the next column to navigate into, or {@code null} for leaf items
     * @return a fully configured finder column with CRUD capabilities
     */
    public static FinderColumn crudColumn(String id, String header, String resourceRoute, List<String> previewAttributes,
            Function<ResolvedFinderPath, AddressTemplate> templateFn, Supplier<FinderColumn> nextColumn) {
        FinderColumn column = finderColumn(id);
        column.addHeader(finderColumnHeader(header).addActions(finderColumnActions()
                        .addButton(button(plus()).plain().small()
                                .ouiaId(OuiaIds.ouia(id, "add", "btn"))
                                .onClick((e, b) ->
                                        addResourceModal(templateFn.apply(column.finder().path()), null, false)
                                                .then(__ -> column.reload())))
                        .addButton(button(rotateRight()).plain().small()
                                .ouiaId(OuiaIds.ouia(id, "refresh", "btn"))
                                .onClick((e, b) -> column.reload()))))
                .defaultSearch()
                .showSearchThreshold(5)
                .addItems(childResources(templateFn, node -> {
                    FinderItem item = finderItem(Id.build(node.asString()));
                    item.text(node.asString()).addActions(finderItemActions()
                            .addButton(button(upRightFromSquare()).plain().small().onClick((e, b) -> {
                                    String address = itemAddress(item);
                                    if (address != null) {
                                        uic().placeManager().goTo(resourceRoute, address);
                                    }
                            }))
                            .addButton(button(trash()).plain().small()
                                    .ouiaId(OuiaIds.ouia(id, "delete", "btn"))
                                    .onClick((e, b) -> {
                                        AddressTemplate template = item.get(FinderSupport.TEMPLATE_KEY);
                                        deleteResourceModal(template).then(n -> {
                                            if (n.isDefined()) {
                                                item.column().reload();
                                            }
                                            return null;
                                        });
                                    })));
                    if (nextColumn != null) {
                        item.nextColumn(nextColumn);
                    }
                    return item;
                }));
        if (!previewAttributes.isEmpty()) {
            column.onPreview((item, preview) -> {
                String name = item.text();
                stackPreview(preview, name, s -> {
                    AddressTemplate template = item.get(FinderSupport.TEMPLATE_KEY);
                    uic().crud().readWithMetadata(template).then(tuple -> {
                        s.addItem(stackItem().add(resourceView(template, tuple.key, tuple.value, previewAttributes)));
                        return null;
                    });
                });
            });
        }
        return column;
    }

    /**
     * Populates a finder preview with a stack layout. The consumer receives the stack to add content items to.
     *
     * @param preview the finder preview to populate
     * @param stack   a consumer that adds content to the stack layout
     */
    public static void stackPreview(FinderPreview preview, Consumer<Stack> stack) {
        stackPreview(preview, null, stack);
    }

    /**
     * Populates a finder preview with a stack layout and an optional heading. When {@code h1} is non-null, it is rendered as an
     * {@code h1} content element at the top of the stack.
     *
     * @param preview the finder preview to populate
     * @param h1      the heading text, or {@code null} for no heading
     * @param stack   a consumer that adds content to the stack layout
     */
    public static void stackPreview(FinderPreview preview, String h1, Consumer<Stack> stack) {
        preview.add(stack().gutter().run(s -> {
            if (h1 != null) {
                s.addItem(stackItem().add(content(ContentType.h1).text(h1)));
            }
            stack.accept(s);
        }));
    }

    private FinderBricks() {
    }
}

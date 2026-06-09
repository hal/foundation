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
import org.patternfly.extension.finder.FinderColumn;
import org.patternfly.extension.finder.FinderItem;
import org.patternfly.extension.finder.FinderPath;
import org.patternfly.extension.finder.FinderPreview;
import org.patternfly.filter.Filter;
import org.patternfly.layout.stack.Stack;

import static org.jboss.hal.core.Notification.nyi;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.resource.dialog.ResourceDialogs.addResourceModal;
import static org.jboss.hal.ui.resource.dialog.ResourceDialogs.deleteResourceModal;
import static org.jboss.hal.ui.resource.finder.FinderSupport.childResources;
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

/** Finder column construction, preview helpers, and empty states. */
public final class FinderBricks {

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

    public static FinderColumn crudColumn(String id, String header, List<String> previewAttributes,
            Function<FinderPath, AddressTemplate> templateFn, Supplier<FinderColumn> nextColumn) {
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
                            .addButton(button(upRightFromSquare()).plain().small().onClick((e, b) ->
                                    uic().notifications().send(nyi())))
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

    public static void stackPreview(FinderPreview preview, Consumer<Stack> stack) {
        stackPreview(preview, null, stack);
    }

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

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
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.resources.Keys;
import org.jboss.hal.resources.OuiaIds;
import org.jboss.hal.ui.resource.pipeline.Pipeline;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags;
import org.jboss.hal.ui.resource.view.ViewItem;
import org.patternfly.component.content.ContentType;
import org.patternfly.extension.finder.Finder;
import org.patternfly.extension.finder.FinderColumn;
import org.patternfly.extension.finder.FinderItem;
import org.patternfly.extension.finder.FinderPreview;
import org.patternfly.extension.finder.ResolvedFinderPath;
import org.patternfly.filter.Filter;
import org.patternfly.layout.stack.Stack;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.resource.dialog.ResourceDialogs.addResourceModal;
import static org.jboss.hal.ui.resource.dialog.ResourceDialogs.deleteResourceModal;
import static org.jboss.hal.ui.resource.finder.FinderSupport.childResources;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.extension.finder.FinderColumn.finderColumn;
import static org.patternfly.extension.finder.FinderColumnActions.finderColumnActions;
import static org.patternfly.extension.finder.FinderColumnHeader.finderColumnHeader;
import static org.patternfly.extension.finder.FinderItem.finderItem;
import static org.patternfly.extension.finder.FinderItemActions.finderItemActions;
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
     * Creates a finder column with full CRUD support: an "add" button that opens an add-resource dialog, a "refresh" button,
     * per-item "view" and "delete" actions, a search bar (shown when there are 5+ items), and an optional preview panel
     * displaying selected resource attributes.
     * <p>
     * The "view" action navigates to the resource page using the {@link org.jboss.hal.ui.navigation.RouteRegistry RouteRegistry}
     * to find the best matching route for the item's address template.
     *
     * @param id                a unique identifier for the column and its OUIA test IDs
     * @param header            the column header text
     * @param previewAttributes attribute names to display in the preview panel; if empty, no preview is shown
     * @param templateFn        resolves the current finder path to an {@link AddressTemplate} for the resource
     * @param nextColumn        supplier for the next column to navigate into, or {@code null} for leaf items
     * @return a fully configured finder column with CRUD capabilities
     */
    public static FinderColumn crudColumn(String id, String header, List<String> previewAttributes,
            Function<ResolvedFinderPath, AddressTemplate> templateFn,
            Supplier<FinderColumn> nextColumn) {
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
                            .addButton(button(upRightFromSquare())
                                    .plain()
                                    .small()
                                    .onClick((e, b) -> {
                                        AddressTemplate template = item.get(Keys.FINDER_TEMPLATE);
                                        if (template != null) {
                                            uic().routeRegistry().goTo(template);
                                        }
                                    }))
                            .addButton(button(trash()).plain().small()
                                    .ouiaId(OuiaIds.ouia(id, "delete", "btn"))
                                    .onClick((e, b) -> {
                                        AddressTemplate template = item.get(Keys.FINDER_TEMPLATE);
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
                    AddressTemplate template = item.get(Keys.FINDER_TEMPLATE);
                    uic().crud().readWithMetadata(template).then(tuple -> {
                        s.addItem(stackItem().add(previewView(template, tuple.key, tuple.value, previewAttributes)));
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

    /** Creates a pipeline-based preview view for finder items, restricted to the given attribute names. */
    public static HTMLElement previewView(AddressTemplate template, ModelNode resource, Metadata metadata,
            List<String> attributes) {
        PipelineContext context = new PipelineContext(template, metadata, resource,
                new PipelineFlags(PipelineFlags.Scope.EXISTING_RESOURCE, PipelineFlags.Placeholder.NONE));
        List<ViewItem> items = Pipeline.create().viewItems(context);
        HTMLElement dl = descriptionList().css(halComponent(HalClasses.resource, HalClasses.view)).element();
        for (ViewItem item : items) {
            if (attributes.isEmpty() || attributes.contains(item.attribute().fqn())) {
                dl.appendChild(item.element());
            }
        }
        return dl;
    }

    private FinderBricks() {
    }
}

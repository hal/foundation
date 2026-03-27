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
package org.jboss.hal.ui;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.Id;
import org.jboss.hal.dmr.Expression;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.env.Stability;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.Deprecation;
import org.jboss.hal.meta.description.Description;
import org.jboss.hal.meta.description.OperationDescription;
import org.jboss.hal.meta.description.RestartMode;
import org.jboss.hal.model.RunningMode;
import org.jboss.hal.model.RunningState;
import org.jboss.hal.model.RuntimeConfigurationState;
import org.jboss.hal.model.SuspendState;
import org.jboss.hal.ui.resource.FinderSupport;
import org.jboss.hal.ui.resource.ResourceDialogs;
import org.patternfly.component.codeblock.CodeBlock;
import org.patternfly.component.content.ContentType;
import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.component.label.Label;
import org.patternfly.component.list.ListItem;
import org.patternfly.component.popover.NativePopover;
import org.patternfly.component.popover.NativePopoverBody;
import org.patternfly.component.popover.Popover;
import org.patternfly.extension.finder.FinderColumn;
import org.patternfly.extension.finder.FinderItem;
import org.patternfly.extension.finder.FinderPath;
import org.patternfly.extension.finder.FinderPreview;
import org.patternfly.filter.Filter;
import org.patternfly.icon.IconSets;
import org.patternfly.icon.PredefinedIcon;
import org.patternfly.layout.flex.Flex;
import org.patternfly.layout.stack.Stack;
import org.patternfly.style.Status;
import org.patternfly.style.Variable;
import org.patternfly.token.Token;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.br;
import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.i;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.strong;
import static org.jboss.hal.core.Notification.nyi;
import static org.jboss.hal.dmr.Expression.startExpressionEnd;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALTERNATIVES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITY_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPRESSIONS_ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESTART_REQUIRED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNIT;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;
import static org.jboss.hal.env.Stability.EXPERIMENTAL;
import static org.jboss.hal.env.Stability.PREVIEW;
import static org.jboss.hal.meta.description.RestartMode.UNKNOWN;
import static org.jboss.hal.resources.HalClasses.colon;
import static org.jboss.hal.resources.HalClasses.curlyBraces;
import static org.jboss.hal.resources.HalClasses.defaultValue;
import static org.jboss.hal.resources.HalClasses.deprecated;
import static org.jboss.hal.resources.HalClasses.dollar;
import static org.jboss.hal.resources.HalClasses.expression;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.resources.HalClasses.name;
import static org.jboss.hal.ui.BuildingBlocks.AttributeDescriptionContent.all;
import static org.jboss.hal.ui.BuildingBlocks.AttributeDescriptionContent.allButReadOnly;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.resource.FinderSupport.childResources;
import static org.jboss.hal.ui.resource.ResourceDialogs.addResourceModal;
import static org.jboss.hal.ui.resource.ResourceDialogs.deleteResourceModal;
import static org.jboss.hal.ui.resource.ResourceView.resourceView;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.Severity.success;
import static org.patternfly.component.Severity.warning;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.codeblock.CodeBlock.codeBlock;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateActions.emptyStateActions;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;
import static org.patternfly.component.label.Label.label;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.component.popover.NativePopover.nativePopover;
import static org.patternfly.component.popover.Popover.popover;
import static org.patternfly.component.popover.PopoverBody.popoverBody;
import static org.patternfly.extension.finder.FinderColumn.finderColumn;
import static org.patternfly.extension.finder.FinderColumnActions.finderColumnActions;
import static org.patternfly.extension.finder.FinderColumnHeader.finderColumnHeader;
import static org.patternfly.extension.finder.FinderItem.finderItem;
import static org.patternfly.extension.finder.FinderItemActions.finderItemActions;
import static org.patternfly.icon.IconSets.fas.exclamationTriangle;
import static org.patternfly.icon.IconSets.fas.externalLinkAlt;
import static org.patternfly.icon.IconSets.fas.flask;
import static org.patternfly.icon.IconSets.fas.infoCircle;
import static org.patternfly.icon.IconSets.fas.plus;
import static org.patternfly.icon.IconSets.fas.redo;
import static org.patternfly.icon.IconSets.fas.search;
import static org.patternfly.icon.IconSets.fas.trash;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.SpaceItems.sm;
import static org.patternfly.layout.stack.Stack.stack;
import static org.patternfly.layout.stack.StackItem.stackItem;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.end;
import static org.patternfly.style.Classes.list;
import static org.patternfly.style.Classes.start;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Color.blue;
import static org.patternfly.style.Color.grey;
import static org.patternfly.style.Variable.componentVar;
import static org.patternfly.style.Variable.utilVar;

/** Contains various UI-related methods used across the UI module. */
public class BuildingBlocks {

    // ------------------------------------------------------ attributes

    public enum AttributeDescriptionContent {
        all, allButReadOnly, descriptionOnly;
    }

    public static Flex attributeName(AttributeDescription attribute, Supplier<Boolean> stabilityCheck) {
        return attributeName(attribute, false, stabilityCheck);
    }

    public static Flex attributeName(AttributeDescription attribute, boolean compact, Supplier<Boolean> stabilityCheck) {
        HTMLContainerBuilder<HTMLElement> name = strong()
                .text(attribute.name())
                .run(element -> {
                    if (attribute.deprecation().isDefined()) {
                        element.css(halModifier(deprecated));
                    }
                });
        if (stabilityCheck.get()) {
            return flex().alignItems(center).spaceItems(sm)
                    .addItem(flexItem().add(name))
                    .addItem(flexItem().add(stabilityLabel(attribute.stability()).compact(compact)));
        } else {
            return flex().add(name);
        }
    }

    public static HTMLContainerBuilder<HTMLDivElement> attributeDescription(AttributeDescription attribute,
            AttributeDescriptionContent content) {
        Variable marginTop = componentVar(component(list), "li", "MarginTop");
        Variable marginLeft = componentVar(component(list), "nested", "MarginLeft");

        var infos = list().plain()
                .css(util("mt-sm"))
                .style("color", Token.globalTextColorSubtle.var)
                .style(marginTop.name, 0)
                .style(marginLeft.name, 0);
        if (content == all && attribute.readOnly()) {
            infos.add(listItem().text("Read-only."));
        }
        if (content == all || content == allButReadOnly) {
            if (attribute.get(REQUIRED).asBoolean(false)) {
                infos.add(listItem().text("Required."));
            }
            if (attribute.hasDefined(CAPABILITY_REFERENCE)) {
                infos.addItem(listItem()
                        .add("References the capability ")
                        .add(code().text(attribute.get(CAPABILITY_REFERENCE).asString()))
                        .add("."));
            }
            if (attribute.get(EXPRESSIONS_ALLOWED).asBoolean(false)) {
                infos.add(listItem()
                        .add("Supports expressions."));
            }
            if (attribute.hasDefined(UNIT)) {
                infos.addItem(listItem()
                        .add("Uses ")
                        .add(i().text(attribute.get(UNIT).asString()))
                        .add(" as unit."));
            }
            if (attribute.hasDefined(DEFAULT)) {
                infos.addItem(listItem()
                        .add("Defaults to ")
                        .add(i().text(attribute.get(DEFAULT).asString()))
                        .add(" when undefined."));
            }
            if (attribute.hasDefined(REQUIRES)) {
                infos.addItem(listItem()
                        .add("Requires ")
                        .run(listItem -> enumerate(listItem, attribute.get(REQUIRES).asList())));
            }
            if (attribute.hasDefined(ALTERNATIVES)) {
                infos.addItem(listItem()
                        .add("Mutually exclusive to ")
                        .run(listItem -> enumerate(listItem, attribute.get(ALTERNATIVES).asList())));
            }
            if (attribute.hasDefined(RESTART_REQUIRED)) {
                RestartMode restartMode = asEnumValue(attribute, RESTART_REQUIRED, RestartMode::valueOf, UNKNOWN);
                if (restartMode != UNKNOWN) {
                    String text = switch (restartMode) {
                        case ALL_SERVICES ->
                                "A modification requires a restart of all services, but does not require a full JVM restart.";
                        case JVM -> "A modification requires a full JVM restart.";
                        case NO_SERVICES -> "A modification doesn't require a restart.";
                        case RESOURCE_SERVICES ->
                                "A modification requires a restart of services, associated with the attribute's resource, but does not require a restart of all services or a full JVM restart.";
                        default -> "";
                    };
                    infos.addItem(listItem().text(text));
                }
            }
        }

        return description(attribute).run(description -> {
            if (!infos.isEmpty()) {
                description.add(infos);
            }
        });
    }

    public static Popover attributeDescriptionPopover(String header, AttributeDescription attribute,
            AttributeDescriptionContent content) {
        return popover()
                .css(util("min-width"))
                .style(utilVar("min-width", "MinWidth").name, "40ch")
                .addHeader(header)
                .addBody(popoverBody()
                        .add(attributeDescription(attribute, content)));
    }

    public static NativePopover attributeDescriptionNativePopover(String header, AttributeDescription attribute,
            AttributeDescriptionContent content) {
        return nativePopover()
                .css(util("min-width"))
                .style(utilVar("min-width", "MinWidth").name, "40ch")
                .addHeader(header)
                .addBody(NativePopoverBody.popoverBody()
                        .add(attributeDescription(attribute, content)));
    }

    public static HTMLElement nestedElementSeparator() {
        return span().css(util("mx-sm")).text("/").element();
    }

    private static HTMLContainerBuilder<HTMLDivElement> description(Description description) {
        HTMLContainerBuilder<HTMLDivElement> div = div();
        div.add(div().text(description.description()));
        Deprecation deprecation = description.deprecation();
        if (deprecation.isDefined()) {
            div.add(div().css(util("mt-sm"))
                    .add("Deprecated since " + deprecation.since().toString())
                    .add(br())
                    .add("Reason: " + deprecation.reason()));
        }
        return div;
    }

    private static void enumerate(ListItem listItem, java.util.List<ModelNode> values) {
        for (Iterator<ModelNode> iterator = values.iterator(); iterator.hasNext(); ) {
            ModelNode value = iterator.next();
            listItem.add(code().text(value.asString()));
            if (iterator.hasNext()) {
                listItem.add(", ");
            }
        }
    }

    // ------------------------------------------------------ code

    public static CodeBlock errorCode(String error) {
        return errorCode(error, 5);
    }

    public static CodeBlock errorCode(String error, int lines) {
        return codeBlock()
                .truncate(lines)
                .code(error.replace("\\/", "/"));
    }

    public static CodeBlock modelNodeCode(ModelNode modelNode) {
        return modelNodeCode(modelNode, 5);
    }

    public static CodeBlock modelNodeCode(ModelNode modelNode, int lines) {
        String code = modelNode.isDefined() ? modelNode.toJSONString().replace("\\/", "/") : "";
        return codeBlock()
                .truncate(lines)
                .code(code);
    }

    // ------------------------------------------------------ empty

    public static <T> EmptyState emptyRow(Filter<T> filter) {
        return emptyState()
                .icon(search())
                .text("No results found")
                .addBody(emptyStateBody()
                        .text(
                                "No results match the filter criteria. Clear all filters and try again."))
                .addFooter(emptyStateFooter()
                        .addActions(emptyStateActions()
                                .add(button("Clear all filters").link()
                                        .onClick((event, component) -> filter.resetAll()))));
    }

    // ------------------------------------------------------ expression

    public static HTMLElement renderExpression(String value) {
        if (Expression.containsExpression(value)) {
            HTMLContainerBuilder<HTMLElement> span = span().css(halComponent(expression));
            internalRenderExpression(span, value);
            return span.element();
        } else {
            return span().text(value).element();
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private static void internalRenderExpression(HTMLContainerBuilder<HTMLElement> builder, String value) {
        String[] startExprEnd = startExpressionEnd(value);
        if (!startExprEnd[0].isEmpty()) {
            builder.add(span().css(halComponent(expression, start)).text(startExprEnd[0]));
        }
        String[] nameDefault = Expression.splitExpression(startExprEnd[1]);
        builder.add(span().css(halComponent(expression, dollar)))
                .add(span().css(halComponent(expression, curlyBraces, start)))
                .add(span().css(halComponent(expression, name)).text(nameDefault[0]));
        if (!nameDefault[1].isEmpty()) {
            builder.add(span().css(halComponent(expression, colon)));
            if (Expression.containsExpression(nameDefault[1])) {
                HTMLContainerBuilder<HTMLElement> nested = span().css(halComponent(expression));
                internalRenderExpression(nested, nameDefault[1]);
                builder.add(nested);
            } else {
                builder.add(span().css(halComponent(expression, defaultValue)).text(nameDefault[1]));
            }
        }
        builder.add(span().css(halComponent(expression, curlyBraces, end)));
        if (!startExprEnd[2].isEmpty()) {
            builder.add(span().css(halComponent(expression, end)).text(startExprEnd[2]));
        }
    }

    // ------------------------------------------------------ icons

    public static Supplier<PredefinedIcon> expressionModeIcon() {
        return IconSets.fas::dollarSign;
    }

    public static Supplier<PredefinedIcon> normalModeIcon() {
        return IconSets.fas::terminal;
    }

    public static Supplier<PredefinedIcon> resolveExpressionIcon() {
        return IconSets.fas::link;
    }

    // ------------------------------------------------------ operations

    public static HTMLContainerBuilder<HTMLDivElement> operationDescription(OperationDescription operation) {
        return description(operation);
    }

    // ------------------------------------------------------ server and host state

    public static Label runtimeConfigurationStateLabel(RuntimeConfigurationState value) {
        return switch (value) {
            case STARTING, STOPPING -> label(value.name(), blue);
            case OK -> label(value.name()).status(success);
            case RELOAD_REQUIRED, RESTART_REQUIRED -> label(value.name()).status(warning);
            case STOPPED -> label(value.name(), grey);
            case UNDEFINED -> label(RuntimeConfigurationState.UNDEFINED.name()).status(danger);
        };
    }

    public static Label runningModeLabel(RunningMode value) {
        return switch (value) {
            case NORMAL -> label(value.name()).status(success);
            case ADMIN_ONLY -> label(value.name(), grey);
            default -> label(RunningMode.UNDEFINED.name()).status(danger);
        };
    }

    public static Label runningStateLabel(RunningState value) {
        return switch (value) {
            case STARTING -> label(value.name(), blue);
            case RUNNING -> label(value.name()).status(success);
            case STOPPED -> label(value.name(), grey);
            case RESTART_REQUIRED, RELOAD_REQUIRED -> label(value.name()).status(warning);
            case UNDEFINED -> label(RunningState.UNDEFINED.name()).status(danger);
        };
    }

    public static Label suspendStateLabel(SuspendState value) {
        return switch (value) {
            case RUNNING -> label(value.name()).status(success);
            case PRE_SUSPEND, SUSPENDED, SUSPENDING -> label(value.name(), blue);
            case UNDEFINED -> label(SuspendState.UNDEFINED.name()).status(danger);
        };
    }

    // ------------------------------------------------------ stability

    public static Status stabilityStatus(Stability stability) {
        if (stability == EXPERIMENTAL) {
            return Status.danger;
        } else if (stability == PREVIEW) {
            return Status.warning;
        }
        return Status.info;
    }

    public static PredefinedIcon stabilityIcon(Stability stability) {
        if (stability == EXPERIMENTAL) {
            return flask();
        } else if (stability == PREVIEW) {
            return exclamationTriangle();
        }
        return infoCircle();
    }

    public static Supplier<PredefinedIcon> stabilityIconSupplier(Stability stability) {
        return () -> stabilityIcon(stability);
    }

    // ------------------------------------------------------ finder

    /**
     * Creates and configures a {@code FinderColumn} with basic settings.
     * <p>
     * The column header has an add and reload button in the header. The add button calls
     * {@link ResourceDialogs#addResourceModal(AddressTemplate, String, boolean)}.
     * <p>
     * The column has a search input that filters on the item text and is visible when more than four items are present.
     * <p>
     * The items are loaded asynchronously using {@link FinderSupport#childResources(Function, Function)}. The items have a view
     * and remove button using icons.
     * <p>
     * If {@code previewAttributes} is not empty, the column has a preview handler that shows the specified attributes.
     *
     * @param id                The unique identifier for the column.
     * @param header            The header text for the column.
     * @param previewAttributes A list of preview attributes to be displayed when an item in the column is selected.
     * @param templateFn        A function that maps a {@code FinderPath} to an {@code AddressTemplate} for managing resources.
     * @param nextColumn        A supplier for the next column in the navigation hierarchy, or {@code null} if no next column is
     *                          present.
     * @return A configured {@code FinderColumn} instance.
     */
    public static FinderColumn crudColumn(String id, String header, List<String> previewAttributes,
            Function<FinderPath, AddressTemplate> templateFn, Supplier<FinderColumn> nextColumn) {
        FinderColumn column = finderColumn(id);
        column.addHeader(finderColumnHeader(header).addActions(finderColumnActions()
                        .addButton(button(plus()).plain().small().onClick((e, b) ->
                                addResourceModal(templateFn.apply(column.finder().path()), null, false)
                                        .then(__ -> column.reload())))
                        .addButton(button(redo()).plain().small().onClick((e, b) -> column.reload()))))
                .defaultSearch()
                .showSearchThreshold(5)
                .addItems(childResources(templateFn, node -> {
                    FinderItem item = finderItem(Id.build(node.asString()));
                    item.text(node.asString()).addActions(finderItemActions()
                            .addButton(button(externalLinkAlt()).plain().small().onClick((e, b) ->
                                    uic().notifications().send(nyi())))
                            .addButton(button(trash()).plain().small().onClick((e, b) -> {
                                AddressTemplate template = item.get(FinderSupport.TEMPLATE_KEY);
                                deleteResourceModal(template).then(n -> {
                                    if (n.isDefined()) { // undefined means canceled
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
                stackPreview(preview, name, stack -> {
                    AddressTemplate template = item.get(FinderSupport.TEMPLATE_KEY);
                    uic().crud().readWithMetadata(template).then(tuple -> {
                        stack.addItem(stackItem().add(resourceView(template, tuple.key, tuple.value, previewAttributes)));
                        return null;
                    });
                });
            });
        }
        return column;
    }

    /**
     * Configures the given {@code FinderPreview} to include a stack layout with additional content provided by a
     * {@code Consumer} of {@code Stack}. This method uses the default configuration for headers.
     *
     * @param preview The {@code FinderPreview} instance to configure.
     * @param stack   A {@code Consumer} that allows additional configuration of the {@code Stack}.
     */
    public static void stackPreview(FinderPreview preview, Consumer<Stack> stack) {
        stackPreview(preview, null, stack);
    }

    /**
     * Configures the given {@code FinderPreview} to include a stack layout with an optional header and additional content
     * provided by a {@code Consumer} of {@code Stack}.
     *
     * @param preview The {@code FinderPreview} instance to configure.
     * @param h1      Optional text to be displayed as the first header (H1). If {@code null}, no H1 is added.
     * @param stack   A {@code Consumer} that allows additional configuration of the {@code Stack}.
     */
    public static void stackPreview(FinderPreview preview, String h1, Consumer<Stack> stack) {
        preview.add(stack().gutter().run(s -> {
            if (h1 != null) {
                s.addItem(stackItem().add(content(ContentType.h1).text(h1)));
            }
            stack.accept(s);
        }));
    }
}

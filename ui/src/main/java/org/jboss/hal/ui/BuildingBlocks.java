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
import java.util.function.Supplier;

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.hal.dmr.Expression;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.env.Stability;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.Deprecation;
import org.jboss.hal.meta.description.Description;
import org.jboss.hal.meta.description.OperationDescription;
import org.jboss.hal.meta.description.RestartMode;
import org.patternfly.component.codeblock.CodeBlock;
import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.component.list.ListItem;
import org.patternfly.component.popover.Popover;
import org.patternfly.filter.Filter;
import org.patternfly.icon.IconSets;
import org.patternfly.icon.PredefinedIcon;
import org.patternfly.layout.flex.Flex;
import org.patternfly.style.Color;
import org.patternfly.style.Variable;
import org.patternfly.style.Variables;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.br;
import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.i;
import static org.jboss.elemento.Elements.small;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.strong;
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
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.codeblock.CodeBlock.codeBlock;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateActions.emptyStateActions;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;
import static org.patternfly.component.emptystate.EmptyStateHeader.emptyStateHeader;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.component.popover.Popover.popover;
import static org.patternfly.component.popover.PopoverBody.popoverBody;
import static org.patternfly.icon.IconSets.fas.exclamationTriangle;
import static org.patternfly.icon.IconSets.fas.flask;
import static org.patternfly.icon.IconSets.fas.infoCircle;
import static org.patternfly.icon.IconSets.fas.search;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.SpaceItems.sm;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.end;
import static org.patternfly.style.Classes.list;
import static org.patternfly.style.Classes.start;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Color.blue;
import static org.patternfly.style.Color.orange;
import static org.patternfly.style.Color.red;
import static org.patternfly.style.Variable.componentVar;
import static org.patternfly.style.Variable.utilVar;

/** Contains various UI-related methods used across the UI module. */
public class BuildingBlocks {

    // ------------------------------------------------------ attributes

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

    public static HTMLContainerBuilder<HTMLDivElement> attributeDescription(AttributeDescription attribute) {
        Variable marginTop = componentVar(component(list), "li", "MarginTop");
        Variable marginLeft = componentVar(component(list), "nested", "MarginLeft");

        org.patternfly.component.list.List infos = list().plain()
                .css(util("mt-sm"))
                .style(marginTop.name, 0)
                .style(marginLeft.name, 0);
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
                String text = "";
                switch (restartMode) {
                    case ALL_SERVICES:
                        text = "A modification requires a restart of all services, but does not require a full JVM restart.";
                        break;
                    case JVM:
                        text = "A modification requires a full JVM restart.";
                        break;
                    case NO_SERVICES:
                        text = "A modification doesn't require a restart.";
                        break;
                    case RESOURCE_SERVICES:
                        text = "A modification requires a restart of services, associated with the attribute's resource, but does not require a restart of all services or a full JVM restart.";
                        break;
                }
                infos.addItem(listItem().text(text));
            }
        }

        return description(attribute).run(description -> {
            if (!infos.isEmpty()) {
                description.add(small().add(infos));
            }
        });
    }

    public static Popover attributeDescriptionPopover(String header, AttributeDescription attribute) {
        return popover()
                .css(util("min-width"))
                .style(utilVar("min-width", Variables.MinWidth).name, "40ch")
                .addHeader(header)
                .addBody(popoverBody()
                        .add(attributeDescription(attribute)));
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
                .addHeader(emptyStateHeader()
                        .icon(search())
                        .text("No results found"))
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
        String[] startExprEnd = Expression.extractExpression(value);
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

    public static Supplier<PredefinedIcon> expressionMode() {
        return IconSets.fas::dollarSign;
    }

    public static Supplier<PredefinedIcon> normalMode() {
        return IconSets.fas::terminal;
    }

    public static Supplier<PredefinedIcon> resolveExpression() {
        return IconSets.fas::link;
    }

    // ------------------------------------------------------ operations

    public static HTMLContainerBuilder<HTMLDivElement> operationDescription(OperationDescription operation) {
        return description(operation);
    }

    // ------------------------------------------------------ stability

    public static Color stabilityColor(Stability stability) {
        if (stability == EXPERIMENTAL) {
            return red;
        } else if (stability == PREVIEW) {
            return orange;
        }
        return blue;
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
}

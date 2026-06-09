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

import java.util.function.Supplier;

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.RestartMode;
import org.jboss.hal.ui.brick.DescriptionBricks.AttributeDescriptionContent;
import org.patternfly.component.popover.Popover;
import org.patternfly.style.Variable;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.i;
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
import static org.jboss.hal.meta.description.RestartMode.UNKNOWN;
import static org.jboss.hal.resources.HalClasses.deprecated;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.brick.DescriptionBricks.AttributeDescriptionContent.all;
import static org.jboss.hal.ui.brick.DescriptionBricks.AttributeDescriptionContent.allButReadOnly;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.component.popover.Popover.popover;
import static org.patternfly.component.popover.PopoverBody.popoverBody;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.list;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Variable.componentVar;
import static org.patternfly.style.Variable.utilVar;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.SpaceItems.sm;
import static org.patternfly.token.Token.globalTextColorSubtle;

/**
 * Factory methods for rendering management model attribute names, descriptions, and popovers as PatternFly UI elements.
 * <p>
 * Attribute names can optionally display a stability label and a deprecation indicator. Descriptions render the full
 * attribute metadata including read-only status, required flag, capability references, expression support, unit, default
 * value, requires/alternatives relationships, and restart requirements.
 */
public final class AttributeBricks {

    /**
     * Renders an attribute name with an optional stability label using default (non-compact) sizing.
     *
     * @param attribute      the attribute description from the management model
     * @param stabilityCheck returns {@code true} if the stability label should be shown
     * @return a flex layout containing the attribute name and optional stability label
     */
    public static org.patternfly.layout.flex.Flex attributeName(AttributeDescription attribute,
            Supplier<Boolean> stabilityCheck) {
        return attributeName(attribute, false, stabilityCheck);
    }

    /**
     * Renders an attribute name with an optional stability label. The name is rendered in bold and receives a
     * {@code deprecated} CSS modifier when the attribute is deprecated.
     *
     * @param attribute      the attribute description from the management model
     * @param compact        whether to render a compact stability label
     * @param stabilityCheck returns {@code true} if the stability label should be shown
     * @return a flex layout containing the attribute name and optional stability label
     */
    public static org.patternfly.layout.flex.Flex attributeName(AttributeDescription attribute, boolean compact,
            Supplier<Boolean> stabilityCheck) {
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

    /**
     * Renders a detailed attribute description including deprecation info and a metadata list. The metadata list is
     * controlled by the {@code content} parameter:
     * <ul>
     *     <li>{@link AttributeDescriptionContent#all} — includes read-only status and all metadata</li>
     *     <li>{@link AttributeDescriptionContent#allButReadOnly} — all metadata except the read-only indicator</li>
     *     <li>{@link AttributeDescriptionContent#descriptionOnly} — only the description text and deprecation</li>
     * </ul>
     *
     * @param attribute the attribute description from the management model
     * @param content   controls which metadata items are included in the rendered output
     * @return a div element containing the formatted description and metadata list
     */
    public static HTMLContainerBuilder<HTMLDivElement> attributeDescription(AttributeDescription attribute,
            AttributeDescriptionContent content) {
        Variable marginTop = componentVar(component(list), "li", "MarginTop");
        Variable marginLeft = componentVar(component(list), "nested", "MarginLeft");

        var infos = org.patternfly.component.list.List.list().plain()
                .css(util("mt-sm"))
                .style("color", globalTextColorSubtle.var)
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
                        .run(li -> DescriptionBricks.enumerate(li, attribute.get(REQUIRES).asList())));
            }
            if (attribute.hasDefined(ALTERNATIVES)) {
                infos.addItem(listItem()
                        .add("Mutually exclusive to ")
                        .run(li -> DescriptionBricks.enumerate(li, attribute.get(ALTERNATIVES).asList())));
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

        return DescriptionBricks.description(attribute).run(description -> {
            if (!infos.isEmpty()) {
                description.add(infos);
            }
        });
    }

    /**
     * Creates a popover that displays the full attribute description. The popover uses a minimum width of 40 characters
     * to ensure readable formatting of the metadata list.
     *
     * @param header    the popover header text
     * @param attribute the attribute description from the management model
     * @param content   controls which metadata items are included
     * @return a configured popover component
     */
    public static Popover attributeDescriptionPopover(String header, AttributeDescription attribute,
            AttributeDescriptionContent content) {
        return popover()
                .css(util("min-width"))
                .style(utilVar("min-width", "MinWidth").name, "40ch")
                .addHeader(header)
                .addBody(popoverBody()
                        .add(attributeDescription(attribute, content)));
    }

    /** Returns a "/" separator element with horizontal margins, used between segments of nested attribute paths. */
    public static HTMLElement nestedElementSeparator() {
        return span().css(util("mx-sm")).text("/").element();
    }

    private AttributeBricks() {
    }
}

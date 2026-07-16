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
package org.jboss.hal.ui.resource.view;

import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;

import org.jboss.elemento.logger.Logger;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.resources.HalClasses;
import org.patternfly.component.list.DescriptionListTerm;
import org.patternfly.core.Roles;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.wrapHtmlContainer;
import static org.jboss.hal.core.Humanize.sentenceCase;
import static org.jboss.hal.core.Notification.nyi;
import static org.jboss.hal.resources.HalClasses.deprecated;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.resources.HalClasses.restricted;
import static org.jboss.hal.resources.HalClasses.stabilityLevel;
import static org.jboss.hal.resources.HalClasses.undefined;
import static org.jboss.hal.resources.HalClasses.view;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.brick.AttributeBricks.attributeDescriptionPopover;
import static org.jboss.hal.ui.brick.AttributeBricks.nestedElementSeparator;
import static org.jboss.hal.ui.brick.DescriptionBricks.AttributeDescriptionContent.all;
import static org.jboss.hal.ui.brick.ExpressionBricks.renderExpression;
import static org.jboss.hal.ui.brick.ExpressionBricks.resolveExpressionIcon;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.icon.Icon.icon;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.core.Attributes.role;
import static org.patternfly.icon.IconSets.fas.lock;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.descriptionList;
import static org.patternfly.style.Classes.helpText;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.text;
import static org.patternfly.style.Classes.util;

/**
 * Base class for all pipeline view items. Self-contained: receives a {@link ResolvedAttribute} and builds the complete
 * {@code DescriptionListGroup} (label + value) internally.
 * <p>
 * The value dispatch chain handles restricted, expression, and undefined states. Subclasses implement
 * {@link #definedValue()} to provide type-specific rendering for defined, non-expression, readable values.
 */
public abstract class AbstractViewItem implements ViewItem {

    private static final Logger logger = Logger.getLogger(AbstractViewItem.class.getName());

    final ResolvedAttribute attribute;
    final PipelineContext context;
    private final String identifier;
    private final HTMLElement labelEl;
    private final HTMLElement valueEl;
    private final HTMLElement root;

    protected AbstractViewItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        this.identifier = identifier;
        this.attribute = attribute;
        this.context = context;
        DescriptionListTerm term = label();
        this.labelEl = term.element();
        this.valueEl = value();
        this.root = descriptionListGroup(identifier)
                .addTerm(term)
                .addDescription(descriptionListDescription().add(valueEl))
                .element();
    }

    // ------------------------------------------------------ label

    private DescriptionListTerm label() {
        AttributeDescription description = attribute.description();
        DescriptionListTerm term;

        if (description.nested()) {
            // <unstable>
            // If the internal DOM of DescriptionListTerm changes, this will no longer work.
            // By default, DescriptionListTerm supports only one text element. But for nested
            // attributes we need one for the parent and one for the nested attribute description.
            AttributeDescription parentDescription = description.parent();
            String parentLabel = sentenceCase(parentDescription.name());
            String nestedLabel = sentenceCase(description.name());
            term = descriptionListTerm(parentLabel)
                    .css(halComponent(resource, HalClasses.nestedLabel))
                    .help(attributeDescriptionPopover(parentLabel, parentDescription, all));
            HTMLElement nestedTextElement = span()
                    .css(component(descriptionList, text), modifier(helpText))
                    .attr(role, Roles.button)
                    .attr("type", "button")
                    .apply(element -> element.tabIndex = 0)
                    .text(nestedLabel)
                    .element();
            nestedTextElement.appendChild(attributeDescriptionPopover(nestedLabel, description, all)
                    .trigger(nestedTextElement)
                    .element());
            wrapHtmlContainer(term.element())
                    .add(nestedElementSeparator())
                    .add(nestedTextElement);
            // </unstable>
        } else {
            String label = sentenceCase(attribute.name());
            term = descriptionListTerm(label)
                    .help(attributeDescriptionPopover(label, description, all));

            if (uic().environment()
                    .highlightStability(context.resourceDescription().stability(), description.stability())) {
                // <unstable>
                // If the internal DOM of DescriptionListTerm changes, this will no longer work.
                // DescriptionListTerm delegates to the internal text element, so we must use
                // term.element().appendChild() to add the stability label after the text element.
                term.element().classList.add(halComponent(resource, stabilityLevel));
                term.element().appendChild(stabilityLabel(description.stability()).compact()
                        .style("align-self", "baseline")
                        .css(util("ml-sm"), util("font-weight-normal"))
                        .element());
                // </unstable>
            }
        }

        if (description.deprecation().isDefined()) {
            term.containerDelegate().classList.add(halModifier(deprecated));
        }
        return term;
    }

    // ------------------------------------------------------ value

    private HTMLElement value() {
        if (!attribute.readable()) {
            return restrictedValue();
        } else if (attribute.expression()) {
            return expressionValue(attribute);
        } else if (attribute.value().isDefined()) {
            return definedValue();
        } else {
            return undefinedValue();
        }
    }

    /**
     * Creates the value element for a defined, non-expression, readable attribute. Called only when all three conditions are
     * met — subclasses can assume the value is present and not an expression.
     */
    protected abstract HTMLElement definedValue();

    // ------------------------------------------------------ value helpers

    protected static HTMLElement restrictedValue() {
        return span().css(halModifier(restricted))
                .text("restricted")
                .add(icon(lock().css(util("ml-sm"))))
                .element();
    }

    protected static HTMLElement expressionValue(ResolvedAttribute ra) {
        HTMLElement resolveButton = button().plain().inline().icon(resolveExpressionIcon().get())
                .onClick((e, b) -> {
                    uic().notifications().send(nyi());
                    logger.info("Resolve expression: %s", ra.value().asString());
                })
                .element();
        return span()
                .add(renderExpression(ra.value().asString()))
                .add(resolveButton)
                .add(tooltip(resolveButton, "Resolve expression"))
                .element();
    }

    protected static HTMLElement undefinedValue() {
        return span()
                .css(halComponent(resource, view, undefined))
                .text("undefined")
                .element();
    }

    protected static HTMLElement plainText(ResolvedAttribute ra) {
        return span()
                .text(ra.value().asString())
                .element();
    }

    // ------------------------------------------------------ ViewItem

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public ResolvedAttribute attribute() {
        return attribute;
    }

    @Override
    public HTMLElement valueElement() {
        return valueEl;
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}

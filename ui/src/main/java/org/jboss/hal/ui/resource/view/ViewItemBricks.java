package org.jboss.hal.ui.resource.view;

import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.Pipeline;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
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
import static org.jboss.hal.ui.brick.AttributeBricks.slashSeparator;
import static org.jboss.hal.ui.brick.DescriptionBricks.AttributeDescriptionContent.all;
import static org.jboss.hal.ui.brick.ExpressionBricks.renderExpression;
import static org.jboss.hal.ui.brick.ExpressionBricks.resolveExpressionIcon;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.icon.Icon.icon;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.core.Attributes.role;
import static org.patternfly.icon.IconSets.fas.lock;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.Gap.sm;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.descriptionList;
import static org.patternfly.style.Classes.helpText;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.text;
import static org.patternfly.style.Classes.util;
import static org.patternfly.token.Token.globalTextColorPlaceholder;

/** Brick class for shared view item UI fragments such as attribute name rendering and value display. */
class ViewItemBricks {

    // ------------------------------------------------------ label

    static DescriptionListTerm label(PipelineContext context, AttributeDescription attributeDescription) {
        if (attributeDescription.nested()) {
            return compositeLabel(context, attributeDescription.parent(), attributeDescription);
        } else {
            return singleLabel(context, attributeDescription);
        }
    }

    static DescriptionListTerm singleLabel(PipelineContext context, AttributeDescription attributeDescription) {
        String label = sentenceCase(attributeDescription.name());
        DescriptionListTerm term = descriptionListTerm(label)
                .help(attributeDescriptionPopover(label, attributeDescription, all));

        if (uic().environment()
                .highlightStability(context.resourceDescription().stability(), attributeDescription.stability())) {
            // <unstable>
            // If the internal DOM of DescriptionListTerm changes, this will no longer work.
            // DescriptionListTerm delegates to the internal text element, so we must use
            // term.element().appendChild() to add the stability label after the text element.
            term.element().classList.add(halComponent(resource, stabilityLevel));
            term.element().appendChild(stabilityLabel(attributeDescription.stability()).compact()
                    .style("align-self", "baseline")
                    .css(util("ml-sm"), util("font-weight-normal"))
                    .element());
            // </unstable>
        }
        if (attributeDescription.deprecation().isDefined()) {
            term.containerDelegate().classList.add(halModifier(deprecated));
        }
        return term;
    }

    static DescriptionListTerm compositeLabel(PipelineContext context,
            AttributeDescription first, AttributeDescription second) {
        // <unstable>
        // If the internal DOM of DescriptionListTerm changes, this will no longer work.
        // By default, DescriptionListTerm supports only one text element.
        // But for parent/child and sibling-pairs we need two attribute descriptions.
        String firstLabel = sentenceCase(first.name());
        String secondLabel = sentenceCase(second.name());
        DescriptionListTerm term = descriptionListTerm(firstLabel)
                .css(halComponent(resource, HalClasses.compositeLabel))
                .help(attributeDescriptionPopover(firstLabel, first, all));
        HTMLElement secondTextElement = span()
                .css(component(descriptionList, text), modifier(helpText))
                .attr(role, Roles.button)
                .attr("type", "button")
                .apply(element -> element.tabIndex = 0)
                .text(secondLabel)
                .element();
        secondTextElement.appendChild(attributeDescriptionPopover(secondLabel, second, all)
                .trigger(secondTextElement)
                .element());
        if (second.deprecation().isDefined()) {
            secondTextElement.classList.add(halModifier(deprecated));
        }
        wrapHtmlContainer(term.element())
                .add(slashSeparator())
                .add(secondTextElement);
        // </unstable>
        if (first.deprecation().isDefined()) {
            term.containerDelegate().classList.add(halModifier(deprecated));
        }
        return term;
    }

    // ------------------------------------------------------ value

    static HTMLElement fileValue(PipelineContext context, ResolvedAttribute path, ResolvedAttribute relativeTo) {
        if (path.isDefined() && relativeTo.isDefined()) {
            return flex().alignItems(center).gap(sm)
                    .addItem(flexItem().add(Pipeline.instance().viewItem(context, path).valueElement()))
                    .addItem(flexItem().style("color", globalTextColorPlaceholder.var).text("relative to"))
                    .addItem(flexItem().add(Pipeline.instance().viewItem(context, relativeTo).valueElement()))
                    .element();
        } else if (path.isDefined()) {
            return Pipeline.instance().viewItem(context, path).valueElement();
        } else {
            return ViewItemBricks.undefinedValue();
        }
    }

    static HTMLElement valueElement(PipelineContext context, ResolvedAttribute attribute, DefinedValue definedValue) {
        if (!attribute.readable()) {
            return restrictedValue();
        } else if (attribute.expression()) {
            return expressionValue(attribute);
        } else if (attribute.value().isDefined()) {
            return definedValue.element(context, attribute);
        } else {
            return undefinedValue();
        }
    }

    static HTMLElement expressionValue(ResolvedAttribute attribute) {
        HTMLElement resolveButton = button().plain().inline().icon(resolveExpressionIcon().get())
                .onClick((e, b) -> uic().notifications().send(nyi()))
                .element();
        return span()
                .add(renderExpression(attribute.value().asString()))
                .add(resolveButton)
                .add(tooltip(resolveButton, "Resolve expression"))
                .element();
    }

    static HTMLElement plainText(ResolvedAttribute attribute) {
        return span()
                .text(attribute.value().asString())
                .element();
    }

    static HTMLElement restrictedValue() {
        return span().css(halModifier(restricted))
                .text("restricted")
                .add(icon(lock().css(util("ml-sm"))))
                .element();
    }

    static HTMLElement undefinedValue() {
        return span()
                .css(halComponent(resource, view, undefined))
                .text("undefined")
                .element();
    }
}

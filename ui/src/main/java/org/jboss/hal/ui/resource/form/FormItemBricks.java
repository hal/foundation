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
package org.jboss.hal.ui.resource.form;

import org.jboss.elemento.Elements;
import org.jboss.elemento.HTMLInputElementBuilder;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.component.form.FormSelect;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.help.HelperText;
import org.patternfly.component.inputgroup.InputGroupText;
import org.patternfly.component.menu.SingleTypeahead;
import org.patternfly.core.Aria;
import org.patternfly.core.Roles;
import org.patternfly.style.Classes;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;

import static org.jboss.elemento.Elements.insertFirst;
import static org.jboss.elemento.Elements.small;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.core.Humanize.sentenceCase;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.resources.HalClasses.deprecated;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.resources.HalClasses.stabilityLevel;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.brick.AttributeBricks.attributeDescriptionPopover;
import static org.jboss.hal.ui.brick.AttributeBricks.slashSeparator;
import static org.jboss.hal.ui.brick.DescriptionBricks.AttributeDescriptionContent.all;
import static org.jboss.hal.ui.brick.ExpressionBricks.resolveExpressionIcon;
import static org.jboss.hal.ui.resource.pipeline.PipelineFlags.Placeholder.DEFAULT_VALUE;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.FormGroupLabel.formGroupLabel;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.help.HelperText.helperText;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.inputgroup.InputGroupText.inputGroupText;
import static org.patternfly.core.Attributes.role;
import static org.patternfly.core.Attributes.tabindex;
import static org.patternfly.core.Attributes.type;
import static org.patternfly.icon.IconSets.fas.circleQuestion;
import static org.patternfly.icon.IconSets.fas.lock;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.form;
import static org.patternfly.style.Classes.group;
import static org.patternfly.style.Classes.help;
import static org.patternfly.style.Classes.icon;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.noPadding;
import static org.patternfly.style.Classes.plain;
import static org.patternfly.style.Classes.text;
import static org.patternfly.style.Classes.util;

/**
 * Reusable UI fragments ("bricks") for composing form items. Following the brick pattern used throughout the console (see
 * {@link org.jboss.hal.ui.brick}), this is a {@code final} utility class with only {@code static} factory methods.
 * <p>
 * Provides shared building blocks for {@link EditableControl}, {@link StandardFormItem}, and {@link NativeControl}
 * implementations: labels with description popovers and stability badges, read-only controls with expression resolve buttons,
 * placeholder application, and validation helper text.
 *
 * @see EditableControl
 * @see StandardFormItem
 * @see org.jboss.hal.ui.brick
 */
final class FormItemBricks {

    // ------------------------------------------------------ label

    /** Creates a {@link FormGroupLabel} with description popover, stability badge, deprecation styling, and nested support. */
    static FormGroupLabel label(PipelineContext context, String identifier, AttributeDescription attributeDescription) {
        FormGroupLabel fgl;

        if (attributeDescription.nested()) {
            AttributeDescription parentDescription = attributeDescription.parent();
            String parentLabel = sentenceCase(parentDescription.name());
            String nestedLabel = sentenceCase(attributeDescription.name());
            fgl = formGroupLabel(nestedLabel)
                    .css(halComponent(resource, HalClasses.compositeLabel))
                    .help(nestedLabel + " description", attributeDescriptionPopover(nestedLabel, attributeDescription, all));

            HTMLElement parentLabelElement = labelElement(identifier, parentLabel);
            HTMLElement parentHelpButton = helpElement(parentLabel);
            insertFirst(fgl.element(), slashSeparator());
            insertFirst(fgl.element(), parentHelpButton);
            insertFirst(fgl.element(), parentLabelElement);
            fgl.add(attributeDescriptionPopover(parentLabel, parentDescription, all)
                    .trigger(parentHelpButton));
        } else {
            String lbl = sentenceCase(attributeDescription.name());
            fgl = formGroupLabel(lbl)
                    .help(lbl + " description", attributeDescriptionPopover(lbl, attributeDescription, all));

            if (uic().environment()
                    .highlightStability(context.resourceDescription().stability(), attributeDescription.stability())) {
                fgl.css(halComponent(resource, stabilityLevel))
                        .add(stabilityLabel(attributeDescription.stability()).compact()
                                .style("align-self", "baseline")
                                .css(util("ml-sm"), util("font-weight-normal"))
                                .element());
            }
        }

        if (attributeDescription.deprecation().isDefined()) {
            fgl.classList().add(halModifier(deprecated));
        }
        return fgl;
    }

    /**
     * Creates a {@link FormGroupLabel} for sibling attribute pairs. Shows both attribute names separated by a slash, each with
     * its own description popover. Analogous to {@code org.jboss.hal.ui.resource.view.ViewItemBricks.compositeLabel()}.
     */
    static FormGroupLabel compositeLabel(PipelineContext context, String identifier,
            AttributeDescription first, AttributeDescription second) {
        String firstLabel = sentenceCase(first.name());
        String secondLabel = sentenceCase(second.name());
        FormGroupLabel fgl = formGroupLabel(firstLabel)
                .css(halComponent(resource, HalClasses.compositeLabel))
                .help(firstLabel + " description", attributeDescriptionPopover(firstLabel, first, all));

        HTMLElement secondLabelElement = labelElement(identifier, secondLabel);
        HTMLElement secondHelpButton = helpElement(secondLabel);
        fgl.element().appendChild(slashSeparator());
        fgl.element().appendChild(secondLabelElement);
        fgl.element().appendChild(secondHelpButton);
        fgl.add(attributeDescriptionPopover(secondLabel, second, all)
                .trigger(secondHelpButton));

        if (first.deprecation().isDefined()) {
            fgl.classList().add(halModifier(deprecated));
        }
        if (second.deprecation().isDefined()) {
            secondLabelElement.classList.add(halModifier(deprecated));
        }
        return fgl;
    }

    private static HTMLElement labelElement(String identifier, String label) {
        return Elements.label().css(Classes.component(form, Classes.label))
                .apply(l -> l.htmlFor = identifier)
                .add(Elements.span().css(Classes.component(form, Classes.label, text))
                        .text(label))
                .element();
    }

    private static HTMLElement helpElement(String label) {
        return span().css(component(form, group, Classes.label, help), util("ml-xs"))
                .add(span().css(component(Classes.button), modifier(plain), modifier(noPadding))
                        .attr(type, "button")
                        .attr(role, Roles.button)
                        .attr(tabindex, 0)
                        .aria(Aria.label, label + " description")
                        .add(span().css(component(Classes.button, icon))
                                .add(circleQuestion())))
                .element();
    }

    // ------------------------------------------------------ read only

    /** Creates a read-only {@link FormGroupControl} with optional expression resolve button. */
    static FormGroupControl readOnlyGroup(String identifier, ResolvedAttribute attribute, PipelineFlags flags) {
        TextInput tc = readOnlyTextControl(identifier, attribute, flags);
        if (attribute.expression()) {
            String resolveId = identifier + "-resolve-expression";
            return formGroupControl()
                    .addInputGroup(inputGroup()
                            .addItem(inputGroupItem().fill().addControl(tc))
                            .addItem(inputGroupItem().addButton(
                                    button().id(resolveId).control().icon(resolveExpressionIcon().get()))));
        } else {
            return formGroupControl().addControl(tc);
        }
    }

    /** Creates a read-only {@link TextInput} with lock icon, populated from the attribute value. */
    static TextInput readOnlyTextControl(String identifier, ResolvedAttribute attribute, PipelineFlags flags) {
        return textInput(identifier)
                .run(ti -> {
                    ti.input().autocomplete("off");
                    if (attribute.value().isDefined()) {
                        ti.value(attribute.value().asString());
                    }
                    applyPlaceholder(ti.input(), attribute, flags);
                })
                .readonly().icon(lock());
    }

    // ------------------------------------------------------ required

    /** Creates a "required" error helper text for the given attribute. */
    static HelperText requiredHelperText(ResolvedAttribute attribute) {
        return helperText(sentenceCase(attribute.name()) + " is a required attribute.", error);
    }

    /** Returns {@code true} if the attribute is required and has no ALTERNATIVES or REQUIRES relationships. */
    static boolean requiredOnItsOwn(ResolvedAttribute attribute) {
        return attribute.description().required()
                && !(attribute.description().hasDefined("alternatives") || attribute.description().hasDefined("requires"));
    }

    // ------------------------------------------------------ value

    /** Selects a value in a {@link FormSelect}, falling back to the first value if the value is not available. */
    static void failSafeSelectValue(FormSelect formSelect, String value) {
        if (formSelect.containsValue(value)) {
            formSelect.value(value, false);
        } else {
            formSelect.selectFirstValue(false);
        }
    }

    /** Selects a value in a {@link SingleTypeahead}, handling async items that may not be loaded yet. */
    static void failSafeSelectValue(SingleTypeahead typeahead, String value) {
        if (typeahead.menu().hasAsyncItems()) {
            typeahead.menuToggle().text(value);
            typeahead.onLoaded((__, st) -> st.select(value));
        } else {
            typeahead.select(value);
        }
    }

    /** Returns the text input's value, or an empty string if the input is {@code null} or its value is {@code null}. */
    static String safeValue(TextInput input) {
        return input != null && input.value() != null ? input.value() : "";
    }

    // ------------------------------------------------------ misc

    /** Applies a placeholder (UNDEFINED or default value) to an input element based on pipeline flags. */
    static void applyPlaceholder(HTMLInputElementBuilder<HTMLInputElement> input, ResolvedAttribute attribute,
            PipelineFlags flags) {
        if (flags.placeholder() == PipelineFlags.Placeholder.UNDEFINED) {
            input.placeholder(UNDEFINED);
        } else if (flags.placeholder() == DEFAULT_VALUE) {
            if (attribute.description().hasDefault()) {
                input.placeholder(attribute.description().get(DEFAULT).asString());
            }
        }
    }

    /** Creates an expression-mode {@link TextInput} for entering WildFly expressions. */
    static TextInput expressionTextControl(String identifier, ResolvedAttribute attribute, PipelineFlags flags) {
        return textInput(identifier)
                .run(ti -> {
                    ti.input().autocomplete("off");
                    if (attribute.value().isDefined()) {
                        ti.value(attribute.value().asString());
                    }
                    applyPlaceholder(ti.input(), attribute, flags);
                });
    }

    /** Creates a unit text element for display in an InputGroup. */
    static InputGroupText unitText(String unit) {
        return inputGroupText().plain().add(small().text(unit));
    }
}

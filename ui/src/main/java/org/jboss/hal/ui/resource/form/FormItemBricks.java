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
import org.patternfly.component.form.TextInput;
import org.patternfly.component.help.HelperText;
import org.patternfly.component.inputgroup.InputGroupText;
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
import static org.patternfly.style.Classes.label;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.noPadding;
import static org.patternfly.style.Classes.plain;
import static org.patternfly.style.Classes.text;
import static org.patternfly.style.Classes.util;

/**
 * Reusable UI fragments ("bricks") for composing form items. Following the brick pattern used throughout the console (see
 * {@link org.jboss.hal.ui.brick}), this is a {@code final} utility class with only {@code static} factory methods.
 * <p>
 * Provides shared building blocks for {@link StandardFormItem} and {@link NativeControl} implementations: labels with
 * description popovers and stability badges, read-only controls with expression resolve buttons,
 * placeholder application, and validation helper text.
 *
 * @see StandardFormItem
 * @see org.jboss.hal.ui.brick
 */
public final class FormItemBricks {

    /** Creates a {@link FormGroupLabel} with description popover, stability badge, deprecation styling, and nested support. */
    public static FormGroupLabel label(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        AttributeDescription description = attribute.description();
        FormGroupLabel fgl;

        if (description.nested()) {
            AttributeDescription parentDescription = description.parent();
            String parentLabel = sentenceCase(parentDescription.name());
            String nestedLabel = sentenceCase(description.name());
            fgl = formGroupLabel(nestedLabel)
                    .css(halComponent(resource, HalClasses.compositeLabel))
                    .help(nestedLabel + " description", attributeDescriptionPopover(nestedLabel, description, all));
            HTMLElement parentLabelElement = Elements.label().css(component(form, label))
                    .apply(l -> l.htmlFor = identifier)
                    .add(span().css(component(form, label, text))
                            .text(parentLabel))
                    .element();
            HTMLElement parentHelpButton = span().css(component(form, group, Classes.label, help), util("ml-xs"))
                    .add(span().css(component(Classes.button), modifier(plain), modifier(noPadding))
                            .attr(type, "button")
                            .attr(role, Roles.button)
                            .attr(tabindex, 0)
                            .aria(Aria.label, parentLabel + " description")
                            .add(span().css(component(Classes.button, icon))
                                    .add(circleQuestion())))
                    .element();
            insertFirst(fgl.element(), slashSeparator());
            insertFirst(fgl.element(), parentHelpButton);
            insertFirst(fgl.element(), parentLabelElement);
            fgl.add(attributeDescriptionPopover(parentLabel, parentDescription, all)
                    .trigger(parentHelpButton));
        } else {
            String lbl = sentenceCase(attribute.name());
            fgl = formGroupLabel(lbl)
                    .help(lbl + " description", attributeDescriptionPopover(lbl, description, all));

            if (uic().environment()
                    .highlightStability(context.resourceDescription().stability(), description.stability())) {
                fgl.css(halComponent(resource, stabilityLevel))
                        .add(stabilityLabel(description.stability()).compact()
                                .style("align-self", "baseline")
                                .css(util("ml-sm"), util("font-weight-normal"))
                                .element());
            }
        }

        if (description.deprecation().isDefined()) {
            fgl.classList().add(halModifier(deprecated));
        }
        return fgl;
    }

    /** Creates a read-only {@link TextInput} with lock icon, populated from the attribute value. */
    public static TextInput readOnlyTextControl(String identifier, ResolvedAttribute attribute, PipelineFlags flags) {
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

    /** Creates a read-only {@link FormGroupControl} with optional expression resolve button. */
    public static FormGroupControl readOnlyGroup(String identifier, ResolvedAttribute attribute, PipelineFlags flags) {
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

    /** Creates an expression-mode {@link TextInput} for entering WildFly expressions. */
    public static TextInput expressionTextControl(String identifier, ResolvedAttribute attribute, PipelineFlags flags) {
        return textInput(identifier)
                .run(ti -> {
                    ti.input().autocomplete("off");
                    if (attribute.value().isDefined()) {
                        ti.value(attribute.value().asString());
                    }
                    applyPlaceholder(ti.input(), attribute, flags);
                });
    }

    /** Applies a placeholder (UNDEFINED or default value) to an input element based on pipeline flags. */
    public static void applyPlaceholder(HTMLInputElementBuilder<HTMLInputElement> input, ResolvedAttribute attribute,
            PipelineFlags flags) {
        if (flags.placeholder() == PipelineFlags.Placeholder.UNDEFINED) {
            input.placeholder(UNDEFINED);
        } else if (flags.placeholder() == DEFAULT_VALUE) {
            if (attribute.description().hasDefault()) {
                input.placeholder(attribute.description().get(DEFAULT).asString());
            }
        }
    }

    /** Creates a unit text element for display in an InputGroup. */
    public static InputGroupText unitText(String unit) {
        return inputGroupText().plain().add(small().text(unit));
    }

    /** Creates a "required" error helper text for the given attribute. */
    public static HelperText requiredHelperText(ResolvedAttribute attribute) {
        return helperText(sentenceCase(attribute.name()) + " is a required attribute.", error);
    }

    /** Returns {@code true} if the attribute is required and has no ALTERNATIVES or REQUIRES relationships. */
    public static boolean requiredOnItsOwn(ResolvedAttribute attribute) {
        return attribute.description().required()
                && !(attribute.description().hasDefined("alternatives") || attribute.description().hasDefined("requires"));
    }

    private FormItemBricks() {
    }
}

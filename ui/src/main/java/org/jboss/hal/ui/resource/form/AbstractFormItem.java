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

import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags;
import org.jboss.hal.ui.resource.ResolvedAttribute;

import java.util.Collections;
import java.util.List;

import org.jboss.elemento.By;
import org.jboss.elemento.Elements;
import org.jboss.elemento.HTMLInputElementBuilder;
import org.jboss.elemento.Id;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.resources.HalClasses;
import org.patternfly.component.button.Button;
import org.patternfly.component.form.FormGroup;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.help.HelperText;
import org.patternfly.component.inputgroup.InputGroupText;
import org.patternfly.core.Aria;
import org.patternfly.core.Roles;
import org.patternfly.style.Classes;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;

import static java.util.Collections.singletonList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.elemento.Elements.insertFirst;
import static org.jboss.elemento.Elements.small;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.core.Humanize.sentenceCase;
import static org.jboss.hal.core.Notification.nyi;
import static org.jboss.hal.dmr.Expression.containsExpression;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALTERNATIVES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.resources.HalClasses.deprecated;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.resources.HalClasses.stabilityLevel;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.brick.AttributeBricks.attributeDescriptionPopover;
import static org.jboss.hal.ui.brick.AttributeBricks.nestedElementSeparator;
import static org.jboss.hal.ui.brick.DescriptionBricks.AttributeDescriptionContent.all;
import static org.jboss.hal.ui.brick.ExpressionBricks.expressionModeIcon;
import static org.jboss.hal.ui.brick.ExpressionBricks.normalModeIcon;
import static org.jboss.hal.ui.brick.ExpressionBricks.resolveExpressionIcon;
import static org.jboss.hal.ui.resource.form.InputMode.EXPRESSION;
import static org.jboss.hal.ui.resource.form.InputMode.MIXED;
import static org.jboss.hal.ui.resource.form.InputMode.NATIVE;
import static org.jboss.hal.ui.resource.pipeline.PipelineFlags.Placeholder.DEFAULT_VALUE;
import static org.jboss.hal.ui.resource.pipeline.PipelineFlags.Scope.EXISTING_RESOURCE;
import static org.jboss.hal.ui.resource.pipeline.PipelineFlags.Scope.NEW_RESOURCE;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.FormGroupLabel.formGroupLabel;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.help.HelperText.helperText;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.inputgroup.InputGroupText.inputGroupText;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
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
 * Base class for all pipeline form items. Self-contained: receives a {@link ResolvedAttribute} and builds the complete
 * {@code FormGroup} (label + control) internally. Provides expression mode switching, modification tracking, and
 * standard DMR operation generation.
 * <p>
 * Subclasses implement {@link #isNativeModifiedForNew()}, {@link #isNativeModifiedForExisting(boolean)}, and
 * {@link #modelNode()} for type-specific behavior. They may also override {@link #readOnlyGroup()},
 * {@link #nativeGroup()}, and {@link #nativeContainer()} to customize the form group control.
 */
public abstract class AbstractFormItem implements FormItem {

    private static final Logger logger = Logger.getLogger(AbstractFormItem.class.getName());

    final String identifier;
    final ResolvedAttribute attribute;
    final PipelineContext context;
    final PipelineFlags flags;
    final String switchToExpressionModeId;
    final String resolveExpressionId;
    final String switchToNativeModeId;

    InputMode inputMode;
    FormGroup formGroup;
    FormGroupControl formGroupControl;
    TextInput textControl;
    HTMLElement expressionContainer;
    HTMLElement nativeContainer;
    private HTMLDivElement switchToNativeModeTooltip;
    private HTMLDivElement resolveExpressionTooltip;
    private HTMLElement switchToExpressionModeTooltip;

    protected AbstractFormItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        this.identifier = identifier;
        this.attribute = attribute;
        this.context = context;
        this.flags = context.flags();
        this.switchToExpressionModeId = Id.build(identifier, "switch-to-expression-mode");
        this.switchToNativeModeId = Id.build(identifier, "switch-to-native-mode");
        this.resolveExpressionId = Id.build(identifier, "resolve-expression");
        this.inputMode = NATIVE;
    }

    // ------------------------------------------------------ label

    FormGroupLabel label() {
        AttributeDescription description = attribute.description();
        FormGroupLabel fgl;

        if (description.nested()) {
            // <unstable>
            AttributeDescription parentDescription = description.parent();
            String parentLabel = sentenceCase(parentDescription.name());
            String nestedLabel = sentenceCase(description.name());
            fgl = formGroupLabel(nestedLabel)
                    .css(halComponent(resource, HalClasses.nestedLabel))
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
            insertFirst(fgl.element(), nestedElementSeparator());
            insertFirst(fgl.element(), parentHelpButton);
            insertFirst(fgl.element(), parentLabelElement);
            fgl.add(attributeDescriptionPopover(parentLabel, parentDescription, all)
                    .trigger(parentHelpButton));
            // </unstable>
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

    // ------------------------------------------------------ setup

    void defaultSetup() {
        if (attribute.description().readOnly()) {
            formGroupControl = readOnlyGroup();
        } else {
            if (attribute.description().expressionAllowed()) {
                expressionContainer();
                nativeContainer();
                formGroupControl = formGroupControl();
                if (attribute.expression()) {
                    switchToExpressionMode();
                } else {
                    switchToNativeMode();
                }
            } else {
                formGroupControl = nativeGroup();
            }
        }
        formGroup = formGroup(identifier)
                .required(attribute.description().required())
                .addLabel(label())
                .addControl(formGroupControl);
    }

    FormGroupControl readOnlyGroup() {
        return formGroupControl();
    }

    FormGroupControl nativeGroup() {
        return formGroupControl();
    }

    HTMLElement nativeContainer() {
        if (nativeContainer == null) {
            nativeContainer = div().element();
        }
        return nativeContainer;
    }

    HTMLElement expressionContainer() {
        if (expressionContainer == null) {
            expressionContainer = inputGroup()
                    .addItem(inputGroupItem().addButton(switchToNormalModeButton()))
                    .addItem(inputGroupItem().fill().addControl(textControl()))
                    .addItem(inputGroupItem().addButton(resolveExpressionButton()))
                    .run(ig -> {
                        if (attribute.description().unit() != null) {
                            ig.addText(unitInputGroupText());
                        }
                    })
                    .element();
        }
        return expressionContainer;
    }

    // ------------------------------------------------------ controls

    public final TextInput textControl() {
        if (textControl == null) {
            textControl = textInput(identifier)
                    .run(ti -> {
                        ti.input().autocomplete("off");
                        if (attribute.value().isDefined()) {
                            ti.value(attribute.value().asString());
                        }
                        applyPlaceholder(ti.input());
                    });
        }
        return textControl;
    }

    final TextInput readOnlyTextControl() {
        return textControl().readonly().icon(lock());
    }

    final FormGroupControl readOnlyGroupWithExpressionSwitch() {
        TextInput tc = readOnlyTextControl();
        if (attribute.expression()) {
            return formGroupControl()
                    .addInputGroup(inputGroup()
                            .addItem(inputGroupItem().fill().addControl(tc))
                            .addItem(inputGroupItem().addButton(resolveExpressionButton())));
        } else {
            return formGroupControl().addControl(tc);
        }
    }

    final InputGroupText unitInputGroupText() {
        return inputGroupText().plain().add(small().text(attribute.description().unit()));
    }

    Button switchToExpressionModeButton() {
        return button().id(switchToExpressionModeId).control().icon(expressionModeIcon().get())
                .onClick((e, b) -> switchToExpressionMode());
    }

    Button switchToNormalModeButton() {
        return button().id(switchToNativeModeId).control().icon(normalModeIcon().get())
                .onClick((e, b) -> switchToNativeMode());
    }

    Button resolveExpressionButton() {
        return button().id(resolveExpressionId).control().icon(resolveExpressionIcon().get())
                .onClick((e, b) -> {
                    if (textControl != null) {
                        uic().notifications().send(nyi());
                        logger.info("Resolve expression: %s", textControl.value());
                    }
                });
    }

    void applyPlaceholder(HTMLInputElementBuilder<HTMLInputElement> textInput) {
        if (flags.placeholder() == PipelineFlags.Placeholder.UNDEFINED) {
            textInput.placeholder(UNDEFINED);
        } else if (flags.placeholder() == DEFAULT_VALUE) {
            if (attribute.description().hasDefault()) {
                textInput.placeholder(attribute.description().get(DEFAULT).asString());
            }
        }
    }

    // ------------------------------------------------------ expression toggle

    final void switchToExpressionMode() {
        resetValidation();
        failSafeRemoveFromParent(nativeContainer);
        failSafeRemoveFromParent(switchToNativeModeTooltip);
        failSafeRemoveFromParent(resolveExpressionTooltip);

        switchToNativeModeTooltip = tooltip(By.id(switchToNativeModeId), "Switch to native mode").element();
        resolveExpressionTooltip = tooltip(By.id(resolveExpressionId), "Resolve expression").element();

        formGroupControl.add(expressionContainer = expressionContainer());
        expressionContainer.appendChild(switchToNativeModeTooltip);
        expressionContainer.appendChild(resolveExpressionTooltip);

        inputMode = EXPRESSION;
        afterSwitchedToExpressionMode();
    }

    void afterSwitchedToExpressionMode() {
        if (textControl != null) {
            textControl.input().element().focus();
        }
    }

    final void switchToNativeMode() {
        resetValidation();
        failSafeRemoveFromParent(expressionContainer);
        failSafeRemoveFromParent(switchToExpressionModeTooltip);

        switchToExpressionModeTooltip = tooltip(By.id(switchToExpressionModeId), "Switch to expression mode").element();
        formGroupControl.add(nativeContainer = nativeContainer());
        nativeContainer.appendChild(switchToExpressionModeTooltip);

        inputMode = NATIVE;
        afterSwitchedToNativeMode();
    }

    void afterSwitchedToNativeMode() {
        // empty — subclasses may override
    }

    // ------------------------------------------------------ validation

    @Override
    public void resetValidation() {
        if (textControl != null) {
            textControl.resetValidation();
        }
        if (formGroupControl != null) {
            formGroupControl.removeHelperText();
        }
    }

    @Override
    public boolean validate() {
        return true;
    }

    boolean emptyTextControl() {
        return textControlValue().isEmpty();
    }

    String textControlValue() {
        return textControl != null ? textControl.value() : "";
    }

    boolean requiredOnItsOwn() {
        return attribute.description().required()
                && !(attribute.description().hasDefined(ALTERNATIVES) || attribute.description().hasDefined(REQUIRES));
    }

    boolean supportsExpression() {
        return textControl != null && !attribute.description().readOnly() && attribute.description().expressionAllowed();
    }

    boolean validateExpressionMode() {
        if (requiredOnItsOwn() && emptyTextControl()) {
            textControl.validated(error);
            formGroupControl.addHelperText(requiredHelperText());
            return false;
        } else {
            if (supportsExpression() && inputMode == EXPRESSION && !containsExpression(textControl.value())) {
                textControl.validated(error);
                formGroupControl.addHelperText(
                        helperText("The value is not a valid expression.", error));
                return false;
            }
        }
        return true;
    }

    HelperText requiredHelperText() {
        return helperText(sentenceCase(attribute.name()) + " is a required attribute.", error);
    }

    // ------------------------------------------------------ modification tracking

    @Override
    public boolean isModified() {
        if (attribute.readable() && !attribute.description().readOnly()) {
            if (flags.scope() == NEW_RESOURCE) {
                if (inputMode == NATIVE) {
                    return isNativeModifiedForNew();
                } else if (inputMode == EXPRESSION || inputMode == MIXED) {
                    return isExpressionModified();
                }
            } else if (flags.scope() == EXISTING_RESOURCE) {
                if (inputMode == NATIVE) {
                    return isNativeModifiedForExisting(attribute.value().isDefined());
                } else if (inputMode == EXPRESSION || inputMode == MIXED) {
                    return isExpressionModified();
                }
            }
        }
        return false;
    }

    abstract boolean isNativeModifiedForNew();

    abstract boolean isNativeModifiedForExisting(boolean wasDefined);

    boolean isExpressionModified() {
        if (flags.scope() == NEW_RESOURCE) {
            return !textControlValue().isEmpty();
        } else if (flags.scope() == EXISTING_RESOURCE) {
            if (attribute.value().isDefined()) {
                return !attribute.value().asString().equals(textControlValue());
            } else {
                return !textControlValue().isEmpty();
            }
        }
        return false;
    }

    // ------------------------------------------------------ data

    @Override
    public abstract ModelNode modelNode();

    ModelNode expressionModelNode() {
        return new ModelNode().setExpression(textControlValue());
    }

    // ------------------------------------------------------ operations

    @Override
    public List<Operation> operations(ResourceAddress address) {
        if (!isModified()) {
            return Collections.emptyList();
        }
        ModelNode currentValue = modelNode();
        Operation operation;
        if (currentValue.isDefined()) {
            operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                    .param(NAME, attribute.fqn())
                    .param(VALUE, currentValue)
                    .build();
        } else {
            operation = new Operation.Builder(address, UNDEFINE_ATTRIBUTE_OPERATION)
                    .param(NAME, attribute.fqn())
                    .build();
        }
        return singletonList(operation);
    }

    // ------------------------------------------------------ FormItem

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public ResolvedAttribute attribute() {
        return attribute;
    }

    @Override
    public HTMLElement element() {
        if (formGroup == null) {
            logger.error("Element for form item %s has not been initialized!", identifier);
            return span().element();
        }
        return formGroup.element();
    }
}

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
package org.jboss.hal.ui.resource;

import java.util.HashMap;
import java.util.Map;

import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.TypedBuilder;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.ui.BuildingBlocks;
import org.jboss.hal.ui.resource.FormItemFlags.Placeholder;
import org.patternfly.component.HasIdentifier;
import org.patternfly.component.ValidationStatus;
import org.patternfly.component.button.Button;
import org.patternfly.component.form.FormGroup;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.help.HelperText;
import org.patternfly.component.inputgroup.InputGroupText;
import org.patternfly.core.ComponentContext;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.elemento.Elements.small;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.core.Notification.nyi;
import static org.jboss.hal.dmr.Expression.containsExpression;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALTERNATIVES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.ui.BuildingBlocks.resolveExpression;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.resource.FormItemFlags.Scope.EXISTING_RESOURCE;
import static org.jboss.hal.ui.resource.FormItemFlags.Scope.NEW_RESOURCE;
import static org.jboss.hal.ui.resource.FormItemInputMode.EXPRESSION;
import static org.jboss.hal.ui.resource.FormItemInputMode.NATIVE;
import static org.jboss.hal.ui.resource.HelperTexts.noExpression;
import static org.jboss.hal.ui.resource.HelperTexts.required;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.inputgroup.InputGroupText.inputGroupText;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.icon.IconSets.fas.dollarSign;

/**
 * An item for a {@link ResourceForm} based on a {@link FormGroup}
 * <p>
 * Do <strong>not</strong> register event handlers for form controls here. Event handlers must only be registered in
 * subclasses!
 */
abstract class FormItem implements
        ComponentContext<HTMLElement, FormItem>,
        HasIdentifier<HTMLElement, FormItem>,
        IsElement<HTMLElement>,
        ResourceItem<FormItem>,
        TypedBuilder<HTMLElement, FormItem> {

    private static final Logger logger = Logger.getLogger(FormItem.class.getName());

    final String identifier;
    final ResourceAttribute ra;
    final FormItemFlags flags;
    final String switchToExpressionModeId;
    final String resolveExpressionId;
    private final String switchToNativeModeId;
    private final Map<String, Object> data;
    private final FormGroupLabel label;

    FormItemInputMode inputMode;
    FormGroup formGroup;
    FormGroupControl formGroupControl;
    TextInput textControl;
    HTMLElement expressionContainer;
    HTMLElement nativeContainer;

    FormItem(String identifier, ResourceAttribute ra, FormGroupLabel label, FormItemFlags flags) {
        this.identifier = identifier;
        this.ra = ra;
        this.label = label;
        this.flags = flags;
        this.switchToExpressionModeId = Id.build(identifier, "switch-to-expression-mode");
        this.switchToNativeModeId = Id.build(identifier, "switch-to-native-mode");
        this.resolveExpressionId = Id.build(identifier, "resolve-expression");
        this.data = new HashMap<>();
        this.inputMode = NATIVE;
    }

    @Override
    public HTMLElement element() {
        if (formGroup == null) {
            logger.error("Element for form item %s has not been initialized!", identifier);
            return span().element();
        }
        return formGroup.element();
    }

    @Override
    public FormItem that() {
        return this;
    }

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public <T> FormItem store(String key, T value) {
        data.put(key, value);
        return this;
    }

    @Override
    public boolean has(String key) {
        return data.containsKey(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (data.containsKey(key)) {
            return (T) data.get(key);
        }
        return null;
    }

    // ------------------------------------------------------ validation

    /**
     * Should be overridden in subclasses if {@link #validate()} is overridden and custom state must be reset.
     * <p>
     * This method resets the validation of the text control (if not null) and removes the helper texts from the form group
     * control.
     * <p>
     * Please call the super method when overriding!
     */
    void resetValidation() {
        if (textControl != null) {
            textControl.resetValidation();
        }
        if (formGroupControl != null) {
            formGroupControl.removeHelperText();
        }
    }

    /**
     * Should be overridden in subclasses if the form item needs to be validated.
     * <p>
     * In case of an error, use {@link TextInput#validated(ValidationStatus)},
     * {@link FormGroupControl#addHelperText(HelperText)}, or one of the methods in {@link HelperTexts} to let the user know
     * what went wrong. Override {@link #resetValidation()} to remove any status and helper texts.
     *
     * @return true if the form item is valid, false otherwise.
     */
    boolean validate() {
        return true;
    }

    boolean emptyTextControl() {
        return textControlValue().isEmpty();
    }

    /**
     * Verifies that this form item is not required on its own. On its own means that the form item is not mutually exclusive to
     * or required by other required form items.
     * <p>
     * These relations between form items are meant to be validated in the form rather than in the form item.
     */
    boolean requiredOnItsOwn() {
        return ra.description.required() && !(ra.description.hasDefined(ALTERNATIVES) || ra.description.hasDefined(REQUIRES));
    }

    /**
     * Verifies that the form item is not read-only and supports expressions.
     */
    boolean supportsExpression() {
        return textControl != null && !ra.description.readOnly() && ra.description.expressionAllowed();
    }

    /**
     * Verifies that the current value contains an expression when {@link #inputMode} is {@link FormItemInputMode#EXPRESSION}.
     */
    boolean noExpressionInExpressionMode() {
        if (supportsExpression() && inputMode == EXPRESSION) {
            return !containsExpression(textControl.value());
        }
        return false;
    }

    /**
     * Helper method meant to be used in an overridden {@link #validate()} method, when {@link #inputMode} is
     * {@link FormItemInputMode#EXPRESSION}.
     * <p>
     * Fails if the form item is {@link #requiredOnItsOwn()} and {@link #emptyTextControl()} or
     * {@link #noExpressionInExpressionMode()}.
     */
    boolean validateExpressionMode() {
        if (requiredOnItsOwn() && emptyTextControl()) {
            textControl.validated(error);
            formGroupControl.addHelperText(required(ra));
        } else {
            if (noExpressionInExpressionMode()) {
                textControl.validated(error);
                formGroupControl.addHelperText(noExpression());
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------ data

    /**
     * Checks if the form item has been modified.
     */
    abstract boolean isModified();

    boolean isExpressionModified() {
        if (flags.scope == NEW_RESOURCE) {
            return !textControlValue().isEmpty();
        } else if (flags.scope == EXISTING_RESOURCE) {
            String originalValue = ra.value.asString();
            boolean wasDefined = ra.value.isDefined();
            if (wasDefined) {
                return !originalValue.equals(textControlValue());
            } else {
                return !textControlValue().isEmpty();
            }
        } else {
            unknownScope();
        }
        return false;
    }

    void unknownScope() {
        logger.error("Unknown scope '%s' in form item %s: %o", flags.scope, identifier, this.element());
    }

    /**
     * Returns the value of this form item as a model node.
     */
    abstract ModelNode modelNode();

    ModelNode expressionModelNode() {
        return new ModelNode().setExpression(textControlValue());
    }

    String textControlValue() {
        return textControl != null ? textControl.value() : "";
    }

    // ------------------------------------------------------ building blocks

    /**
     * Sets up the default DOM tree based on the resource attribute's description.
     * <p>
     * The method performs the following actions:
     * <ol>
     * <li>Checks if the description is read-only.</li>
     * <li>If read-only, it sets the form group control to a read-only group by calling {@link #readOnlyGroup()}.</li>
     * <li>If not read-only, it checks if expressions are allowed.</li>
     * <li>If expressions are allowed, it sets up both expression and native modes by calling {@link #expressionContainer()} and {@link #nativeContainer()} and assigns the relevant form group control. It then switches to the appropriate mode based on  the current expression state.</li>
     * <li>If expressions are not allowed, it sets the form group control to a native group by calling {@link #nativeGroup()}.</li>
     * <li>Finally, it creates and configures a form group with the specified identifier, required status, label, and form group control.</li>
     * </ol>
     */
    void defaultSetup() {
        if (ra.description.readOnly()) {
            formGroupControl = readOnlyGroup();
        } else {
            if (ra.description.expressionAllowed()) {
                expressionContainer = expressionContainer();
                nativeContainer = nativeContainer();
                formGroupControl = formGroupControl();
                if (ra.expression) {
                    switchToExpressionMode();
                } else {
                    switchToNativeMode();
                }
            } else {
                formGroupControl = nativeGroup();
            }
        }
        formGroup = formGroup(identifier)
                .required(ra.description.required())
                .addLabel(label)
                .addControl(formGroupControl);
    }

    /**
     * Creates and returns the form group control if the form item is read-only. Must be overridden in subclasses if
     * {@link #defaultSetup()} is used.
     */
    FormGroupControl readOnlyGroup() {
        return formGroupControl();
    }

    /**
     * Creates and returns the form group control for the native mode. Must be overridden in subclasses if
     * {@link #defaultSetup()} is used.
     */
    FormGroupControl nativeGroup() {
        return formGroupControl();
    }

    /**
     * Creates and returns the container for the native form controls and a button to switch to the expression mode. Must be
     * overridden in subclasses if {@link #defaultSetup()} is used.
     */
    HTMLElement nativeContainer() {
        return div().element();
    }

    /**
     * Creates and returns the container for the expression form controls and a button to switch to the native mode. May be
     * overridden in subclasses
     */
    HTMLElement expressionContainer() {
        return inputGroup()
                .addItem(inputGroupItem().addButton(switchToNormalModeButton()))
                .addItem(inputGroupItem().fill().addControl(textControl()))
                .addItem(inputGroupItem().addButton(resolveExpressionButton()))
                .run(ig -> {
                    if (ra.description.unit() != null) {
                        ig.addText(unitInputGroupText());
                    }
                })
                .element();
    }

    /**
     * Creates and returns a text control mostly used for the expression mode. Should not be overridden in subclasses unless
     * necessary.
     */
    TextInput textControl() {
        if (textControl == null) {
            textControl = textInput(identifier)
                    .run(ti -> {
                        if (ra.value.isDefined()) {
                            ti.value(ra.value.asString());
                        }
                        applyPlaceholder(ti);
                    });
        }
        return textControl;
    }

    InputGroupText unitInputGroupText() {
        return inputGroupText().plain().add(small().text(ra.description.unit()));
    }

    Button resolveExpressionButton() {
        return button().id(resolveExpressionId).control().icon(resolveExpression().get())
                .onClick((e, b) -> {
                    if (textControl != null) {
                        // TODO Resolve expression
                        uic().notifications().send(nyi());
                        logger.info("Resolve expression: %s", textControl.value());
                    }
                });
    }

    Button switchToExpressionModeButton() {
        return button().id(switchToExpressionModeId).control().icon(dollarSign())
                .onClick((e, b) -> switchToExpressionMode());
    }

    Button switchToNormalModeButton() {
        return button().id(switchToNativeModeId).control().icon(BuildingBlocks.normalMode().get())
                .onClick((e, b) -> switchToNativeMode());
    }

    void applyPlaceholder(TextInput textInput) {
        if (flags.placeholder == Placeholder.UNDEFINED) {
            textInput.placeholder(UNDEFINED);
        } else if (flags.placeholder == Placeholder.DEFAULT_VALUE) {
            if (ra.description.hasDefault()) {
                textInput.placeholder(ra.description.get(DEFAULT).asString());
            }
        }
    }

    // ------------------------------------------------------ events

    final void switchToExpressionMode() {
        resetValidation();
        failSafeRemoveFromParent(nativeContainer);
        formGroupControl.add(expressionContainer);
        tooltip(By.id(switchToNativeModeId), "Switch to native mode").appendTo(expressionContainer);
        tooltip(By.id(resolveExpressionId), "Resolve expression").appendTo(expressionContainer);
        inputMode = FormItemInputMode.EXPRESSION;
        afterSwitchedToExpressionMode();
    }

    /**
     * Method called after the input mode has been switched to expression mode.
     * <p>
     * This method sets focus to the text control's input element if the text control is not null.
     */
    void afterSwitchedToExpressionMode() {
        if (textControl != null) {
            textControl.input().element().focus();
        }
    }

    final void switchToNativeMode() {
        resetValidation();
        failSafeRemoveFromParent(expressionContainer);
        formGroupControl.add(nativeContainer);
        tooltip(By.id(switchToExpressionModeId), "Switch to expression mode").appendTo(nativeContainer);
        inputMode = FormItemInputMode.NATIVE;
        afterSwitchedToNativeMode();
    }

    /**
     * Method called after the input mode has been switched to native mode.
     * <p>
     * This method should be overridden in subclasses to implement specific behaviors when the mode is changed to native. The
     * default implementation is empty.
     */
    void afterSwitchedToNativeMode() {
        // empty
    }
}

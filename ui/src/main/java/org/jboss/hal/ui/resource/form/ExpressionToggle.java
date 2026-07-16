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

import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.help.HelperText;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.hal.core.Notification.nyi;
import static org.jboss.hal.dmr.Expression.containsExpression;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.brick.ExpressionBricks.expressionModeIcon;
import static org.jboss.hal.ui.brick.ExpressionBricks.normalModeIcon;
import static org.jboss.hal.ui.brick.ExpressionBricks.resolveExpressionIcon;
import static org.jboss.hal.ui.resource.form.InputMode.EXPRESSION;
import static org.jboss.hal.ui.resource.form.InputMode.NATIVE;
import static org.jboss.hal.ui.resource.pipeline.PipelineFlags.Scope.EXISTING_RESOURCE;
import static org.jboss.hal.ui.resource.pipeline.PipelineFlags.Scope.NEW_RESOURCE;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.help.HelperText.helperText;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.tooltip.Tooltip.tooltip;

/**
 * Encapsulates expression/native mode switching for a form item. This is a stateful component that manages:
 * <ul>
 *   <li>The current {@link InputMode} (NATIVE or EXPRESSION)</li>
 *   <li>The expression {@link TextInput} for entering WildFly expressions</li>
 *   <li>Container swapping — removes the native container and adds the expression container (or vice versa) on the
 *       {@link FormGroupControl}</li>
 *   <li>Tooltip lifecycle for the mode-switch and resolve-expression buttons</li>
 *   <li>Expression value reading, modification detection, and validation</li>
 * </ul>
 * <p>
 * Initialization accepts two callbacks: one invoked after switching to native mode (to restore control state and apply native
 * helper text), and one after switching to expression mode (to apply expression helper text). Both callbacks fire on every mode
 * switch, not just during initialization.
 * <p>
 * Used by {@link StandardFormItem} for items whose attributes allow expressions.
 *
 * @see StandardFormItem
 * @see NativeControl
 */
final class ExpressionToggle {

    private static final Logger logger = Logger.getLogger(ExpressionToggle.class.getName());

    private final String identifier;
    private final ResolvedAttribute attribute;
    private final PipelineFlags flags;
    private final String switchToExpressionModeId;
    private final String switchToNativeModeId;
    private final String resolveExpressionId;
    private final TextInput textControl;
    private final HTMLElement expressionContainer;

    private InputMode inputMode;
    private FormGroupControl formGroupControl;
    private HTMLElement nativeContainer;
    private Runnable afterSwitchedToNative;
    private Runnable afterSwitchedToExpression;
    private HTMLDivElement switchToNativeModeTooltip;
    private HTMLDivElement resolveExpressionTooltip;
    private HTMLElement switchToExpressionModeTooltip;

    ExpressionToggle(String identifier, ResolvedAttribute attribute, PipelineFlags flags) {
        this.identifier = identifier;
        this.attribute = attribute;
        this.flags = flags;
        this.switchToExpressionModeId = Id.build(identifier, "switch-to-expression-mode");
        this.switchToNativeModeId = Id.build(identifier, "switch-to-native-mode");
        this.resolveExpressionId = Id.build(identifier, "resolve-expression");
        this.inputMode = NATIVE;

        this.textControl = FormItemBricks.expressionTextControl(identifier, attribute, flags);

        this.expressionContainer = inputGroup()
                .addItem(inputGroupItem().addButton(
                        button().id(switchToNativeModeId).control().icon(normalModeIcon().get())
                                .onClick((e, b) -> switchToNative())))
                .addItem(inputGroupItem().fill().addControl(textControl))
                .addItem(inputGroupItem().addButton(
                        button().id(resolveExpressionId).control().icon(resolveExpressionIcon().get())
                                .onClick((e, b) -> {
                                    uic().notifications().send(nyi());
                                    logger.info("Resolve expression: %s", textControl.value());
                                })))
                .run(ig -> {
                    if (attribute.description().unit() != null) {
                        ig.addText(FormItemBricks.unitText(attribute.description().unit()));
                    }
                })
                .element();
    }

    /** Builds the native container wrapping the given control element with the expression-mode toggle button. */
    HTMLElement nativeContainer(HTMLElement controlElement) {
        return inputGroup()
                .addItem(inputGroupItem().addButton(
                        button().id(switchToExpressionModeId).control().icon(expressionModeIcon().get())
                                .onClick((e, b) -> switchToExpression())))
                .addItem(inputGroupItem().fill().add(controlElement))
                .element();
    }

    /** Initializes the toggle with the default InputGroup-based native container. */
    void initialize(FormGroupControl fgc, HTMLElement nativeCtl,
            Runnable afterSwitchedToNative, Runnable afterSwitchedToExpression) {
        doInitialize(fgc, nativeContainer(nativeCtl), afterSwitchedToNative, afterSwitchedToExpression);
    }

    /** Initializes the toggle with a custom native container (e.g. flex layout for Switch controls). */
    void initializeWithCustomContainer(FormGroupControl fgc, HTMLElement customNativeContainer,
            Runnable afterSwitchedToNative, Runnable afterSwitchedToExpression) {
        doInitialize(fgc, customNativeContainer, afterSwitchedToNative, afterSwitchedToExpression);
    }

    private void doInitialize(FormGroupControl fgc, HTMLElement container,
            Runnable afterSwitchedToNative, Runnable afterSwitchedToExpression) {
        this.formGroupControl = fgc;
        this.nativeContainer = container;
        this.afterSwitchedToNative = afterSwitchedToNative;
        this.afterSwitchedToExpression = afterSwitchedToExpression;
        if (attribute.expression()) {
            switchToExpression();
        } else {
            switchToNative();
        }
    }

    /** Returns a button that switches to expression mode. For use by {@link NativeControl#nativeContainer}. */
    org.patternfly.component.button.Button switchToExpressionButton() {
        return button().id(switchToExpressionModeId).control().icon(expressionModeIcon().get())
                .onClick((e, b) -> switchToExpression());
    }

    // ------------------------------------------------------ mode switching

    void switchToExpression() {
        failSafeRemoveFromParent(nativeContainer);
        failSafeRemoveFromParent(switchToExpressionModeTooltip);

        switchToNativeModeTooltip = tooltip(By.id(switchToNativeModeId), "Switch to native mode").element();
        resolveExpressionTooltip = tooltip(By.id(resolveExpressionId), "Resolve expression").element();

        formGroupControl.add(expressionContainer);
        expressionContainer.appendChild(switchToNativeModeTooltip);
        expressionContainer.appendChild(resolveExpressionTooltip);

        inputMode = EXPRESSION;
        if (afterSwitchedToExpression != null) {
            afterSwitchedToExpression.run();
        }
        textControl.input().element().focus();
    }

    void switchToNative() {
        failSafeRemoveFromParent(expressionContainer);
        failSafeRemoveFromParent(switchToNativeModeTooltip);
        failSafeRemoveFromParent(resolveExpressionTooltip);

        switchToExpressionModeTooltip = tooltip(By.id(switchToExpressionModeId), "Switch to expression mode").element();
        formGroupControl.add(nativeContainer);
        nativeContainer.appendChild(switchToExpressionModeTooltip);

        inputMode = NATIVE;
        if (afterSwitchedToNative != null) {
            afterSwitchedToNative.run();
        }
    }

    // ------------------------------------------------------ state

    InputMode inputMode() {
        return inputMode;
    }

    // ------------------------------------------------------ expression value

    boolean isExpressionModified() {
        String value = textControlValue();
        if (flags.scope() == NEW_RESOURCE) {
            return !value.isEmpty();
        } else if (flags.scope() == EXISTING_RESOURCE) {
            if (attribute.value().isDefined()) {
                return !attribute.value().asString().equals(value);
            } else {
                return !value.isEmpty();
            }
        }
        return false;
    }

    ModelNode expressionModelNode() {
        return new ModelNode().setExpression(textControlValue());
    }

    boolean validateExpression(FormGroupControl fgc, ResolvedAttribute ra) {
        if (FormItemBricks.requiredOnItsOwn(ra) && textControlValue().isEmpty()) {
            textControl.validated(error);
            fgc.addHelperText(FormItemBricks.requiredHelperText(ra));
            return false;
        }
        if (!containsExpression(textControl.value())) {
            textControl.validated(error);
            fgc.addHelperText(helperText("The value is not a valid expression.", error));
            return false;
        }
        return true;
    }

    void resetValidation() {
        textControl.resetValidation();
    }

    private String textControlValue() {
        return textControl.value();
    }
}

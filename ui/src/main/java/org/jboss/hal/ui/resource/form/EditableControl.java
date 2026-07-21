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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.help.HelperText;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.ui.resource.pipeline.PipelineFlags.Scope.EXISTING_RESOURCE;
import static org.jboss.hal.ui.resource.pipeline.PipelineFlags.Scope.NEW_RESOURCE;

/**
 * The composable unit of a form item: a {@link NativeControl} + optional {@link ExpressionToggle} behind a unified, mode-aware
 * API. This is the primary building block for both standalone form items ({@link StandardFormItem}) and composite form items
 * ({@link PathRelativeToFormItem}).
 * <p>
 * All behavioral methods ({@link #modelNode()}, {@link #isModified()}, {@link #validate()}, {@link #resetValidation()})
 * dispatch to the expression toggle or native control based on the current {@link InputMode}, so callers never need to check
 * the mode themselves.
 * <p>
 * The {@link #controlElement()} returns the switchable container — a plain DOM element that the expression toggle swaps content
 * in. This element can be reparented into any layout (e.g., a composite's flex container) without breaking expression support.
 * <p>
 * Validation helper text is directed to a {@link FormGroupControl} set via {@link #setValidationTarget(FormGroupControl)}. The
 * owning form item (standalone or composite) provides its own {@code FormGroupControl} as the target.
 *
 * @param <C> the PatternFly component type of the native control
 * @see NativeControl
 * @see ExpressionToggle
 * @see StandardFormItem
 */
public final class EditableControl<C> {

    private final ResolvedAttribute attribute;
    private final PipelineFlags flags;
    private final NativeControl<C> nativeControl;
    private final C control;
    private final ExpressionToggle expressionToggle;
    private final HTMLElement switchableContainer;
    private final HelperText nativeHelperText;
    private final HelperText expressionHelperText;
    private FormGroupControl validationTarget;

    EditableControl(PipelineContext context, String identifier, ResolvedAttribute attribute,
            NativeControl<C> nativeControl) {
        this.attribute = attribute;
        this.flags = context.flags();
        this.nativeControl = nativeControl;
        this.control = nativeControl.create(context, identifier, attribute);
        this.nativeHelperText = nativeControl.nativeHelperText();
        this.expressionHelperText = nativeControl.expressionHelperText();

        if (attribute.description().expressionAllowed() && !nativeControl.handlesMixedExpressions()) {
            this.expressionToggle = new ExpressionToggle(identifier, attribute, flags);
            this.switchableContainer = div().element();
            HTMLElement customContainer = nativeControl.nativeContainer(control, expressionToggle);
            Runnable afterNative = () -> {
                nativeControl.afterSwitchedToNativeMode(control, attribute);
                applyHelperText(nativeHelperText);
            };
            Runnable afterExpression = () -> applyHelperText(expressionHelperText);
            if (customContainer != null) {
                expressionToggle.initializeWithCustomContainer(switchableContainer, customContainer,
                        afterNative, afterExpression);
            } else {
                expressionToggle.initialize(switchableContainer, nativeControl.element(control),
                        afterNative, afterExpression);
            }
        } else {
            this.expressionToggle = null;
            this.switchableContainer = nativeControl.element(control);
            applyHelperText(nativeHelperText);
        }
    }

    // ------------------------------------------------------ accessors

    /** Returns the resolved attribute this control edits. */
    public ResolvedAttribute attribute() {
        return attribute;
    }

    /** Returns the native control strategy. */
    NativeControl<C> nativeControl() {
        return nativeControl;
    }

    /** Returns the native control instance. */
    C control() {
        return control;
    }

    /** Returns the switchable container element for embedding in composite layouts. */
    public HTMLElement controlElement() {
        return switchableContainer;
    }

    /** Sets the target {@link FormGroupControl} for validation helper text and mode-switch helper text. */
    public void setValidationTarget(FormGroupControl target) {
        this.validationTarget = target;
    }

    // ------------------------------------------------------ value

    /** Returns the current value as a DMR model node, dispatching to expression or native mode. */
    public ModelNode modelNode() {
        if (expressionToggle != null && expressionToggle.inputMode() == InputMode.EXPRESSION) {
            return expressionToggle.expressionModelNode();
        }
        return nativeControl.modelNode(control, attribute);
    }

    // ------------------------------------------------------ modification tracking

    /** Returns whether the value has been modified, dispatching to expression or native mode. */
    public boolean isModified() {
        if (attribute.readable() && !attribute.description().readOnly()) {
            if (expressionToggle != null && expressionToggle.inputMode() == InputMode.EXPRESSION) {
                return expressionToggle.isExpressionModified();
            }
            if (flags.scope() == NEW_RESOURCE) {
                return nativeControl.isModifiedForNew(control, attribute);
            } else if (flags.scope() == EXISTING_RESOURCE) {
                return nativeControl.isModifiedForExisting(control, attribute, attribute.value().isDefined());
            }
        }
        return false;
    }

    // ------------------------------------------------------ validation

    /** Validates the current input, dispatching to expression or native mode. */
    public boolean validate() {
        if (expressionToggle != null && expressionToggle.inputMode() == InputMode.EXPRESSION) {
            return expressionToggle.validateExpression(validationTarget, attribute);
        }
        return nativeControl.validate(control, attribute, validationTarget);
    }

    /** Resets validation state on both native and expression controls, restoring the correct helper text. */
    public void resetValidation() {
        nativeControl.resetValidation(control);
        if (expressionToggle != null) {
            expressionToggle.resetValidation();
        }
        if (validationTarget != null) {
            validationTarget.removeHelperText();
            if (expressionToggle == null || expressionToggle.inputMode() == InputMode.NATIVE) {
                applyHelperText(nativeHelperText);
            } else {
                applyHelperText(expressionHelperText);
            }
        }
    }

    private void applyHelperText(HelperText ht) {
        if (validationTarget != null) {
            validationTarget.removeHelperText();
            if (ht != null) {
                validationTarget.addHelperText(ht);
            }
        }
    }
}

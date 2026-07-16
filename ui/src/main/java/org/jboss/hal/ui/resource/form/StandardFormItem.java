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

import java.util.Collections;
import java.util.List;

import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags;
import org.patternfly.component.form.FormGroup;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.help.HelperText;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.ui.resource.pipeline.PipelineFlags.Scope.EXISTING_RESOURCE;
import static org.jboss.hal.ui.resource.pipeline.PipelineFlags.Scope.NEW_RESOURCE;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;

/**
 * A composed, {@code final} form item for single-attribute controls with optional expression toggle. This is the central class
 * of the form item architecture, assembling a complete {@link FormGroup} from four building blocks:
 * <ul>
 *   <li>{@link NativeControl} — the widget and its value semantics (strategy pattern)</li>
 *   <li>{@link ExpressionToggle} — expression/native mode switching (composition, created only when
 *       {@code expressionAllowed})</li>
 *   <li>{@link FormItemBricks} — label, read-only controls, placeholders (brick pattern)</li>
 *   <li>{@link OperationStrategy} — DMR operation generation (strategy pattern, defaults to
 *       {@link OperationStrategy#WRITE_ATTRIBUTE})</li>
 * </ul>
 * <p>
 * Construction handles three paths based on attribute metadata:
 * <ol>
 *   <li><b>Read-only</b> — delegates to {@link FormItemBricks#readOnlyGroup}</li>
 *   <li><b>Expression-allowed</b> — creates an {@link ExpressionToggle} and initializes it in the appropriate mode
 *       (expression if the current value is an expression, native otherwise)</li>
 *   <li><b>Native-only</b> — adds the native control element directly to the {@code FormGroupControl}</li>
 * </ol>
 * <p>
 * Helper text is managed per mode: {@link NativeControl#helperText()} is shown in native mode,
 * {@link NativeControl#expressionHelperText()} in expression mode. Mode switches and validation resets apply the correct
 * helper text automatically.
 * <p>
 * Usage (from pipeline item providers):
 * <pre>{@code
 * // simple — default WRITE_ATTRIBUTE operations
 * new StandardFormItem<>(identifier, attribute, context, new SelectControl());
 *
 * // custom operations — e.g. map-put / map-remove
 * new StandardFormItem<>(identifier, attribute, context, new MapControl(), MapOperationStrategy.INSTANCE);
 * }</pre>
 *
 * @param <C> the PatternFly component type of the native control
 * @see NativeControl
 * @see ExpressionToggle
 * @see OperationStrategy
 * @see FormItemBricks
 */
public final class StandardFormItem<C> implements FormItem {

    private static final Logger logger = Logger.getLogger(StandardFormItem.class.getName());

    private final String identifier;
    private final ResolvedAttribute attribute;
    private final PipelineFlags flags;
    private final NativeControl<C> nativeControl;
    private final OperationStrategy operationStrategy;
    private final C control;
    private final ExpressionToggle expressionToggle;
    private final FormGroup formGroup;
    private final FormGroupControl formGroupControl;
    private final HelperText nativeHelperText;
    private final HelperText expressionHelperText;

    public StandardFormItem(String identifier, ResolvedAttribute attribute, PipelineContext context,
            NativeControl<C> nativeControl) {
        this(identifier, attribute, context, nativeControl, OperationStrategy.WRITE_ATTRIBUTE);
    }

    public StandardFormItem(String identifier, ResolvedAttribute attribute, PipelineContext context,
            NativeControl<C> nativeControl, OperationStrategy operationStrategy) {
        this.identifier = identifier;
        this.attribute = attribute;
        this.flags = context.flags();
        this.nativeControl = nativeControl;
        this.operationStrategy = operationStrategy;
        this.control = nativeControl.create(identifier, attribute, context);
        this.nativeHelperText = nativeControl.helperText();
        this.expressionHelperText = nativeControl.expressionHelperText();

        if (attribute.description().readOnly()) {
            this.expressionToggle = null;
            this.formGroupControl = FormItemBricks.readOnlyGroup(identifier, attribute, flags);
        } else if (attribute.description().expressionAllowed() && !nativeControl.handlesMixedExpressions()) {
            this.expressionToggle = new ExpressionToggle(identifier, attribute, flags);
            this.formGroupControl = formGroupControl();
            HTMLElement customContainer = nativeControl.nativeContainer(control, expressionToggle);
            Runnable afterNative = () -> {
                nativeControl.afterSwitchedToNativeMode(control, attribute);
                applyHelperText(nativeHelperText);
            };
            Runnable afterExpression = () -> applyHelperText(expressionHelperText);
            if (customContainer != null) {
                expressionToggle.initializeWithCustomContainer(formGroupControl, customContainer,
                        afterNative, afterExpression);
            } else {
                expressionToggle.initialize(formGroupControl, nativeControl.element(control),
                        afterNative, afterExpression);
            }
        } else {
            this.expressionToggle = null;
            this.formGroupControl = formGroupControl().add(nativeControl.element(control));
            applyHelperText(nativeHelperText);
        }

        this.formGroup = formGroup(identifier)
                .required(attribute.description().required())
                .addLabel(FormItemBricks.label(identifier, attribute, context))
                .addControl(formGroupControl);
    }

    // ------------------------------------------------------ accessors

    /** Returns the native control strategy. */
    NativeControl<C> nativeControl() {
        return nativeControl;
    }

    /** Returns the native control instance. */
    C control() {
        return control;
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
        return formGroup.element();
    }

    // ------------------------------------------------------ value

    @Override
    public ModelNode modelNode() {
        if (expressionToggle != null && expressionToggle.inputMode() == InputMode.EXPRESSION) {
            return expressionToggle.expressionModelNode();
        }
        return nativeControl.modelNode(control, attribute);
    }

    // ------------------------------------------------------ modification tracking

    @Override
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

    @Override
    public boolean validate() {
        if (expressionToggle != null && expressionToggle.inputMode() == InputMode.EXPRESSION) {
            return expressionToggle.validateExpression(formGroupControl, attribute);
        }
        return nativeControl.validate(control, attribute, formGroupControl);
    }

    @Override
    public void resetValidation() {
        nativeControl.resetValidation(control);
        if (expressionToggle != null) {
            expressionToggle.resetValidation();
        }
        formGroupControl.removeHelperText();
        if (expressionToggle == null || expressionToggle.inputMode() == InputMode.NATIVE) {
            applyHelperText(nativeHelperText);
        } else {
            applyHelperText(expressionHelperText);
        }
    }

    private void applyHelperText(HelperText ht) {
        formGroupControl.removeHelperText();
        if (ht != null) {
            formGroupControl.addHelperText(ht);
        }
    }

    // ------------------------------------------------------ operations

    @Override
    public List<Operation> operations(ResourceAddress address) {
        return operationStrategy.operations(this, address);
    }
}

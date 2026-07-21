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

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.patternfly.component.form.FormGroup;
import org.patternfly.component.form.FormGroupControl;

import elemental2.dom.HTMLElement;

import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;

/**
 * A composed, {@code final} form item for single-attribute controls. This is the central class of the form item architecture,
 * assembling a complete {@link FormGroup} from three building blocks:
 * <ul>
 *   <li>{@link EditableControl} — the composable unit that pairs a {@link NativeControl} with an optional
 *       {@link ExpressionToggle} behind a unified, mode-aware API ({@code null} for read-only attributes)</li>
 *   <li>{@link FormItemBricks} — label, read-only controls, placeholders (brick pattern)</li>
 *   <li>{@link OperationStrategy} — DMR operation generation (strategy pattern, defaults to
 *       {@link OperationStrategy#WRITE_ATTRIBUTE})</li>
 * </ul>
 * <p>
 * Construction handles two paths based on attribute metadata:
 * <ol>
 *   <li><b>Read-only</b> — {@link #editableControl()} returns {@code null}; delegates display to
 *       {@link FormItemBricks#readOnlyGroup}</li>
 *   <li><b>Editable</b> — creates an {@link EditableControl} that handles expression/native mode switching, value reading,
 *       modification tracking, and validation internally</li>
 * </ol>
 * <p>
 * Composite form items (e.g. {@link PathRelativeToFormItem}) access the editable control via
 * {@link #editableControl()} to embed it in custom layouts without needing to cast or use workarounds.
 * <p>
 * Usage (from pipeline item providers):
 * <pre>{@code
 * // simple — default WRITE_ATTRIBUTE operations
 * new StandardFormItem<>(context, identifier, attribute, new SelectControl());
 *
 * // custom operations — e.g. map-put / map-remove
 * new StandardFormItem<>(context, identifier, attribute, new MapControl(), MapOperationStrategy.INSTANCE);
 * }</pre>
 *
 * @param <C> the PatternFly component type of the native control
 * @see EditableControl
 * @see NativeControl
 * @see OperationStrategy
 * @see FormItemBricks
 */
public final class StandardFormItem<C> implements FormItem {

    private final String identifier;
    private final ResolvedAttribute attribute;
    private final EditableControl<C> editableControl;
    private final OperationStrategy operationStrategy;
    private final FormGroup formGroup;

    public StandardFormItem(PipelineContext context, String identifier, ResolvedAttribute attribute,
            NativeControl<C> nativeControl) {
        this(context, identifier, attribute, nativeControl, OperationStrategy.WRITE_ATTRIBUTE);
    }

    public StandardFormItem(PipelineContext context, String identifier, ResolvedAttribute attribute,
            NativeControl<C> nativeControl, OperationStrategy operationStrategy) {
        this.identifier = identifier;
        this.attribute = attribute;
        this.operationStrategy = operationStrategy;

        FormGroupControl formGroupControl;
        if (attribute.description().readOnly()) {
            this.editableControl = null;
            formGroupControl = FormItemBricks.readOnlyGroup(identifier, attribute, context.flags());
        } else {
            this.editableControl = new EditableControl<>(context, identifier, attribute, nativeControl);
            formGroupControl = formGroupControl().add(editableControl.controlElement());
            editableControl.setValidationTarget(formGroupControl);
        }

        this.formGroup = formGroup(identifier)
                .required(attribute.description().required())
                .addLabel(FormItemBricks.label(identifier, attribute, context))
                .addControl(formGroupControl);
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

    @Override
    public EditableControl<C> editableControl() {
        return editableControl;
    }

    @Override
    public ModelNode modelNode() {
        return editableControl != null ? editableControl.modelNode() : new ModelNode();
    }

    @Override
    public boolean isModified() {
        return editableControl != null && editableControl.isModified();
    }

    @Override
    public boolean validate() {
        return editableControl == null || editableControl.validate();
    }

    @Override
    public void resetValidation() {
        if (editableControl != null) {
            editableControl.resetValidation();
        }
    }

    @Override
    public List<Operation> operations(ResourceAddress address) {
        return operationStrategy.operations(this, address);
    }
}

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
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.help.HelperText;

import elemental2.dom.HTMLElement;

/**
 * Strategy interface for the native (non-expression) control within an {@link EditableControl}. This is the primary extension
 * point of the form item architecture — one implementation per widget type.
 * <p>
 * Each implementation captures:
 * <ul>
 *   <li><b>Widget creation</b> — {@link #create(PipelineContext, String, ResolvedAttribute)}</li>
 *   <li><b>DOM element</b> — {@link #element(Object)}</li>
 *   <li><b>Value reading</b> — {@link #modelNode(Object, ResolvedAttribute)}</li>
 *   <li><b>Modification detection</b> — {@link #isModifiedForNew(Object, ResolvedAttribute)} and
 *       {@link #isModifiedForExisting(Object, ResolvedAttribute, boolean)}</li>
 *   <li><b>Validation</b> — {@link #validate(Object, ResolvedAttribute, FormGroupControl)} and
 *       {@link #resetValidation(Object)}</li>
 *   <li><b>Mode-aware helper text</b> — {@link #helperText()} for native mode and {@link #expressionHelperText()} for
 *       expression mode, both returning {@link HelperText} components that support rich content with nested elements</li>
 *   <li><b>Custom container layout</b> — {@link #nativeContainer(Object, ExpressionToggle)} for controls that need a
 *       non-standard layout in expression-toggle mode (e.g. flex instead of InputGroup)</li>
 * </ul>
 * <p>
 * Implementations should be {@code final} classes with no inheritance. They are stateless with respect to DOM assembly —
 * all DOM concerns are handled by {@link EditableControl} and {@link ExpressionToggle}.
 *
 * @param <C> the PatternFly component type (e.g. {@code FormSelect}, {@code Switch}, {@code FilterInput})
 * @see EditableControl
 * @see ExpressionToggle
 */
public interface NativeControl<C> {

    /** Creates the PatternFly control component. Called once during construction. */
    C create(PipelineContext context, String identifier, ResolvedAttribute attribute);

    /** Returns the root element of the control for DOM insertion. */
    HTMLElement element(C control);

    /** Reads the current value from the control as a DMR model node. Returns an undefined node for "no value". */
    ModelNode modelNode(C control, ResolvedAttribute attribute);

    /** Returns {@code true} if the control's value represents a modification for a new resource. */
    boolean isModifiedForNew(C control, ResolvedAttribute attribute);

    /** Returns {@code true} if the control's value represents a modification for an existing resource. */
    boolean isModifiedForExisting(C control, ResolvedAttribute attribute, boolean wasDefined);

    /** Validates the control's current value. Returns {@code true} if valid. */
    default boolean validate(C control, ResolvedAttribute attribute, FormGroupControl formGroupControl) {
        return true;
    }

    /** Resets any validation state on the control. */
    default void resetValidation(C control) {
    }

    /** Called after switching from expression mode back to native mode. Restore control state here. */
    default void afterSwitchedToNativeMode(C control, ResolvedAttribute attribute) {
    }

    /** Optional helper text displayed below the control in native mode. */
    default HelperText helperText() {
        return null;
    }

    /** Optional helper text displayed below the control in expression mode. */
    default HelperText expressionHelperText() {
        return null;
    }

    /**
     * Returns {@code true} if this control handles expression input internally (mixed mode). When {@code true},
     * {@link EditableControl} will not create an {@link ExpressionToggle}, even if the attribute allows expressions.
     * The control is responsible for rendering its own expression support (e.g. a resolve button in an InputGroup).
     */
    default boolean handlesMixedExpressions() {
        return false;
    }

    /**
     * Builds a custom native container for use with the expression toggle. The container must include
     * the control element and a button to switch to expression mode (provided by
     * {@link ExpressionToggle#switchToExpressionButton()}). Returns {@code null} to use the default
     * InputGroup-based container.
     */
    default HTMLElement nativeContainer(C control, ExpressionToggle toggle) {
        return null;
    }
}

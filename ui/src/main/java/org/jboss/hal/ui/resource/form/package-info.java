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

/**
 * Editable form items for WildFly management resource attributes.
 *
 * <h2>Architecture overview</h2>
 * <p>
 * The form item architecture is composition-based. Each form item is assembled from small, single-responsibility building
 * blocks rather than using deep inheritance. The key types and their roles are:
 *
 * <h3>Interfaces</h3>
 * <dl>
 *   <dt>{@link org.jboss.hal.ui.resource.form.FormItem FormItem}</dt>
 *   <dd>The contract every form item implements. A form item knows how to read its current value as a DMR model node, detect
 *       whether it has been modified, validate its input, and produce the DMR operations needed to persist its state. Operations
 *       are produced by the item, not the form — this allows composite items to generate multiple operations from a single form
 *       group.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.NativeControl NativeControl&lt;C&gt;}</dt>
 *   <dd>The primary extension point. A strategy interface with one implementation per widget type (switch, select, number input,
 *       typeahead, etc.). Each implementation captures widget creation, DOM element extraction, value reading, modification
 *       detection, and validation. Implementations should be {@code final} classes with no inheritance among them.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.OperationStrategy OperationStrategy}</dt>
 *   <dd>A functional interface for producing DMR operations from a form item's state. Most items use the default
 *       {@link org.jboss.hal.ui.resource.form.OperationStrategy#WRITE_ATTRIBUTE WRITE_ATTRIBUTE} strategy (single
 *       {@code write-attribute} or {@code undefine-attribute}). Custom implementations like
 *       {@link org.jboss.hal.ui.resource.form.MapOperationStrategy MapOperationStrategy} produce granular per-entry
 *       operations.</dd>
 * </dl>
 *
 * <h3>Composition classes</h3>
 * <dl>
 *   <dt>{@link org.jboss.hal.ui.resource.form.EditableControl EditableControl&lt;C&gt;}</dt>
 *   <dd>The composable unit at the heart of the architecture. Pairs a {@link org.jboss.hal.ui.resource.form.NativeControl
 *       NativeControl} with an optional {@link org.jboss.hal.ui.resource.form.ExpressionToggle ExpressionToggle} behind a
 *       unified, mode-aware API. All behavioral methods (value reading, modification tracking, validation) dispatch to the
 *       correct {@link org.jboss.hal.ui.resource.form.InputMode InputMode} internally, so callers never check the mode
 *       themselves.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.ExpressionToggle ExpressionToggle}</dt>
 *   <dd>Manages expression/native mode switching: the expression text input, container swapping, tooltip lifecycle, and
 *       expression validation. Created automatically by {@code EditableControl} when the attribute allows expressions and the
 *       native control does not handle them in mixed mode.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.StandardFormItem StandardFormItem&lt;C&gt;}</dt>
 *   <dd>The standard {@link org.jboss.hal.ui.resource.form.FormItem FormItem} for single-attribute controls. A thin visual
 *       shell that wraps an {@link org.jboss.hal.ui.resource.form.EditableControl EditableControl} in a PatternFly
 *       {@code FormGroup} with a label and an {@link org.jboss.hal.ui.resource.form.OperationStrategy OperationStrategy}. Most
 *       form items in the console are {@code StandardFormItem}s.</dd>
 * </dl>
 *
 * <h3>Shared utilities</h3>
 * <dl>
 *   <dt>{@link org.jboss.hal.ui.resource.form.FormItemBricks FormItemBricks}</dt>
 *   <dd>Static utility class providing reusable UI fragments (brick pattern): labels with description popovers and stability
 *       badges, read-only controls, expression text inputs, placeholder application, validation helper text, and fail-safe
 *       value selection for {@code FormSelect} and {@code SingleTypeahead} controls.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.ResourceForm ResourceForm}</dt>
 *   <dd>The form container. Aggregates a list of {@link org.jboss.hal.ui.resource.form.FormItem FormItem}s, orchestrates
 *       validation across all items, collects their DMR operations into a single composite, and manages grouped/flat layouts
 *       with expandable sections.</dd>
 * </dl>
 *
 * <h2>How the pieces fit together</h2>
 * <p>
 * The composition hierarchy for a typical single-attribute form item is:
 * <pre>
 * ResourceForm
 *   └── StandardFormItem (implements FormItem)
 *         ├── FormItemBricks.label(...)        → FormGroupLabel
 *         ├── EditableControl                  → mode-aware control container
 *         │     ├── NativeControl              → widget strategy (e.g. SelectControl)
 *         │     └── ExpressionToggle           → expression/native mode switching (optional)
 *         └── OperationStrategy               → DMR operation generation
 * </pre>
 * <p>
 * Composite form items (e.g. {@link org.jboss.hal.ui.resource.form.PathRelativeToFormItem PathRelativeToFormItem}) implement
 * {@link org.jboss.hal.ui.resource.form.FormItem FormItem} directly and use the pipeline to create child
 * {@code StandardFormItem}s, then extract their {@link org.jboss.hal.ui.resource.form.EditableControl EditableControl}s via
 * {@link org.jboss.hal.ui.resource.form.FormItem#editableControl()} to embed them in a custom layout. This reuses the full
 * expression support, validation, and value reading of each child without duplicating any logic.
 *
 * <h2>Data and control flow</h2>
 * <p>
 * When the user saves the form, {@link org.jboss.hal.ui.resource.form.ResourceForm ResourceForm} iterates over all form items:
 * <ol>
 *   <li><b>Validation</b> — each {@code FormItem.validate()} delegates to {@code EditableControl.validate()}, which dispatches
 *       to the expression toggle or native control based on the current mode.</li>
 *   <li><b>Modification detection</b> — each {@code FormItem.isModified()} delegates to {@code EditableControl.isModified()},
 *       which checks scope ({@code NEW_RESOURCE} vs {@code EXISTING_RESOURCE}) and dispatches to the native control's
 *       {@code isModifiedForNew()} or {@code isModifiedForExisting()}.</li>
 *   <li><b>Operation generation</b> — each modified item's {@code FormItem.operations(address)} delegates to its
 *       {@code OperationStrategy}, which reads {@code FormItem.modelNode()} (via {@code EditableControl.modelNode()}) and
 *       produces the appropriate DMR operations.</li>
 *   <li><b>Submission</b> — all operations from all items are flat-mapped into a single DMR composite operation.</li>
 * </ol>
 *
 * <h2>Expression mode</h2>
 * <p>
 * Expression support is handled at two levels. Most controls use the {@link org.jboss.hal.ui.resource.form.ExpressionToggle
 * ExpressionToggle} created by {@code EditableControl}: a toggle button swaps between the native control and an expression text
 * input, and all behavioral methods dispatch transparently. Controls that handle expressions internally (mixed mode, e.g.
 * {@link org.jboss.hal.ui.resource.form.StringControl StringControl}) signal this via
 * {@link org.jboss.hal.ui.resource.form.NativeControl#handlesMixedExpressions()}, and no toggle is created.
 * <p>
 * Helper text is mode-aware: {@link org.jboss.hal.ui.resource.form.NativeControl#nativeHelperText()} and
 * {@link org.jboss.hal.ui.resource.form.NativeControl#expressionHelperText()} provide per-mode helper text, and
 * {@code EditableControl} applies the correct one when switching modes and after resetting validation.
 *
 * <h2>Pipeline integration</h2>
 * <p>
 * Form items are created by the pipeline's item providers. The {@code DefaultItemProvider} dispatches by attribute type and
 * metadata to the appropriate {@code NativeControl} implementation. Specialized providers (e.g. {@code MapProvider})
 * handle composite matches. Composite form items use the pipeline to create child items, then compose their
 * {@code EditableControl}s into a single form group.
 */
package org.jboss.hal.ui.resource.form;

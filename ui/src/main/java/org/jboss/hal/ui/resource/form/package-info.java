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
 * <h2>Architecture</h2>
 * <p>
 * Form items use a composition-based architecture with four building blocks:
 * <ol>
 *   <li>{@link org.jboss.hal.ui.resource.form.NativeControl NativeControl&lt;C&gt;} — strategy interface that captures the
 *       widget type and its value semantics (creation, reading, modification detection, validation). One implementation per
 *       control type (switch, select, number input, typeahead, filter input, etc.).</li>
 *   <li>{@link org.jboss.hal.ui.resource.form.ExpressionToggle ExpressionToggle} — encapsulates expression/native mode
 *       switching, managing the {@link org.jboss.hal.ui.resource.form.InputMode InputMode}, the expression text input,
 *       container swapping on the {@code FormGroupControl}, and tooltip lifecycle.</li>
 *   <li>{@link org.jboss.hal.ui.resource.form.FormItemBricks FormItemBricks} — static factory methods (brick pattern) for
 *       shared UI fragments: labels with description popovers, read-only controls, placeholders, and validation helper
 *       text.</li>
 *   <li>{@link org.jboss.hal.ui.resource.form.OperationStrategy OperationStrategy} — functional interface for producing DMR
 *       operations from a form item's current state. Most items use the default
 *       {@link org.jboss.hal.ui.resource.form.OperationStrategy#WRITE_ATTRIBUTE WRITE_ATTRIBUTE} strategy;
 *       {@link org.jboss.hal.ui.resource.form.MapOperationStrategy MapOperationStrategy} provides granular
 *       {@code map-put}/{@code map-remove} operations.</li>
 * </ol>
 * <p>
 * {@link org.jboss.hal.ui.resource.form.StandardFormItem StandardFormItem&lt;C&gt;} is the single {@code final} class that
 * composes all four building blocks. It handles the three assembly paths (read-only, expression-allowed, native-only),
 * mode-aware helper text lifecycle, modification tracking, validation, and DMR operation generation — with no template methods
 * and no subclassing.
 *
 * <h2>Item Categories</h2>
 * <p>
 * Form items fall into two categories:
 *
 * <h3>Category A — Standard single-attribute items</h3>
 * <p>
 * These follow a uniform pattern (single attribute, native control + optional expression toggle) and are created as
 * {@code StandardFormItem} instances composed with a {@code NativeControl} implementation:
 * <dl>
 *   <dt>{@link org.jboss.hal.ui.resource.form.SwitchControl SwitchControl}</dt>
 *   <dd>Toggle switch for boolean attributes. Uses a flex layout for the expression-toggle container.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.NumberInputControl NumberInputControl}</dt>
 *   <dd>Number input with min/max validation (INT, LONG, DOUBLE) or allowed-values select.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.SelectControl SelectControl}</dt>
 *   <dd>Dropdown select for string attributes with predefined allowed values.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.TypeaheadControl TypeaheadControl}</dt>
 *   <dd>Single-select typeahead for string attributes with a capability reference.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.MultiTypeaheadControl MultiTypeaheadControl}</dt>
 *   <dd>Multi-select typeahead for list-of-string attributes with a capability reference.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.StringListControl StringListControl}</dt>
 *   <dd>Label-based multi-value input for list-of-string attributes.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.MapControl MapControl}</dt>
 *   <dd>Key=value filter input for free-form map attributes. Paired with
 *       {@link org.jboss.hal.ui.resource.form.MapOperationStrategy MapOperationStrategy} for granular operations.</dd>
 * </dl>
 *
 * <h3>Category B — Composite and special-case items</h3>
 * <p>
 * These implement {@link org.jboss.hal.ui.resource.form.FormItem FormItem} directly (with or without
 * {@link org.jboss.hal.ui.resource.form.AbstractFormItem AbstractFormItem}) and use
 * {@link org.jboss.hal.ui.resource.form.FormItemBricks FormItemBricks} for shared fragments:
 * <dl>
 *   <dt>{@link org.jboss.hal.ui.resource.form.StringFormItem StringFormItem}</dt>
 *   <dd>Text input for plain string attributes, using MIXED mode (no expression toggle).</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.FileFormItem FileFormItem}</dt>
 *   <dd>Composite form item for path + relative-to OBJECT attributes.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.TimeUnitFormItem TimeUnitFormItem}</dt>
 *   <dd>Composite form item for time + unit OBJECT attributes.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.CredentialReferenceFormItem CredentialReferenceFormItem}</dt>
 *   <dd>Multi-mode composite for credential-reference OBJECT attributes.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.RestrictedFormItem RestrictedFormItem}</dt>
 *   <dd>Locked sentinel for permission-restricted attributes.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.UnsupportedFormItem UnsupportedFormItem}</dt>
 *   <dd>Read-only fallback for unsupported attribute types.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.PathRelativeToFormItem PathRelativeToFormItem}</dt>
 *   <dd>Sibling-attribute form item for path + relative-to STRING pairs.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.RelativeToFormItem RelativeToFormItem}</dt>
 *   <dd>Standalone form item for relative-to STRING attributes (used in feature-in-profile scope).</dd>
 * </dl>
 *
 * <h2>Helper Text</h2>
 * <p>
 * Native and expression modes manage helper text independently. {@link org.jboss.hal.ui.resource.form.NativeControl#helperText()
 * NativeControl.helperText()} provides helper text for native mode, and
 * {@link org.jboss.hal.ui.resource.form.NativeControl#expressionHelperText() NativeControl.expressionHelperText()} provides
 * helper text for expression mode. Both return {@link org.patternfly.component.help.HelperText HelperText} components, which
 * support rich content with nested elements and markup. {@code StandardFormItem} applies the correct helper text when switching
 * modes and after resetting validation.
 *
 * <h2>Shared Support Types</h2>
 * <dl>
 *   <dt>{@link org.jboss.hal.ui.resource.form.ResourceForm ResourceForm}</dt>
 *   <dd>Container that orchestrates form items, validation, and DMR operation submission.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.CapabilityReferenceSupport CapabilityReferenceSupport}</dt>
 *   <dd>Helper for loading capability reference options into typeahead controls.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.StringListSupport StringListSupport}</dt>
 *   <dd>Shared logic for label-based multi-value string list inputs.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.SearchReloadInput SearchReloadInput}</dt>
 *   <dd>Search input that reloads options on focus, for single-select typeahead controls.</dd>
 *   <dt>{@link org.jboss.hal.ui.resource.form.FilterReloadInput FilterReloadInput}</dt>
 *   <dd>Filter input that reloads options on focus, for multi-select typeahead controls.</dd>
 * </dl>
 *
 * <h2>Pipeline Integration</h2>
 * <p>
 * Form items are created by the pipeline's item providers. The
 * {@link org.jboss.hal.ui.resource.pipeline.DefaultItemProvider DefaultItemProvider} dispatches by attribute type to the
 * appropriate {@code NativeControl} implementation. Specialized providers (e.g.
 * {@link org.jboss.hal.ui.resource.pipeline.MapProvider MapProvider}) handle composite matches.
 */
package org.jboss.hal.ui.resource.form;

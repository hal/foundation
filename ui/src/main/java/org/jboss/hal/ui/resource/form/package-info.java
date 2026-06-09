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
 * <p>
 * This package contains the abstract {@link org.jboss.hal.ui.resource.form.FormItem} base class and all concrete form
 * item implementations for different attribute types (boolean, numeric, string, select, capability references, etc.),
 * along with the {@link org.jboss.hal.ui.resource.form.FormItemFactory} that creates the appropriate form item based on
 * attribute metadata, and the {@link org.jboss.hal.ui.resource.form.ResourceForm} container that manages form layout,
 * validation, and modification tracking.
 * <p>
 * Key components:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.form.FormItem}</dt>
 * <dd>Abstract base class for all form items with expression mode support and validation.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.FormItemFactory}</dt>
 * <dd>Creates the appropriate form item subclass based on attribute type and constraints.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.ResourceForm}</dt>
 * <dd>Horizontal form container with validation and DMR operation generation.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.BooleanFormItem}</dt>
 * <dd>Toggle switch for boolean attributes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.NumberFormItem}</dt>
 * <dd>Numeric input with range validation.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.StringFormItem}</dt>
 * <dd>Text input for string attributes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.SelectFormItem}</dt>
 * <dd>Dropdown select for attributes with predefined allowed values.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.CapabilityReferenceFormItem}</dt>
 * <dd>Typeahead for single capability reference attributes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.CapabilityReferencesFormItem}</dt>
 * <dd>Multi-select typeahead for list-valued capability reference attributes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.StringListFormItem}</dt>
 * <dd>Label-based multi-value input for LIST-type string attributes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.FormItemFlags}</dt>
 * <dd>Configuration flags controlling scope and placeholder behavior.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.FormItemInputMode}</dt>
 * <dd>Enumeration of input modes: native, expression, or mixed.</dd>
 * </dl>
 */
package org.jboss.hal.ui.resource.form;

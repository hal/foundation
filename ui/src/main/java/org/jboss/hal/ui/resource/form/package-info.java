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
 * This package contains the abstract {@link org.jboss.hal.ui.resource.form.OldFormItem} base class and all concrete form
 * item implementations for different attribute types (boolean, numeric, string, select, capability references, etc.),
 * along with the {@link org.jboss.hal.ui.resource.form.FormItemFactory} that creates the appropriate form item based on
 * attribute metadata, and the {@link org.jboss.hal.ui.resource.form.ResourceForm} container that manages form layout,
 * validation, and modification tracking.
 * <p>
 * Key components:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.form.OldFormItem}</dt>
 * <dd>Abstract base class for all form items with expression mode support, validation, and a template method for
 * modification detection ({@link org.jboss.hal.ui.resource.form.OldFormItem#isModified()}).</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.FormItemFactory}</dt>
 * <dd>Creates the appropriate form item subclass based on attribute type and constraints.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.ResourceForm}</dt>
 * <dd>Horizontal form container with validation and DMR operation generation.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.OldBooleanFormItem}</dt>
 * <dd>Toggle switch for boolean attributes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.OldNumberFormItem}</dt>
 * <dd>Numeric input with range validation.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.OldStringFormItem}</dt>
 * <dd>Text input for string attributes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.OldSelectFormItem}</dt>
 * <dd>Dropdown select for attributes with predefined allowed values.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.OldCapabilityReferenceFormItem}</dt>
 * <dd>Typeahead for single capability reference attributes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.OldCapabilityReferencesFormItem}</dt>
 * <dd>Multi-select typeahead for list-valued capability reference attributes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.OldStringListFormItem}</dt>
 * <dd>Label-based multi-value input for LIST-type string attributes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.FormItemFlags}</dt>
 * <dd>Configuration flags controlling scope and placeholder behavior.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.FormItemInputMode}</dt>
 * <dd>Enumeration of input modes: native, expression, or mixed.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.FormItemProvider}</dt>
 * <dd>Strategy interface for custom form item creation based on address template and attribute metadata.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.FormItemProviders}</dt>
 * <dd>Registry of special-case form item providers consulted before default creation.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.OldRestrictedFormItem}</dt>
 * <dd>Locked form item shown when the user lacks read permission for the attribute.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.OldUnsupportedFormItem}</dt>
 * <dd>Read-only fallback for attribute types not yet supported.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.HelperTexts}</dt>
 * <dd>Factory methods for validation helper text messages.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.CapabilityReferenceSupport}</dt>
 * <dd>Shared helpers for loading capability suggestions and creating new provider resources.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.StringListSupport}</dt>
 * <dd>Shared helpers for modification detection and model node conversion for string list attributes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.SearchReloadInput}</dt>
 * <dd>Search input with reload button for single-select capability typeaheads.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form.FilterReloadInput}</dt>
 * <dd>Filter input with reload button for multi-select capability typeaheads.</dd>
 * </dl>
 */
package org.jboss.hal.ui.resource.form;

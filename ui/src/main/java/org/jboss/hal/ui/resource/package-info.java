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
 * Shared abstractions and data types for WildFly management resource UI components.
 * <p>
 * This package defines the core data types and interfaces used across the resource sub-packages for viewing and editing WildFly
 * management resource attributes. Concrete UI components are organized in sub-packages by concern:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.form}</dt>
 * <dd>Editable form items for resource attributes (boolean, numeric, string, capability references, etc.).</dd>
 * <dt>{@link org.jboss.hal.ui.resource.view}</dt>
 * <dd>Read-only display of resource attributes using description lists.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.data}</dt>
 * <dd>View/edit state machine for resource attribute values.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.dialog}</dt>
 * <dd>Modal dialogs for resource CRUD operations and operation execution.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.finder}</dt>
 * <dd>Finder navigation support.</dd>
 * </dl>
 * <p>
 * Key types in this package:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.ItemIdentifier}</dt>
 * <dd>Utility for generating stable HTML element IDs for form and view items.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.ResourceAttribute}</dt>
 * <dd>Data holder for an attribute's fully-qualified name, value, description, and security context.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.ResourceItem}</dt>
 * <dd>Shared interface for form and view items with component context and identifier support.</dd>
 * </dl>
 */
package org.jboss.hal.ui.resource;

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
 * Composable UI components for viewing and interacting with WildFly management resources.
 * <p>
 * This package provides a layered component architecture for building resource views. Components can be composed together
 * via {@link org.jboss.hal.ui.resource.ResourceShell} or used individually. All components accept
 * {@link org.jboss.hal.meta.AddressTemplate} and {@link org.jboss.hal.meta.Metadata} at construction time, and those
 * requiring runtime data load it on attach via the Elemento {@code Attachable} pattern.
 * <p>
 * Top-level composable components:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.ResourceShell}</dt>
 * <dd>Layout shell that accepts breadcrumb, header, and content (tabs or resource list).</dd>
 * <dt>{@link org.jboss.hal.ui.resource.ResourceBreadcrumb}</dt>
 * <dd>Clickable breadcrumb trail for resource addresses with copy-to-clipboard.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.ResourceHeader}</dt>
 * <dd>Resource name, stability label, and description.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.ResourceTabs}</dt>
 * <dd>Tab container with Data, Attributes, Operations, and Capabilities perspectives.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.ResourceList}</dt>
 * <dd>Filterable list of child resources with add/remove/view actions.</dd>
 * </dl>
 * <p>
 * Sub-packages by concern:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.data}</dt>
 * <dd>View/edit state machine for resource attribute values.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.view}</dt>
 * <dd>Read-only display of resource attributes using description lists.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.form}</dt>
 * <dd>Editable form items for resource attributes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.dialog}</dt>
 * <dd>Modal dialogs for resource CRUD operations and operation execution.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.table}</dt>
 * <dd>Read-only metadata tables for attributes, operations, and capabilities.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.finder}</dt>
 * <dd>Finder navigation support.</dd>
 * </dl>
 * <p>
 * Shared data types in this package:
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

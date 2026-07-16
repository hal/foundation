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
 * Read-only metadata tables for WildFly management resource attributes, operations, and capabilities.
 * <p>
 * These tables display the management model metadata (attribute descriptions, operation signatures, capability declarations) in
 * a filterable table format. They are used by both the resource tabs and the model browser.
 * <p>
 * Key components:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.table.AttributesTable}</dt>
 * <dd>Filterable table showing all attribute definitions with type, storage, access type, and constraints.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.table.AttributesToolbar}</dt>
 * <dd>Toolbar with search and deprecation filter for the attributes table.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.table.OperationsTable}</dt>
 * <dd>Filterable table showing operation signatures with parameters and an execute button.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.table.OperationsToolbar}</dt>
 * <dd>Toolbar with search and global operations filter for the operations table.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.table.CapabilitiesTable}</dt>
 * <dd>Table showing declared and referenced capabilities for a resource.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.table.AttributeRow}</dt>
 * <dd>Row renderer that maps an {@code AttributeDescription} to a table row with type, storage, and constraint cells.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.table.AttributesFilter}</dt>
 * <dd>Multi-criteria filter for attribute descriptions (name, type, deprecation, storage, access type).</dd>
 * <dt>{@link org.jboss.hal.ui.resource.table.OperationsFilter}</dt>
 * <dd>Multi-criteria filter for operation descriptions (name, global operations toggle).</dd>
 * </dl>
 */
package org.jboss.hal.ui.resource.table;

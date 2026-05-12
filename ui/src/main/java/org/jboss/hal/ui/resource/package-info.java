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
 * Resource management UI components for viewing and editing WildFly management resource attributes through forms,
 * dialogs, and read-only views.
 * <p>
 * This package provides components for CRUD operations on WildFly management resources. It includes form items for
 * editing attributes, view items for displaying read-only values, dialogs for common operations, and utilities for
 * building finder-style navigation.
 * <p>
 * Key components include:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.ResourceForm}</dt>
 * <dd>Form for editing management resource attributes with validation and expression support.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.ResourceView}</dt>
 * <dd>Read-only view of management resource attributes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.ResourceDialogs}</dt>
 * <dd>Factory methods for creating resource management dialogs (add, reset, remove, execute operation).</dd>
 * <dt>{@link org.jboss.hal.ui.resource.ViewItemFactory}</dt>
 * <dd>Creates read-only view items for displaying management attribute values.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.FinderSupport}</dt>
 * <dd>Utility for building finder-style navigation paths from management resource addresses.</dd>
 * </dl>
 */
package org.jboss.hal.ui.resource;

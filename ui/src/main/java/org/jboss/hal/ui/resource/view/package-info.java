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
 * Read-only view components for displaying WildFly management resource attributes.
 * <p>
 * This package provides components for rendering resource attributes as read-only description lists. Each attribute is
 * displayed with type-appropriate formatting (booleans, expressions, capability references, complex types, etc.).
 * <p>
 * <h2>Type Hierarchy</h2>
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.view.ViewItem}</dt>
 * <dd>Core interface for read-only view items, extends {@code ResourceItem}.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.view.ViewItemDefaults}</dt>
 * <dd>Default constants and configurations shared across view item implementations.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.view.AbstractViewItem}</dt>
 * <dd>Base class for pipeline view items. Self-contained: receives a {@code ResolvedAttribute} and builds the complete
 * description list entry.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.view.DefaultViewItem}</dt>
 * <dd>Default view item for standard single-attribute display with type-appropriate formatting.</dd>
 * </dl>
 *
 * <h2>Composite View Items</h2>
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.view.CredentialReferenceViewItem}</dt>
 * <dd>Read-only display for credential-reference OBJECT attributes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.view.FileViewItem}</dt>
 * <dd>Read-only display for file (path + relative-to) OBJECT attributes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.view.MapViewItem}</dt>
 * <dd>Read-only display for MAP-typed attributes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.view.PathRelativeToViewItem}</dt>
 * <dd>Read-only display for sibling path + relative-to STRING pairs.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.view.TimeUnitViewItem}</dt>
 * <dd>Read-only display for time + unit OBJECT attributes.</dd>
 * </dl>
 *
 * <h2>Support</h2>
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.view.ResourceView}</dt>
 * <dd>Description list container for read-only attribute display with responsive layout.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.view.CapabilityReference}</dt>
 * <dd>Interactive capability reference display with navigation to the providing resource(s).</dd>
 * </dl>
 */
package org.jboss.hal.ui.resource.view;

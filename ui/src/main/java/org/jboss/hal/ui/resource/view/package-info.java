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
 * Key components:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.view.ResourceView}</dt>
 * <dd>Description list container for read-only attribute display with responsive layout.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.view.ViewItem}</dt>
 * <dd>Individual description list entry for a single attribute value.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.view.ViewItemFactory}</dt>
 * <dd>Creates type-appropriate view items based on attribute metadata.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.view.CapabilityReference}</dt>
 * <dd>Interactive capability reference display with navigation to the providing resource(s).</dd>
 * <dt>{@link org.jboss.hal.ui.resource.view.ViewItemProvider}</dt>
 * <dd>Strategy interface for creating specialised view items for specific attributes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.view.ViewItemProviders}</dt>
 * <dd>Registry of special view item providers consulted before the default rendering.</dd>
 * </dl>
 */
package org.jboss.hal.ui.resource.view;

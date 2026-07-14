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
 * Reusable UI fragments ("bricks") for composing the HAL management console. Each class groups related static factory
 * methods by domain, producing small PatternFly-based UI elements that are used across the console.
 * <p>
 * Key classes include:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.brick.AttributeBricks}</dt>
 * <dd>Attribute name rendering (with stability labels and deprecation indicators), detailed metadata descriptions, and
 * description popovers.</dd>
 * <dt>{@link org.jboss.hal.ui.brick.DescriptionBricks}</dt>
 * <dd>Shared description and deprecation rendering foundation used by {@code AttributeBricks}. Also provides
 * {@link org.jboss.hal.ui.brick.DescriptionBricks#operationDescription(org.jboss.hal.meta.description.OperationDescription)
 * operationDescription()} for operation descriptions and defines
 * {@link org.jboss.hal.ui.brick.DescriptionBricks.AttributeDescriptionContent} to control the level of detail in
 * attribute descriptions.</dd>
 * <dt>{@link org.jboss.hal.ui.brick.CodeBricks}</dt>
 * <dd>Truncatable code blocks for error messages and {@code ModelNode} JSON output.</dd>
 * <dt>{@link org.jboss.hal.ui.brick.ExpressionBricks}</dt>
 * <dd>Colour-coded rendering of WildFly expressions ({@code ${name:default}}) with nested expression support, and icon
 * suppliers for expression-related toggle buttons.</dd>
 * <dt>{@link org.jboss.hal.ui.brick.ServerStateBricks}</dt>
 * <dd>Colour-coded PatternFly labels for server runtime states: configuration state, running mode, running state, and
 * suspend state.</dd>
 * <dt>{@link org.jboss.hal.ui.brick.StabilityBricks}</dt>
 * <dd>Maps stability levels (experimental, preview, community, default) to PatternFly statuses and icons.</dd>
 * <dt>{@link org.jboss.hal.ui.brick.FinderBricks}</dt>
 * <dd>Finder column construction with CRUD actions and preview panel layouts.</dd>
 * <dt>{@link org.jboss.hal.ui.brick.EmptyStateBricks}</dt>
 * <dd>Reusable empty-state components for "no results", "no match", "no items", and "error" scenarios.</dd>
 * <dt>{@link org.jboss.hal.ui.brick.DomBricks}</dt>
 * <dd>General-purpose DOM manipulation utilities such as element toggling.</dd>
 * </dl>
 *
 * @see org.jboss.hal.ui
 */
package org.jboss.hal.ui.brick;

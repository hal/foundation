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
 * <dd>Attribute name, description, and popover rendering.</dd>
 * <dt>{@link org.jboss.hal.ui.brick.OperationBricks}</dt>
 * <dd>Operation description rendering.</dd>
 * <dt>{@link org.jboss.hal.ui.brick.CodeBricks}</dt>
 * <dd>Code block rendering for errors and model nodes.</dd>
 * <dt>{@link org.jboss.hal.ui.brick.ExpressionBricks}</dt>
 * <dd>Expression rendering and expression-related icons.</dd>
 * <dt>{@link org.jboss.hal.ui.brick.ServerStateBricks}</dt>
 * <dd>Server and host runtime state labels.</dd>
 * <dt>{@link org.jboss.hal.ui.brick.StabilityBricks}</dt>
 * <dd>Stability-related UI helpers.</dd>
 * <dt>{@link org.jboss.hal.ui.brick.FinderBricks}</dt>
 * <dd>Finder column construction, preview helpers, and empty states.</dd>
 * </dl>
 *
 * @see org.jboss.hal.ui
 */
package org.jboss.hal.ui.brick;

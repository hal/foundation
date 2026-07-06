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
 * View/edit state machine for WildFly management resource attribute values.
 * <p>
 * This package contains the central state machine that coordinates viewing and editing of resource attributes. It manages
 * transitions between view, edit, and error states, orchestrates resource loading via the {@code Dispatcher}, and combines
 * toolbar actions with attribute filtering.
 * <p>
 * Key components:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.data.ResourceData}</dt>
 * <dd>State machine orchestrating the view/edit/error lifecycle for resource attributes. Loads resource data on attach and
 * switches between {@link org.jboss.hal.ui.resource.view.ResourceView} (read-only) and
 * {@link org.jboss.hal.ui.resource.form.ResourceForm} (editable).</dd>
 * <dt>{@link org.jboss.hal.ui.resource.data.ResourceDataToolbar}</dt>
 * <dd>Action toolbar with attribute filters and context-aware buttons for view and edit modes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.data.ResourceFilter}</dt>
 * <dd>Multi-criteria attribute filter supporting name search, type, status, storage, and access type.</dd>
 * </dl>
 */
package org.jboss.hal.ui.resource.data;

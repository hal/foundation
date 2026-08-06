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
 * Composable layout and orchestration components for WildFly management resource views.
 * <p>
 * This package provides the outer frame for resource views and the state machine that drives the view/edit lifecycle:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.shell.ResourceShell}</dt>
 * <dd>Layout shell that accepts breadcrumb, header, and content (tabs or resource list).</dd>
 * <dt>{@link org.jboss.hal.ui.resource.shell.ResourceBreadcrumb}</dt>
 * <dd>Clickable breadcrumb trail for resource addresses with copy-to-clipboard.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.shell.ResourceHeader}</dt>
 * <dd>Resource name, stability label, and description.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.shell.ResourceTabs}</dt>
 * <dd>Tab container with Data, Attributes, Operations, and Capabilities perspectives. Customizable via
 * {@link org.jboss.hal.ui.resource.spi.ResourceTabsProvider}.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.shell.ResourceList}</dt>
 * <dd>Filterable list of child resources with add/remove/view actions.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.shell.ResourceData}</dt>
 * <dd>State machine orchestrating the view/edit/error lifecycle for resource attributes.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.shell.ResourceDataToolbar}</dt>
 * <dd>Toolbar with attribute filters and context-aware action buttons for {@link
 * org.jboss.hal.ui.resource.shell.ResourceData}.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.shell.ResourceFilter}</dt>
 * <dd>Multi-criteria attribute filter supporting name search, type, status, storage, access type, and expression
 * filtering.</dd>
 * </dl>
 */
package org.jboss.hal.ui.resource.shell;

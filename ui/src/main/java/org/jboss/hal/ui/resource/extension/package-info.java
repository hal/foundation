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
 * Extension points for the resource shell components. Providers are CDI beans that declare an address template
 * {@link org.jboss.hal.ui.resource.extension.ResourceHeaderProvider#scopes() scopes} and optional runtime
 * {@link org.jboss.hal.ui.resource.extension.ResourceHeaderProvider#appliesTo activation conditions}. The registries
 * ({@link org.jboss.hal.ui.resource.extension.ResourceHeaderRegistry},
 * {@link org.jboss.hal.ui.resource.extension.ResourceTabsRegistry}) collect all providers at startup and return the
 * best-matching one at lookup time. Providers that need runtime data (attribute values) inject the required services via CDI
 * and load data themselves. All registries are grouped in {@link org.jboss.hal.ui.resource.extension.ResourceExtensions}.
 */
package org.jboss.hal.ui.resource.extension;

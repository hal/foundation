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
 * SPI for customizing resource shell components. Providers are CDI beans that declare address template
 * {@link org.jboss.hal.ui.resource.spi.ResourceHeaderProvider#scopes() scopes} and optional runtime
 * {@link org.jboss.hal.ui.resource.spi.ResourceHeaderProvider#appliesTo activation conditions}. The registries
 * ({@link org.jboss.hal.ui.resource.spi.ResourceHeaderRegistry},
 * {@link org.jboss.hal.ui.resource.spi.ResourceTabsRegistry}) collect all providers at startup via CDI and return the
 * best-matching one at lookup time. Providers that need runtime data (attribute values) inject the required services via CDI
 * and load data themselves. All registries are grouped in {@link ResourceRegistries}.
 * <p>
 * External modules can contribute providers by placing CDI beans implementing
 * {@link org.jboss.hal.ui.resource.spi.ResourceHeaderProvider} or
 * {@link org.jboss.hal.ui.resource.spi.ResourceTabsProvider} on the classpath.
 */
package org.jboss.hal.ui.resource.spi;

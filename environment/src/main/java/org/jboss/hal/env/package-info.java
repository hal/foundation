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
 * Provides environment information, configuration, and stability tracking for the HAL management console.
 * <p>
 * This package contains the core services that manage runtime state, user preferences, and endpoint configuration:
 * <dl>
 *     <dt>{@link org.jboss.hal.env.Environment}</dt>
 *     <dd>Central registry of runtime information including product version, operation mode, stability level, and access
 *     control configuration.</dd>
 *
 *     <dt>{@link org.jboss.hal.env.Settings}</dt>
 *     <dd>Cookie-backed user preferences with typed value access for locale, display options, and other configurable
 *     settings.</dd>
 *
 *     <dt>{@link org.jboss.hal.env.Endpoints}</dt>
 *     <dd>Management endpoint URLs (DMR, upload, logout) derived from the console's origin and updated when connecting to
 *     different WildFly instances.</dd>
 *
 *     <dt>{@link org.jboss.hal.env.Version}</dt>
 *     <dd>Semantic version parsing and comparison for product and management API versions.</dd>
 *
 *     <dt>{@link org.jboss.hal.env.Stability}</dt>
 *     <dd>WildFly stability level ordering (default &lt; community &lt; preview &lt; experimental) used for feature
 *     availability checks.</dd>
 * </dl>
 *
 * <h2>Usage Example</h2>
 * {@snippet :
 *     // Check environment state
 *     if (environment.standalone()) {
 *         Version version = environment.productVersion();
 *         Stability stability = environment.serverStability();
 *     }
 *
 *     // Read a setting
 *     Settings.Value locale = settings.get(Settings.Key.LOCALE);
 * }
 */
package org.jboss.hal.env;

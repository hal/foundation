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
 * Resource definitions, constants, and UI utilities for the HAL management console. This package provides centralized access to
 * identifiers, styles, URLs, and localization bundles used across the application.
 *
 * <p>
 * <strong>Key Interfaces:</strong>
 *
 * <dl>
 * <dt>{@link org.jboss.hal.resources.OuiaIds}</dt>
 * <dd>Static OUIA IDs for QA automation and dynamic OUIA ID composition via {@link org.jboss.hal.resources.OuiaIds#ouia(String, String...)}</dd>
 *
 * <dt>{@link org.jboss.hal.resources.Ids}</dt>
 * <dd>Non-OUIA HTML element IDs (not synced to the test suite)</dd>
 *
 * <dt>{@link org.jboss.hal.resources.Keys}</dt>
 * <dd>Typed keys for map-like component contexts</dd>
 *
 * <dt>{@link org.jboss.hal.resources.HalClasses}</dt>
 * <dd>CSS class name constants and BEM-style generators</dd>
 *
 * <dt>{@link org.jboss.hal.resources.Names}</dt>
 * <dd>Untranslatable technical terms and display names</dd>
 *
 * <dt>{@link org.jboss.hal.resources.Urls}</dt>
 * <dd>External links with version placeholder support</dd>
 *
 * <dt>{@link org.jboss.hal.resources.Dataset}</dt>
 * <dd>HTML data attribute names</dd>
 *
 * <dt>{@link org.jboss.hal.resources.LocalStorage}</dt>
 * <dd>Browser {@code localStorage} keys</dd>
 *
 * <dt>{@link org.jboss.hal.resources.L10nBundle}</dt>
 * <dd>Localization bundle for translated strings</dd>
 * </dl>
 *
 * <p>
 * <strong>Example Usage:</strong>
 *
 * {@snippet :
 *     // Use a static OUIA ID
 *     String id = OuiaIds.MASTHEAD;
 *
 *     // Compose a dynamic OUIA ID
 *     String dynamicId = OuiaIds.ouia("operation", "read-resource", "execute", "btn");
 *
 *     // Build a BEM-style CSS class
 *     String css = HalClasses.halComponent("resource", "header");
 * }
 */
package org.jboss.hal.resources;

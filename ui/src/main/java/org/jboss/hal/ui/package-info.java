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
 * Provides UI components and utilities for the HAL management console, built on PatternFly 6 with Elemento for DOM
 * abstraction. The components in this package are designed to integrate with WildFly's management model and provide a
 * consistent user experience.
 * <p>
 * The package includes:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.BuildingBlocks}</dt>
 * <dd>Reusable UI fragments including status icons, stability badges, empty states, and helper components for consistent
 * visual patterns across the console.</dd>
 * <dt>{@link org.jboss.hal.ui.UIContext}</dt>
 * <dd>Central access point for shared UI services including environment configuration, user settings, dispatcher for DMR
 * operations, and metadata repository.</dd>
 * <dt>{@link org.jboss.hal.ui.Format}</dt>
 * <dd>Locale-aware formatters for file sizes, durations, and percentages using the Internationalization API.</dd>
 * <dt>{@link org.jboss.hal.ui.StabilityLabel}</dt>
 * <dd>PatternFly label component displaying WildFly stability levels with appropriate icons and colors.</dd>
 * </dl>
 *
 * <h2>Example Usage</h2>
 * {@snippet :
 *     // Format a file size
 *     String size = Format.humanReadableBytes(1_048_576);
 *
 *     // Create a stability label
 *     StabilityLabel label = StabilityLabel.stabilityLabel(Stability.PREVIEW);
 *
 *     // Access shared UI services
 *     UIContext uic = UIContext.uic();
 *     Environment env = uic.environment();
 * }
 *
 * @see org.jboss.hal.ui.filter
 * @see org.jboss.hal.ui.modelbrowser
 * @see org.jboss.hal.ui.resource
 */
package org.jboss.hal.ui;

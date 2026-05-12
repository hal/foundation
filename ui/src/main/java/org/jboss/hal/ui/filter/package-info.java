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
 * Filter UI components for the model browser, providing multi-select dropdowns, search inputs, and filter label displays
 * for narrowing management model entries.
 * <p>
 * These components integrate with PatternFly's filter framework and the model browser's attribute and operation filtering
 * system. They provide a consistent user experience for filtering management resources, attributes, and operations by
 * various criteria such as name, type, storage mode, deprecation status, and more.
 * <p>
 * Key components include:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.filter.NameSearchInput}</dt>
 * <dd>Search input for filtering entries by name with autocomplete support.</dd>
 * <dt>{@link org.jboss.hal.ui.filter.MultiSelects}</dt>
 * <dd>Factory methods for creating standard filter multi-select components.</dd>
 * <dt>{@link org.jboss.hal.ui.filter.FilterLabels}</dt>
 * <dd>Displays active filter selections as removable PatternFly labels.</dd>
 * <dt>{@link org.jboss.hal.ui.filter.ItemCount}</dt>
 * <dd>Displays the count of visible items relative to the total.</dd>
 * </dl>
 */
package org.jboss.hal.ui.filter;

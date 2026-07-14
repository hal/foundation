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
 * Grouping strategies for organizing {@link org.jboss.hal.ui.resource.ResourceItem}s into named sections.
 * <p>
 * Key components:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.grouping.GroupingSupport}</dt>
 * <dd>Shared grouping logic for resource items, supporting metadata-based grouping and auto-grouping for large item
 * sets.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.grouping.AutoGrouping}</dt>
 * <dd>Groups items into alphabetical letter-range chunks for resources without metadata-defined attribute groups.</dd>
 * </dl>
 */
package org.jboss.hal.ui.resource.grouping;

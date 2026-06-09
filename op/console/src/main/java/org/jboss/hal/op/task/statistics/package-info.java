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
 * Statistics task implementation for enabling and disabling the {@code statistics-enabled} attribute across WildFly
 * subsystems. The {@link org.jboss.hal.op.task.statistics.StatisticsTask} traverses the management model tree, collects
 * resources with the attribute, and presents a UI with two sections:
 *
 * <ul>
 *     <li>{@link org.jboss.hal.op.task.statistics.ResourcesSection} - table of resources with filtering, bulk selection,
 *         and update capabilities</li>
 *     <li>{@link org.jboss.hal.op.task.statistics.ExpressionsSection} - table of expression keys found in attribute values
 *         with system property management</li>
 * </ul>
 */
package org.jboss.hal.op.task.statistics;

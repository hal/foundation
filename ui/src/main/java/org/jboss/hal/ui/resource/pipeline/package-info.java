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
 * Attribute-to-item pipeline that transforms resource metadata into view and form items.
 * <p>
 * The pipeline has two stages:
 * <ol>
 *     <li><b>Group</b> — {@link org.jboss.hal.ui.resource.pipeline.AttributeMatcher}s scan the attribute pool in priority order, claiming
 *         groups of related attributes into {@link org.jboss.hal.ui.resource.pipeline.AttributeGroup}s.</li>
 *     <li><b>Itemize</b> — {@link org.jboss.hal.ui.resource.pipeline.ItemProvider}s create
 *         {@link org.jboss.hal.ui.resource.pipeline.PipelineViewItem}s or
 *         {@link org.jboss.hal.ui.resource.pipeline.PipelineFormItem}s for each group.</li>
 * </ol>
 * <p>
 * Entry point: {@link org.jboss.hal.ui.resource.pipeline.Pipeline#create()}.
 *
 * @see org.jboss.hal.ui.resource.pipeline.Pipeline
 */
package org.jboss.hal.ui.resource.pipeline;

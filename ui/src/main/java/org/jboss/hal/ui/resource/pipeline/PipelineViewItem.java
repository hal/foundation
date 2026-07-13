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
package org.jboss.hal.ui.resource.pipeline;

import elemental2.dom.HTMLElement;

import org.jboss.elemento.IsElement;

/**
 * A read-only view item produced by the pipeline. Implementations wrap a PatternFly {@code DescriptionListGroup} or similar
 * component for displaying attribute values.
 */
public interface PipelineViewItem extends IsElement<HTMLElement> {

    /** Returns a unique identifier for this view item, suitable for use as a DOM element ID. */
    String identifier();
}

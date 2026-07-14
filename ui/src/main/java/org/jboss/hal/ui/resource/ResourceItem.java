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
package org.jboss.hal.ui.resource;

import org.jboss.elemento.IsElement;
import org.jboss.hal.ui.resource.pipeline.ResolvedAttribute;

import elemental2.dom.HTMLElement;

/**
 * Common contract for pipeline-produced items (view items and form items). Provides a unique identifier and the resolved
 * attribute the item represents, used for filtering and grouping.
 *
 * @see org.jboss.hal.ui.resource.view.ViewItem
 * @see org.jboss.hal.ui.resource.form.FormItem
 */
public interface ResourceItem extends IsElement<HTMLElement> {

    /** Returns a unique identifier for this item, suitable for use as a DOM element ID. */
    String identifier();

    /** Returns the primary resolved attribute this item represents. Used for filtering and grouping. */
    ResolvedAttribute attribute();
}

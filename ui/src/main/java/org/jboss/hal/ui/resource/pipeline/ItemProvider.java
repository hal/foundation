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

import org.jboss.hal.ui.resource.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;

import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.view.ViewItem;

/**
 * Leaf-level item factory that operates in the value world — receives already-resolved attributes and produces view or form
 * items. Providers are the common exit path for all attributes: both unclaimed top-level attributes and child attributes
 * delegated by handlers pass through the provider chain.
 * <p>
 * Providers are tried in registration order; the first one whose {@link #handles(ResolvedAttribute)} returns {@code true}
 * creates the item. Default methods return {@code null}, allowing a provider to be form-only or view-only.
 *
 * @see AttributeHandler
 * @see Pipeline
 */
public interface ItemProvider {

    /** Tests whether this provider handles the given resolved attribute. */
    boolean handles(ResolvedAttribute ra);

    /**
     * Creates a view item for the given resolved attribute. Returns {@code null} to fall through to the next provider.
     */
    default ViewItem viewItem(PipelineContext context, ResolvedAttribute ra) {
        return null;
    }

    /**
     * Creates a form item for the given resolved attribute. Returns {@code null} to fall through to the next provider.
     */
    default FormItem formItem(PipelineContext context, ResolvedAttribute ra) {
        return null;
    }
}

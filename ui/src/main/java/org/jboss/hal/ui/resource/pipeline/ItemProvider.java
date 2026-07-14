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

import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import java.util.List;

/**
 * Stage 2 of the pipeline: creates view and form items for matched attribute groups. Providers are tried in registration order;
 * the first one whose {@link #matches(AttributeMatch)} returns {@code true} handles the group.
 * <p>
 * Default methods return {@code null}, which signals "use default rendering for this mode." This allows a provider to be
 * "FIP only" (custom form item, default view item) or "VIP only" (custom view item, default form item).
 * <p>
 * The {@link #viewItems(AttributeMatch, PipelineContext)} and {@link #formItems(AttributeMatch, PipelineContext)} methods
 * return lists because a provider may produce multiple items from a single group (e.g. the default provider flattens OBJECT
 * simpleRecords into n items).
 */
public interface ItemProvider {

    /** Tests whether this provider handles the given attribute group. */
    boolean matches(AttributeMatch group);

    /**
     * Creates view items for the given group. Returns {@code null} to fall through to the next provider or default rendering.
     * Most providers return a single-element list; the default provider may return multiple items for flattened OBJECT
     * simpleRecords.
     */
    default List<ViewItem> viewItems(AttributeMatch group, PipelineContext context) {
        return null;
    }

    /**
     * Creates form items for the given group. Returns {@code null} to fall through to the next provider or default rendering.
     * Most providers return a single-element list; the default provider may return multiple items for flattened OBJECT
     * simpleRecords.
     */
    default List<FormItem> formItems(AttributeMatch group, PipelineContext context) {
        return null;
    }
}

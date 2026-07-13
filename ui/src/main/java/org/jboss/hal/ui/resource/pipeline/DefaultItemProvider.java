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

import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Catch-all provider that handles all unmatched single-attribute groups with type-based rendering. Must be registered last in the
 * provider chain, after {@link FlatteningProvider} (which handles simpleRecord OBJECTs).
 * <p>
 * TODO: Replace placeholder items with proper type-specific implementations (expression support, validation, modification
 * tracking, capability references, allowed values, RBAC restrictions).
 */
class DefaultItemProvider implements ItemProvider {

    @Override
    public boolean matches(AttributeGroup group) {
        return true;
    }

    @Override
    public List<ViewItem> viewItems(AttributeGroup group, PipelineContext context) {
        return singletonList(new PlaceholderViewItem(group.primary().name(), group, context));
    }

    @Override
    public List<FormItem> formItems(AttributeGroup group, PipelineContext context) {
        return singletonList(new PlaceholderFormItem(group.primary().name(), group, context));
    }
}

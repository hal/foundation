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

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.AttributeDescriptions;

import static java.util.Collections.singletonList;

/**
 * Catch-all provider that handles all unmatched attribute groups. Must be registered last in the provider chain.
 * <p>
 * Handles two cases:
 * <ul>
 *     <li><b>OBJECT simpleRecord:</b> Flattens the OBJECT into n items, one per sub-attribute. Each sub-attribute uses its
 *         {@link AttributeDescription#fullyQualifiedName()} for DMR writes (e.g. {@code "file.path"}).</li>
 *     <li><b>Single attribute:</b> Creates a type-based item (text input for STRING, checkbox for BOOLEAN, number input for
 *         INT/LONG/DOUBLE, etc.).</li>
 * </ul>
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
    public List<PipelineViewItem> viewItems(AttributeGroup group, PipelineContext context) {
        if (group.isSingle() && group.primary().simpleRecord()) {
            return flattenViewItems(group.primary(), context);
        }
        return singletonList(new PlaceholderViewItem(group.primary().name(), group, context));
    }

    @Override
    public List<PipelineFormItem> formItems(AttributeGroup group, PipelineContext context) {
        if (group.isSingle() && group.primary().simpleRecord()) {
            return flattenFormItems(group.primary(), context);
        }
        return singletonList(new PlaceholderFormItem(group.primary().name(), group, context));
    }

    private List<PipelineViewItem> flattenViewItems(AttributeDescription parent, PipelineContext context) {
        AttributeDescriptions nested = parent.valueTypeAttributeDescriptions();
        List<PipelineViewItem> items = new ArrayList<>();
        for (AttributeDescription nad : nested) {
            AttributeGroup subGroup = AttributeGroup.single(nad);
            items.add(new PlaceholderViewItem(nad.fullyQualifiedName(), subGroup, context));
        }
        return items;
    }

    private List<PipelineFormItem> flattenFormItems(AttributeDescription parent, PipelineContext context) {
        AttributeDescriptions nested = parent.valueTypeAttributeDescriptions();
        List<PipelineFormItem> items = new ArrayList<>();
        for (AttributeDescription nad : nested) {
            AttributeGroup subGroup = AttributeGroup.single(nad);
            items.add(new PlaceholderFormItem(nad.fullyQualifiedName(), subGroup, context));
        }
        return items;
    }
}

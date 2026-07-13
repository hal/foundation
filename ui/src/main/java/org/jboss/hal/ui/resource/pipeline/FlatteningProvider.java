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

/**
 * Provider for unclaimed OBJECT simpleRecord attributes. Flattens the OBJECT into n items, one per sub-attribute. Each
 * sub-attribute uses its {@link AttributeDescription#fullyQualifiedName()} for DMR writes (e.g. {@code "my-record.foo"}).
 * <p>
 * Must be registered after all composite providers (which claim known OBJECT structures) and before
 * {@link DefaultItemProvider} (which handles single non-OBJECT attributes).
 */
class FlatteningProvider implements ItemProvider {

    @Override
    public boolean matches(AttributeGroup group) {
        return group.isSingle() && group.primary().simpleRecord();
    }

    @Override
    public List<ViewItem> viewItems(AttributeGroup group, PipelineContext context) {
        return flattenViewItems(group.primary(), context);
    }

    @Override
    public List<FormItem> formItems(AttributeGroup group, PipelineContext context) {
        return flattenFormItems(group.primary(), context);
    }

    private List<ViewItem> flattenViewItems(AttributeDescription parent, PipelineContext context) {
        AttributeDescriptions nested = parent.valueTypeAttributeDescriptions();
        List<ViewItem> items = new ArrayList<>();
        for (AttributeDescription nad : nested) {
            AttributeGroup subGroup = AttributeGroup.single(nad);
            items.add(new PlaceholderViewItem(nad.fullyQualifiedName(), subGroup, context));
        }
        return items;
    }

    private List<FormItem> flattenFormItems(AttributeDescription parent, PipelineContext context) {
        AttributeDescriptions nested = parent.valueTypeAttributeDescriptions();
        List<FormItem> items = new ArrayList<>();
        for (AttributeDescription nad : nested) {
            AttributeGroup subGroup = AttributeGroup.single(nad);
            items.add(new PlaceholderFormItem(nad.fullyQualifiedName(), subGroup, context));
        }
        return items;
    }
}

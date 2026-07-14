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

import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.form.MapControl;
import org.jboss.hal.ui.resource.form.MapOperationStrategy;
import org.jboss.hal.ui.resource.form.StandardFormItem;
import org.jboss.hal.ui.resource.view.MapViewItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.jboss.hal.ui.resource.pipeline.AttributeMatcher.hasSimpleValueType;

/**
 * Provider for free-form key-value map attributes. Matches OBJECT attributes with a simple VALUE_TYPE (detected by
 * {@link MapMatcher} in stage 1). Creates {@link MapViewItem} for read-only display and a {@link StandardFormItem} with
 * {@link MapControl} for editing.
 */
class MapProvider implements ItemProvider {

    @Override
    public boolean matches(AttributeMatch group) {
        return group.isSingle() && hasSimpleValueType(group.primary());
    }

    @Override
    public List<ViewItem> viewItems(AttributeMatch group, PipelineContext context) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(group.primary(), context);
        return singletonList(new MapViewItem(ra.fqn(), ra, context));
    }

    @Override
    public List<FormItem> formItems(AttributeMatch group, PipelineContext context) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(group.primary(), context);
        return singletonList(new StandardFormItem<>(ra.fqn(), ra, context,
                new MapControl(), MapOperationStrategy.INSTANCE));
    }
}

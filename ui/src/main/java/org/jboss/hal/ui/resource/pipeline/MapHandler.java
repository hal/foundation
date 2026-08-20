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

import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.ui.resource.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.form.DefaultFormItem;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.form.MapControl;
import org.jboss.hal.ui.resource.form.MapOperationStrategy;
import org.jboss.hal.ui.resource.view.MapViewItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import static java.util.Collections.singletonList;
import static org.jboss.hal.ui.resource.pipeline.AttributeHandler.hasSimpleValueType;
import static org.jboss.hal.ui.resource.pipeline.AttributeHandler.partition;

/**
 * Handler for free-form key-value map attributes. Claims OBJECT attributes with a simple scalar VALUE_TYPE.
 */
class MapHandler implements AttributeHandler {

    @Override
    public MatchResult match(List<AttributeDescription> pool) {
        return partition(pool, ad -> hasSimpleValueType(ad));
    }

    @Override
    public List<ViewItem> viewItems(PipelineContext context, AttributeMatch match) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(context, match.primary());
        return singletonList(new MapViewItem(context, ra.fqn(), ra));
    }

    @Override
    public List<FormItem> formItems(PipelineContext context, AttributeMatch match) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(context, match.primary());
        return singletonList(new DefaultFormItem<>(context, ra.fqn(), ra,
                new MapControl(), MapOperationStrategy.INSTANCE));
    }
}

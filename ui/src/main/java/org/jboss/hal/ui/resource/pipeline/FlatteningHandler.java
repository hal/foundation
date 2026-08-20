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
import org.jboss.hal.ui.resource.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import static org.jboss.hal.ui.resource.pipeline.AttributeHandler.partition;

/**
 * Handler for simpleRecord OBJECT attributes. Claims OBJECTs whose sub-attributes are all simple types and flattens them into
 * n items, one per sub-attribute. Each child inherits the parent's RBAC state and uses its
 * {@link AttributeDescription#fullyQualifiedName()} for DMR writes.
 * <p>
 * Must run after all composite handlers (credential-reference, time-unit, file, map) which claim known OBJECT structures at
 * higher priority.
 */
public class FlatteningHandler implements AttributeHandler {

    @Override
    public MatchResult match(List<AttributeDescription> pool) {
        return partition(pool, AttributeDescription::simpleRecord);
    }

    @Override
    public List<ViewItem> viewItems(PipelineContext context, AttributeMatch match) {
        ResolvedAttribute parent = ResolvedAttribute.resolve(context, match.primary());
        AttributeDescriptions childDescriptions = parent.description().valueTypeAttributeDescriptions();
        List<ViewItem> items = new ArrayList<>();
        for (AttributeDescription childDescription : childDescriptions) {
            ResolvedAttribute child = parent.child(childDescription.name());
            ViewItem viewItem = Pipeline.instance().viewItem(context, child);
            if (viewItem != null) {
                items.add(viewItem);
            }
        }
        return items;
    }

    @Override
    public List<FormItem> formItems(PipelineContext context, AttributeMatch match) {
        ResolvedAttribute parent = ResolvedAttribute.resolve(context, match.primary());
        AttributeDescriptions childDescriptions = parent.description().valueTypeAttributeDescriptions();
        List<FormItem> items = new ArrayList<>();
        for (AttributeDescription childDescription : childDescriptions) {
            ResolvedAttribute child = parent.child(childDescription.name());
            FormItem formItem = Pipeline.instance().formItem(context, child);
            if (formItem != null) {
                items.add(formItem);
            }
        }
        return items;
    }
}

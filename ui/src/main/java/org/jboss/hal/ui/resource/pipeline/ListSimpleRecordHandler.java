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
import org.jboss.hal.ui.resource.form.ListSimpleRecordControl;
import org.jboss.hal.ui.resource.view.ListSimpleRecordViewItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import static java.util.Collections.singletonList;
import static org.jboss.hal.ui.resource.pipeline.AttributeHandler.partition;

/**
 * Handler for LIST attributes whose value-type is an OBJECT with all simple sub-attributes. It claims LIST attributes that
 * match the {@link AttributeDescription#listOfSimpleRecords()} pattern and produces table-based view/form items.
 * <p>
 * Must run before {@link FlatteningHandler} but after all composite handlers. The handler covers attributes like
 * {@code global-modules}, {@code match-rules}, {@code permissions}, {@code realms}, {@code filters}, and others where each list
 * entry is a flat record of simple fields.
 */
class ListSimpleRecordHandler implements AttributeHandler {

    @Override
    public MatchResult match(List<AttributeDescription> pool) {
        return partition(pool, AttributeDescription::listOfSimpleRecords);
    }

    @Override
    public List<ViewItem> viewItems(PipelineContext context, AttributeMatch match) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(context, match.primary());
        return singletonList(new ListSimpleRecordViewItem(context, ra.fqn(), ra));
    }

    @Override
    public List<FormItem> formItems(PipelineContext context, AttributeMatch match) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(context, match.primary());
        return singletonList(new DefaultFormItem<>(context, ra.fqn(), ra, new ListSimpleRecordControl()));
    }
}

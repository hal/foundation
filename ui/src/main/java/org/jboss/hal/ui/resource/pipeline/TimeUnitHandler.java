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

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.form.DefaultFormItem;
import org.jboss.hal.ui.resource.form.TimeUnitControl;
import org.jboss.hal.ui.resource.view.TimeUnitViewItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import static java.util.Collections.singletonList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNIT;
import static org.jboss.hal.ui.resource.pipeline.AttributeHandler.hasObjectValueType;
import static org.jboss.hal.ui.resource.pipeline.AttributeHandler.partition;

/**
 * Handler for time-unit composite attributes (e.g. {@code keepalive-time}). Claims OBJECT attributes with the time-unit
 * structure ({@code time} + {@code unit}).
 */
class TimeUnitHandler implements AttributeHandler {

    /** Returns the time value from a keepalive-time model node, or -1 if undefined. */
    public static long time(ModelNode value) {
        if (value.isDefined() && value.hasDefined(TIME)) {
            return value.get(TIME).asLong();
        }
        return -1;
    }

    /** Returns the unit value from a keepalive-time model node, or {@code null} if undefined. */
    public static String unit(ModelNode value) {
        if (value.isDefined() && value.hasDefined(UNIT)) {
            return value.get(UNIT).asString();
        }
        return null;
    }

    @Override
    public MatchResult match(List<AttributeDescription> pool) {
        return partition(pool, ad -> hasObjectValueType(ad, TIME, UNIT));
    }

    @Override
    public List<ViewItem> viewItems(PipelineContext context, AttributeMatch match) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(context, match.primary());
        return singletonList(new TimeUnitViewItem(context, ra.fqn(), ra));
    }

    @Override
    public List<FormItem> formItems(PipelineContext context, AttributeMatch match) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(context, match.primary());
        return singletonList(new DefaultFormItem<>(context, ra.fqn(), ra, new TimeUnitControl()));
    }
}

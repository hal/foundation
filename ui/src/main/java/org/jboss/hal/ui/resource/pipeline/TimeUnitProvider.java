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
import org.jboss.hal.ui.resource.form.StandardFormItem;
import org.jboss.hal.ui.resource.form.TimeUnitControl;
import org.jboss.hal.ui.resource.view.TimeUnitViewItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import java.util.List;

import org.jboss.hal.dmr.ModelNode;

import static java.util.Collections.singletonList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNIT;
import static org.jboss.hal.ui.resource.pipeline.AttributeMatcher.hasObjectValueType;

/**
 * Provider for time-unit composite attributes (e.g. {@code keepalive-time}). Matches groups containing a single OBJECT attribute
 * with the time-unit structure ({@code time} + {@code unit}).
 */
public class TimeUnitProvider implements ItemProvider {

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
    public boolean matches(AttributeMatch match) {
        return match.isSingle() && hasObjectValueType(match.primary(), TIME, UNIT);
    }

    @Override
    public List<ViewItem> viewItems(PipelineContext context, AttributeMatch match) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(context, match.primary());
        return singletonList(new TimeUnitViewItem(context, ra.fqn(), ra));
    }

    @Override
    public List<FormItem> formItems(PipelineContext context, AttributeMatch match) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(context, match.primary());
        return singletonList(new StandardFormItem<>(ra.fqn(), ra, context, new TimeUnitControl()));
    }
}

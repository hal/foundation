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
import org.jboss.hal.ui.resource.form.StringFormItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;

/**
 * Provider for standalone {@code *relative-to} attributes that were not claimed by the {@link PathRelativeToMatcher} (no
 * sibling path attribute found). FIP only — uses default view rendering.
 * <p>
 * Only 1 non-deprecated occurrence exists: {@code ejb3/file-passivation-store=*}.
 */
// TODO Replace with typeahead populated from /path=* and standard paths
class RelativeToProvider implements ItemProvider {

    @Override
    public boolean matches(AttributeGroup group) {
        if (!group.isSingle()) {
            return false;
        }
        return group.primary().name().endsWith(RELATIVE_TO);
    }

    @Override
    public List<FormItem> formItems(AttributeGroup group, PipelineContext context) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(group.primary(), context);
        return singletonList(new StringFormItem(ra.fqn(), ra, context));
    }
}

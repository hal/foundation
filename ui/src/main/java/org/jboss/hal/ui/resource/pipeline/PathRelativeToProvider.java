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
import org.jboss.hal.ui.resource.form.PathRelativeToFormItem;
import org.jboss.hal.ui.resource.view.PathRelativeToViewItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;

/**
 * Provider for sibling path + relative-to attribute groups. Matches groups claimed by {@link PathRelativeToMatcher} — two
 * separate top-level STRING attributes that are semantically coupled.
 * <p>
 * This is the sibling counterpart to {@link FileProvider}, which handles the same concept as a composite OBJECT attribute. Both
 * render identically but differ in DMR write semantics: this provider produces 2 separate {@code write-attribute} operations
 * (one per attribute), while {@link FileProvider} produces 1 operation writing the whole OBJECT.
 */
class PathRelativeToProvider implements ItemProvider {

    @Override
    public boolean matches(AttributeMatch match) {
        if (match.size() != 2) {
            return false;
        }
        return match.attributes().stream()
                .anyMatch(ad -> ad.name().endsWith(RELATIVE_TO));
    }

    @Override
    public List<ViewItem> viewItems(AttributeMatch match, PipelineContext context) {
        List<ResolvedAttribute> resolved = match.resolveAll(context);
        return singletonList(new PathRelativeToViewItem(match.name(), resolved, context));
    }

    @Override
    public List<FormItem> formItems(AttributeMatch match, PipelineContext context) {
        List<ResolvedAttribute> resolved = match.resolveAll(context);
        return singletonList(new PathRelativeToFormItem(match.name(), resolved, context));
    }
}

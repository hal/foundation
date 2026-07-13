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

import static java.util.Collections.singletonList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;

/**
 * Provider for sibling path + relative-to attribute groups. Matches groups claimed by {@link PathRelativeToMatcher} — two
 * separate top-level STRING attributes that are semantically coupled.
 * <p>
 * This is the sibling counterpart to {@link FileProvider}, which handles the same concept as a composite OBJECT attribute. Both
 * render identically but differ in DMR write semantics: this provider produces 2 separate {@code write-attribute} operations
 * (one per attribute), while {@link FileProvider} produces 1 operation writing the whole OBJECT.
 * <p>
 * TODO: Replace placeholder items with proper path + relative-to UI (text input + path typeahead/dropdown, consolidated view
 * display).
 */
class PathRelativeToProvider implements ItemProvider {

    @Override
    public boolean matches(AttributeGroup group) {
        if (group.size() != 2) {
            return false;
        }
        return group.attributes().stream()
                .anyMatch(ad -> ad.name().endsWith(RELATIVE_TO));
    }

    @Override
    public List<PipelineViewItem> viewItems(AttributeGroup group, PipelineContext context) {
        return singletonList(new PlaceholderViewItem(group.name(), group, context));
    }

    @Override
    public List<PipelineFormItem> formItems(AttributeGroup group, PipelineContext context) {
        return singletonList(new PlaceholderFormItem(group.name(), group, context));
    }
}

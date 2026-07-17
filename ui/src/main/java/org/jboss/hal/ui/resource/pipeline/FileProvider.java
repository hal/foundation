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
import org.jboss.hal.ui.resource.form.FileControl;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.form.StandardFormItem;
import org.jboss.hal.ui.resource.view.FileViewItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;
import static org.jboss.hal.ui.resource.pipeline.AttributeMatcher.hasObjectValueType;

/**
 * Provider for the composite {@code file} attribute ({@code {path: STRING, relative-to: STRING}}). Used in the logging
 * subsystem. Renders path and relative-to as a single visual unit.
 * <p>
 * This is the composite counterpart to {@link PathRelativeToProvider}, which handles the same concept at the sibling attribute
 * level.
 */
class FileProvider implements ItemProvider {

    @Override
    public boolean matches(AttributeMatch match) {
        return match.isSingle() && hasObjectValueType(match.primary(), PATH, RELATIVE_TO);
    }

    @Override
    public List<ViewItem> viewItems(PipelineContext context, AttributeMatch match) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(context, match.primary());
        return singletonList(new FileViewItem(context, ra.fqn(), ra));
    }

    @Override
    public List<FormItem> formItems(PipelineContext context, AttributeMatch match) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(context, match.primary());
        return singletonList(new StandardFormItem<>(ra.fqn(), ra, context, new FileControl()));
    }
}

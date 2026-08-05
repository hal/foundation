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

import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.form.RelativeToControl;
import org.jboss.hal.ui.resource.form.DefaultFormItem;

import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;

/**
 * Provider for standalone {@code *relative-to} attributes that were not claimed by the {@link PathRelativeToHandler} (no
 * sibling path attribute found). Form-item-only — uses default view rendering.
 */
class RelativeToProvider implements ItemProvider {

    @Override
    public boolean handles(ResolvedAttribute ra) {
        return ra.name().endsWith(RELATIVE_TO);
    }

    @Override
    public FormItem formItem(PipelineContext context, ResolvedAttribute ra) {
        return new DefaultFormItem<>(context, ra.fqn(), ra, new RelativeToControl());
    }
}

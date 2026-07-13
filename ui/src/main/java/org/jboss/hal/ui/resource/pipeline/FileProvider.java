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

import org.jboss.hal.ui.resource.form.FileFormItem;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.view.FileViewItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.description.AttributeDescription;

import static java.util.Collections.singletonList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;

/**
 * Provider for the composite {@code file} attribute ({@code {path: STRING, relative-to: STRING}}). Used in the logging
 * subsystem. Renders path and relative-to as a single visual unit.
 * <p>
 * This is the composite counterpart to {@link PathRelativeToProvider}, which handles the same concept at the sibling attribute
 * level. Both can share UI components.
 * <p>
 */
class FileProvider implements ItemProvider {

    @Override
    public boolean matches(AttributeGroup group) {
        if (!group.isSingle()) {
            return false;
        }
        AttributeDescription ad = group.primary();
        try {
            ModelType type = ad.get(TYPE).asType();
            if (type != ModelType.OBJECT || !ad.hasDefined(VALUE_TYPE)) {
                return false;
            }
            if (ad.get(VALUE_TYPE).getType() != ModelType.OBJECT) {
                return false;
            }
            ModelNode valueType = ad.get(VALUE_TYPE);
            return valueType.has(PATH) && valueType.has(RELATIVE_TO);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public List<ViewItem> viewItems(AttributeGroup group, PipelineContext context) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(group.primary(), context);
        return singletonList(new FileViewItem(ra.fqn(), ra, context));
    }

    @Override
    public List<FormItem> formItems(AttributeGroup group, PipelineContext context) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(group.primary(), context);
        return singletonList(new FileFormItem(ra.fqn(), ra, context));
    }
}

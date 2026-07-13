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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.description.AttributeDescription;

import static java.util.Collections.singletonList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAR_TEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;

/**
 * Provider for credential reference composite attributes. Matches groups containing a single OBJECT attribute with the
 * credential reference structure ({@code store}, {@code alias}, {@code clear-text}).
 * <p>
 * TODO: Replace placeholder items with proper credential reference UI (radio mode selection, store typeahead, clear-text
 * toggle) during migration from the existing {@code CredentialReferenceFormItem} and {@code CredentialReferenceView}.
 */
class CredentialReferenceProvider implements ItemProvider {

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
            return valueType.has(STORE) && valueType.has(ALIAS) && valueType.has(CLEAR_TEXT);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public List<PipelineViewItem> viewItems(AttributeGroup group, PipelineContext context) {
        return singletonList(new PlaceholderViewItem(group.primary().name(), group, context));
    }

    @Override
    public List<PipelineFormItem> formItems(AttributeGroup group, PipelineContext context) {
        return singletonList(new PlaceholderFormItem(group.primary().name(), group, context));
    }
}

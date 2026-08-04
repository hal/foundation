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
import org.jboss.hal.ui.resource.form.CredentialReferenceControl;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.form.StandardFormItem;
import org.jboss.hal.ui.resource.view.CredentialReferenceViewItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import static java.util.Collections.singletonList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAR_TEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORE;
import static org.jboss.hal.ui.resource.pipeline.AttributeHandler.hasObjectValueType;
import static org.jboss.hal.ui.resource.pipeline.AttributeHandler.partition;

/**
 * Handler for credential reference composite attributes. Claims OBJECT attributes with the credential reference structure
 * ({@code store}, {@code alias}, {@code clear-text}) and produces composite view/form items.
 */
public class CredentialReferenceHandler implements AttributeHandler {

    /** The credential reference mode derived from which sub-attributes have values. */
    public enum Mode {
        STORE_REFERENCE,
        CLEAR_TEXT,
        UNDEFINED
    }

    /** Detects the credential reference mode from the attribute's current value. */
    public static Mode mode(ModelNode value) {
        if (value.isDefined()) {
            boolean hasStore = value.hasDefined(STORE);
            boolean hasAlias = value.hasDefined(ALIAS);
            boolean hasClearText = value.hasDefined(CLEAR_TEXT);
            if (hasStore || hasAlias) {
                return Mode.STORE_REFERENCE;
            } else if (hasClearText) {
                return Mode.CLEAR_TEXT;
            }
        }
        return Mode.UNDEFINED;
    }

    @Override
    public MatchResult match(List<AttributeDescription> pool) {
        return partition(pool, ad -> hasObjectValueType(ad, STORE, ALIAS, CLEAR_TEXT));
    }

    @Override
    public List<ViewItem> viewItems(PipelineContext context, AttributeMatch match) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(context, match.primary());
        return singletonList(new CredentialReferenceViewItem(context, ra.fqn(), ra));
    }

    @Override
    public List<FormItem> formItems(PipelineContext context, AttributeMatch match) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(context, match.primary());
        return singletonList(new StandardFormItem<>(context, ra.fqn(), ra, new CredentialReferenceControl()));
    }
}

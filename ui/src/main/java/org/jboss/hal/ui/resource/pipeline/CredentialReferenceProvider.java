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
import org.jboss.hal.ui.resource.form.CredentialReferenceControl;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.form.StandardFormItem;
import org.jboss.hal.ui.resource.view.CredentialReferenceViewItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import java.util.List;

import org.jboss.hal.dmr.ModelNode;

import static java.util.Collections.singletonList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAR_TEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORE;
import static org.jboss.hal.ui.resource.pipeline.AttributeMatcher.hasObjectValueType;

/**
 * Provider for credential reference composite attributes. Matches groups containing a single OBJECT attribute with the
 * credential reference structure ({@code store}, {@code alias}, {@code clear-text}).
 * <p>
 * A credential reference operates in one of three {@linkplain Mode modes}:
 * <ol>
 *     <li><b>Store reference</b> — {@code store} + {@code alias} are set, referencing an entry in a credential store.</li>
 *     <li><b>Clear text</b> — only {@code clear-text} is set. The password is visible in the server configuration.</li>
 *     <li><b>Undefined</b> — no sub-attributes are set.</li>
 * </ol>
 */
public class CredentialReferenceProvider implements ItemProvider {

    /** The credential reference mode derived from which sub-attributes have values. */
    public enum Mode {
        /** {@code store} and {@code alias} are set — references a credential store entry. */
        STORE_REFERENCE,
        /** Only {@code clear-text} is set — password visible in configuration. */
        CLEAR_TEXT,
        /** No sub-attributes are set. */
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
    public boolean matches(AttributeMatch match) {
        return match.isSingle() && hasObjectValueType(match.primary(), STORE, ALIAS, CLEAR_TEXT);
    }

    @Override
    public List<ViewItem> viewItems(AttributeMatch match, PipelineContext context) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(match.primary(), context);
        return singletonList(new CredentialReferenceViewItem(ra.fqn(), ra, context));
    }

    @Override
    public List<FormItem> formItems(AttributeMatch match, PipelineContext context) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(match.primary(), context);
        return singletonList(new StandardFormItem<>(ra.fqn(), ra, context, new CredentialReferenceControl()));
    }
}

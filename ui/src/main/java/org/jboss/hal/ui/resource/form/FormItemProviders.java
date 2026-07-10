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
package org.jboss.hal.ui.resource.form;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.hal.core.Humanize.sentenceCase;
import static org.jboss.hal.ui.resource.CompositeAttributes.CREDENTIAL_REFERENCE;
import static org.jboss.hal.ui.resource.ItemIdentifier.identifier;
import static org.jboss.hal.ui.resource.data.ResourceData.State.EDIT;
import static org.patternfly.component.form.FormGroupLabel.formGroupLabel;

/** Registry of special-case {@link FormItemProvider} instances consulted by {@link FormItemFactory} before falling back to default form item creation. */
public class FormItemProviders {

    /** Ordered list of special-case providers. The first matching provider wins. */
    public static final List<FormItemProvider> specialFormItems = new ArrayList<>();

    static {
        // Credential reference — consolidated form item for all credential-reference variants
        specialFormItems.add(new FormItemProvider() {
            @Override
            public boolean test(org.jboss.hal.meta.AddressTemplate template,
                    org.jboss.hal.meta.Metadata metadata,
                    org.jboss.hal.ui.resource.ResourceAttribute ra,
                    FormItemFlags flags) {
                return CREDENTIAL_REFERENCE.matches(ra.description);
            }

            @Override
            public FormItem formItem(org.jboss.hal.meta.AddressTemplate template,
                    org.jboss.hal.meta.Metadata metadata,
                    org.jboss.hal.ui.resource.ResourceAttribute ra,
                    FormItemFlags flags) {
                // TODO Replace with custom credential reference form item
                String id = identifier(ra, EDIT);
                return new UnsupportedFormItem(id, ra, formGroupLabel(id).text(sentenceCase(ra.name)), flags);
            }
        });
    }
}

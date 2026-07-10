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

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.ui.resource.ResourceAttribute;

import static org.jboss.hal.ui.resource.ItemIdentifier.identifier;
import static org.jboss.hal.ui.resource.composite.CompositeAttributes.CREDENTIAL_REFERENCE;
import static org.jboss.hal.ui.resource.composite.CompositeAttributes.TIME_UNIT;
import static org.jboss.hal.ui.resource.data.ResourceData.State.EDIT;
import static org.jboss.hal.ui.resource.form.FormItemFactory.defaultLabel;

/**
 * Registry of special-case {@link FormItemProvider} instances consulted by {@link FormItemFactory} before falling back to
 * default form item creation.
 */
public class FormItemProviders {

    private static final List<FormItemProvider> registry = new ArrayList<>();

    static {
        // Credential reference — consolidated form item for all credential-reference variants
        registry.add(new FormItemProvider() {
            @Override
            public boolean test(AddressTemplate template, Metadata metadata, ResourceAttribute ra,
                    FormItemFlags flags) {
                return CREDENTIAL_REFERENCE.matches(ra.description);
            }

            @Override
            public FormItem formItem(AddressTemplate template, Metadata metadata, ResourceAttribute ra,
                    FormItemFlags flags) {
                String id = identifier(ra, EDIT);
                return new CredentialReferenceFormItem(id, ra, defaultLabel(id, metadata, ra), flags, template);
            }
        });

        // Time unit — consolidated form item for keepalive-time and similar {time, unit} attributes
        registry.add(new FormItemProvider() {
            @Override
            public boolean test(AddressTemplate template, Metadata metadata, ResourceAttribute ra,
                    FormItemFlags flags) {
                return TIME_UNIT.matches(ra.description);
            }

            @Override
            public FormItem formItem(AddressTemplate template, Metadata metadata, ResourceAttribute ra,
                    FormItemFlags flags) {
                String id = identifier(ra, EDIT);
                return new TimeUnitFormItem(id, ra, defaultLabel(id, metadata, ra), flags);
            }
        });
    }

    /**
     * Attempts to create a custom {@code FormItem} by consulting a registry of {@code FormItemProvider} instances. Each
     * provider in the registry is checked in order until one matches the given parameters and returns a suitable form item. If
     * no provider matches, {@code null} is returned.
     *
     * @param template the address template associated with the form item
     * @param metadata the metadata describing the resource or attribute
     * @param ra       the resource attribute for which the form item is being created
     * @param flags    additional form item flags providing context or behavior instructions
     * @return a custom {@code FormItem} created by a matching provider, or {@code null} if no providers match
     */
    public static FormItem customFormItem(AddressTemplate template, Metadata metadata, ResourceAttribute ra,
            FormItemFlags flags) {
        for (FormItemProvider fip : registry) {
            if (fip.test(template, metadata, ra, flags)) {
                return fip.formItem(template, metadata, ra, flags);
            }
        }
        return null;
    }
}

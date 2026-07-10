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
package org.jboss.hal.ui.resource.view;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.ui.resource.ResourceAttribute;

import static org.jboss.hal.ui.resource.CompositeAttributes.CREDENTIAL_REFERENCE;
import static org.jboss.hal.ui.resource.view.CredentialReferenceValue.credentialReferenceValue;
import static org.jboss.hal.ui.resource.view.ViewItemFactory.defaultViewItem;

/**
 * Registry of {@link ViewItemProvider} instances that supply specialised {@link ViewItem} components for specific
 * attributes. Providers are evaluated in order; the first matching provider wins. If no provider matches, the default
 * rendering in {@link ViewItemFactory} is used.
 */
public class ViewItemProviders {

    /** Ordered list of special view item providers. The first matching provider wins. */
    public static final List<ViewItemProvider> specialViewItems = new ArrayList<>();

    static {
        // Credential reference — consolidated view for all credential-reference variants
        specialViewItems.add(new ViewItemProvider() {
            @Override
            public boolean test(AddressTemplate template, Metadata metadata, ResourceAttribute ra) {
                return CREDENTIAL_REFERENCE.matches(ra.description);
            }

            @Override
            public ViewItem viewItem(AddressTemplate template, Metadata metadata, ResourceAttribute ra) {
                return defaultViewItem(metadata, ra, credentialReferenceValue(ra).element());
            }
        });
    }
}

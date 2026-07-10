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

import static org.jboss.hal.ui.resource.composite.CompositeAttributes.CREDENTIAL_REFERENCE;
import static org.jboss.hal.ui.resource.composite.CompositeAttributes.TIME_UNIT;
import static org.jboss.hal.ui.resource.view.CredentialReferenceView.credentialReferenceValue;
import static org.jboss.hal.ui.resource.view.TimeUnitView.timeUnitValue;
import static org.jboss.hal.ui.resource.view.ViewItemFactory.defaultViewItem;

/**
 * Registry of {@link ViewItemProvider} instances that supply specialised {@link ViewItem} components for specific attributes.
 * Providers are evaluated in order; the first matching provider wins. If no provider matches, the default rendering in
 * {@link ViewItemFactory} is used.
 */
public class ViewItemProviders {

    private static final List<ViewItemProvider> registry = new ArrayList<>();

    static {
        // Credential reference — consolidated view for all credential-reference variants
        registry.add(new ViewItemProvider() {
            @Override
            public boolean test(AddressTemplate template, Metadata metadata, ResourceAttribute ra) {
                return CREDENTIAL_REFERENCE.matches(ra.description);
            }

            @Override
            public ViewItem viewItem(AddressTemplate template, Metadata metadata, ResourceAttribute ra) {
                return defaultViewItem(metadata, ra, credentialReferenceValue(ra).element());
            }
        });

        // Time unit — consolidated view for keepalive-time and similar {time, unit} attributes
        registry.add(new ViewItemProvider() {
            @Override
            public boolean test(AddressTemplate template, Metadata metadata, ResourceAttribute ra) {
                return TIME_UNIT.matches(ra.description);
            }

            @Override
            public ViewItem viewItem(AddressTemplate template, Metadata metadata, ResourceAttribute ra) {
                return defaultViewItem(metadata, ra, timeUnitValue(ra).element());
            }
        });
    }

    /**
     * Returns a custom {@link ViewItem} based on the provided {@link AddressTemplate}, {@link Metadata}, and
     * {@link ResourceAttribute}. This method iterates through a registry of {@link ViewItemProvider} instances and delegates to
     * the first provider that matches the specified inputs. If no provider matches, it returns {@code null}.
     *
     * @param template the {@link AddressTemplate} representing the resource address from which the view item is derived
     * @param metadata the {@link Metadata} containing the resource's description and attribute details
     * @param ra       the {@link ResourceAttribute} for which the {@link ViewItem} is being created
     * @return a custom {@link ViewItem} created by a matching {@link ViewItemProvider}, or {@code null} if no provider matches
     */
    public static ViewItem customViewItem(AddressTemplate template, Metadata metadata, ResourceAttribute ra) {
        for (ViewItemProvider vip : registry) {
            if (vip.test(template, metadata, ra)) {
                return vip.viewItem(template, metadata, ra);
            }
        }
        return null;
    }
}

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
package org.jboss.hal.ui.resource;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.meta.description.AttributeDescription;

/**
 * Registry of {@link CompositeAttribute} instances. Consulted by
 * {@link ResourceAttribute#resourceAttributes(org.jboss.hal.dmr.ModelNode, org.jboss.hal.meta.Metadata, java.util.function.Predicate)
 * ResourceAttribute.resourceAttributes()} to decide whether a {@code simpleRecord} attribute should be kept as a single
 * composite unit or flattened into individual nested attributes.
 * <p>
 * The same instances are reused by
 * {@link org.jboss.hal.ui.resource.view.ViewItemProvider ViewItemProvider} and
 * {@link org.jboss.hal.ui.resource.form.FormItemProvider FormItemProvider} implementations, ensuring that the match predicate
 * is consistent across flattening, view rendering, and form rendering.
 */
public final class CompositeAttributes {

    /** Shared {@link CredentialReference} instance for use by view and form item providers. */
    public static final CredentialReference CREDENTIAL_REFERENCE = new CredentialReference();

    private static final List<CompositeAttribute> ALL = new ArrayList<>();

    static {
        ALL.add(CREDENTIAL_REFERENCE);
    }

    /** Returns {@code true} if any registered {@link CompositeAttribute} matches the given attribute description. */
    public static boolean isComposite(AttributeDescription description) {
        for (CompositeAttribute ca : ALL) {
            if (ca.matches(description)) {
                return true;
            }
        }
        return false;
    }

    private CompositeAttributes() {
    }
}

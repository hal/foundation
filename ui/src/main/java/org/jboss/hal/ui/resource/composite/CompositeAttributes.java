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
package org.jboss.hal.ui.resource.composite;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.meta.description.AttributeDescription;

/**
 * Registry of {@link CompositeAttribute} instances. Used to decide whether a {@code simpleRecord} attribute should be kept as
 * a single composite unit or flattened into individual nested attributes.
 */
public final class CompositeAttributes {

    public static final CredentialReferenceAttribute CREDENTIAL_REFERENCE = new CredentialReferenceAttribute();
    public static final TimeUnitAttribute TIME_UNIT = new TimeUnitAttribute();

    private static final List<CompositeAttribute> registry = new ArrayList<>();

    static {
        registry.add(CREDENTIAL_REFERENCE);
        registry.add(TIME_UNIT);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isComposite(AttributeDescription description) {
        for (CompositeAttribute ca : registry) {
            if (ca.matches(description)) {
                return true;
            }
        }
        return false;
    }
}

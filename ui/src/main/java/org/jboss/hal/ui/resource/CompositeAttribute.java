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

import org.jboss.hal.meta.description.AttributeDescription;

/**
 * Predicate for identifying management model attributes that should be treated as indivisible composite units rather than being
 * flattened into individual nested attributes.
 * <p>
 * Implementations match based on the attribute's <em>structure</em> (type, value-type shape, sub-attribute names) rather than
 * its name, so they naturally cover all name variants of the same structural pattern (e.g. {@code credential-reference},
 * {@code recovery-credential-reference}, {@code source-credential-reference}).
 * <p>
 * A registered {@code CompositeAttribute} affects three stages:
 * <ol>
 *     <li>{@link ResourceAttribute#resourceAttributes ResourceAttribute.resourceAttributes()} — prevents flattening, keeping
 *         the attribute as a single {@link ResourceAttribute} with the full OBJECT value.</li>
 *     <li>{@link org.jboss.hal.ui.resource.view.ViewItemProvider} — enables a custom read-only view rendering.</li>
 *     <li>{@link org.jboss.hal.ui.resource.form.FormItemProvider} — enables a custom form input rendering.</li>
 * </ol>
 * Registration and lookup are managed by {@link CompositeAttributes}.
 *
 * @see CompositeAttributes
 */
@FunctionalInterface
public interface CompositeAttribute {

    /**
     * Tests whether the given attribute description matches this composite pattern. Implementations should inspect the
     * attribute's type and value-type structure, not its name. Must be fast — called for every attribute during a resource
     * attribute collection.
     */
    boolean matches(AttributeDescription description);
}

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

import org.jboss.hal.meta.description.AttributeDescription;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

/**
 * Attributes claimed together by an {@link AttributeHandler} in the match phase. Lives in the description world — carries raw
 * metadata descriptions, not values or RBAC state.
 * <p>
 * A match can hold:
 * <ul>
 *     <li>1 attribute — a single top-level attribute or a composite OBJECT attribute</li>
 *     <li>n attributes — a sibling group of related top-level attributes (e.g. {@code path} + {@code relative-to})</li>
 * </ul>
 */
public record AttributeMatch(String name, List<AttributeDescription> descriptions) {

    /** Creates a match containing a single attribute. */
    public static AttributeMatch single(AttributeDescription description) {
        return new AttributeMatch(description.name(), singletonList(description));
    }

    /** Creates a match containing multiple related attributes (e.g. sibling path + relative-to). */
    public static AttributeMatch of(String name, List<AttributeDescription> descriptions) {
        if (descriptions == null || descriptions.isEmpty()) {
            throw new IllegalArgumentException("AttributeMatch must contain at least one attribute");
        }
        return new AttributeMatch(name, unmodifiableList(descriptions));
    }

    public AttributeMatch {
        if (descriptions == null || descriptions.isEmpty()) {
            throw new IllegalArgumentException("AttributeMatch must contain at least one attribute");
        }
    }

    /** Returns the primary attribute — the first in the group, used for ordering and identification. */
    public AttributeDescription primary() {
        return descriptions.get(0);
    }

    /** Returns the number of attributes in this group. */
    public int size() {
        return descriptions.size();
    }

    /** Returns {@code true} if this match contains exactly one attribute. */
    public boolean isSingle() {
        return descriptions.size() == 1;
    }

    @Override
    public String toString() {
        return name + "(" + size() + ")";
    }
}

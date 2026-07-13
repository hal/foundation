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
 * A group of semantically related attribute descriptions that should be rendered as a single visual unit. This is the contract
 * between stage 1 (grouping) and stage 2 (itemization) of the pipeline.
 * <p>
 * A group can hold:
 * <ul>
 *     <li>1 attribute — a single top-level attribute or a composite OBJECT attribute</li>
 *     <li>n attributes — a sibling group of related top-level attributes (e.g. {@code path} + {@code relative-to})</li>
 * </ul>
 * There is no {@code kind} enum. Providers in stage 2 match by inspecting the attributes inside the group, not by checking a
 * type flag.
 */
public record AttributeGroup(List<AttributeDescription> attributes, String name) {

    /** Creates a group containing a single attribute. */
    public static AttributeGroup single(AttributeDescription description) {
        return new AttributeGroup(singletonList(description), description.name());
    }

    /** Creates a group containing multiple related attributes. */
    public static AttributeGroup of(String name, List<AttributeDescription> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            throw new IllegalArgumentException("AttributeGroup must contain at least one attribute");
        }
        return new AttributeGroup(unmodifiableList(attributes), name);
    }

    public AttributeGroup {
        if (attributes == null || attributes.isEmpty()) {
            throw new IllegalArgumentException("AttributeGroup must contain at least one attribute");
        }
    }

    /** Returns the primary attribute — the first in the group, used for ordering and identification. */
    public AttributeDescription primary() {
        return attributes.get(0);
    }

    /** Returns the number of attributes in this group. */
    public int size() {
        return attributes.size();
    }

    /** Returns {@code true} if this group contains exactly one attribute. */
    public boolean isSingle() {
        return attributes.size() == 1;
    }

    @Override
    public String toString() {
        return name + "(" + size() + ")";
    }
}

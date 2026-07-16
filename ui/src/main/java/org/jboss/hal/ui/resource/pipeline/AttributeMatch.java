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
import org.jboss.hal.ui.resource.ResolvedAttribute;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * Attributes claimed together by an {@link AttributeMatcher} in stage 1 of the pipeline. This is the contract between stage 1
 * (matching) and stage 2 (itemization) — it carries raw metadata descriptions, not yet resolved against values or RBAC.
 * <p>
 * A match can hold:
 * <ul>
 *     <li>1 attribute — a single top-level attribute or a composite OBJECT attribute</li>
 *     <li>n attributes — a sibling group of related top-level attributes (e.g. {@code path} + {@code relative-to})</li>
 * </ul>
 * Providers in stage 2 match by inspecting the attributes inside the match, not by checking a type flag.
 *
 * @see AttributeMatcher
 * @see ItemProvider
 */
public record AttributeMatch(List<AttributeDescription> attributes, String name) {

    /** Creates a match containing a single attribute. */
    public static AttributeMatch single(AttributeDescription description) {
        return new AttributeMatch(singletonList(description), description.name());
    }

    /** Creates a match containing multiple related attributes (e.g. sibling path + relative-to). */
    public static AttributeMatch of(String name, List<AttributeDescription> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            throw new IllegalArgumentException("AttributeMatch must contain at least one attribute");
        }
        return new AttributeMatch(unmodifiableList(attributes), name);
    }

    public AttributeMatch {
        if (attributes == null || attributes.isEmpty()) {
            throw new IllegalArgumentException("AttributeMatch must contain at least one attribute");
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

    /** Returns {@code true} if this match contains exactly one attribute. */
    public boolean isSingle() {
        return attributes.size() == 1;
    }

    /**
     * Resolves all attribute descriptions in this match against the pipeline context. Delegates to
     * {@link ResolvedAttribute#resolve(AttributeDescription, PipelineContext)} for each attribute. Used by providers that handle
     * multi-attribute matches (e.g. sibling path + relative-to).
     */
    List<ResolvedAttribute> resolveAll(PipelineContext context) {
        return attributes.stream()
                .map(ad -> ResolvedAttribute.resolve(ad, context))
                .collect(toList());
    }

    @Override
    public String toString() {
        return name + "(" + size() + ")";
    }
}

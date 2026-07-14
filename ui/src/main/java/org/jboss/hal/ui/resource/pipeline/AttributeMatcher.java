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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.description.AttributeDescription;

import static java.util.Collections.unmodifiableList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;

/**
 * Stage 1 of the pipeline: scans the attribute pool and claims groups of related attributes. Matchers run in priority order.
 * Each matcher receives the remaining pool (attributes not yet claimed by higher-priority matchers) and returns a
 * {@link MatchResult} containing the claimed groups and the remaining unclaimed attributes.
 * <p>
 * The pool is processed immutably — matchers do not modify the input list. Instead, they return a new {@code MatchResult} with
 * the remaining attributes.
 */
@FunctionalInterface
public interface AttributeMatcher {

    /** The result of a matcher's scan: claimed groups and the remaining unclaimed attributes. */
    final class MatchResult {

        private final List<AttributeMatch> groups;
        private final List<AttributeDescription> remaining;

        public MatchResult(List<AttributeMatch> groups, List<AttributeDescription> remaining) {
            this.groups = unmodifiableList(groups);
            this.remaining = unmodifiableList(remaining);
        }

        /** Returns the groups claimed by this matcher. May be empty if nothing matched. */
        public List<AttributeMatch> groups() {
            return groups;
        }

        /** Returns the attributes that were not claimed and are still available for subsequent matchers. */
        public List<AttributeDescription> remaining() {
            return remaining;
        }
    }

    /**
     * Partitions the pool by a predicate: matching attributes become single-attribute groups, the rest stays in remaining. Use
     * this for composite matchers that claim individual attributes based on their structure.
     */
    static MatchResult partition(List<AttributeDescription> pool, Predicate<AttributeDescription> predicate) {
        List<AttributeMatch> groups = new ArrayList<>();
        List<AttributeDescription> remaining = new ArrayList<>();
        for (AttributeDescription ad : pool) {
            if (predicate.test(ad)) {
                groups.add(AttributeMatch.single(ad));
            } else {
                remaining.add(ad);
            }
        }
        return new MatchResult(groups, remaining);
    }

    /**
     * Tests whether the given attribute is an OBJECT whose structured VALUE_TYPE contains all the specified keys. Use this
     * for composite matchers that identify attributes by their internal structure.
     */
    static boolean hasObjectValueType(AttributeDescription description, String... requiredKeys) {
        try {
            ModelType type = description.get(TYPE).asType();
            if (type != ModelType.OBJECT) {
                return false;
            }
            if (!description.hasDefined(VALUE_TYPE)) {
                return false;
            }
            if (description.get(VALUE_TYPE).getType() != ModelType.OBJECT) {
                return false;
            }
            ModelNode valueType = description.get(VALUE_TYPE);
            for (String key : requiredKeys) {
                if (!valueType.has(key)) {
                    return false;
                }
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Tests whether the given attribute is an OBJECT whose VALUE_TYPE is a simple scalar type (STRING, INT, LONG, etc.).
     * This identifies free-form key-value map attributes — the complement of {@link #hasObjectValueType}.
     */
    static boolean hasSimpleValueType(AttributeDescription description) {
        try {
            ModelType type = description.get(TYPE).asType();
            if (type != ModelType.OBJECT) {
                return false;
            }
            if (!description.hasDefined(VALUE_TYPE)) {
                return false;
            }
            ModelNode valueTypeNode = description.get(VALUE_TYPE);
            if (valueTypeNode.getType() != ModelType.TYPE) {
                return false;
            }
            return valueTypeNode.asType().simple();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Scans the given pool of attributes and claims groups of related attributes.
     *
     * @param pool the attributes available for claiming (not yet claimed by higher-priority matchers)
     * @return a result containing the claimed groups and the remaining unclaimed attributes
     */
    MatchResult match(List<AttributeDescription> pool);
}

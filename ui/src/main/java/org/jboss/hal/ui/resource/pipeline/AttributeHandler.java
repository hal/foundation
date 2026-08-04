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

import org.jboss.hal.ui.resource.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import static java.util.Collections.unmodifiableList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;

/**
 * Unified handler that bridges the description world and the value world. Handlers both claim attributes from the pool (match
 * phase) and produce view/form items (itemize phase), performing resolution internally.
 * <p>
 * Handlers run in priority order during matching. Each handler receives the remaining pool (attributes not yet claimed by
 * higher-priority handlers) and returns a {@link MatchResult} with claimed matches and remaining attributes. The same handler
 * then produces items for its claimed matches — no separate provider needed, no dual matching.
 *
 * @see ItemProvider
 * @see Pipeline
 */
public interface AttributeHandler {

    /** The result of a handler's scan: claimed matches and the remaining unclaimed attributes. */
    final class MatchResult {

        private final List<AttributeMatch> matches;
        private final List<AttributeDescription> remaining;

        public MatchResult(List<AttributeMatch> matches, List<AttributeDescription> remaining) {
            this.matches = unmodifiableList(matches);
            this.remaining = unmodifiableList(remaining);
        }

        public List<AttributeMatch> matches() {
            return matches;
        }

        public List<AttributeDescription> remaining() {
            return remaining;
        }
    }

    // ------------------------------------------------------ static helpers

    /**
     * Partitions the pool by a predicate: matching attributes become single-attribute matches, the rest stays in remaining.
     */
    static MatchResult partition(List<AttributeDescription> pool, Predicate<AttributeDescription> predicate) {
        List<AttributeMatch> matches = new ArrayList<>();
        List<AttributeDescription> remaining = new ArrayList<>();
        for (AttributeDescription ad : pool) {
            if (predicate.test(ad)) {
                matches.add(AttributeMatch.single(ad));
            } else {
                remaining.add(ad);
            }
        }
        return new MatchResult(matches, remaining);
    }

    /**
     * Tests whether the given attribute is an OBJECT whose structured VALUE_TYPE contains all the specified keys.
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
     * Tests whether the given attribute is an OBJECT whose VALUE_TYPE is a simple scalar type. Identifies free-form key-value
     * map attributes.
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

    // ------------------------------------------------------ handler methods

    /**
     * Scans the pool and claims matches of related attributes.
     *
     * @param pool the attributes available for claiming (not yet claimed by higher-priority handlers)
     * @return claimed matches and remaining unclaimed attributes
     */
    MatchResult match(List<AttributeDescription> pool);

    /**
     * Creates view items for the given match. The handler resolves attributes against the context internally and may delegate
     * child attributes to the pipeline's provider chain via {@link Pipeline#viewItem(PipelineContext, ResolvedAttribute)}.
     *
     * @return view items, or {@code null} to fall through to the provider chain
     */
    List<ViewItem> viewItems(PipelineContext context, AttributeMatch match);

    /**
     * Creates form items for the given match. The handler resolves attributes against the context internally and may delegate
     * child attributes to the pipeline's provider chain via {@link Pipeline#formItem(PipelineContext, ResolvedAttribute)}.
     *
     * @return form items, or {@code null} to fall through to the provider chain
     */
    List<FormItem> formItems(PipelineContext context, AttributeMatch match);
}

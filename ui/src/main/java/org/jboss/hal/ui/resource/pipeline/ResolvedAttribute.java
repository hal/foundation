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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.description.AttributeDescription;

import static java.util.stream.Collectors.toList;

/**
 * A snapshot of an attribute's description, current value, and security state. Created by resolving an
 * {@link AttributeDescription} against a {@link PipelineContext}. This is the data that flows into
 * {@link ViewItem} and {@link FormItem} constructors.
 * <p>
 * For single attributes and composites, items hold one {@code ResolvedAttribute}. For sibling groups, items hold a list.
 *
 * @see AttributeGroup
 * @see PipelineContext
 */
public record ResolvedAttribute(
        AttributeDescription description,
        ModelNode value,
        boolean readable,
        boolean writable) {

    /** Resolves a single attribute description against the pipeline context. */
    public static ResolvedAttribute resolve(AttributeDescription description, PipelineContext context) {
        return new ResolvedAttribute(
                description,
                context.value(description),
                context.readable(description),
                context.writable(description));
    }

    /** Resolves all attribute descriptions in the group against the pipeline context. */
    public static List<ResolvedAttribute> resolveAll(AttributeGroup group, PipelineContext context) {
        return group.attributes().stream()
                .map(ad -> resolve(ad, context))
                .collect(toList());
    }

    /** Returns the dot-separated fully qualified name, used for DMR write-attribute operations. */
    public String fqn() {
        return description.fullyQualifiedName();
    }

    /** Returns the simple attribute name. */
    public String name() {
        return description.name();
    }

    /** Returns whether the current value is a DMR expression. */
    public boolean expression() {
        return value.isDefined() && value.getType() == ModelType.EXPRESSION;
    }

    @Override
    public String toString() {
        return fqn() + "=" + value.asString();
    }
}

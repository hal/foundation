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

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.AttributeDescriptions;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;

/**
 * A snapshot of an attribute's description, current value, and security state. Created by resolving an
 * {@link AttributeDescription} against a {@link PipelineContext}. This is the data that flows into
 * {@link org.jboss.hal.ui.resource.view.ViewItem} and {@link org.jboss.hal.ui.resource.form.FormItem} constructors.
 * <p>
 * For single attributes and composites, items hold one {@code ResolvedAttribute}. For sibling groups, items hold a list.
 *
 * @see PipelineContext
 */
public record ResolvedAttribute(
        AttributeDescription description,
        ModelNode value,
        boolean readable,
        boolean writable) {

    /** Finds a resolved attribute by name from a list of siblings. Returns an undefined attribute if not found. */
    public static ResolvedAttribute find(String name, List<ResolvedAttribute> attributes) {
        return attributes.stream()
                .filter(ra -> ra.name().equals(name))
                .findFirst()
                .orElse(new ResolvedAttribute(AttributeDescription.undefined(), new ModelNode(), false, false));
    }

    /**
     * Resolves a single {@link AttributeDescription} against the given {@link PipelineContext}: looks up the attribute's
     * current value and RBAC state (readable/writable) to produce an immutable snapshot. This is the low-level primitive used
     * by pipeline providers and by {@code AttributeMatch#resolveAll(PipelineContext)} for batch resolution.
     */
    public static ResolvedAttribute resolve(AttributeDescription description, PipelineContext context) {
        return new ResolvedAttribute(
                description,
                context.value(description),
                context.readable(description),
                context.writable(description));
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

    public ResolvedAttribute child(String name) {
        if (value.hasDefined(name) && description.simpleRecord()) {
            ModelNode childNode = value.get(name);
            AttributeDescriptions descriptions = description.valueTypeAttributeDescriptions();
            AttributeDescription childDescription = descriptions.get(name);
            if (childNode != null && childDescription != null) {
                return new ResolvedAttribute(childDescription, childNode, readable, writable);
            }
        }
        return new ResolvedAttribute(AttributeDescription.undefined(), new ModelNode(), false, false);
    }

    @Override
    public String toString() {
        return fqn() + "=" + value.asString();
    }
}

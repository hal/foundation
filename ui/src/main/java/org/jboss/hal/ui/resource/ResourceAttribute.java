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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.AttributeDescriptions;
import org.jboss.hal.meta.description.OperationDescription;
import org.jboss.hal.meta.security.SecurityContext;

import static org.jboss.hal.dmr.ModelType.EXPRESSION;

/**
 * Data holder for a WildFly management attribute. Combines the attribute's value, description metadata, and security context
 * into a single object used by both form and view components.
 * <p>
 * Attributes can be nested inside record-type (simple record) attributes. In that case, the fully qualified name contains the
 * parent attribute name as a prefix separated by a dot.
 */
public class ResourceAttribute {

    /**
     * Represents the default group name for attributes that do not belong to any defined group. This constant is used as a key
     * when grouping attributes for categorization. Ungrouped attributes are typically placed under this key as the first entry
     * when attributes are grouped alphabetically.
     */
    public static final String UNGROUPED = "ungrouped";

    // ------------------------------------------------------ predicates

    /**
     * Returns a predicate that accepts only attributes whose fully qualified name is in the given list. An empty list accepts
     * all.
     */
    public static Predicate<AttributeDescription> includes(List<String> attributes) {
        return ad -> {
            if (attributes.isEmpty()) {
                return true;
            }
            return attributes.contains(ad.fullyQualifiedName());
        };
    }

    /** Returns a predicate that rejects deprecated attributes. */
    public static Predicate<AttributeDescription> notDeprecated() {
        return ad -> !ad.deprecation().isDefined();
    }

    // ------------------------------------------------------ factories

    /**
     * Collects and returns a list of resource attributes based on the provided operation description.
     *
     * @param operationDescription The operation description.
     * @param predicate            A predicate to filter which attributes should be collected.
     * @return A list of ResourceAttribute objects representing the collected attributes.
     */
    public static List<ResourceAttribute> resourceAttributes(OperationDescription operationDescription,
            Predicate<AttributeDescription> predicate) {
        List<ResourceAttribute> resourceAttributes = new ArrayList<>();
        for (AttributeDescription description : operationDescription.parameters()) {
            if (description.simpleRecord()) {
                AttributeDescriptions nestedDescriptions = description.valueTypeAttributeDescriptions();
                for (AttributeDescription nestedDescription : nestedDescriptions) {
                    if (predicate.test(nestedDescription)) {
                        resourceAttributes.add(new ResourceAttribute(new ModelNode(), nestedDescription, SecurityContext.RWX));
                    }
                }
            } else {
                if (predicate.test(description)) {
                    resourceAttributes.add(new ResourceAttribute(new ModelNode(), description, SecurityContext.RWX));
                }
            }
        }
        return resourceAttributes;
    }

    /**
     * Collects and returns a list of resource attributes based on an existing resource and its metadata.
     *
     * @param resource  The model node representing the resource.
     * @param metadata  The metadata containing resource descriptions and attribute descriptions.
     * @param predicate A predicate to filter which attributes should be collected.
     * @return A list of ResourceAttribute objects representing the collected attributes.
     */
    public static List<ResourceAttribute> resourceAttributes(ModelNode resource, Metadata metadata,
            Predicate<AttributeDescription> predicate) {
        List<ResourceAttribute> resourceAttributes = new ArrayList<>();
        for (AttributeDescription ad : metadata.resourceDescription().attributes()) {
            if (ad.simpleRecord()) {
                AttributeDescriptions nads = ad.valueTypeAttributeDescriptions();
                for (AttributeDescription nad : nads) {
                    if (predicate.test(nad)) {
                        ModelNode nestedValue = ModelNodeHelper.nested(resource, nad.fullyQualifiedName());
                        resourceAttributes.add(new ResourceAttribute(nestedValue, nad, metadata.securityContext()));
                    }
                }
            } else {
                if (predicate.test(ad)) {
                    ModelNode value = resource.get(ad.name());
                    resourceAttributes.add(new ResourceAttribute(value, ad, metadata.securityContext()));
                }
            }
        }
        return resourceAttributes;
    }

    /**
     * Groups attributes by their attribute group name. Ungrouped attributes are placed under the key {@code "ungrouped"} as the
     * first entry. Groups are sorted alphabetically.
     */
    public static Map<String, List<ResourceAttribute>> grouped(List<ResourceAttribute> attributes) {
        List<ResourceAttribute> ungrouped = new ArrayList<>();
        TreeMap<String, List<ResourceAttribute>> groups = new TreeMap<>();
        for (ResourceAttribute attribute : attributes) {
            if (attribute.group == null) {
                ungrouped.add(attribute);
            } else {
                groups.computeIfAbsent(attribute.group, k -> new ArrayList<>()).add(attribute);
            }
        }
        LinkedHashMap<String, List<ResourceAttribute>> result = new LinkedHashMap<>();
        if (!ungrouped.isEmpty()) {
            result.put(UNGROUPED, ungrouped);
        }
        result.putAll(groups);
        return result;
    }

    /** Returns {@code true} if any attribute in the list has a non-null attribute group. */
    public static boolean hasGroups(List<ResourceAttribute> attributes) {
        for (ResourceAttribute attribute : attributes) {
            if (attribute.group != null) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------ instance

    /** Fully qualified attribute name (includes parent prefix for nested attributes). */
    public final String fqn;

    /** Simple attribute name. */
    public final String name;

    /** Attribute group name, or {@code null} if the attribute is ungrouped. */
    public final String group;

    /** Current attribute value from the resource. */
    public final ModelNode value;

    /** Management model description for this attribute. */
    public final AttributeDescription description;

    /** Whether the current security context allows reading this attribute. */
    public final boolean readable;

    /** Whether the current security context allows writing this attribute. */
    public final boolean writable;

    /** Whether the current value is a DMR expression. */
    public final boolean expression;

    /** Creates a new resource attribute from the given value, description, and security context. */
    public ResourceAttribute(ModelNode value, AttributeDescription description, SecurityContext securityContext) {
        this.fqn = description.fullyQualifiedName();
        this.name = description.name();
        this.group = description.group();
        this.value = value;
        this.description = description;
        this.expression = value.isDefined() && value.getType() == EXPRESSION;
        if (description.nested()) {
            readable = securityContext.readable(description.root().name());
            writable = securityContext.writable(description.root().name());
        } else {
            readable = securityContext.readable(name);
            writable = securityContext.writable(name);
        }
    }

    @Override
    public String toString() {
        return fqn + "=" + value.asString();
    }
}

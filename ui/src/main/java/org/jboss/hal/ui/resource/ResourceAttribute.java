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
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.AttributeDescriptions;
import org.jboss.hal.meta.description.OperationDescription;
import org.jboss.hal.meta.security.SecurityContext;

import static org.jboss.hal.dmr.ModelType.EXPRESSION;

/** Simple record for an attribute name/value/description triple. */
class ResourceAttribute {

    // ------------------------------------------------------ predicates

    static Predicate<AttributeDescription> includes(List<String> attributes) {
        return ad -> {
            if (attributes.isEmpty()) {
                return true;
            }
            return attributes.contains(ad.fullyQualifiedName());
        };
    }

    static Predicate<AttributeDescription> notDeprecated() {
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
    static List<ResourceAttribute> resourceAttributes(OperationDescription operationDescription,
            Predicate<AttributeDescription> predicate) {
        List<ResourceAttribute> resourceAttributes = new ArrayList<>();
        for (AttributeDescription description : operationDescription.parameters()) {
            if (description.simpleValueType()) {
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
     * Collects and returns a list of resource attributes based on an existing resource.
     *
     * @param resource  The model node representing the resource.
     * @param metadata  The metadata containing resource descriptions and attribute descriptions.
     * @param predicate A predicate to filter which attributes should be collected.
     * @return A list of ResourceAttribute objects representing the collected attributes.
     */
    static List<ResourceAttribute> resourceAttributes(ModelNode resource, Metadata metadata,
            Predicate<AttributeDescription> predicate) {
        List<ResourceAttribute> resourceAttributes = new ArrayList<>();
        for (Property property : resource.asPropertyList()) {
            String name = property.getName();
            AttributeDescription description = metadata.resourceDescription().attributes().get(name);
            if (description.simpleValueType()) {
                AttributeDescriptions nestedDescriptions = description.valueTypeAttributeDescriptions();
                for (AttributeDescription nestedDescription : nestedDescriptions) {
                    if (predicate.test(nestedDescription)) {
                        ModelNode nestedValue = ModelNodeHelper.nested(resource, nestedDescription.fullyQualifiedName());
                        resourceAttributes.add(
                                new ResourceAttribute(nestedValue, nestedDescription, metadata.securityContext()));
                    }
                }
            } else {
                if (predicate.test(description)) {
                    ModelNode value = property.getValue();
                    resourceAttributes.add(new ResourceAttribute(value, description, metadata.securityContext()));
                }
            }
        }
        return resourceAttributes;
    }

    static Map<String, List<ResourceAttribute>> grouped(List<ResourceAttribute> attributes) {
        List<ResourceAttribute> ungrouped = new ArrayList<>();
        TreeMap<String, List<ResourceAttribute>> groups = new TreeMap<>();
        for (ResourceAttribute attribute : attributes) {
            if (attribute.group == null) {
                ungrouped.add(attribute);
            } else {
                groups.computeIfAbsent(attribute.group, k -> new ArrayList<>()).add(attribute);

            }
        }
        if (ungrouped.isEmpty()) {
            return groups;
        } else {
            // Groups are sorted, but ungrouped is always the last one
            LinkedHashMap<String, List<ResourceAttribute>> withUngrouped = new LinkedHashMap<>(groups);
            withUngrouped.put("ungrouped", ungrouped);
            return withUngrouped;
        }
    }

    // ------------------------------------------------------ instance

    final String fqn;
    final String name;
    final String group;
    final ModelNode value;
    final AttributeDescription description;
    final boolean readable;
    final boolean writable;
    final boolean expression;

    ResourceAttribute(ModelNode value, AttributeDescription description, SecurityContext securityContext) {
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

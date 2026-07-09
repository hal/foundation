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
package org.jboss.hal.ui.resource.data;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.AttributeDescriptions;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.ui.resource.ResourceAttribute;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTE_GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;

/** Factory methods for creating {@link ResourceAttribute} instances in tests. */
final class TestAttributes {

    static ResourceAttribute attribute(String name) {
        ModelNode description = new ModelNode();
        return new ResourceAttribute(new ModelNode(),
                new AttributeDescription(new Property(name, description)),
                SecurityContext.RWX);
    }

    static ResourceAttribute attributeWithGroup(String name, String group) {
        ModelNode description = new ModelNode();
        description.get(ATTRIBUTE_GROUP).set(group);
        return new ResourceAttribute(new ModelNode(),
                new AttributeDescription(new Property(name, description)),
                SecurityContext.RWX);
    }

    /** Creates nested resource attributes for a simple record (OBJECT with simple value-type children). */
    static List<ResourceAttribute> nestedAttributes(String parentName, String... childNames) {
        ModelNode parentDesc = new ModelNode();
        parentDesc.get(TYPE).set("OBJECT");
        for (String childName : childNames) {
            parentDesc.get(VALUE_TYPE).get(childName).get(TYPE).set("STRING");
        }
        AttributeDescription parent = new AttributeDescription(new Property(parentName, parentDesc));
        AttributeDescriptions nested = parent.valueTypeAttributeDescriptions();
        List<ResourceAttribute> result = new ArrayList<>();
        for (AttributeDescription nad : nested) {
            result.add(new ResourceAttribute(new ModelNode(), nad, SecurityContext.RWX));
        }
        return result;
    }

    private TestAttributes() {
    }
}

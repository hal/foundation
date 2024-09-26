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
package org.jboss.hal.meta.description;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;

import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;
import static org.jboss.hal.dmr.ModelType.LIST;
import static org.jboss.hal.dmr.ModelType.OBJECT;

public class AttributeDescription extends NamedNode implements Description {

    private final AttributeDescription parent;

    public AttributeDescription(Property property) {
        super(property);
        this.parent = null;
    }

    AttributeDescription() {
        super();
        this.parent = null;
    }

    AttributeDescription(String name, ModelNode modelNode) {
        super(name, modelNode);
        this.parent = null;
    }

    AttributeDescription(AttributeDescription parent, Property property) {
        super(property);
        this.parent = parent;
    }

    @Override
    public ModelNode modelNode() {
        return asModelNode();
    }

    public String fullyQualifiedName() {
        if (parent != null) {
            List<String> names = new ArrayList<>();
            names.add(name());
            AttributeDescription p = this.parent;
            while (p != null) {
                names.add(p.name());
                p = p.parent();
            }
            Collections.reverse(names);
            return String.join(".", names);
        }
        return name();
    }

    /**
     * Same as {@link #get(String)}, but finds {@code name} upwards (if {@link #nested()}).
     */
    public ModelNode find(String name) {
        if (parent != null) {
            AttributeDescription current = this;
            ModelNode modelNode = current.get(name);
            while (!modelNode.isDefined() || !modelNode.isDefined()) {
                current = current.parent;
                modelNode = current.get(name);
            }
            return modelNode;
        }
        return get(name);
    }

    public boolean nested() {
        return parent != null;
    }

    public AttributeDescription parent() {
        return parent;
    }

    public String formatType() {
        StringBuilder builder = new StringBuilder();
        if (hasDefined(TYPE)) {
            builder.append(get(TYPE).asString());
            if (hasDefined(VALUE_TYPE)) {
                ModelNode node = get(VALUE_TYPE);
                if (ModelType.TYPE.equals(node.getType())) {
                    builder.append("<").append(node.asString()).append(">");
                } else {
                    builder.append("<").append("T").append(">");
                }
            }
        }
        return builder.toString();
    }

    /**
     * Checks if the attribute description is either of type {@link ModelType#LIST} or {@link ModelType#OBJECT} with an object
     * value-type.
     */
    public boolean listOrObjectValueType() {
        try {
            ModelType type = get(TYPE).asType();
            if (type == LIST || type == OBJECT) {
                ModelType valueType = get(VALUE_TYPE).getType();
                return valueType == OBJECT;
            } else {
                return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Checks if the attribute description is of type {@link ModelType#OBJECT} with an object value-type that only contains
     * {@linkplain ModelType#simple() simple attributes} or lists of simple attributes.
     */
    public boolean simpleValueType() {
        try {
            ModelType type = get(TYPE).asType();
            if (type == OBJECT) {
                ModelType valueType = get(VALUE_TYPE).getType();
                if (valueType == OBJECT) {
                    List<Property> properties = get(VALUE_TYPE).asPropertyList();
                    for (Property property : properties) {
                        ModelType propertyType = property.getValue().get(TYPE).asType();
                        if (propertyType == ModelType.LIST) {
                            ModelType listValueType = property.getValue().has(VALUE_TYPE)
                                    ? property.getValue().get(VALUE_TYPE).asType()
                                    : null;
                            if (listValueType == null || !listValueType.simple()) {
                                return false;
                            }
                        } else if (!propertyType.simple()) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Returns the attribute descriptions of the value-type, if {@link #listOrObjectValueType()} is true or an empty
     * {@link AttributeDescriptions} otherwise.
     */
    public AttributeDescriptions valueTypeAttributeDescriptions() {
        ModelNode modelNode = new ModelNode();
        try {
            ModelType type = get(TYPE).asType();
            if (type == LIST || type == OBJECT) {
                ModelType valueType = get(VALUE_TYPE).getType();
                if (valueType == OBJECT) {
                    List<AttributeDescription> nestedDescriptions = new ArrayList<>();
                    for (Property property : get(VALUE_TYPE).asPropertyList()) {
                        nestedDescriptions.add(new AttributeDescription(this, property));
                    }
                    return new AttributeDescriptions(this, nestedDescriptions);
                }
            }
        } catch (IllegalArgumentException ignored) {}
        return new AttributeDescriptions(modelNode);
    }
}

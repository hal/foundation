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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.description.AttributeDescription;

import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;

final class AttributeFixtures {

    static AttributeDescription simpleAttribute(String name, ModelType type) {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(type);
        return new AttributeDescription(new Property(name, node));
    }

    static AttributeDescription objectWithSimpleValueType(String name, ModelType valueType) {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.OBJECT);
        node.get(VALUE_TYPE).set(valueType);
        return new AttributeDescription(new Property(name, node));
    }

    static AttributeDescription objectWithKeys(String name, String... keys) {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.OBJECT);
        for (String key : keys) {
            node.get(VALUE_TYPE).get(key).get(TYPE).set(ModelType.STRING);
        }
        return new AttributeDescription(new Property(name, node));
    }

    static AttributeDescription simpleRecordObject(String name, String... keys) {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.OBJECT);
        for (String key : keys) {
            node.get(VALUE_TYPE).get(key).get(TYPE).set(ModelType.STRING);
        }
        return new AttributeDescription(new Property(name, node));
    }

    static AttributeDescription listOfSimpleType(String name) {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.LIST);
        node.get(VALUE_TYPE).set(ModelType.STRING);
        return new AttributeDescription(new Property(name, node));
    }

    static AttributeDescription listOfSimpleRecords(String name, String... keys) {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.LIST);
        for (String key : keys) {
            node.get(VALUE_TYPE).get(key).get(TYPE).set(ModelType.STRING);
        }
        return new AttributeDescription(new Property(name, node));
    }

    static AttributeDescription listWithNestedSimpleList(String name, String simpleKey, String listKey) {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.LIST);
        node.get(VALUE_TYPE).get(simpleKey).get(TYPE).set(ModelType.STRING);
        node.get(VALUE_TYPE).get(listKey).get(TYPE).set(ModelType.LIST);
        node.get(VALUE_TYPE).get(listKey).get(VALUE_TYPE).set(ModelType.STRING);
        return new AttributeDescription(new Property(name, node));
    }

    static AttributeDescription listWithNestedComplexList(String name, String simpleKey, String listKey,
            String... listSubKeys) {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.LIST);
        node.get(VALUE_TYPE).get(simpleKey).get(TYPE).set(ModelType.STRING);
        node.get(VALUE_TYPE).get(listKey).get(TYPE).set(ModelType.LIST);
        for (String subKey : listSubKeys) {
            node.get(VALUE_TYPE).get(listKey).get(VALUE_TYPE).get(subKey).get(TYPE).set(ModelType.STRING);
        }
        return new AttributeDescription(new Property(name, node));
    }

    static AttributeDescription listWithNestedObject(String name, String simpleKey, String objectKey,
            String... objectSubKeys) {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.LIST);
        node.get(VALUE_TYPE).get(simpleKey).get(TYPE).set(ModelType.STRING);
        node.get(VALUE_TYPE).get(objectKey).get(TYPE).set(ModelType.OBJECT);
        for (String subKey : objectSubKeys) {
            node.get(VALUE_TYPE).get(objectKey).get(VALUE_TYPE).get(subKey).get(TYPE).set(ModelType.STRING);
        }
        return new AttributeDescription(new Property(name, node));
    }

    static AttributeDescription credentialReference(String name) {
        return objectWithKeys(name, "store", "alias", "clear-text");
    }

    static AttributeDescription timeUnit(String name) {
        return objectWithKeys(name, "time", "unit");
    }

    static AttributeDescription fileAttribute(String name) {
        return objectWithKeys(name, "path", "relative-to");
    }

    private AttributeFixtures() {
    }
}

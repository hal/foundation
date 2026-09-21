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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.junit.jupiter.api.Test;

import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttributeDescriptionTest {

    @Test
    void undefined() {
        AttributeDescription attributeDescription = new AttributeDescription();
        assertFalse(attributeDescription.isDefined());
        assertEquals("undefined", attributeDescription.description());
        assertFalse(attributeDescription.deprecation().isDefined());
        assertEquals("", attributeDescription.formatType());
        assertFalse(attributeDescription.simpleRecord());
    }

    // ------------------------------------------------------ simpleRecord

    @Test
    void simpleRecordWithAllSimpleTypes() {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.OBJECT);
        node.get(VALUE_TYPE).get("host").get(TYPE).set(ModelType.STRING);
        node.get(VALUE_TYPE).get("port").get(TYPE).set(ModelType.INT);
        AttributeDescription ad = new AttributeDescription(new Property("settings", node));
        assertTrue(ad.simpleRecord());
    }

    @Test
    void simpleRecordRejectsNestedObject() {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.OBJECT);
        node.get(VALUE_TYPE).get("name").get(TYPE).set(ModelType.STRING);
        node.get(VALUE_TYPE).get("nested").get(TYPE).set(ModelType.OBJECT);
        AttributeDescription ad = new AttributeDescription(new Property("complex", node));
        assertFalse(ad.simpleRecord());
    }

    @Test
    void simpleRecordNotForList() {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.LIST);
        node.get(VALUE_TYPE).get("name").get(TYPE).set(ModelType.STRING);
        AttributeDescription ad = new AttributeDescription(new Property("items", node));
        assertFalse(ad.simpleRecord());
    }

    // ------------------------------------------------------ listOfSimpleRecords

    @Test
    void listOfSimpleRecordsWithAllSimpleTypes() {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.LIST);
        node.get(VALUE_TYPE).get("class-name").get(TYPE).set(ModelType.STRING);
        node.get(VALUE_TYPE).get("module").get(TYPE).set(ModelType.STRING);
        node.get(VALUE_TYPE).get("action").get(TYPE).set(ModelType.STRING);
        AttributeDescription ad = new AttributeDescription(new Property("permissions", node));
        assertTrue(ad.listOfSimpleRecords());
    }

    @Test
    void listOfSimpleRecordsAcceptsNestedListOfSimpleType() {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.LIST);
        node.get(VALUE_TYPE).get("name").get(TYPE).set(ModelType.STRING);
        node.get(VALUE_TYPE).get("items").get(TYPE).set(ModelType.LIST);
        node.get(VALUE_TYPE).get("items").get(VALUE_TYPE).set(ModelType.STRING);
        AttributeDescription ad = new AttributeDescription(new Property("records", node));
        assertTrue(ad.listOfSimpleRecords());
    }

    @Test
    void listOfSimpleRecordsRejectsNestedListOfComplexType() {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.LIST);
        node.get(VALUE_TYPE).get("name").get(TYPE).set(ModelType.STRING);
        node.get(VALUE_TYPE).get("items").get(TYPE).set(ModelType.LIST);
        node.get(VALUE_TYPE).get("items").get(VALUE_TYPE).get("sub").get(TYPE).set(ModelType.STRING);
        AttributeDescription ad = new AttributeDescription(new Property("records", node));
        assertFalse(ad.listOfSimpleRecords());
    }

    @Test
    void listOfSimpleRecordsRejectsNestedObject() {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.LIST);
        node.get(VALUE_TYPE).get("name").get(TYPE).set(ModelType.STRING);
        node.get(VALUE_TYPE).get("options").get(TYPE).set(ModelType.OBJECT);
        AttributeDescription ad = new AttributeDescription(new Property("modules", node));
        assertFalse(ad.listOfSimpleRecords());
    }

    @Test
    void listOfSimpleRecordsNotForObject() {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.OBJECT);
        node.get(VALUE_TYPE).get("name").get(TYPE).set(ModelType.STRING);
        AttributeDescription ad = new AttributeDescription(new Property("settings", node));
        assertFalse(ad.listOfSimpleRecords());
    }

    @Test
    void listOfSimpleRecordsNotForSimpleList() {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.LIST);
        node.get(VALUE_TYPE).set(ModelType.STRING);
        AttributeDescription ad = new AttributeDescription(new Property("items", node));
        assertFalse(ad.listOfSimpleRecords());
    }

    @Test
    void listOfSimpleRecordsEmptyValueType() {
        ModelNode node = new ModelNode();
        node.get(TYPE).set(ModelType.LIST);
        node.get(VALUE_TYPE).setEmptyObject();
        AttributeDescription ad = new AttributeDescription(new Property("items", node));
        assertFalse(ad.listOfSimpleRecords());
    }
}
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
import java.util.Collections;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.description.AttributeDescription;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAR_TEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNIT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;

/** Factory methods for creating {@link AttributeDescription} instances in pipeline tests. */
final class TestData {

    static AttributeDescription stringAttribute(String name) {
        ModelNode desc = new ModelNode();
        desc.get(TYPE).set("STRING");
        return new AttributeDescription(new Property(name, desc));
    }

    static AttributeDescription booleanAttribute(String name) {
        ModelNode desc = new ModelNode();
        desc.get(TYPE).set("BOOLEAN");
        return new AttributeDescription(new Property(name, desc));
    }

    static AttributeDescription intAttribute(String name) {
        ModelNode desc = new ModelNode();
        desc.get(TYPE).set("INT");
        return new AttributeDescription(new Property(name, desc));
    }

    static AttributeDescription credentialReference(String name) {
        ModelNode desc = new ModelNode();
        desc.get(TYPE).set("OBJECT");
        desc.get(VALUE_TYPE).get(STORE).get(TYPE).set("STRING");
        desc.get(VALUE_TYPE).get(ALIAS).get(TYPE).set("STRING");
        desc.get(VALUE_TYPE).get(CLEAR_TEXT).get(TYPE).set("STRING");
        desc.get(VALUE_TYPE).get(TYPE).get(TYPE).set("STRING");
        return new AttributeDescription(new Property(name, desc));
    }

    static AttributeDescription timeUnit(String name) {
        ModelNode desc = new ModelNode();
        desc.get(TYPE).set("OBJECT");
        desc.get(VALUE_TYPE).get(TIME).get(TYPE).set("LONG");
        desc.get(VALUE_TYPE).get(UNIT).get(TYPE).set("STRING");
        return new AttributeDescription(new Property(name, desc));
    }

    static AttributeDescription fileObject(String name) {
        ModelNode desc = new ModelNode();
        desc.get(TYPE).set("OBJECT");
        desc.get(VALUE_TYPE).get(PATH).get(TYPE).set("STRING");
        desc.get(VALUE_TYPE).get(RELATIVE_TO).get(TYPE).set("STRING");
        return new AttributeDescription(new Property(name, desc));
    }

    static AttributeDescription simpleRecordObject(String name, String... subAttributes) {
        ModelNode desc = new ModelNode();
        desc.get(TYPE).set("OBJECT");
        for (String sub : subAttributes) {
            desc.get(VALUE_TYPE).get(sub).get(TYPE).set("STRING");
        }
        return new AttributeDescription(new Property(name, desc));
    }

    /** Builds a pool of attribute descriptions from varargs. */
    static List<AttributeDescription> pool(AttributeDescription... descriptions) {
        List<AttributeDescription> list = new ArrayList<>();
        Collections.addAll(list, descriptions);
        return list;
    }
}

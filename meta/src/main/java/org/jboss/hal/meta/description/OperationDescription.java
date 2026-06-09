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

import java.util.HashSet;
import java.util.Set;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;

import static org.jboss.hal.dmr.ModelDescriptionConstants.LIST_ADD_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LIST_CLEAR_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LIST_GET_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LIST_REMOVE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAP_CLEAR_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAP_GET_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAP_PUT_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAP_REMOVE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.QUERY_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ATTRIBUTE_GROUP_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ATTRIBUTE_GROUP_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_TYPES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_OPERATION_DESCRIPTION_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_OPERATION_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_DESCRIPTION_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REPLY_PROPERTIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WHOAMI_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

/**
 * Description of a management operation, providing access to its parameters and return type.
 */
public class OperationDescription extends NamedNode implements Description {

    /** The names of global management operations that are present on every resource. */
    public static final Set<String> GLOBAL_OPERATIONS = new HashSet<>();

    static {
        GLOBAL_OPERATIONS.add(LIST_ADD_OPERATION);
        GLOBAL_OPERATIONS.add(LIST_CLEAR_OPERATION);
        GLOBAL_OPERATIONS.add(LIST_GET_OPERATION);
        GLOBAL_OPERATIONS.add(LIST_REMOVE_OPERATION);
        GLOBAL_OPERATIONS.add(MAP_CLEAR_OPERATION);
        GLOBAL_OPERATIONS.add(MAP_GET_OPERATION);
        GLOBAL_OPERATIONS.add(MAP_PUT_OPERATION);
        GLOBAL_OPERATIONS.add(MAP_REMOVE_OPERATION);
        GLOBAL_OPERATIONS.add(QUERY_OPERATION);
        GLOBAL_OPERATIONS.add(READ_ATTRIBUTE_GROUP_NAMES_OPERATION);
        GLOBAL_OPERATIONS.add(READ_ATTRIBUTE_GROUP_OPERATION);
        GLOBAL_OPERATIONS.add(READ_ATTRIBUTE_OPERATION);
        GLOBAL_OPERATIONS.add(READ_CHILDREN_NAMES_OPERATION);
        GLOBAL_OPERATIONS.add(READ_CHILDREN_RESOURCES_OPERATION);
        GLOBAL_OPERATIONS.add(READ_CHILDREN_TYPES_OPERATION);
        GLOBAL_OPERATIONS.add(READ_OPERATION_DESCRIPTION_OPERATION);
        GLOBAL_OPERATIONS.add(READ_OPERATION_NAMES_OPERATION);
        GLOBAL_OPERATIONS.add(READ_RESOURCE_DESCRIPTION_OPERATION);
        GLOBAL_OPERATIONS.add(READ_RESOURCE_OPERATION);
        GLOBAL_OPERATIONS.add(UNDEFINE_ATTRIBUTE_OPERATION);
        GLOBAL_OPERATIONS.add(WHOAMI_OPERATION);
        GLOBAL_OPERATIONS.add(WRITE_ATTRIBUTE_OPERATION);
    }

    public OperationDescription() {
        super();
    }

    OperationDescription(Property property) {
        super(property);
    }

    @Override
    public ModelNode modelNode() {
        return asModelNode();
    }

    /** @return whether this operation is a global management operation */
    public boolean global() {
        return GLOBAL_OPERATIONS.contains(name());
    }

    /** @return the request properties (parameters) of this operation */
    public AttributeDescriptions parameters() {
        return new AttributeDescriptions(get(REQUEST_PROPERTIES));
    }

    /** @return the description of the operation's return value, or an empty description if no return type is defined */
    public AttributeDescription returnValue() {
        if (hasDefined(REPLY_PROPERTIES) && get(REPLY_PROPERTIES).hasDefined(TYPE)) {
            return new AttributeDescription("return-value", get(REPLY_PROPERTIES));
        } else {
            return new AttributeDescription();
        }
    }
}

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
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;

import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUEST_PROPERTIES;

public class OperationDescription extends NamedNode implements Description {

    private final OperationDescriptions operations;

    public OperationDescription(OperationDescriptions operations, Property property) {
        super(property);
        this.operations = operations;
    }

    @Override
    public ModelNode modelNode() {
        return asModelNode();
    }

    public AttributeDescriptions requestProperties() {
        return new AttributeDescriptions(get(REQUEST_PROPERTIES));
    }
}

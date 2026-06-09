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

import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATIONS;

/** Wrapper around the result of the read-resource-description operation. */
public class ResourceDescription extends ModelNode implements Description {

    private final AttributeDescriptions attributeDescriptions;
    private final OperationDescriptions operationDescriptions;
    private final CapabilityDescriptions capabilityDescriptions;

    public ResourceDescription() {
        super();
        attributeDescriptions = new AttributeDescriptions();
        operationDescriptions = new OperationDescriptions();
        capabilityDescriptions = new CapabilityDescriptions();
    }

    public ResourceDescription(ModelNode payload) {
        set(payload);
        attributeDescriptions = new AttributeDescriptions(get(ATTRIBUTES));
        operationDescriptions = new OperationDescriptions(get(OPERATIONS));
        capabilityDescriptions = new CapabilityDescriptions(get(CAPABILITIES));
    }

    @Override
    public ModelNode modelNode() {
        return this;
    }

    /** @return the attribute descriptions of this resource */
    public AttributeDescriptions attributes() {
        return attributeDescriptions;
    }

    /** @return the operation descriptions of this resource */
    public OperationDescriptions operations() {
        return operationDescriptions;
    }

    /** @return the capability descriptions of this resource */
    public CapabilityDescriptions capabilities() {
        return capabilityDescriptions;
    }
}

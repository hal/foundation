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

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DYNAMIC;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DYNAMIC_ELEMENTS;

/**
 * Description of a capability provided or referenced by a management resource.
 */
public class CapabilityDescription extends NamedNode implements Description {

    public CapabilityDescription(ModelNode modelNode) {
        super(modelNode);
    }

    public boolean dynamic() {
        return get(DYNAMIC).asBoolean(false);
    }

    public List<String> dynamicElements() {
        return get(DYNAMIC_ELEMENTS).isDefined()
                ? get(DYNAMIC_ELEMENTS).asList().stream().map(ModelNode::asString).collect(toList())
                : emptyList();
    }

    @Override
    public ModelNode modelNode() {
        return asModelNode();
    }
}

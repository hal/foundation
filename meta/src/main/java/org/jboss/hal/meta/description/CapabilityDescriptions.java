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

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;

/** Wrapper around the capabilities of a {@link ResourceDescription} */
public class CapabilityDescriptions implements Iterable<CapabilityDescription> {

    private final List<CapabilityDescription> capabilities;

    CapabilityDescriptions() {
        this.capabilities = emptyList();
    }

    CapabilityDescriptions(ModelNode modelNode) {
        this.capabilities = modelNode.isDefined()
                ? modelNode.asList()
                .stream()
                .map(CapabilityDescription::new)
                .sorted(comparing(NamedNode::name))
                .collect(Collectors.toList())
                : List.of();
    }

    @Override
    public Iterator<CapabilityDescription> iterator() {
        return capabilities.iterator();
    }

    /** @return whether this collection contains no capability descriptions */
    public boolean isEmpty() {
        return capabilities.isEmpty();
    }

    /** @return the number of capability descriptions */
    public int size() {
        return capabilities.size();
    }
}

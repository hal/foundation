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
import java.util.LinkedHashMap;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;

import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/** Wrapper around the operations of a {@link ResourceDescription} */
public class OperationDescriptions implements Iterable<OperationDescription> {

    private final LinkedHashMap<String, OperationDescription> operations;

    OperationDescriptions() {
        this.operations = new LinkedHashMap<>();
    }

    OperationDescriptions(ModelNode modelNode) {
        this.operations = modelNode.isDefined()
                ? modelNode.asPropertyList()
                .stream()
                .map(OperationDescription::new)
                .sorted(comparing(NamedNode::name))
                .collect(toMap(NamedNode::name, identity(), (existing, replacement) -> replacement, LinkedHashMap::new))
                : new LinkedHashMap<>();
    }

    @Override
    public Iterator<OperationDescription> iterator() {
        return operations.values().iterator();
    }

    /** @return whether the resource supports an operation with the given name */
    public boolean supports(String name) {
        return operations.containsKey(name);
    }

    /** @return the operation description with the given name, or an empty description if not found */
    public OperationDescription get(String name) {
        return operations.getOrDefault(name, new OperationDescription());
    }

    /** @return whether this collection contains no operation descriptions */
    public boolean isEmpty() {
        return operations.isEmpty();
    }

    /** @return the number of operation descriptions */
    public int size() {
        return operations.size();
    }
}

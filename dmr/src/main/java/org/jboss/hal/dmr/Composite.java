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
package org.jboss.hal.dmr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.joining;
import static org.jboss.hal.dmr.ModelDescriptionConstants.COMPOSITE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATION_HEADERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STEPS;

/**
 * A composite (batch) operation consisting of multiple {@link Operation}s that are executed atomically against the WildFly
 * management endpoint. Steps are added sequentially and referenced by index in the {@link CompositeResult}.
 */
public class Composite extends Operation implements Iterable<Operation> {

    private final List<Operation> operations;

    /** Creates a new empty composite. */
    public Composite() {
        this(ResourceAddress.root());
    }

    /** Creates a new empty composite with the given address. */
    public Composite(ResourceAddress address) {
        super(COMPOSITE, address, new ModelNode(), new ModelNode(), emptySet());
        this.operations = new ArrayList<>();
    }

    /** Creates a new composite from the given operations. */
    public Composite(Operation first, Operation... rest) {
        this(ResourceAddress.root()); // required by JsInterop
        add(first);
        if (rest != null) {
            for (Operation operation : rest) {
                add(operation);
            }
        }
    }

    /** Creates a new composite from the given list of operations. */
    public Composite(List<Operation> operations) {
        this(ResourceAddress.root());
        operations.forEach(this::add);
    }

    /**
     * Adds the specified operation to this composite.
     *
     * @param operation The operation to add.
     *
     * @return this composite
     */
    public Composite add(Operation operation) {
        operations.add(operation);
        get(STEPS).add(operation);
        return this;
    }

    /** Adds a string operation header to this composite. */
    public Composite addHeader(String name, String value) {
        get(OPERATION_HEADERS).get(name).set(value);
        return this;
    }

    /** Adds an integer operation header to this composite. */
    public Composite addHeader(String name, int value) {
        get(OPERATION_HEADERS).get(name).set(value);
        return this;
    }

    /** Adds a boolean operation header to this composite. */
    public Composite addHeader(String name, boolean value) {
        get(OPERATION_HEADERS).get(name).set(value);
        return this;
    }

    /** Returns an iterator over the operations in this composite. */
    @Override
    public Iterator<Operation> iterator() {
        return operations.iterator();
    }

    /** @return whether this composite contains operations */
    public boolean isEmpty() {
        return operations.isEmpty();
    }

    /** @return the number of operations */
    public int size() {
        return operations.size();
    }

    /** Returns a new composite with all operations configured to run with the specified RBAC roles. */
    public Composite runAs(Set<String> runAs) {
        List<Operation> runAsOperations = operations.stream()
                .map(operation -> operation.runAs(runAs))
                .collect(Collectors.toList());
        return new Composite(runAsOperations);
    }

    /** @return a string representation of this composite */
    @Override
    public String toString() {
        return "Composite(" + operations.size() + ")";
    }

    /** @return the string representation of the operation as used in the CLI */
    public String asCli() {
        return operations.stream().map(Operation::asCli).collect(joining("\n"));
    }
}

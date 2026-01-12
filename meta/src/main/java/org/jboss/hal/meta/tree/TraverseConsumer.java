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
package org.jboss.hal.meta.tree;

import java.util.Set;

import org.jboss.hal.meta.AddressTemplate;

/**
 * A functional interface that consumes the results of the
 * {@link ModelTree#traverse(TraverseContinuation, AddressTemplate, Set, Set, TraverseOperation, TraverseConsumer)} method. This
 * interface should be used to define actions that involve processing an {@link AddressTemplate}, a payload of a generic type
 * (the result of {@link TraverseOperation#execute(AddressTemplate)}), and a {@link TraverseContext}.
 *
 * @param <T> the type of the payload to be consumed during traversal
 */
@FunctionalInterface
public interface TraverseConsumer<T> {

    /**
     * Consumes the traversal results by processing an {@link AddressTemplate}, a payload of a generic type, and the associated
     * {@link TraverseContext}.
     *
     * @param template the address template associated with the current context of the traversal
     * @param payload  the generic payload to be consumed during the traversal
     * @param context  the traverse context that provides additional details or state during the traversal
     */
    void accept(AddressTemplate template, T payload, TraverseContext context);
}

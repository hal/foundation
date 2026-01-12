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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import elemental2.promise.Promise;

/**
 * Represents an operation performed during the
 * {@link ModelTree#traverse(TraverseContinuation, AddressTemplate, Set, Set, TraverseOperation, TraverseConsumer)} methodbased
 * on a specified {@link AddressTemplate}. The operation is executed asynchronously and returns a {@link Promise} containing the
 * result.
 *
 * @param <T> the type of the result produced by the operation
 */
public interface TraverseOperation<T> {

    /**
     * A no-operation implementation of {@link TraverseOperation} that performs no action and resolves immediately with an
     * undefined {@link ModelNode}.
     * <p>
     * This implementation is commonly used as a placeholder or default behavior when no specific operation needs to be executed
     * during a traversal.
     */
    TraverseOperation<ModelNode> NOOP = (template, context) -> Promise.resolve(new ModelNode());

    /**
     * Executes an operation based on the specified {@link AddressTemplate}. The operation is executed asynchronously and
     * returns a {@link Promise} containing the result.
     *
     * @param template the {@link AddressTemplate} that provides the context or scope for the operation
     * @param context  the {@link StatementContext} used to turn the {@link AddressTemplate} into a
     *                 {@link org.jboss.hal.dmr.ResourceAddress}
     * @return a {@link Promise} containing the result of the operation
     */
    Promise<T> execute(AddressTemplate template, StatementContext context);
}

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

/**
 * DMR (Dynamic Model Representation) protocol implementation for communicating with the WildFly management API. Provides
 * the data model, operations, and serialization for management commands.
 *
 * <p>
 * The DMR protocol is WildFly's native management protocol, enabling remote administration through a flexible, typed data
 * model. All management resources, attributes, and operations are represented as {@link org.jboss.hal.dmr.ModelNode}
 * instances, which can contain primitives, lists, or nested objects.
 *
 * <h2>Core Components</h2>
 * <dl>
 * <dt>{@link org.jboss.hal.dmr.ModelNode}</dt>
 * <dd>The fundamental data structure for all management model values</dd>
 *
 * <dt>{@link org.jboss.hal.dmr.ModelType}</dt>
 * <dd>Type system for model values (STRING, INT, LIST, OBJECT, etc.)</dd>
 *
 * <dt>{@link org.jboss.hal.dmr.Operation}</dt>
 * <dd>A management operation with address, name, and parameters</dd>
 *
 * <dt>{@link org.jboss.hal.dmr.Composite}</dt>
 * <dd>A batch of operations executed atomically</dd>
 *
 * <dt>{@link org.jboss.hal.dmr.ResourceAddress}</dt>
 * <dd>A fully qualified path to a management resource</dd>
 *
 * <dt>{@link org.jboss.hal.dmr.NamedNode}</dt>
 * <dd>A model node with an associated name (key from a property list)</dd>
 *
 * <dt>{@link org.jboss.hal.dmr.Property}</dt>
 * <dd>A name/value pair in the management model</dd>
 * </dl>
 *
 * <h2>Example Usage</h2>
 * {@snippet :
 *     // Build and execute a management operation
 *     ResourceAddress address = ResourceAddress.of("subsystem", "datasources");
 *     Operation operation = new Operation.Builder(address, "read-resource")
 *             .param("recursive", true)
 *             .build();
 *     dispatcher.execute(operation).then(result -> {
 *         String name = result.get("name").asString();
 *         return null;
 *     });
 * }
 *
 * @see org.jboss.hal.dmr.dispatch
 */
package org.jboss.hal.dmr;

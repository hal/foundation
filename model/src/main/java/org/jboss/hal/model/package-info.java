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
 * Domain model classes for WildFly management concepts.
 * <p>
 * This package contains model classes representing core WildFly management entities and their states.
 * Most model classes extend {@link org.jboss.hal.dmr.NamedNode}, wrapping a DMR {@link org.jboss.hal.dmr.ModelNode}
 * with a name identifier.
 *
 * <dl>
 * <dt>{@link org.jboss.hal.model.deployment}</dt>
 * <dd>Deployments and content repository</dd>
 *
 * <dt>{@link org.jboss.hal.model.filter}</dt>
 * <dd>Filter attributes for model browser filtering</dd>
 *
 * <dt>{@link org.jboss.hal.model.host}</dt>
 * <dd>Domain hosts</dd>
 *
 * <dt>{@link org.jboss.hal.model.server}</dt>
 * <dd>Server configurations and runtime state</dd>
 *
 * <dt>{@link org.jboss.hal.model.subsystem}</dt>
 * <dd>Subsystem metadata</dd>
 *
 * <dt>{@link org.jboss.hal.model.user}</dt>
 * <dd>Users, roles, and RBAC</dd>
 * </dl>
 */
package org.jboss.hal.model;

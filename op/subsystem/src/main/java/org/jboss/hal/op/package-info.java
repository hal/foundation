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
 * WildFly subsystem extension for the HAL on premise management console. Registers the console as a static web
 * context on the WildFly management HTTP interface at {@code /halop}.
 *
 * <p>The subsystem is registered at the {@code experimental} stability level and consists of:
 * <dl>
 *     <dt>{@link org.jboss.hal.op.HalOpExtension}</dt>
 *     <dd>The WildFly extension entry point that wires subsystem configuration, model, and XML persistence.</dd>
 *     <dt>{@link org.jboss.hal.op.HalOpSubsystemSchema}</dt>
 *     <dd>XML schema definitions for parsing and marshalling the subsystem configuration.</dd>
 * </dl>
 */
package org.jboss.hal.op;

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
 * Composite attribute detection for structured OBJECT-type management attributes that should be rendered as a single
 * consolidated UI component rather than flattened into individual sub-attributes.
 * <p>
 * A composite attribute is an OBJECT attribute whose value-type defines a fixed set of well-known sub-attributes forming a
 * semantic unit (e.g., a credential reference or a duration with time and unit). The classes in this package detect these
 * attributes by their structure, not by name, so they cover all naming variants across the WildFly management model.
 * <p>
 * Key components:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.composite.CompositeAttribute}</dt>
 * <dd>Functional interface for structural attribute matching.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.composite.CompositeAttributes}</dt>
 * <dd>Registry of all composite attribute instances, consulted to prevent flattening and to route to custom view/form
 * item providers.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.composite.CredentialReferenceAttribute}</dt>
 * <dd>Matches credential reference attributes ({@code store}, {@code alias}, {@code clear-text}).</dd>
 * <dt>{@link org.jboss.hal.ui.resource.composite.TimeUnitAttribute}</dt>
 * <dd>Matches keepalive-time attributes ({@code time}, {@code unit}).</dd>
 * </dl>
 */
package org.jboss.hal.ui.resource.composite;

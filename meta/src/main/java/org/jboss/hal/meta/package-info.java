/*
 *  Copyright 2025 Red Hat
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
 * Metadata layer for the HAL management console providing address templates, metadata caching, statement context
 * resolution, and security information for WildFly management resources.
 *
 * <p>
 * This package provides the core abstractions for working with WildFly's management model:
 * </p>
 *
 * <dl>
 * <dt>{@link org.jboss.hal.meta.AddressTemplate AddressTemplate}</dt>
 * <dd>Parameterized DMR addresses with placeholder support</dd>
 * <dt>{@link org.jboss.hal.meta.MetadataRepository MetadataRepository}</dt>
 * <dd>Two-level cache for resource descriptions and security contexts</dd>
 * <dt>{@link org.jboss.hal.meta.StatementContext StatementContext}</dt>
 * <dd>Resolves placeholders based on current environment selections</dd>
 * <dt>{@link org.jboss.hal.meta.Metadata Metadata}</dt>
 * <dd>Pairs a resource description with its security context</dd>
 * </dl>
 *
 * <h2>Example Usage</h2>
 * {@snippet :
 *     // Create an address template
 *     AddressTemplate template = AddressTemplate.ofTrusted("subsystem=datasources/data-source=*");
 *
 *     // Look up metadata
 *     metadataRepository.lookup(template, metadata -> {
 *         ResourceDescription description = metadata.resourceDescription();
 *         SecurityContext security = metadata.securityContext();
 *     });
 * }
 */
package org.jboss.hal.meta;

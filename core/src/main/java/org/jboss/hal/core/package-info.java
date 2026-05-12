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
 * Core services for the HAL management console: notifications, CRUD operations, and human-readable label generation.
 *
 * <h2>Key Concepts</h2>
 * <p>
 * This package provides three main areas of functionality:
 * <dl>
 *     <dt>{@link org.jboss.hal.core.CrudOperations}</dt>
 *     <dd>An {@code @ApplicationScoped} CDI bean that performs create, read, update, and delete operations against WildFly
 *     management resources using the {@link org.jboss.hal.dmr.dispatch.Dispatcher}. Emits
 *     {@link org.jboss.hal.core.Notification notifications} for success and error outcomes.</dd>
 *
 *     <dt>{@link org.jboss.hal.core.Notifications}</dt>
 *     <dd>An {@code @ApplicationScoped} CDI bean that manages the lifecycle of {@link org.jboss.hal.core.Notification}
 *     instances using an {@link org.jboss.hal.db.LRUCache LRU cache}. Supports sending, reading, clearing, and removing
 *     notifications, and fires CDI events to notify the UI.</dd>
 *
 *     <dt>{@link org.jboss.hal.core.Humanize}</dt>
 *     <dd>Converts WildFly management model terms (such as attribute and resource names) into human-readable labels using
 *     sentence or capital casing, with special handling for common abbreviations (HTTP, SSL, JPA, etc.).</dd>
 * </dl>
 *
 * <h2>Usage</h2>
 * {@snippet :
 *     // Send a notification
 *     notifications.send(Notification.success("Resource added",
 *             "Datasource MyDS has been successfully added."));
 *
 *     // CRUD: create a resource
 *     crudOperations.create(template, resourceData)
 *             .then(result -> handleSuccess(result));
 *
 *     // Generate labels
 *     String label = Humanize.capitalCase("ejb-connection-pool");
 *     // produces "EJB Connection Pool"
 * }
 */
package org.jboss.hal.core;

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
 * Provides local data storage and caching for the HAL management console.
 *
 * <h2>Key Concepts</h2>
 * <p>
 * This package contains an {@link org.jboss.hal.db.LRUCache LRUCache} for bounded, in-memory caching with least-recently-used
 * eviction, and a {@link org.jboss.hal.db.Document Document} interface for PouchDB document interop. The LRU cache is used by
 * other modules (such as the metadata repository) to store frequently accessed data with automatic eviction when the cache
 * reaches capacity.
 *
 * <h2>Usage</h2>
 * {@snippet :
 *     LRUCache<String, ModelNode> cache = new LRUCache<>(100);
 *     cache.addRemovalHandler((key, value) ->
 *             logger.debug("Evicted %s", key));
 *     cache.put("datasource-1", modelNode);
 *     ModelNode result = cache.get("datasource-1");
 * }
 */
package org.jboss.hal.db;

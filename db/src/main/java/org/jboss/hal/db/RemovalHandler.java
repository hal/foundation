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
package org.jboss.hal.db;

/** Callback invoked when an entry is evicted from an {@link LRUCache} due to capacity overflow. */
@FunctionalInterface
public interface RemovalHandler<K, V> {

    /**
     * Called when an entry is evicted from the cache.
     *
     * @param key   the key of the evicted entry
     * @param value the value of the evicted entry
     */
    void onRemoval(K key, V value);
}

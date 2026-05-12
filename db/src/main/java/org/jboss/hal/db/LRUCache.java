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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * A fixed-capacity, least-recently-used (LRU) cache. When the cache exceeds its capacity, the least recently accessed entry is
 * evicted automatically. Registered {@link RemovalHandler}s are notified on eviction.
 *
 * <p>
 * Accessing an entry via {@link #get(Object)} or updating it via {@link #put(Object, Object)} promotes it to the most recently
 * used position. This implementation is not thread-safe.
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 */
public class LRUCache<K, V> {

    private final int capacity;
    private final LinkedList<Node<K, V>> cacheList;
    private final HashMap<K, Node<K, V>> cacheMap;
    private final List<RemovalHandler<K, V>> removalHandlers;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cacheList = new LinkedList<>();
        this.cacheMap = new HashMap<>();
        this.removalHandlers = new ArrayList<>();
    }

    // ------------------------------------------------------ api

    /** Returns {@code true} if the cache contains an entry for the given key. */
    public boolean contains(K key) {
        return cacheMap.containsKey(key);
    }

    /** Returns the number of entries currently in the cache. */
    public int size() {
        return cacheMap.size();
    }

    /**
     * Returns the value associated with the given key, or {@code null} if no entry exists. Accessing an entry promotes it to the
     * most recently used position.
     */
    public V get(K key) {
        Node<K, V> node = cacheMap.get(key);
        if (node == null) {
            return null;
        }

        moveToHead(node);
        return node.value;
    }

    /** Returns a set view of the cache entries. */
    public Set<Map.Entry<K, Node<K, V>>> entries() {
        return cacheMap.entrySet();
    }

    /** Returns a set of all keys in the cache. */
    public Set<K> keys() {
        return cacheMap.values().stream().map(n -> n.key).collect(toSet());
    }

    /** Returns a list of all values in the cache. */
    public List<V> values() {
        return cacheMap.values().stream().map(n -> n.value).collect(toList());
    }

    /**
     * Associates the given value with the given key. If the key already exists, its value is updated and the entry is promoted to
     * the most recently used position. If the cache is at capacity, the least recently used entry is evicted and
     * {@link RemovalHandler}s are notified.
     */
    public void put(K key, V value) {
        Node<K, V> existingNode = cacheMap.get(key);
        if (existingNode != null) {
            existingNode.value = value;
            moveToHead(existingNode);
            return;
        }

        Node<K, V> newNode = new Node<>(key, value);
        cacheList.addFirst(newNode);
        cacheMap.put(key, newNode);

        if (cacheList.size() > capacity) {
            removeLeastRecentlyUsed();
        }
    }

    /**
     * Removes the entry for the given key and returns its value, or {@code null} if no entry exists. Removal handlers are
     * <strong>not</strong> notified for explicit removals.
     */
    public V remove(K key) {
        Node<K, V> existingNode = cacheMap.remove(key);
        if (existingNode != null) {
            cacheList.remove(existingNode);
            return existingNode.value;
        }
        return null;
    }

    /** Registers a handler that is called whenever an entry is evicted due to capacity overflow. */
    public void addRemovalHandler(RemovalHandler<K, V> handler) {
        removalHandlers.add(handler);
    }

    // ------------------------------------------------------ internal

    // for testing purposes
    @SuppressWarnings("unchecked")
    K[] keysArray() {
        return (K[]) cacheList.stream().map(n -> n.key).toArray();
    }

    private void moveToHead(Node<K, V> node) {
        cacheList.remove(node);
        cacheList.addFirst(node);
    }

    private void removeLeastRecentlyUsed() {
        Node<K, V> tail = cacheList.removeLast();
        cacheMap.remove(tail.key);
        for (RemovalHandler<K, V> removalHandler : removalHandlers) {
            removalHandler.onRemoval(tail.key, tail.value);
        }
    }

    // ------------------------------------------------------ inner classes

    /** A key-value pair stored in the cache. */
    public static class Node<K, V> {

        public final K key;
        public V value;

        private Node(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {return true;}
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Node<?, ?> node = (Node<?, ?>) o;
            return Objects.equals(key, node.key);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key);
        }
    }
}


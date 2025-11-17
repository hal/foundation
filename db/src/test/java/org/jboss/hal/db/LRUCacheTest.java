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

import java.util.Random;

import org.jboss.elemento.Id;
import org.junit.jupiter.api.Test;

import static java.lang.Math.max;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("GrazieInspection")
class LRUCacheTest {

    @Test
    void lifecycle() {
        String value;
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        Recorder recorder = new Recorder();
        cache.addRemovalHandler(recorder);

        // populate (3:C, 2:B, 1:A)
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");
        assertNull(recorder.lastKey);
        assertNull(recorder.lastValue);
        assertArrayEquals(new Integer[]{3, 2, 1}, cache.keys());

        // new entry (4:D, 3:C, 2:B) [1:A]
        cache.put(4, "D");
        assertEquals(1, recorder.lastKey);
        assertEquals("A", recorder.lastValue);
        assertArrayEquals(new Integer[]{4, 3, 2}, cache.keys());

        // update entry (2:b, 4:D, 3:C)
        cache.put(2, "b");
        assertEquals(1, recorder.lastKey);
        assertEquals("A", recorder.lastValue);
        assertArrayEquals(new Integer[]{2, 4, 3}, cache.keys());

        // get entry (3:C, 2:b, 4:D)
        value = cache.get(3);
        assertEquals("C", value);
        assertEquals(1, recorder.lastKey);
        assertEquals("A", recorder.lastValue);
        assertArrayEquals(new Integer[]{3, 2, 4}, cache.keys());

        // new entry (5:E, 3:C, 2:b) [4:D]
        cache.put(5, "E");
        assertEquals(4, recorder.lastKey);
        assertEquals("D", recorder.lastValue);
        assertArrayEquals(new Integer[]{5, 3, 2}, cache.keys());

        // remove entry (5:E, 2:b)
        value = cache.remove(3);
        assertEquals("C", value);
        assertEquals(4, recorder.lastKey);
        assertEquals("D", recorder.lastValue);
        assertArrayEquals(new Integer[]{5, 2}, cache.keys());

        // new entry (6:F, 5:E, 2:b)
        cache.put(6, "F");
        assertEquals(4, recorder.lastKey);
        assertEquals("D", recorder.lastValue);
        assertArrayEquals(new Integer[]{6, 5, 2}, cache.keys());

        // new entry (7:G, 6:F, 5:E) [2:b]
        cache.put(7, "G");
        assertEquals(2, recorder.lastKey);
        assertEquals("b", recorder.lastValue);
        assertArrayEquals(new Integer[]{7, 6, 5}, cache.keys());
    }

    @Test
    void stressTest() {
        int key = 0;
        Random random = new Random();
        for (int capacity = 10; capacity < 1_000; capacity += 10) {
            final int fc = capacity;
            final LRUCache<Integer, String> cache = new LRUCache<>(capacity);
            cache.addRemovalHandler((k, v) -> assertEquals(fc, cache.size()));
            for (int i = 0; i < capacity / 2 + random.nextInt(2 * capacity); i++) {

                // read operation
                if (random.nextInt(9) % 3 == 0) {
                    for (int j = 0; j < 10 + random.nextInt(10); j++) {
                        int z = random.nextInt(max(1, cache.size()));
                        cache.get(z); // read operation
                        assertTrue(cache.size() <= capacity);
                    }
                }

                // write operation
                cache.put(key++, Id.uuid());
                assertTrue(cache.size() <= capacity);

                // read operation
                if (random.nextInt(9) % 3 == 0) {
                    for (int j = 0; j < 10 + random.nextInt(10); j++) {
                        int z = random.nextInt(max(1, cache.size()));
                        cache.get(z); // read operation
                        assertTrue(cache.size() <= capacity);
                    }
                }
            }
        }
    }

    private static class Recorder implements RemovalHandler<Integer, String> {

        Integer lastKey = null;
        String lastValue = null;

        @Override
        public void onRemoval(Integer key, String value) {
            this.lastKey = key;
            this.lastValue = value;
        }
    }
}
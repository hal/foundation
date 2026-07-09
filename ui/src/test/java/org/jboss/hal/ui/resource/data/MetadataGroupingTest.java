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
package org.jboss.hal.ui.resource.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.hal.ui.resource.ResourceAttribute;
import org.junit.jupiter.api.Test;

import static org.jboss.hal.ui.resource.ResourceAttribute.UNGROUPED;
import static org.jboss.hal.ui.resource.data.TestAttributes.attribute;
import static org.jboss.hal.ui.resource.data.TestAttributes.attributeWithGroup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataGroupingTest {

    private final MetadataGrouping strategy = new MetadataGrouping();

    @Test
    void allUngrouped() {
        List<ResourceAttribute> attributes = List.of(
                attribute("alpha"),
                attribute("beta"),
                attribute("gamma"));
        Map<String, List<ResourceAttribute>> result = strategy.group(attributes);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(UNGROUPED));
        assertEquals(3, result.get(UNGROUPED).size());
    }

    @Test
    void allGrouped() {
        List<ResourceAttribute> attributes = List.of(
                attributeWithGroup("a", "security"),
                attributeWithGroup("b", "pool"),
                attributeWithGroup("c", "security"));
        Map<String, List<ResourceAttribute>> result = strategy.group(attributes);

        assertEquals(2, result.size());
        assertFalse(result.containsKey(UNGROUPED));
        assertEquals(1, result.get("pool").size());
        assertEquals(2, result.get("security").size());
    }

    @Test
    void mixedGroupedAndUngrouped() {
        List<ResourceAttribute> attributes = List.of(
                attribute("standalone"),
                attributeWithGroup("pooled", "pool"),
                attributeWithGroup("secured", "security"));
        Map<String, List<ResourceAttribute>> result = strategy.group(attributes);

        assertEquals(3, result.size());
        // ungrouped comes first
        List<String> keys = new ArrayList<>(result.keySet());
        assertEquals(UNGROUPED, keys.get(0));
        assertEquals(1, result.get(UNGROUPED).size());
        assertEquals(1, result.get("pool").size());
        assertEquals(1, result.get("security").size());
    }

    @Test
    void groupsSortedAlphabetically() {
        List<ResourceAttribute> attributes = List.of(
                attributeWithGroup("z", "zebra"),
                attributeWithGroup("a", "alpha"),
                attributeWithGroup("m", "middle"));
        Map<String, List<ResourceAttribute>> result = strategy.group(attributes);

        List<String> keys = new ArrayList<>(result.keySet());
        assertEquals("alpha", keys.get(0));
        assertEquals("middle", keys.get(1));
        assertEquals("zebra", keys.get(2));
    }

    @Test
    void ungroupedBeforeNamedGroups() {
        List<ResourceAttribute> attributes = List.of(
                attribute("standalone"),
                attributeWithGroup("grouped", "aaa"));
        Map<String, List<ResourceAttribute>> result = strategy.group(attributes);

        List<String> keys = new ArrayList<>(result.keySet());
        assertEquals(UNGROUPED, keys.get(0));
        assertEquals("aaa", keys.get(1));
    }

    @Test
    void emptyList() {
        Map<String, List<ResourceAttribute>> result = strategy.group(List.of());
        assertTrue(result.isEmpty());
    }
}

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
import static org.jboss.hal.ui.resource.data.TestAttributes.nestedAttributes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoGroupingTest {

    private final AutoGrouping strategy = new AutoGrouping();

    @Test
    void neverProducesUngroupedKey() {
        List<ResourceAttribute> attributes = generateAttributes("a", "b", "c", "d", "e",
                "f", "g", "h", "i", "j",
                "k", "l", "m", "n", "o",
                "p", "q", "r", "s", "t");
        Map<String, List<ResourceAttribute>> result = strategy.group(attributes);
        assertFalse(result.containsKey(UNGROUPED));
    }

    @Test
    void allAttributesCovered() {
        List<ResourceAttribute> attributes = generateAttributes(
                "alpha", "beta", "charlie", "delta", "echo",
                "foxtrot", "golf", "hotel", "india", "juliet",
                "kilo", "lima", "mike", "november", "oscar",
                "papa", "quebec", "romeo", "sierra", "tango",
                "uniform", "victor", "whiskey", "xray", "yankee", "zulu");
        Map<String, List<ResourceAttribute>> result = strategy.group(attributes);

        int totalInGroups = result.values().stream().mapToInt(List::size).sum();
        assertEquals(attributes.size(), totalInGroups);
    }

    @Test
    void groupKeysAreLetterRanges() {
        List<ResourceAttribute> attributes = generateAttributes(
                "alpha", "bravo", "charlie", "delta", "echo",
                "foxtrot", "golf", "hotel", "india", "juliet",
                "kilo", "lima", "mike", "november", "oscar",
                "papa", "quebec", "romeo", "sierra", "tango");
        Map<String, List<ResourceAttribute>> result = strategy.group(attributes);

        for (String key : result.keySet()) {
            // each key is either a single letter or a range like "A – D"
            assertTrue(key.matches("^[A-Z]$") || key.matches("^[A-Z] – [A-Z]$"),
                    "Unexpected group key: " + key);
        }
    }

    @Test
    void sameLetterNeverSplitAcrossGroups() {
        // 15 attributes starting with 'A', then 5 starting with 'B'
        List<ResourceAttribute> attributes = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            attributes.add(attribute("a" + String.format("%02d", i)));
        }
        for (int i = 0; i < 5; i++) {
            attributes.add(attribute("b" + i));
        }
        Map<String, List<ResourceAttribute>> result = strategy.group(attributes);

        // all 'A' attributes must be in the same group
        for (List<ResourceAttribute> group : result.values()) {
            boolean hasA = group.stream().anyMatch(ra -> ra.name.startsWith("a"));
            if (hasA) {
                assertTrue(group.stream().allMatch(ra -> ra.name.startsWith("a") || ra.name.startsWith("b")),
                        "Group with 'A' attributes contains unexpected letters");
                long aCount = group.stream().filter(ra -> ra.name.startsWith("a")).count();
                assertEquals(15, aCount, "All 'A' attributes must be in the same group");
                break;
            }
        }
    }

    @Test
    void singleLetterGroupKey() {
        // all attributes start with 'X'
        List<ResourceAttribute> attributes = generateAttributes(
                "x1", "x2", "x3", "x4", "x5",
                "x6", "x7", "x8", "x9", "x10",
                "x11", "x12", "x13", "x14", "x15",
                "x16", "x17", "x18", "x19", "x20");
        Map<String, List<ResourceAttribute>> result = strategy.group(attributes);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("X"));
        assertEquals(20, result.get("X").size());
    }

    @Test
    void preservesInputOrderWithinGroups() {
        List<ResourceAttribute> attributes = generateAttributes(
                "alpha", "ant", "apple", "arc",
                "beta", "bird", "bloom", "box",
                "cat", "cow", "cup", "cycle",
                "dog", "dot", "drum", "duck",
                "egg", "elm", "eye", "era");
        Map<String, List<ResourceAttribute>> result = strategy.group(attributes);

        // verify each group's items appear in the same relative order as the input
        for (List<ResourceAttribute> group : result.values()) {
            int lastIndex = -1;
            for (ResourceAttribute ra : group) {
                int inputIndex = attributes.indexOf(ra);
                assertTrue(inputIndex > lastIndex,
                        "Attributes within a group must preserve input order");
                lastIndex = inputIndex;
            }
        }
    }

    @Test
    void nestedAttributesGroupedByParentName() {
        // nested attributes like "credential-reference.store" should be grouped by 'C', not 'S'
        List<ResourceAttribute> attributes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            attributes.add(attribute("alpha-" + i));
        }
        attributes.addAll(nestedAttributes("credential-reference", "store", "alias", "type", "clear-text"));
        for (int i = 0; i < 10; i++) {
            attributes.add(attribute("delta-" + i));
        }
        Map<String, List<ResourceAttribute>> result = strategy.group(attributes);

        // credential-reference children (fqn starts with 'c') must be in a group whose range covers 'C'
        for (Map.Entry<String, List<ResourceAttribute>> entry : result.entrySet()) {
            for (ResourceAttribute ra : entry.getValue()) {
                if (ra.fqn.startsWith("credential-reference.")) {
                    String key = entry.getKey();
                    assertTrue(key.contains("C"),
                            "Nested attribute '" + ra.fqn + "' should be in a group covering 'C', but was in '" + key + "'");
                }
            }
        }
    }

    @Test
    void datasourceScenario() {
        // simulate a data-source-like resource with many attributes across various letters
        List<ResourceAttribute> attributes = generateAttributes(
                "allocation-retry", "allocation-retry-wait-millis", "allow-multiple-users",
                "authentication-context", "background-validation", "background-validation-millis",
                "blocking-timeout-wait-millis", "capacity-decrementer-class",
                "capacity-decrementer-properties", "capacity-incrementer-class",
                "capacity-incrementer-properties", "check-valid-connection-sql",
                "connectable", "connection-listener-class", "connection-listener-property",
                "connection-url", "credential-reference", "datasource-class",
                "driver-class", "driver-name", "enabled",
                "enlistment-trace", "exception-sorter-class-name", "exception-sorter-properties",
                "flush-strategy", "idle-timeout-minutes", "initial-pool-size",
                "jndi-name", "jta", "max-pool-size",
                "mcp", "min-pool-size", "new-connection-sql",
                "pad-xid", "password", "pool-fair",
                "pool-prefill", "pool-use-strict-min", "prepared-statements-cache-size",
                "query-timeout", "reauth-plugin-class-name", "reauth-plugin-properties",
                "security-domain", "set-tx-query-timeout", "share-prepared-statements",
                "spy", "stale-connection-checker-class-name", "stale-connection-checker-properties",
                "statistics-enabled", "tracking", "transaction-isolation",
                "url-delimiter", "url-property", "url-selector-strategy-class-name",
                "use-ccm", "use-fast-fail", "use-java-context",
                "user-name", "valid-connection-checker-class-name", "valid-connection-checker-properties",
                "validate-on-match", "wrap-xa-resource");
        Map<String, List<ResourceAttribute>> result = strategy.group(attributes);

        // should produce multiple groups
        assertTrue(result.size() > 1, "Should produce multiple groups for 62 attributes");
        assertTrue(result.size() <= 10, "Should not produce too many groups");

        // all attributes accounted for
        int total = result.values().stream().mapToInt(List::size).sum();
        assertEquals(attributes.size(), total);
    }

    private List<ResourceAttribute> generateAttributes(String... names) {
        List<ResourceAttribute> attributes = new ArrayList<>();
        for (String name : names) {
            attributes.add(attribute(name));
        }
        return attributes;
    }
}

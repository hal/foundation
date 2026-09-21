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
package org.jboss.hal.ui.resource.pipeline;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.ui.resource.pipeline.AttributeHandler.MatchResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.jboss.hal.ui.resource.pipeline.AttributeFixtures.credentialReference;
import static org.jboss.hal.ui.resource.pipeline.AttributeFixtures.fileAttribute;
import static org.jboss.hal.ui.resource.pipeline.AttributeFixtures.listOfSimpleRecords;
import static org.jboss.hal.ui.resource.pipeline.AttributeFixtures.listOfSimpleType;
import static org.jboss.hal.ui.resource.pipeline.AttributeFixtures.listWithNestedComplexList;
import static org.jboss.hal.ui.resource.pipeline.AttributeFixtures.listWithNestedObject;
import static org.jboss.hal.ui.resource.pipeline.AttributeFixtures.listWithNestedSimpleList;
import static org.jboss.hal.ui.resource.pipeline.AttributeFixtures.objectWithKeys;
import static org.jboss.hal.ui.resource.pipeline.AttributeFixtures.objectWithSimpleValueType;
import static org.jboss.hal.ui.resource.pipeline.AttributeFixtures.simpleAttribute;
import static org.jboss.hal.ui.resource.pipeline.AttributeFixtures.simpleRecordObject;
import static org.jboss.hal.ui.resource.pipeline.AttributeFixtures.timeUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandlerMatchTest {

    // ------------------------------------------------------ credential reference handler

    @Nested
    class CredentialReferenceHandlerTest {

        private final CredentialReferenceHandler handler = new CredentialReferenceHandler();

        @Test
        void claimsCredentialReference() {
            List<AttributeDescription> pool = List.of(
                    credentialReference("credential-reference"),
                    simpleAttribute("enabled", ModelType.BOOLEAN));

            MatchResult result = handler.match(pool);
            assertEquals(1, result.matches().size());
            assertEquals("credential-reference", result.matches().get(0).primary().name());
            assertEquals(1, result.remaining().size());
        }

        @Test
        void ignoresNonCredentialReference() {
            List<AttributeDescription> pool = List.of(
                    objectWithKeys("settings", "host", "port"),
                    simpleAttribute("enabled", ModelType.BOOLEAN));

            MatchResult result = handler.match(pool);
            assertEquals(0, result.matches().size());
            assertEquals(2, result.remaining().size());
        }

        @Test
        void ignoresObjectWithSimpleValueType() {
            List<AttributeDescription> pool = List.of(
                    objectWithSimpleValueType("properties", ModelType.STRING));

            MatchResult result = handler.match(pool);
            assertEquals(0, result.matches().size());
            assertEquals(1, result.remaining().size());
        }
    }

    // ------------------------------------------------------ time unit handler

    @Nested
    class TimeUnitHandlerTest {

        private final TimeUnitHandler handler = new TimeUnitHandler();

        @Test
        void claimsTimeUnit() {
            List<AttributeDescription> pool = List.of(
                    timeUnit("timeout"),
                    simpleAttribute("enabled", ModelType.BOOLEAN));

            MatchResult result = handler.match(pool);
            assertEquals(1, result.matches().size());
            assertEquals("timeout", result.matches().get(0).primary().name());
            assertEquals(1, result.remaining().size());
        }

        @Test
        void ignoresNonTimeUnit() {
            List<AttributeDescription> pool = List.of(
                    objectWithKeys("settings", "host", "port"));

            MatchResult result = handler.match(pool);
            assertEquals(0, result.matches().size());
            assertEquals(1, result.remaining().size());
        }
    }

    // ------------------------------------------------------ file handler

    @Nested
    class FileHandlerTest {

        private final FileHandler handler = new FileHandler();

        @Test
        void claimsFileAttribute() {
            List<AttributeDescription> pool = List.of(
                    fileAttribute("file"),
                    simpleAttribute("enabled", ModelType.BOOLEAN));

            MatchResult result = handler.match(pool);
            assertEquals(1, result.matches().size());
            assertEquals("file", result.matches().get(0).primary().name());
            assertEquals(1, result.remaining().size());
        }

        @Test
        void ignoresNonFile() {
            List<AttributeDescription> pool = List.of(
                    objectWithKeys("settings", "host", "port"));

            MatchResult result = handler.match(pool);
            assertEquals(0, result.matches().size());
            assertEquals(1, result.remaining().size());
        }
    }

    // ------------------------------------------------------ path relative-to handler

    @Nested
    class PathRelativeToHandlerTest {

        private final PathRelativeToHandler handler = new PathRelativeToHandler();

        @Test
        void claimsSiblingPair() {
            List<AttributeDescription> pool = List.of(
                    simpleAttribute("path", ModelType.STRING),
                    simpleAttribute("relative-to", ModelType.STRING),
                    simpleAttribute("enabled", ModelType.BOOLEAN));

            MatchResult result = handler.match(pool);
            assertEquals(1, result.matches().size());
            assertEquals(2, result.matches().get(0).size());
            assertEquals("path", result.matches().get(0).primary().name());
            assertEquals(1, result.remaining().size());
        }

        @Test
        void claimsPrefixedPair() {
            List<AttributeDescription> pool = List.of(
                    simpleAttribute("base-path", ModelType.STRING),
                    simpleAttribute("base-relative-to", ModelType.STRING));

            MatchResult result = handler.match(pool);
            assertEquals(1, result.matches().size());
            assertEquals(2, result.matches().get(0).size());
        }

        @Test
        void ignoresOrphanRelativeTo() {
            List<AttributeDescription> pool = List.of(
                    simpleAttribute("relative-to", ModelType.STRING),
                    simpleAttribute("enabled", ModelType.BOOLEAN));

            MatchResult result = handler.match(pool);
            assertEquals(0, result.matches().size());
            assertEquals(2, result.remaining().size());
        }

        @Test
        void claimsDirectoryFallback() {
            List<AttributeDescription> pool = List.of(
                    simpleAttribute("directory", ModelType.STRING),
                    simpleAttribute("relative-to", ModelType.STRING));

            MatchResult result = handler.match(pool);
            assertEquals(1, result.matches().size());
            assertEquals("directory", result.matches().get(0).primary().name());
        }
    }

    // ------------------------------------------------------ map handler

    @Nested
    class MapHandlerTest {

        private final MapHandler handler = new MapHandler();

        @Test
        void claimsMapAttribute() {
            List<AttributeDescription> pool = List.of(
                    objectWithSimpleValueType("properties", ModelType.STRING),
                    simpleAttribute("enabled", ModelType.BOOLEAN));

            MatchResult result = handler.match(pool);
            assertEquals(1, result.matches().size());
            assertEquals("properties", result.matches().get(0).primary().name());
            assertEquals(1, result.remaining().size());
        }

        @Test
        void ignoresObjectWithStructuredValueType() {
            List<AttributeDescription> pool = List.of(
                    objectWithKeys("settings", "host", "port"));

            MatchResult result = handler.match(pool);
            assertEquals(0, result.matches().size());
            assertEquals(1, result.remaining().size());
        }

        @Test
        void ignoresSimpleAttribute() {
            List<AttributeDescription> pool = List.of(
                    simpleAttribute("name", ModelType.STRING));

            MatchResult result = handler.match(pool);
            assertEquals(0, result.matches().size());
            assertEquals(1, result.remaining().size());
        }
    }

    // ------------------------------------------------------ flattening handler

    @Nested
    class FlatteningHandlerTest {

        private final FlatteningHandler handler = new FlatteningHandler();

        @Test
        void claimsSimpleRecord() {
            List<AttributeDescription> pool = List.of(
                    simpleRecordObject("settings", "host", "port", "protocol"),
                    simpleAttribute("enabled", ModelType.BOOLEAN));

            MatchResult result = handler.match(pool);
            assertEquals(1, result.matches().size());
            assertEquals("settings", result.matches().get(0).primary().name());
            assertEquals(1, result.remaining().size());
        }

        @Test
        void ignoresMapAttribute() {
            List<AttributeDescription> pool = List.of(
                    objectWithSimpleValueType("properties", ModelType.STRING));

            MatchResult result = handler.match(pool);
            assertEquals(0, result.matches().size());
            assertEquals(1, result.remaining().size());
        }

        @Test
        void ignoresListAttribute() {
            List<AttributeDescription> pool = List.of(
                    listOfSimpleType("handlers"));

            MatchResult result = handler.match(pool);
            assertEquals(0, result.matches().size());
            assertEquals(1, result.remaining().size());
        }
    }

    // ------------------------------------------------------ handler priority (full chain)

    @Nested
    class HandlerPriorityTest {

        @Test
        void credentialReferenceBeforeFlattening() {
            // credential-reference is also a simple record, but CredentialReferenceHandler
            // should claim it first
            CredentialReferenceHandler credHandler = new CredentialReferenceHandler();
            FlatteningHandler flatHandler = new FlatteningHandler();

            List<AttributeDescription> pool = List.of(
                    credentialReference("credential-reference"),
                    simpleRecordObject("settings", "foo", "bar"));

            MatchResult credResult = credHandler.match(pool);
            assertEquals(1, credResult.matches().size());
            assertEquals("credential-reference", credResult.matches().get(0).primary().name());

            MatchResult flatResult = flatHandler.match(credResult.remaining());
            assertEquals(1, flatResult.matches().size());
            assertEquals("settings", flatResult.matches().get(0).primary().name());
            assertTrue(flatResult.remaining().isEmpty());
        }

        @Test
        void mapBeforeFlattening() {
            MapHandler mapHandler = new MapHandler();
            FlatteningHandler flatHandler = new FlatteningHandler();

            List<AttributeDescription> pool = List.of(
                    objectWithSimpleValueType("properties", ModelType.STRING),
                    simpleRecordObject("settings", "foo", "bar"));

            MatchResult mapResult = mapHandler.match(pool);
            assertEquals(1, mapResult.matches().size());
            assertEquals("properties", mapResult.matches().get(0).primary().name());

            MatchResult flatResult = flatHandler.match(mapResult.remaining());
            assertEquals(1, flatResult.matches().size());
            assertEquals("settings", flatResult.matches().get(0).primary().name());
        }

        @Test
        void fullHandlerChain() {
            List<AttributeHandler> handlers = List.of(
                    new CredentialReferenceHandler(),
                    new TimeUnitHandler(),
                    new FileHandler(),
                    new PathRelativeToHandler(),
                    new MapHandler(),
                    new ListSimpleRecordHandler(),
                    new FlatteningHandler());

            List<AttributeDescription> pool = List.of(
                    credentialReference("cred"),
                    timeUnit("timeout"),
                    fileAttribute("file"),
                    simpleAttribute("path", ModelType.STRING),
                    simpleAttribute("relative-to", ModelType.STRING),
                    objectWithSimpleValueType("props", ModelType.STRING),
                    listOfSimpleRecords("permissions", "class-name", "module"),
                    simpleRecordObject("config", "a", "b"),
                    simpleAttribute("enabled", ModelType.BOOLEAN),
                    listOfSimpleType("handlers"));

            List<AttributeDescription> remaining = pool;
            Set<String> allClaimed = new java.util.HashSet<>();

            for (AttributeHandler handler : handlers) {
                MatchResult result = handler.match(remaining);
                for (AttributeMatch match : result.matches()) {
                    for (AttributeDescription desc : match.descriptions()) {
                        allClaimed.add(desc.name());
                    }
                }
                remaining = result.remaining();
            }

            assertEquals(Set.of("cred", "timeout", "file", "path", "relative-to", "props", "permissions", "config"),
                    allClaimed);

            Set<String> remainingNames = remaining.stream()
                    .map(AttributeDescription::name)
                    .collect(Collectors.toSet());
            assertEquals(Set.of("enabled", "handlers"), remainingNames);
        }
    }

    // ------------------------------------------------------ attribute match

    @Nested
    class AttributeMatchTest {

        @Test
        void singleMatch() {
            AttributeDescription ad = simpleAttribute("enabled", ModelType.BOOLEAN);
            AttributeMatch match = AttributeMatch.single(ad);
            assertTrue(match.isSingle());
            assertEquals(1, match.size());
            assertEquals("enabled", match.primary().name());
        }

        @Test
        void multipleMatch() {
            List<AttributeDescription> descriptions = List.of(
                    simpleAttribute("path", ModelType.STRING),
                    simpleAttribute("relative-to", ModelType.STRING));
            AttributeMatch match = AttributeMatch.multiple("path", descriptions);
            assertEquals(2, match.size());
            assertEquals("path", match.primary().name());
        }
    }

    // ------------------------------------------------------ static helpers

    @Nested
    class StaticHelpersTest {

        @Test
        void hasObjectValueTypeWithAllKeys() {
            AttributeDescription ad = objectWithKeys("cred", "store", "alias", "clear-text");
            assertTrue(AttributeHandler.hasObjectValueType(ad, "store", "alias", "clear-text"));
        }

        @Test
        void hasObjectValueTypeWithMissingKey() {
            AttributeDescription ad = objectWithKeys("cred", "store", "alias");
            assertTrue(!AttributeHandler.hasObjectValueType(ad, "store", "alias", "clear-text"));
        }

        @Test
        void hasObjectValueTypeOnSimpleType() {
            AttributeDescription ad = simpleAttribute("enabled", ModelType.BOOLEAN);
            assertTrue(!AttributeHandler.hasObjectValueType(ad, "store"));
        }

        @Test
        void hasSimpleValueTypeOnMap() {
            AttributeDescription ad = objectWithSimpleValueType("properties", ModelType.STRING);
            assertTrue(AttributeHandler.hasSimpleValueType(ad));
        }

        @Test
        void hasSimpleValueTypeOnStructuredObject() {
            AttributeDescription ad = objectWithKeys("settings", "host", "port");
            assertTrue(!AttributeHandler.hasSimpleValueType(ad));
        }

        @Test
        void partitionSplitsCorrectly() {
            List<AttributeDescription> pool = List.of(
                    simpleAttribute("a", ModelType.STRING),
                    simpleAttribute("b", ModelType.BOOLEAN),
                    simpleAttribute("c", ModelType.STRING));

            MatchResult result = AttributeHandler.partition(pool,
                    ad -> ad.name().equals("a") || ad.name().equals("c"));
            assertEquals(2, result.matches().size());
            assertEquals(1, result.remaining().size());
            assertEquals("b", result.remaining().get(0).name());
        }
    }

    // ------------------------------------------------------ list simple record handler

    @Nested
    class ListSimpleRecordHandlerTest {

        private final ListSimpleRecordHandler handler = new ListSimpleRecordHandler();

        @Test
        void claimsListOfSimpleRecords() {
            List<AttributeDescription> pool = List.of(
                    listOfSimpleRecords("permissions", "class-name", "module", "target-name", "action"),
                    simpleAttribute("enabled", ModelType.BOOLEAN));

            MatchResult result = handler.match(pool);
            assertEquals(1, result.matches().size());
            assertEquals("permissions", result.matches().get(0).primary().name());
            assertEquals(1, result.remaining().size());
        }

        @Test
        void ignoresListOfSimpleType() {
            List<AttributeDescription> pool = List.of(
                    listOfSimpleType("handlers"));

            MatchResult result = handler.match(pool);
            assertEquals(0, result.matches().size());
            assertEquals(1, result.remaining().size());
        }

        @Test
        void ignoresObjectAttribute() {
            List<AttributeDescription> pool = List.of(
                    simpleRecordObject("settings", "host", "port"));

            MatchResult result = handler.match(pool);
            assertEquals(0, result.matches().size());
            assertEquals(1, result.remaining().size());
        }

        @Test
        void claimsListWithNestedSimpleList() {
            List<AttributeDescription> pool = List.of(
                    listWithNestedSimpleList("role-map", "from", "to"));

            MatchResult result = handler.match(pool);
            assertEquals(1, result.matches().size());
            assertEquals("role-map", result.matches().get(0).primary().name());
        }

        @Test
        void ignoresListWithNestedComplexList() {
            List<AttributeDescription> pool = List.of(
                    listWithNestedComplexList("mechanism-configurations", "mechanism-name",
                            "realm-configs", "realm-name", "realm-mapper"));

            MatchResult result = handler.match(pool);
            assertEquals(0, result.matches().size());
            assertEquals(1, result.remaining().size());
        }

        @Test
        void ignoresListWithNestedObject() {
            List<AttributeDescription> pool = List.of(
                    listWithNestedObject("auth-modules", "class-name", "options", "key1", "key2"));

            MatchResult result = handler.match(pool);
            assertEquals(0, result.matches().size());
            assertEquals(1, result.remaining().size());
        }
    }

    // ------------------------------------------------------ unclaimed types (what falls through)

    @Nested
    class UnclaimedTest {

        @Test
        void listOfSimpleRecordsIsClaimed() {
            List<AttributeHandler> handlers = List.of(
                    new CredentialReferenceHandler(),
                    new TimeUnitHandler(),
                    new FileHandler(),
                    new PathRelativeToHandler(),
                    new MapHandler(),
                    new ListSimpleRecordHandler(),
                    new FlatteningHandler());

            List<AttributeDescription> pool = List.of(
                    listOfSimpleRecords("permissions", "class-name", "module", "target-name", "action"));

            List<AttributeDescription> remaining = pool;
            boolean claimed = false;
            for (AttributeHandler handler : handlers) {
                MatchResult result = handler.match(remaining);
                if (!result.matches().isEmpty()) {
                    assertEquals(ListSimpleRecordHandler.class, handler.getClass(),
                            "ListSimpleRecordHandler should be the one claiming LIST of simple records");
                    claimed = true;
                }
                remaining = result.remaining();
            }
            assertTrue(claimed, "LIST of simple records should be claimed by ListSimpleRecordHandler");
            assertTrue(remaining.isEmpty());
        }

        @Test
        void listWithNestedComplexListIsUnclaimed() {
            List<AttributeHandler> handlers = List.of(
                    new CredentialReferenceHandler(),
                    new TimeUnitHandler(),
                    new FileHandler(),
                    new PathRelativeToHandler(),
                    new MapHandler(),
                    new ListSimpleRecordHandler(),
                    new FlatteningHandler());

            List<AttributeDescription> pool = List.of(
                    listWithNestedComplexList("mechanism-configurations", "mechanism-name",
                            "mechanism-realm-configurations", "realm-name", "realm-mapper"));

            List<AttributeDescription> remaining = pool;
            for (AttributeHandler handler : handlers) {
                MatchResult result = handler.match(remaining);
                assertEquals(0, result.matches().size(),
                        handler.getClass().getSimpleName() + " should not claim LIST with nested complex LIST");
                remaining = result.remaining();
            }
            assertEquals(1, remaining.size());
        }
    }
}

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

import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.ui.resource.pipeline.AttributeMatcher.MatchResult;
import org.junit.jupiter.api.Test;

import static org.jboss.hal.ui.resource.pipeline.TestData.pool;
import static org.jboss.hal.ui.resource.pipeline.TestData.stringAttribute;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathRelativeToMatcherTest {

    private final PathRelativeToMatcher matcher = new PathRelativeToMatcher();

    @Test
    void claimsPathAndRelativeTo() {
        List<AttributeDescription> pool = pool(
                stringAttribute("path"),
                stringAttribute("relative-to"),
                stringAttribute("enabled"));
        MatchResult result = matcher.match(pool);

        assertEquals(1, result.groups().size());
        AttributeGroup group = result.groups().get(0);
        assertEquals(2, group.size());
        assertEquals("path", group.primary().name());
        assertEquals("relative-to", group.attributes().get(1).name());
        assertEquals(1, result.remaining().size());
        assertEquals("enabled", result.remaining().get(0).name());
    }

    @Test
    void claimsPrefixedVariant() {
        List<AttributeDescription> pool = pool(
                stringAttribute("keystore-path"),
                stringAttribute("keystore-relative-to"),
                stringAttribute("keystore-password"));
        MatchResult result = matcher.match(pool);

        assertEquals(1, result.groups().size());
        AttributeGroup group = result.groups().get(0);
        assertEquals("keystore-path", group.primary().name());
        assertEquals("keystore-relative-to", group.attributes().get(1).name());
        assertEquals(1, result.remaining().size());
    }

    @Test
    void claimsObjectStorePrefixedVariant() {
        List<AttributeDescription> pool = pool(
                stringAttribute("object-store-path"),
                stringAttribute("object-store-relative-to"),
                stringAttribute("node-identifier"));
        MatchResult result = matcher.match(pool);

        assertEquals(1, result.groups().size());
        assertEquals("object-store-path", result.groups().get(0).primary().name());
        assertEquals(1, result.remaining().size());
    }

    @Test
    void claimsDirectoryFallback() {
        List<AttributeDescription> pool = pool(
                stringAttribute("directory"),
                stringAttribute("relative-to"),
                stringAttribute("pattern"));
        MatchResult result = matcher.match(pool);

        assertEquals(1, result.groups().size());
        AttributeGroup group = result.groups().get(0);
        assertEquals("directory", group.primary().name());
        assertEquals("relative-to", group.attributes().get(1).name());
        assertEquals(1, result.remaining().size());
    }

    @Test
    void standaloneRelativeToNotClaimed() {
        List<AttributeDescription> pool = pool(
                stringAttribute("relative-to"),
                stringAttribute("max-size"));
        MatchResult result = matcher.match(pool);

        assertTrue(result.groups().isEmpty());
        assertEquals(2, result.remaining().size());
    }

    @Test
    void multipleGroupsInOneResource() {
        List<AttributeDescription> pool = pool(
                stringAttribute("path"),
                stringAttribute("relative-to"),
                stringAttribute("keystore-path"),
                stringAttribute("keystore-relative-to"),
                stringAttribute("enabled"));
        MatchResult result = matcher.match(pool);

        assertEquals(2, result.groups().size());
        assertEquals(1, result.remaining().size());
        assertEquals("enabled", result.remaining().get(0).name());
    }

    @Test
    void emptyPool() {
        MatchResult result = matcher.match(pool());

        assertTrue(result.groups().isEmpty());
        assertTrue(result.remaining().isEmpty());
    }
}

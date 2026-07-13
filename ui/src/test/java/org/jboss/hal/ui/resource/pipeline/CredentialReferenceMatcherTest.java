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

import static org.jboss.hal.ui.resource.pipeline.TestData.credentialReference;
import static org.jboss.hal.ui.resource.pipeline.TestData.pool;
import static org.jboss.hal.ui.resource.pipeline.TestData.stringAttribute;
import static org.jboss.hal.ui.resource.pipeline.TestData.timeUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CredentialReferenceMatcherTest {

    private final CredentialReferenceMatcher matcher = new CredentialReferenceMatcher();

    @Test
    void claimsCredentialReference() {
        List<AttributeDescription> pool = pool(
                stringAttribute("name"),
                credentialReference("credential-reference"),
                stringAttribute("enabled"));
        MatchResult result = matcher.match(pool);

        assertEquals(1, result.groups().size());
        assertEquals("credential-reference", result.groups().get(0).primary().name());
        assertEquals(2, result.remaining().size());
    }

    @Test
    void claimsMultipleVariants() {
        List<AttributeDescription> pool = pool(
                credentialReference("credential-reference"),
                credentialReference("recovery-credential-reference"),
                stringAttribute("jndi-name"));
        MatchResult result = matcher.match(pool);

        assertEquals(2, result.groups().size());
        assertEquals(1, result.remaining().size());
        assertEquals("jndi-name", result.remaining().get(0).name());
    }

    @Test
    void ignoresNonMatching() {
        List<AttributeDescription> pool = pool(
                stringAttribute("name"),
                timeUnit("keepalive-time"),
                stringAttribute("enabled"));
        MatchResult result = matcher.match(pool);

        assertTrue(result.groups().isEmpty());
        assertEquals(3, result.remaining().size());
    }

    @Test
    void emptyPool() {
        MatchResult result = matcher.match(pool());

        assertTrue(result.groups().isEmpty());
        assertTrue(result.remaining().isEmpty());
    }
}

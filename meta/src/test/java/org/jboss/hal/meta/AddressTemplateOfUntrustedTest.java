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
package org.jboss.hal.meta;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpellCheckingInspection")
class AddressTemplateOfUntrustedTest {

    // ------------------------------------------------------ valid: root / empty

    @Test
    void nullInput() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted(null);
        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }

    @Test
    void emptyString() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("");
        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }

    @Test
    void blankString() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("   ");
        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }

    @Test
    void slashOnly() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("/");
        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }

    // ------------------------------------------------------ valid: simple templates

    @Test
    void singleSegment() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("subsystem=io");
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("/subsystem=io", result.get().template);
    }

    @Test
    void singleSegmentWithLeadingSlash() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("/subsystem=io");
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("/subsystem=io", result.get().template);
    }

    @Test
    void twoSegments() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("subsystem=logging/logger=com.example");
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        assertEquals("/subsystem=logging/logger=com.example", result.get().template);
    }

    @Test
    void threeSegments() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted(
                "subsystem=datasources/data-source=ExampleDS/connection-properties=foo");
        assertTrue(result.isPresent());
        assertEquals(3, result.get().size());
    }

    // ------------------------------------------------------ valid: wildcards

    @Test
    void wildcardValue() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("subsystem=datasources/data-source=*");
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        assertEquals("*", result.get().last().value);
    }

    // ------------------------------------------------------ valid: placeholders

    @Test
    void placeholderSegment() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("{selected.profile}/subsystem=io");
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
    }

    @Test
    void placeholderValue() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("subsystem=logging/logger={selection}");
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        assertEquals("{selection}", result.get().last().value);
    }

    // ------------------------------------------------------ valid: backslash-encoded values

    @Test
    void encodedSlashInValue() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("subsystem=logging/logger=com\\/example");
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        assertEquals("com/example", result.get().last().value);
    }

    @Test
    void encodedColonInValue() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("subsystem=logging/logger=com\\:example");
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        assertEquals("com:example", result.get().last().value);
    }

    @Test
    void encodedEqualsInValue() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("subsystem=logging/logger=com\\=example");
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        assertEquals("com=example", result.get().last().value);
    }

    @Test
    void multipleEncodedChars() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("a=b/c=\\:\\=\\/");
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        assertEquals(":=/", result.get().last().value);
    }

    // ------------------------------------------------------ invalid: partial / malformed

    @Test
    void invalidSegmentInMiddle() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("a=b/c/d=e");
        assertTrue(result.isEmpty());
    }

    @Test
    void invalidSegmentAtEnd() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("a=b/c");
        assertTrue(result.isEmpty());
    }

    @Test
    void noEqualsNoPlaceholder() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("garbage");
        assertTrue(result.isEmpty());
    }

    @Test
    void startsWithEquals() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("=value");
        assertTrue(result.isEmpty());
    }

    @Test
    void endsWithEquals() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("key=");
        assertTrue(result.isEmpty());
    }

    @Test
    void emptyBraces() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("{}");
        assertTrue(result.isEmpty());
    }

    @Test
    void emptyBracesEquals() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("{}=a");
        assertTrue(result.isEmpty());
    }

    @Test
    void equalsEmptyBraces() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("a={}");
        assertTrue(result.isEmpty());
    }

    @Test
    void unmatchedOpenBrace() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("a={");
        assertTrue(result.isEmpty());
    }

    @Test
    void unmatchedCloseBrace() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("a=}");
        assertTrue(result.isEmpty());
    }

    @Test
    void multipleSegmentsFirstInvalid() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("garbage/a=b");
        assertTrue(result.isEmpty());
    }

    @Test
    void validThenInvalidThenValid() {
        Optional<AddressTemplate> result = AddressTemplate.ofUntrusted("a=b/invalid/c=d");
        assertTrue(result.isEmpty());
    }
}

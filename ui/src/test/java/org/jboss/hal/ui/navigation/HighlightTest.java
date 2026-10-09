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
package org.jboss.hal.ui.navigation;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HighlightTest {

    @ParameterizedTest(name = "\"{0}\" → attribute")
    @CsvSource({
            "a:max-pool-size, max-pool-size",
            "a:enabled, enabled",
            "a:default-host, default-host",
            "a:connection-pool-size, connection-pool-size",
    })
    void parseAttribute(String input, String expectedName) {
        Optional<Highlight> result = Highlight.parse(input);
        assertTrue(result.isPresent());
        assertEquals(Highlight.Type.ATTRIBUTE, result.get().type());
        assertEquals(expectedName, result.get().name());
    }

    @ParameterizedTest(name = "\"{0}\" → operation")
    @CsvSource({
            "o:suspend, suspend",
            "o:read-resource, read-resource",
            "o:list-log-files, list-log-files",
    })
    void parseOperation(String input, String expectedName) {
        Optional<Highlight> result = Highlight.parse(input);
        assertTrue(result.isPresent());
        assertEquals(Highlight.Type.OPERATION, result.get().type());
        assertEquals(expectedName, result.get().name());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "x:name",       // unknown type prefix
            "a",            // too short, no colon
            "a:",           // no name after colon
            ":name",        // no type before colon
            "attribute",    // no colon at all
    })
    void parseInvalid(String input) {
        Optional<Highlight> result = Highlight.parse(input);
        assertTrue(result.isEmpty());
    }

    @Test
    void toStringAttribute() {
        assertEquals("a:max-pool-size", new Highlight(Highlight.Type.ATTRIBUTE, "max-pool-size").toString());
    }

    @Test
    void toStringOperation() {
        assertEquals("o:suspend", new Highlight(Highlight.Type.OPERATION, "suspend").toString());
    }

    @Test
    void roundTrip() {
        String original = "a:default-host";
        Optional<Highlight> parsed = Highlight.parse(original);
        assertTrue(parsed.isPresent());
        assertEquals(original, parsed.get().toString());
    }
}

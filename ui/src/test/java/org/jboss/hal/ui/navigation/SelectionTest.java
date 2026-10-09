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

import org.jboss.hal.meta.AddressTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectionTest {

    // ------------------------------------------------------ parse: address only

    @Test
    void parseAddressOnly() {
        Selection selection = Selection.parse("subsystem=datasources");
        assertTrue(selection.address().isPresent());
        assertEquals("/subsystem=datasources", selection.address().get().template);
        assertTrue(selection.highlight().isEmpty());
    }

    @Test
    void parseNestedAddress() {
        Selection selection = Selection.parse("subsystem=datasources/data-source=ExampleDS");
        assertTrue(selection.address().isPresent());
        assertEquals("/subsystem=datasources/data-source=ExampleDS", selection.address().get().template);
        assertTrue(selection.highlight().isEmpty());
    }

    // ------------------------------------------------------ parse: address + highlight

    @Test
    void parseAddressWithAttributeHighlight() {
        Selection selection = Selection.parse("subsystem=datasources@a:max-pool-size");
        assertTrue(selection.address().isPresent());
        assertEquals("/subsystem=datasources", selection.address().get().template);
        assertTrue(selection.highlight().isPresent());
        assertEquals(Highlight.Type.ATTRIBUTE, selection.highlight().get().type());
        assertEquals("max-pool-size", selection.highlight().get().name());
    }

    @Test
    void parseAddressWithOperationHighlight() {
        Selection selection = Selection.parse("subsystem=datasources@o:suspend");
        assertTrue(selection.address().isPresent());
        assertEquals("/subsystem=datasources", selection.address().get().template);
        assertTrue(selection.highlight().isPresent());
        assertEquals(Highlight.Type.OPERATION, selection.highlight().get().type());
        assertEquals("suspend", selection.highlight().get().name());
    }

    // ------------------------------------------------------ parse: highlight only

    @Test
    void parseHighlightOnly() {
        Selection selection = Selection.parse("@a:max-pool-size");
        assertTrue(selection.address().isEmpty());
        assertTrue(selection.highlight().isPresent());
        assertEquals(Highlight.Type.ATTRIBUTE, selection.highlight().get().type());
        assertEquals("max-pool-size", selection.highlight().get().name());
    }

    // ------------------------------------------------------ parse: invalid highlight

    @Test
    void parseAddressWithInvalidHighlight() {
        Selection selection = Selection.parse("subsystem=datasources@invalid");
        assertTrue(selection.address().isPresent());
        assertEquals("/subsystem=datasources", selection.address().get().template);
        assertTrue(selection.highlight().isEmpty());
    }

    // ------------------------------------------------------ parse: null and empty

    @ParameterizedTest
    @NullAndEmptySource
    void parseNullAndEmpty(String input) {
        Selection selection = Selection.parse(input);
        assertTrue(selection.address().isEmpty());
        assertTrue(selection.highlight().isEmpty());
    }

    // ------------------------------------------------------ encode

    @Test
    void encodeAddressOnly() {
        String encoded = Selection.encode(AddressTemplate.ofTrusted("subsystem=datasources"), null);
        assertEquals("/subsystem=datasources", encoded);
    }

    @Test
    void encodeAddressWithHighlight() {
        String encoded = Selection.encode(AddressTemplate.ofTrusted("subsystem=datasources"), "a:max-pool-size");
        assertEquals("/subsystem=datasources@a:max-pool-size", encoded);
    }

    @Test
    void encodeHighlightOnly() {
        String encoded = Selection.encode(null, "a:max-pool-size");
        assertEquals("@a:max-pool-size", encoded);
    }

    @Test
    void encodeNulls() {
        assertNull(Selection.encode(null, null));
    }

    // ------------------------------------------------------ round trip

    @Test
    void roundTripAddressOnly() {
        String original = "subsystem=datasources/data-source=ExampleDS";
        Selection parsed = Selection.parse(original);
        String encoded = Selection.encode(parsed.address().orElse(null),
                parsed.highlight().map(Highlight::toString).orElse(null));
        Selection reparsed = Selection.parse(encoded);
        assertEquals(parsed.address().get().template, reparsed.address().get().template);
        assertEquals(parsed.highlight(), reparsed.highlight());
    }

    @Test
    void roundTripAddressWithHighlight() {
        String original = "subsystem=datasources@a:max-pool-size";
        Selection parsed = Selection.parse(original);
        String encoded = Selection.encode(parsed.address().orElse(null),
                parsed.highlight().map(Highlight::toString).orElse(null));
        Selection reparsed = Selection.parse(encoded);
        assertEquals(parsed.address().get().template, reparsed.address().get().template);
        assertEquals(parsed.highlight(), reparsed.highlight());
    }

    @Test
    void roundTripHighlightOnly() {
        String original = "@o:suspend";
        Selection parsed = Selection.parse(original);
        String encoded = Selection.encode(parsed.address().orElse(null),
                parsed.highlight().map(Highlight::toString).orElse(null));
        Selection reparsed = Selection.parse(encoded);
        assertEquals(parsed.address(), reparsed.address());
        assertEquals(parsed.highlight(), reparsed.highlight());
    }
}

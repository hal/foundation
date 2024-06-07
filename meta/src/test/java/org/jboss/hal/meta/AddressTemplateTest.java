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

import java.util.List;

import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("DataFlowIssue")
class AddressTemplateTest {

    @Test
    void root() {
        assertTrue(AddressTemplate.root().template.isEmpty());
    }

    @Test
    void empty() {
        assertTrue(AddressTemplate.of("").template.isEmpty());
        assertTrue(AddressTemplate.of("   ").template.isEmpty());
        assertTrue(AddressTemplate.of("/").template.isEmpty());
        assertTrue(AddressTemplate.of(" /").template.isEmpty());
        assertTrue(AddressTemplate.of("/ ").template.isEmpty());
        assertTrue(AddressTemplate.of(" / ").template.isEmpty());
        assertTrue(AddressTemplate.of(emptyList()).template.isEmpty());
    }

    @Test
    void nil() {
        assertTrue(AddressTemplate.of((String) null).template.isEmpty());
        assertTrue(AddressTemplate.of((Placeholder) null).template.isEmpty());
        assertTrue(AddressTemplate.of((List<Segment>) null).template.isEmpty());
    }

    @Test
    void absolute() {
        assertEquals("a=b/c=d", AddressTemplate.of("/a=b/c=d").template);
    }

    @Test
    void relative() {
        assertEquals("a=b/c=d", AddressTemplate.of("a=b/c=d").template);
    }

    @Test
    void placeholder() {
        assertEquals("{a}/b=c", AddressTemplate.of("{a}/b=c").template);
        assertEquals("a=b/b={c}", AddressTemplate.of("a=b/b={c}").template);
    }

    @Test
    void first() {
        assertNull(AddressTemplate.root().first());
        assertEquals("a", AddressTemplate.of("a=b").first().key);
        assertEquals("b", AddressTemplate.of("a=b").first().value);

        assertEquals("a", AddressTemplate.of("a=b/{c}").first().key);
        assertEquals("b", AddressTemplate.of("a=b/{c}").first().value);

        assertNull(AddressTemplate.of("{a}/b={c}").first().key);
        assertEquals("{a}", AddressTemplate.of("{a}/b={c}").first().value);
    }

    @Test
    void last() {
        assertNull(AddressTemplate.root().last());
        assertEquals("a", AddressTemplate.of("a=b").last().key);
        assertEquals("b", AddressTemplate.of("a=b").last().value);

        assertNull(AddressTemplate.of("a=b/{c}").last().key);
        assertEquals("{c}", AddressTemplate.of("a=b/{c}").last().value);

        assertEquals("b", AddressTemplate.of("{a}/b={c}").last().key);
        assertEquals("{c}", AddressTemplate.of("{a}/b={c}").last().value);
    }

    @Test
    void append() {
        AddressTemplate template = AddressTemplate.of("a=b");
        assertEquals("a=b/c=d", template.append("c=d").template);
        assertEquals("a=b/c=d", template.append("/c=d").template);
        assertEquals("a=b/{c}", template.append("{c}").template);
        assertEquals("a=b/c={d}", template.append("c={d}").template);
    }

    @Test
    void parentOfRoot() {
        assertEquals(AddressTemplate.of("/"), AddressTemplate.of("/").parent());
    }

    @Test
    void parentOfFirstLevel() {
        assertEquals(AddressTemplate.of("/"), AddressTemplate.of("/a=b").parent());
    }

    @Test
    void parent() {
        AddressTemplate template = AddressTemplate.of("{a}/b=c/{d}=e/f=g"); // 4 tokens

        assertEquals(AddressTemplate.of("{a}/b=c/{d}=e"), template.parent());
        assertEquals(AddressTemplate.of("{a}/b=c"), template.parent().parent());
        assertEquals(AddressTemplate.of("{a}"), template.parent().parent().parent());
        assertEquals(AddressTemplate.of("/"), template.parent().parent().parent().parent());
    }

    @Test
    void subTemplate() {
        AddressTemplate template = AddressTemplate.of("{a}/b=c/{d}=e/f=g"); // 4 tokens

        assertEquals("", template.subTemplate(0, 0).template);
        assertEquals("", template.subTemplate(2, 2).template);
        assertEquals("b=c", template.subTemplate(1, 2).template);
        assertEquals("{d}=e/f=g", template.subTemplate(2, 4).template);
        assertEquals(template, template.subTemplate(0, 4));
    }

    @Test
    void encode() {
        AddressTemplate template = AddressTemplate.of("a=b=c");
        assertEquals("a", template.first().key);
        assertEquals("b\\=c", template.first().value);
    }
}
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpellCheckingInspection")
class AddressTemplateTest {

    @Test
    void root() {
        assertTrue(AddressTemplate.root().isEmpty());
        assertEquals("/", AddressTemplate.root().template);
    }

    @Test
    void empty() {
        assertTrue(AddressTemplate.ofTrusted("").isEmpty());
        assertTrue(AddressTemplate.ofTrusted("   ").isEmpty());
        assertTrue(AddressTemplate.ofTrusted("/").isEmpty());
        assertTrue(AddressTemplate.ofTrusted(" /").isEmpty());
        assertTrue(AddressTemplate.ofTrusted("/ ").isEmpty());
        assertTrue(AddressTemplate.ofTrusted(" / ").isEmpty());
        assertTrue(AddressTemplate.of(emptyList()).isEmpty());
    }

    @Test
    void nil() {
        assertTrue(AddressTemplate.ofTrusted((String) null).isEmpty());
        assertTrue(AddressTemplate.of((Placeholder) null).isEmpty());
        assertTrue(AddressTemplate.of((List<Segment>) null).isEmpty());
    }

    @Test
    void malformed() {
        assertEquals("/", AddressTemplate.ofTrusted("a").template);
        assertEquals("/", AddressTemplate.ofTrusted("a=").template);
        assertEquals("/", AddressTemplate.ofTrusted("=a").template);
        assertEquals("/", AddressTemplate.ofTrusted("=a=").template);
        assertEquals("/", AddressTemplate.ofTrusted("{}").template);
        assertEquals("/", AddressTemplate.ofTrusted("={}").template);
        assertEquals("/", AddressTemplate.ofTrusted("{}=").template);
        assertEquals("/", AddressTemplate.ofTrusted("={}=").template);
        assertEquals("/", AddressTemplate.ofTrusted("{a}=").template);
        assertEquals("/", AddressTemplate.ofTrusted("={a}").template);
        assertEquals("/", AddressTemplate.ofTrusted("={a}=").template);
        assertEquals("/", AddressTemplate.ofTrusted("a={").template);
        assertEquals("/", AddressTemplate.ofTrusted("a=}").template);
        assertEquals("/", AddressTemplate.ofTrusted("a={}").template);
        assertEquals("/", AddressTemplate.ofTrusted("{}=a").template);
        assertEquals("/a=b", AddressTemplate.ofTrusted("a=b/c/d=e/f=g").template);
    }

    @Test
    void absolute() {
        assertEquals("/a=b/c=d", AddressTemplate.ofTrusted("/a=b/c=d").template);
    }

    @Test
    void relative() {
        assertEquals("/a=b/c=d", AddressTemplate.ofTrusted("a=b/c=d").template);
    }

    @Test
    void placeholder() {
        assertEquals("/{a}/b=c", AddressTemplate.ofTrusted("{a}/b=c").template);
        assertEquals("/a=b/b={c}", AddressTemplate.ofTrusted("a=b/b={c}").template);
    }

    @Test
    void first() {
        assertSame(Segment.EMPTY, AddressTemplate.root().first());
        assertEquals("a", AddressTemplate.ofTrusted("a=b").first().key);
        assertEquals("b", AddressTemplate.ofTrusted("a=b").first().value);

        assertEquals("a", AddressTemplate.ofTrusted("a=b/{c}").first().key);
        assertEquals("b", AddressTemplate.ofTrusted("a=b/{c}").first().value);

        assertNull(AddressTemplate.ofTrusted("{a}/b={c}").first().key);
        assertEquals("{a}", AddressTemplate.ofTrusted("{a}/b={c}").first().value);
    }

    @Test
    void last() {
        assertSame(Segment.EMPTY, AddressTemplate.root().last());
        assertEquals("a", AddressTemplate.ofTrusted("a=b").last().key);
        assertEquals("b", AddressTemplate.ofTrusted("a=b").last().value);

        assertNull(AddressTemplate.ofTrusted("a=b/{c}").last().key);
        assertEquals("{c}", AddressTemplate.ofTrusted("a=b/{c}").last().value);

        assertEquals("b", AddressTemplate.ofTrusted("{a}/b={c}").last().key);
        assertEquals("{c}", AddressTemplate.ofTrusted("{a}/b={c}").last().value);
    }

    @Test
    void appendTrusted() {
        AddressTemplate template = AddressTemplate.ofTrusted("a=b");
        assertEquals("/a=b/c=d", template.appendTrusted("c=d").template);
        assertEquals("/a=b/c=d", template.appendTrusted("/c=d").template);
        assertEquals("/a=b/{c}", template.appendTrusted("{c}").template);
        assertEquals("/a=b/c={d}", template.appendTrusted("c={d}").template);
        assertEquals("/a=b/c=*", template.appendTrusted("c=*").template);
    }

    @Test
    void appendKeyValue() {
        AddressTemplate template = AddressTemplate.ofTrusted("a=b");
        assertEquals("/a=b/c=d", template.append("c", "d").template);
        assertEquals("/a=b/c=d", template.append("/c", "d").template);
        assertEquals("/a=b/c={d}", template.append("c", "{d}").template);
        assertEquals("/a=b/c=*", template.append("c", "*").template);
    }

    @Test
    void parentOfRoot() {
        assertEquals(AddressTemplate.ofTrusted("/"), AddressTemplate.ofTrusted("/").parent());
    }

    @Test
    void parentOfFirstLevel() {
        assertEquals(AddressTemplate.ofTrusted("/"), AddressTemplate.ofTrusted("/a=b").parent());
    }

    @Test
    void parent() {
        AddressTemplate template = AddressTemplate.ofTrusted("{a}/b=c/{d}=e/f=g"); // 4 tokens

        assertEquals(AddressTemplate.ofTrusted("{a}/b=c/{d}=e"), template.parent());
        assertEquals(AddressTemplate.ofTrusted("{a}/b=c"), template.parent().parent());
        assertEquals(AddressTemplate.ofTrusted("{a}"), template.parent().parent().parent());
        assertEquals(AddressTemplate.ofTrusted("/"), template.parent().parent().parent().parent());
    }

    @Test
    void subTemplate() {
        AddressTemplate template = AddressTemplate.ofTrusted("{a}/b=c/{d}=e/f=g"); // 4 tokens

        assertEquals("/", template.subTemplate(0, 0).template);
        assertEquals("/", template.subTemplate(2, 2).template);
        assertEquals("/b=c", template.subTemplate(1, 2).template);
        assertEquals("/{d}=e/f=g", template.subTemplate(2, 4).template);
        assertEquals(template, template.subTemplate(0, 4));
    }

    @Test
    void encode() {
        AddressTemplate template = AddressTemplate.ofTrusted(
                "a=b/slash=\\//colon=\\:/equals=\\=/c=d/e={f}/multiple=\\:\\=\\//end=\\/");

        assertEquals(8, template.size());
        assertEquals("/a=b/slash=\\//colon=\\:/equals=\\=/c=d/e={f}/multiple=\\:\\=\\//end=\\/", template.template);
        assertEquals("b", template.segments().get(0).value);
        assertEquals("/", template.segments().get(1).value);
        assertEquals(":", template.segments().get(2).value);
        assertEquals("=", template.segments().get(3).value);
        assertEquals("d", template.segments().get(4).value);
        assertEquals("{f}", template.segments().get(5).value);
        assertEquals(":=/", template.segments().get(6).value);
        assertEquals("/", template.segments().get(7).value);
    }

    @Test
    void encodeAppendMixed() {
        AddressTemplate template = AddressTemplate.root()
                .appendTrusted("a=b")
                .append("slash", "/")
                .appendTrusted("colon=\\:")
                .append("equals", "=")
                .append("c", "d")
                .append("e", "{f}")
                .append("multiple", ":=/")
                .appendTrusted("end=\\/");

        assertEquals(8, template.size());
        assertEquals("/a=b/slash=\\//colon=\\:/equals=\\=/c=d/e={f}/multiple=\\:\\=\\//end=\\/", template.template);
        assertEquals("b", template.segments().get(0).value);
        assertEquals("/", template.segments().get(1).value);
        assertEquals(":", template.segments().get(2).value);
        assertEquals("=", template.segments().get(3).value);
        assertEquals("d", template.segments().get(4).value);
        assertEquals("{f}", template.segments().get(5).value);
        assertEquals(":=/", template.segments().get(6).value);
        assertEquals("/", template.segments().get(7).value);
    }

    @Test
    void special() {
        String dataSourceConstraint = "/core-service=management/access=authorization/constraint=application-classification/type=datasources/classification=data-source/applies-to=\\/deployment\\=*\\/subdeployment\\=*\\/subsystem\\=datasources\\/data-source\\=*";
        AddressTemplate template = AddressTemplate.ofTrusted(dataSourceConstraint);
        assertSpecial(template, dataSourceConstraint);

        template = AddressTemplate.root()
                .append("core-service", "management")
                .append("access", "authorization")
                .append("constraint", "application-classification")
                .append("type", "datasources")
                .append("classification", "data-source")
                .append("applies-to", "/deployment=*/subdeployment=*/subsystem=datasources/data-source=*");
        assertSpecial(template, dataSourceConstraint);
    }

    @Test
    void ofKeyValue() {
        assertEquals("/a=b", AddressTemplate.of("a", "b").template);
        assertEquals("b", AddressTemplate.of("a", "b").first().value);
        assertEquals("/system-property=foo", AddressTemplate.of("system-property", "foo").template);
        assertEquals("/a=\\/", AddressTemplate.of("a", "/").template);
        assertEquals("/", AddressTemplate.of("a", "/").first().value);
        assertEquals("/a=\\:", AddressTemplate.of("a", ":").template);
        assertEquals(":", AddressTemplate.of("a", ":").first().value);
        assertEquals("/a=\\=", AddressTemplate.of("a", "=").template);
        assertEquals("=", AddressTemplate.of("a", "=").first().value);
        assertEquals("/a=\\:\\=\\/", AddressTemplate.of("a", ":=/").template);
        assertEquals(":=/", AddressTemplate.of("a", ":=/").first().value);
    }

    @Test
    void appendPlaceholder() {
        AddressTemplate template = AddressTemplate.ofTrusted("a=b");
        assertEquals("/a=b/{selected.resource}", template.append(Placeholder.SELECTED_RESOURCE).template);
    }

    // ------------------------------------------------------ internal

    private static void assertSpecial(AddressTemplate template, String dataSourceConstraint) {
        assertEquals(6, template.size());
        assertEquals(dataSourceConstraint, template.template);
        assertEquals("core-service", template.segments().get(0).key);
        assertEquals("management", template.segments().get(0).value);
        assertEquals("access", template.segments().get(1).key);
        assertEquals("authorization", template.segments().get(1).value);
        assertEquals("constraint", template.segments().get(2).key);
        assertEquals("application-classification", template.segments().get(2).value);
        assertEquals("type", template.segments().get(3).key);
        assertEquals("datasources", template.segments().get(3).value);
        assertEquals("classification", template.segments().get(4).key);
        assertEquals("data-source", template.segments().get(4).value);
        assertEquals("applies-to", template.segments().get(5).key);
        assertEquals("/deployment=*/subdeployment=*/subsystem=datasources/data-source=*", template.segments().get(5).value);
    }
}

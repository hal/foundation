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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SelectionResolverTest {

    @Test
    void nil() {
        assertThrows(IllegalArgumentException.class, () -> new SelectionResolver(null));
    }

    @Test
    void resolve() {
        SelectionResolver resolver = new SelectionResolver(() -> "foo");
        assertEquals("/a=b", resolver.resolve(AddressTemplate.ofTrusted("a=b")).template);
        assertEquals("/a={b}", resolver.resolve(AddressTemplate.ofTrusted("a={b}")).template);
        assertEquals("/{a}=b", resolver.resolve(AddressTemplate.ofTrusted("{a}=b")).template);
        assertEquals("/a=foo", resolver.resolve(AddressTemplate.ofTrusted("a={selected.resource}")).template);
        assertEquals("/{selected.resource}=b", resolver.resolve(AddressTemplate.ofTrusted("{selected.resource}=b")).template);
    }
}
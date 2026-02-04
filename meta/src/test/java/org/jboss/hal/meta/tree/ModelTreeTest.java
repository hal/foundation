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
package org.jboss.hal.meta.tree;

import org.jboss.hal.meta.AddressTemplate;
import org.junit.jupiter.api.Test;

import static org.jboss.hal.meta.tree.ModelTree.nextWildcard;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ModelTreeTest {

    @Test
    void allWildcards() {
        AddressTemplate template = AddressTemplate.of("a=*/b=*/c=*");
        assertEquals(AddressTemplate.of("a=a0/b=*"), nextWildcard(template, AddressTemplate.of("a=a0")));
        assertEquals(AddressTemplate.of("a=a0/b=b0/c=*"), nextWildcard(template, AddressTemplate.of("a=a0/b=b0")));
        assertNull(nextWildcard(template, AddressTemplate.of("a=a0/b=b0/c=c0")));
    }

    @Test
    void withFixedPart() {
        AddressTemplate template = AddressTemplate.of("a=*/b=fix/c=*");
        assertEquals(AddressTemplate.of("a=a0/b=fix/c=*"), nextWildcard(template, AddressTemplate.of("a=a0")));
        assertNull(nextWildcard(template, AddressTemplate.of("a=a0/b=fix/c=c0")));
    }
}
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
package org.jboss.hal.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LabelBuilderAllWordsTest {

    private LabelBuilder labelBuilder;

    @BeforeEach
    void beforeAll() {
        labelBuilder = new LabelBuilder(true);
    }

    @Test
    void capitalize() {
        assertEquals("Background Validation", labelBuilder.label("background-validation"));
        assertEquals("Enabled", labelBuilder.label("enabled"));
    }

    @Test
    void specials() {
        assertEquals("Check Valid Connection SQL", labelBuilder.label("check-valid-connection-sql"));
        assertEquals("Connection URL", labelBuilder.label("connection-url"));
        assertEquals("JNDI Name", labelBuilder.label("jndi-name"));
        assertEquals("URL Selector Strategy Class Name", labelBuilder.label("url-selector-strategy-class-name"));
        assertEquals("Modify WSDL Address", labelBuilder.label("modify-wsdl-address"));
        assertEquals("WSDL Port", labelBuilder.label("wsdl-port"));
    }

    @Test
    void enumeration() {
        assertEquals("'First'", labelBuilder.enumeration(singletonList("first"), "and"));
        assertEquals("'First' or 'Second'", labelBuilder.enumeration(asList("first", "second"), "or"));
        assertEquals("'First', 'Second' and / or 'Third'",
                labelBuilder.enumeration(asList("first", "second", "third"), "and / or"));
    }

    @Test
    void label() {
        assertEquals("Profile Name", labelBuilder.label("Profile Name"));
    }

    @Test
    void nested() {
        assertEquals("HTTP Upgrade / Enabled", labelBuilder.label("http-upgrade.enabled"));
        assertEquals("A / B / C / D", labelBuilder.label("a.b.c.d"));
    }
}

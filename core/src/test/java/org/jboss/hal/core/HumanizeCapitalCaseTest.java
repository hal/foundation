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
import static org.jboss.hal.core.Humanize.CaseType.CAPITAL;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HumanizeCapitalCaseTest {

    private Humanize humanize;

    @BeforeEach
    void beforeAll() {
        humanize = new Humanize(CAPITAL);
    }

    @Test
    void capitalize() {
        assertEquals("Background Validation", humanize.label("background-validation"));
        assertEquals("Enabled", humanize.label("enabled"));
    }

    @Test
    void specials() {
        assertEquals("Check Valid Connection SQL", humanize.label("check-valid-connection-sql"));
        assertEquals("Connection URL", humanize.label("connection-url"));
        assertEquals("JNDI Name", humanize.label("jndi-name"));
        assertEquals("URL Selector Strategy Class Name", humanize.label("url-selector-strategy-class-name"));
        assertEquals("Modify WSDL Address", humanize.label("modify-wsdl-address"));
        assertEquals("WSDL Port", humanize.label("wsdl-port"));
    }

    @Test
    void enumeration() {
        assertEquals("'First'", humanize.enumeration(singletonList("first"), "and"));
        assertEquals("'First' or 'Second'", humanize.enumeration(asList("first", "second"), "or"));
        assertEquals("'First', 'Second' and / or 'Third'",
                humanize.enumeration(asList("first", "second", "third"), "and / or"));
    }

    @Test
    void label() {
        assertEquals("Profile Name", humanize.label("Profile Name"));
    }

    @Test
    void nested() {
        assertEquals("HTTP Upgrade / Enabled", humanize.label("http-upgrade.enabled"));
        assertEquals("A / B / C / D", humanize.label("a.b.c.d"));
    }
}

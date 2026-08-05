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
package org.jboss.hal.dmr;

import org.junit.jupiter.api.Test;

import static org.jboss.hal.dmr.JndiName.isJndiName;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JndiNameTest {

    // ------------------------------------------------------ invalid

    @Test
    public void nil() {
        assertFalse(isJndiName(null));
    }

    @Test
    public void empty() {
        assertFalse(isJndiName(""));
    }

    @Test
    public void plainText() {
        assertFalse(isJndiName("ExampleDS"));
    }

    @Test
    public void schemeOnly() {
        assertFalse(isJndiName("java:"));
    }

    @Test
    public void wrongScheme() {
        assertFalse(isJndiName("ldap:something"));
    }

    @Test
    public void colonWithoutScheme() {
        assertFalse(isJndiName(":jboss/datasources/ExampleDS"));
    }

    @Test
    public void unknownNamespace() {
        assertFalse(isJndiName("java:unknown/path"));
    }

    @Test
    public void slashOnly() {
        assertFalse(isJndiName("java:/"));
    }

    // ------------------------------------------------------ valid: standard namespaces

    @Test
    public void compNamespace() {
        assertTrue(isJndiName("java:comp/env/jdbc/MyDB"));
    }

    @Test
    public void compNamespaceAlone() {
        assertTrue(isJndiName("java:comp"));
    }

    @Test
    public void moduleNamespace() {
        assertTrue(isJndiName("java:module/myBean"));
    }

    @Test
    public void appNamespace() {
        assertTrue(isJndiName("java:app/myBean"));
    }

    @Test
    public void globalNamespace() {
        assertTrue(isJndiName("java:global/myApp/myBean"));
    }

    @Test
    public void globalNamespaceAlone() {
        assertTrue(isJndiName("java:global"));
    }

    // ------------------------------------------------------ valid: WildFly namespaces

    @Test
    public void jbossNamespace() {
        assertTrue(isJndiName("java:jboss/datasources/ExampleDS"));
    }

    @Test
    public void jbossExportedNamespace() {
        assertTrue(isJndiName("java:jboss/exported/myService"));
    }

    // ------------------------------------------------------ valid: legacy format

    @Test
    public void legacySlash() {
        assertTrue(isJndiName("java:/comp/env/jdbc/MyDB"));
    }

    @Test
    public void legacySlashSimple() {
        assertTrue(isJndiName("java:/DefaultDS"));
    }
}

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

import org.jboss.hal.env.Environment;
import org.jboss.hal.env.Version;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.jboss.hal.env.OperationMode.STANDALONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteRegistryTest {

    private RouteRegistry registry;

    @BeforeEach
    void setUp() {
        Environment environment = new Environment();
        environment.update("test", "test", "test",
                Version.EMPTY_VERSION, Version.EMPTY_VERSION, STANDALONE);
        StatementContext statementContext = new StatementContext(environment);
        registry = new RouteRegistry(null, statementContext, "/fallback");
    }

    // ------------------------------------------------------ byTemplate: empty registry

    @Test
    void noMatchWhenEmpty() {
        Optional<RouteBinding> result = registry.byTemplate(AddressTemplate.ofTrusted("subsystem=datasources"));
        assertFalse(result.isPresent());
    }

    // ------------------------------------------------------ byTemplate: integration with TemplateMatcher

    @Test
    void prefixMatchSelectsLongest() {
        register("/subsystem", "subsystem=*");
        register("/datasources", "subsystem=datasources");
        register("/ds-detail", "subsystem=datasources/data-source=*");

        Optional<RouteBinding> result = registry.byTemplate(
                AddressTemplate.ofTrusted("subsystem=datasources/data-source=ExampleDS"));
        assertTrue(result.isPresent());
        assertEquals("/ds-detail", result.get().route());
    }

    // ------------------------------------------------------ byTemplate: overwrite on re-register

    @Test
    void reRegisterOverwritesPreviousBinding() {
        register("/old-route", "interface=*");
        register("/old-route", "subsystem=*");

        Optional<RouteBinding> byRoute = registry.byRoute("/old-route");
        assertTrue(byRoute.isPresent());
        assertEquals("/subsystem=*", byRoute.get().template().template);

        assertFalse(registry.byTemplate(AddressTemplate.ofTrusted("interface=public")).isPresent());
        assertTrue(registry.byTemplate(AddressTemplate.ofTrusted("subsystem=logging")).isPresent());
    }

    // ------------------------------------------------------ byRoute

    @Test
    void byRouteExactLookup() {
        register("/interfaces", "interface=*");
        Optional<RouteBinding> result = registry.byRoute("/interfaces");
        assertTrue(result.isPresent());
        assertEquals("/interfaces", result.get().route());
    }

    @Test
    void byRouteNotFound() {
        register("/interfaces", "interface=*");
        Optional<RouteBinding> result = registry.byRoute("/nonexistent");
        assertFalse(result.isPresent());
    }

    // ------------------------------------------------------ helper

    private void register(String route, String template) {
        registry.register(new RouteBinding(route, AddressTemplate.ofTrusted(template),
                (t, p) -> t,
                (t, r) -> new String[0]));
    }
}

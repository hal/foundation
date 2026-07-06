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

    // ------------------------------------------------------ byTemplate: no match

    @Test
    void noMatchWhenEmpty() {
        Optional<RouteBinding> result = registry.byTemplate(AddressTemplate.ofTrusted("subsystem=datasources"));
        assertFalse(result.isPresent());
    }

    @Test
    void noMatchWhenKeysDiffer() {
        register("/interfaces", "interface=*");
        Optional<RouteBinding> result = registry.byTemplate(AddressTemplate.ofTrusted("subsystem=datasources"));
        assertFalse(result.isPresent());
    }

    // ------------------------------------------------------ byTemplate: exact match

    @Test
    void exactMatchSingleSegment() {
        register("/interfaces", "interface=*");
        Optional<RouteBinding> result = registry.byTemplate(AddressTemplate.ofTrusted("interface=public"));
        assertTrue(result.isPresent());
        assertEquals("/interfaces", result.get().route());
    }

    @Test
    void exactMatchMultipleSegments() {
        register("/datasources", "subsystem=datasources/data-source=*");
        Optional<RouteBinding> result = registry.byTemplate(
                AddressTemplate.ofTrusted("subsystem=datasources/data-source=ExampleDS"));
        assertTrue(result.isPresent());
        assertEquals("/datasources", result.get().route());
    }

    // ------------------------------------------------------ byTemplate: prefix match

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

    @Test
    void prefixMatchWithWildcard() {
        register("/subsystem", "subsystem=*");

        Optional<RouteBinding> result = registry.byTemplate(
                AddressTemplate.ofTrusted("subsystem=logging/logger=com.example"));
        assertTrue(result.isPresent());
        assertEquals("/subsystem", result.get().route());
    }

    // ------------------------------------------------------ byTemplate: exact value beats wildcard

    @Test
    void exactValueBeatsWildcardAtSameDepth() {
        register("/any-subsystem", "subsystem=*");
        register("/logging", "subsystem=logging");

        Optional<RouteBinding> result = registry.byTemplate(
                AddressTemplate.ofTrusted("subsystem=logging/logger=com.example"));
        assertTrue(result.isPresent());
        assertEquals("/logging", result.get().route());
    }

    @Test
    void wildcardMatchesWhenExactValueDiffers() {
        register("/any-subsystem", "subsystem=*");
        register("/logging", "subsystem=logging");

        Optional<RouteBinding> result = registry.byTemplate(
                AddressTemplate.ofTrusted("subsystem=datasources/data-source=ExampleDS"));
        assertTrue(result.isPresent());
        assertEquals("/any-subsystem", result.get().route());
    }

    // ------------------------------------------------------ byTemplate: deeper prefix wins

    @Test
    void deeperPrefixWinsOverShallowerExact() {
        register("/logging", "subsystem=logging");
        register("/log-handler", "subsystem=logging/console-handler=*");

        Optional<RouteBinding> result = registry.byTemplate(
                AddressTemplate.ofTrusted("subsystem=logging/console-handler=CONSOLE"));
        assertTrue(result.isPresent());
        assertEquals("/log-handler", result.get().route());
    }

    // ------------------------------------------------------ byTemplate: pattern longer than input

    @Test
    void patternLongerThanInputDoesNotMatch() {
        register("/ds-detail", "subsystem=datasources/data-source=*");

        Optional<RouteBinding> result = registry.byTemplate(AddressTemplate.ofTrusted("subsystem=datasources"));
        assertFalse(result.isPresent());
    }

    // ------------------------------------------------------ byTemplate: multiple wildcards

    @Test
    void multipleWildcardsMatchDeepInput() {
        register("/generic-child", "subsystem=*/data-source=*");

        Optional<RouteBinding> result = registry.byTemplate(
                AddressTemplate.ofTrusted("subsystem=datasources/data-source=ExampleDS"));
        assertTrue(result.isPresent());
        assertEquals("/generic-child", result.get().route());
    }

    @Test
    void exactValuesPreferredOverMultipleWildcards() {
        register("/generic-child", "subsystem=*/data-source=*");
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

    // ------------------------------------------------------ byTemplate: single segment input

    @Test
    void singleSegmentInputMatchesSingleSegmentPattern() {
        register("/subsystem", "subsystem=*");
        register("/ds-detail", "subsystem=datasources/data-source=*");

        Optional<RouteBinding> result = registry.byTemplate(AddressTemplate.ofTrusted("subsystem=logging"));
        assertTrue(result.isPresent());
        assertEquals("/subsystem", result.get().route());
    }

    // ------------------------------------------------------ byTemplate: value mismatch

    @Test
    void exactValueMismatchFallsBackToWildcard() {
        register("/logging", "subsystem=logging");
        register("/any-subsystem", "subsystem=*");

        Optional<RouteBinding> result = registry.byTemplate(AddressTemplate.ofTrusted("subsystem=elytron"));
        assertTrue(result.isPresent());
        assertEquals("/any-subsystem", result.get().route());
    }

    @Test
    void exactValueMismatchWithNoWildcardReturnsEmpty() {
        register("/logging", "subsystem=logging");

        Optional<RouteBinding> result = registry.byTemplate(AddressTemplate.ofTrusted("subsystem=elytron"));
        assertFalse(result.isPresent());
    }

    // ------------------------------------------------------ byTemplate: deeply nested

    @Test
    void deeplyNestedInputMatchesShallowPrefix() {
        register("/subsystem", "subsystem=*");

        Optional<RouteBinding> result = registry.byTemplate(
                AddressTemplate.ofTrusted("subsystem=logging/console-handler=CONSOLE/formatter=PATTERN"));
        assertTrue(result.isPresent());
        assertEquals("/subsystem", result.get().route());
    }

    @Test
    void deeplyNestedSelectsMostSpecificPrefix() {
        register("/subsystem", "subsystem=*");
        register("/logging", "subsystem=logging");
        register("/handler", "subsystem=logging/console-handler=*");

        Optional<RouteBinding> result = registry.byTemplate(
                AddressTemplate.ofTrusted("subsystem=logging/console-handler=CONSOLE/formatter=PATTERN"));
        assertTrue(result.isPresent());
        assertEquals("/handler", result.get().route());
    }

    // ------------------------------------------------------ byTemplate: mixed wildcards and exact values

    @Test
    void mixedWildcardAndExactAtDifferentDepths() {
        register("/any-any", "subsystem=*/child=*");
        register("/logging-any", "subsystem=logging/child=*");
        register("/any-specific", "subsystem=*/child=foo");

        Optional<RouteBinding> result = registry.byTemplate(
                AddressTemplate.ofTrusted("subsystem=logging/child=foo"));
        assertTrue(result.isPresent());
        // both /logging-any and /any-specific match with 2 segments,
        // but /logging-any has 1 exact value (logging) and /any-specific has 1 exact value (foo)
        // — tied on segments and exactValues, so first-registered wins.
        // /any-any also matches with 2 segments but 0 exact values.
        // The important thing: /any-any does NOT win.
        String route = result.get().route();
        assertTrue("/logging-any".equals(route) || "/any-specific".equals(route));
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

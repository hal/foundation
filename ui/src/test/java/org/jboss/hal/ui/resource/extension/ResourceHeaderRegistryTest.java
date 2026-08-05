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
package org.jboss.hal.ui.resource.extension;

import java.util.List;
import java.util.Optional;

import org.jboss.hal.env.Environment;
import org.jboss.hal.env.Version;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.ui.resource.ResourceHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import elemental2.promise.Promise;

import static org.jboss.hal.env.OperationMode.STANDALONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceHeaderRegistryTest {

    private Environment environment;

    @BeforeEach
    void setUp() {
        environment = new Environment();
        environment.update("test", "test", "test",
                Version.EMPTY_VERSION, Version.EMPTY_VERSION, STANDALONE);
    }

    // ------------------------------------------------------ no match

    @Test
    void noMatchWhenEmpty() {
        ResourceHeaderRegistry registry = registry();
        Optional<ResourceHeaderProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=datasources"));
        assertFalse(result.isPresent());
    }

    @Test
    void noMatchWhenKeysDiffer() {
        ResourceHeaderRegistry registry = registry(
                provider("interface=*"));
        Optional<ResourceHeaderProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=datasources"));
        assertFalse(result.isPresent());
    }

    // ------------------------------------------------------ exact match

    @Test
    void exactMatchSingleSegment() {
        ResourceHeaderProvider p = provider("interface=*");
        ResourceHeaderRegistry registry = registry(p);
        Optional<ResourceHeaderProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("interface=public"));
        assertTrue(result.isPresent());
        assertEquals(p, result.get());
    }

    @Test
    void exactMatchMultipleSegments() {
        ResourceHeaderProvider p = provider("subsystem=datasources/data-source=*");
        ResourceHeaderRegistry registry = registry(p);
        Optional<ResourceHeaderProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=datasources/data-source=ExampleDS"));
        assertTrue(result.isPresent());
        assertEquals(p, result.get());
    }

    // ------------------------------------------------------ prefix match

    @Test
    void prefixMatchSelectsLongest() {
        ResourceHeaderProvider shallow = provider("subsystem=*");
        ResourceHeaderProvider mid = provider("subsystem=datasources");
        ResourceHeaderProvider deep = provider("subsystem=datasources/data-source=*");
        ResourceHeaderRegistry registry = registry(shallow, mid, deep);

        Optional<ResourceHeaderProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=datasources/data-source=ExampleDS"));
        assertTrue(result.isPresent());
        assertEquals(deep, result.get());
    }

    // ------------------------------------------------------ exact value beats wildcard

    @Test
    void exactValueBeatsWildcardAtSameDepth() {
        ResourceHeaderProvider wildcard = provider("subsystem=*");
        ResourceHeaderProvider exact = provider("subsystem=logging");
        ResourceHeaderRegistry registry = registry(wildcard, exact);

        Optional<ResourceHeaderProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=logging/logger=com.example"));
        assertTrue(result.isPresent());
        assertEquals(exact, result.get());
    }

    // ------------------------------------------------------ pattern longer than input

    @Test
    void patternLongerThanInputDoesNotMatch() {
        ResourceHeaderRegistry registry = registry(
                provider("subsystem=datasources/data-source=*"));
        Optional<ResourceHeaderProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=datasources"));
        assertFalse(result.isPresent());
    }

    // ------------------------------------------------------ appliesTo filtering

    @Test
    void appliesToFilterExcludesProvider() {
        ResourceHeaderProvider never = provider("subsystem=*",
                (env, tmpl) -> false);
        ResourceHeaderRegistry registry = registry(never);

        Optional<ResourceHeaderProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=logging"));
        assertFalse(result.isPresent());
    }

    @Test
    void appliesToFilterFallsBackToNextMatch() {
        ResourceHeaderProvider filtered = provider("subsystem=logging",
                (env, tmpl) -> false);
        ResourceHeaderProvider fallback = provider("subsystem=*");
        ResourceHeaderRegistry registry = registry(filtered, fallback);

        Optional<ResourceHeaderProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=logging"));
        assertTrue(result.isPresent());
        assertEquals(fallback, result.get());
    }

    @Test
    void appliesToReceivesConcreteTemplate() {
        AddressTemplate[] captured = new AddressTemplate[1];
        ResourceHeaderProvider p = provider("subsystem=*",
                (env, tmpl) -> {
                    captured[0] = tmpl;
                    return true;
                });
        ResourceHeaderRegistry registry = registry(p);

        AddressTemplate input = AddressTemplate.ofTrusted("subsystem=logging");
        registry.lookup(environment, input);
        assertEquals(input, captured[0]);
    }

    // ------------------------------------------------------ helpers

    private ResourceHeaderRegistry registry(ResourceHeaderProvider... providers) {
        return new ResourceHeaderRegistry(List.of(providers));
    }

    @FunctionalInterface
    interface AppliesToFn {

        boolean test(Environment env, AddressTemplate template);
    }

    private ResourceHeaderProvider provider(String scope) {
        return provider(scope, (env, tmpl) -> true);
    }

    private ResourceHeaderProvider provider(String scope, AppliesToFn appliesTo) {
        AddressTemplate scopeTemplate = AddressTemplate.ofTrusted(scope);
        return new ResourceHeaderProvider() {
            @Override
            public AddressTemplate scope() {
                return scopeTemplate;
            }

            @Override
            public boolean appliesTo(Environment environment, AddressTemplate template) {
                return appliesTo.test(environment, template);
            }

            @Override
            public Promise<ResourceHeader> createHeader(AddressTemplate template, Metadata metadata,
                    ResourceHeader defaultHeader) {
                return null; // not tested here
            }
        };
    }
}

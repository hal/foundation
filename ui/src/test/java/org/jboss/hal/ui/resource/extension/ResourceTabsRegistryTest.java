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
import java.util.Set;

import org.jboss.hal.env.Environment;
import org.jboss.hal.env.Version;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.ui.resource.shell.ResourceTabs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import elemental2.promise.Promise;

import static org.jboss.hal.env.OperationMode.STANDALONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceTabsRegistryTest {

    private Environment environment;

    @BeforeEach
    void setUp() {
        environment = new Environment();
        environment.update("test", "test", "test",
                Version.EMPTY_VERSION, Version.EMPTY_VERSION, STANDALONE);
    }

    // ------------------------------------------------------ empty registry

    @Test
    void noMatchWhenEmpty() {
        ResourceTabsRegistry registry = registry();
        Optional<ResourceTabsProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=datasources"));
        assertFalse(result.isPresent());
    }

    // ------------------------------------------------------ appliesTo filtering

    @Test
    void appliesToFilterExcludesProvider() {
        ResourceTabsProvider never = provider("subsystem=*",
                (env, tmpl) -> false);
        ResourceTabsRegistry registry = registry(never);

        Optional<ResourceTabsProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=logging"));
        assertFalse(result.isPresent());
    }

    @Test
    void appliesToFilterFallsBackToNextMatch() {
        ResourceTabsProvider filtered = provider("subsystem=logging",
                (env, tmpl) -> false);
        ResourceTabsProvider fallback = provider("subsystem=*");
        ResourceTabsRegistry registry = registry(filtered, fallback);

        Optional<ResourceTabsProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=logging"));
        assertTrue(result.isPresent());
        assertEquals(fallback, result.get());
    }

    @Test
    void appliesToReceivesConcreteTemplate() {
        AddressTemplate[] captured = new AddressTemplate[1];
        ResourceTabsProvider p = provider("subsystem=*",
                (env, tmpl) -> {
                    captured[0] = tmpl;
                    return true;
                });
        ResourceTabsRegistry registry = registry(p);

        AddressTemplate input = AddressTemplate.ofTrusted("subsystem=logging");
        registry.lookup(environment, input);
        assertEquals(input, captured[0]);
    }

    // ------------------------------------------------------ helpers

    private ResourceTabsRegistry registry(ResourceTabsProvider... providers) {
        return new ResourceTabsRegistry(List.of(providers));
    }

    @FunctionalInterface
    interface AppliesToFn {

        boolean test(Environment env, AddressTemplate template);
    }

    private ResourceTabsProvider provider(String scope) {
        return provider(scope, (env, tmpl) -> true);
    }

    private ResourceTabsProvider provider(String scope, AppliesToFn appliesTo) {
        AddressTemplate scopeTemplate = AddressTemplate.ofTrusted(scope);
        return new ResourceTabsProvider() {
            @Override
            public Set<AddressTemplate> scopes() {
                return Set.of(scopeTemplate);
            }

            @Override
            public boolean appliesTo(Environment environment, AddressTemplate template) {
                return appliesTo.test(environment, template);
            }

            @Override
            public Promise<ResourceTabs> customizeTabs(AddressTemplate template, Metadata metadata,
                    ResourceTabs defaultTabs) {
                return null; // not tested here
            }
        };
    }
}

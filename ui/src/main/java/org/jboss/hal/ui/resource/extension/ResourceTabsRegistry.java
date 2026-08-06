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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.ejb.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.hal.env.Environment;
import org.jboss.hal.meta.AddressTemplate;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.meta.TemplateMatcher.bestMatchMultiple;

/**
 * CDI-managed registry that collects all {@link ResourceTabsProvider} implementations at startup and provides best-prefix
 * template matching at lookup time.
 * <p>
 * The matching algorithm compares each of the provider's {@link ResourceTabsProvider#scopes()} segments against the input
 * template segments left-to-right. When multiple providers match, the one with the longest matching prefix wins; ties are
 * broken by the number of exact (non-wildcard) value matches. Only providers where
 * {@link ResourceTabsProvider#appliesTo(Environment, AddressTemplate)} returns {@code true} are considered.
 */
@Startup
@ApplicationScoped
public class ResourceTabsRegistry {

    private final List<ResourceTabsProvider> providers;

    @Inject
    public ResourceTabsRegistry(Instance<ResourceTabsProvider> providers) {
        this.providers = new ArrayList<>();
        for (ResourceTabsProvider provider : providers) {
            this.providers.add(provider);
        }
    }

    /** Constructor for testing without CDI. */
    ResourceTabsRegistry(List<ResourceTabsProvider> providers) {
        this.providers = new ArrayList<>(providers);
    }

    /**
     * Finds the best matching provider for the given environment and template. Only providers whose
     * {@link ResourceTabsProvider#appliesTo(Environment, AddressTemplate)} returns {@code true} are considered, so callers do
     * not need to re-check activation conditions. Among eligible providers, the one with the best prefix match against
     * {@link ResourceTabsProvider#scopes()} wins. Returns {@link Optional#empty()} if no provider matches.
     */
    public Optional<ResourceTabsProvider> lookup(Environment environment, AddressTemplate template) {
        List<ResourceTabsProvider> eligible = providers.stream()
                .filter(p -> p.appliesTo(environment, template))
                .collect(toList());
        return bestMatchMultiple(eligible, ResourceTabsProvider::scopes, template);
    }
}

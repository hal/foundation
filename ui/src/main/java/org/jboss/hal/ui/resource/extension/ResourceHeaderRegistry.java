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
import org.jboss.hal.meta.Segment;

/**
 * CDI-managed registry that collects all {@link ResourceHeaderProvider} implementations at startup and provides
 * best-prefix template matching at lookup time.
 * <p>
 * The matching algorithm compares the provider's {@link ResourceHeaderProvider#scope()} segments against the input
 * template segments left-to-right. When multiple providers match, the one with the longest matching prefix wins;
 * ties are broken by the number of exact (non-wildcard) value matches. Only providers where
 * {@link ResourceHeaderProvider#appliesTo(Environment, AddressTemplate)} returns {@code true} are considered.
 */
@Startup
@ApplicationScoped
public class ResourceHeaderRegistry {

    private final List<ResourceHeaderProvider> providers;

    @Inject
    public ResourceHeaderRegistry(Instance<ResourceHeaderProvider> providers) {
        this.providers = new ArrayList<>();
        for (ResourceHeaderProvider provider : providers) {
            this.providers.add(provider);
        }
    }

    /** Constructor for testing without CDI. */
    ResourceHeaderRegistry(List<ResourceHeaderProvider> providers) {
        this.providers = new ArrayList<>(providers);
    }

    /**
     * Finds the best matching provider for the given environment and template. Returns {@link Optional#empty()} if no
     * provider matches.
     */
    public Optional<ResourceHeaderProvider> lookup(Environment environment, AddressTemplate template) {
        ResourceHeaderProvider best = null;
        int bestSegments = -1;
        int bestExactValues = -1;

        for (ResourceHeaderProvider provider : providers) {
            if (!provider.appliesTo(environment, template)) {
                continue;
            }
            Match match = match(provider.scope(), template);
            if (match.matches) {
                if (match.segments > bestSegments ||
                        (match.segments == bestSegments && match.exactValues > bestExactValues)) {
                    best = provider;
                    bestSegments = match.segments;
                    bestExactValues = match.exactValues;
                }
            }
        }
        return Optional.ofNullable(best);
    }

    // ------------------------------------------------------ internal

    private Match match(AddressTemplate pattern, AddressTemplate input) {
        List<Segment> patternSegments = pattern.segments();
        List<Segment> inputSegments = input.segments();
        if (patternSegments.isEmpty() || patternSegments.size() > inputSegments.size()) {
            return Match.NO_MATCH;
        }

        int exactValues = 0;
        for (int i = 0; i < patternSegments.size(); i++) {
            Segment ps = patternSegments.get(i);
            Segment is = inputSegments.get(i);
            if (!ps.key.equals(is.key)) {
                return Match.NO_MATCH;
            }
            if ("*".equals(ps.value)) {
                continue;
            }
            if (!ps.value.equals(is.value)) {
                return Match.NO_MATCH;
            }
            exactValues++;
        }
        return new Match(true, patternSegments.size(), exactValues);
    }

    private record Match(boolean matches, int segments, int exactValues) {

        static final Match NO_MATCH = new Match(false, 0, 0);
    }
}

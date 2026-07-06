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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jboss.elemento.router.PlaceManager;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Segment;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.StatementContextResolver;

/**
 * Central registry that holds all {@link RouteBinding}s and provides lookup in both directions:
 * <ul>
 *     <li>{@link #byRoute(String)} — exact match by route string (used by {@link RouteBindingPage})</li>
 *     <li>{@link #byTemplate(AddressTemplate)} — best-prefix match by address template (used for navigation)</li>
 * </ul>
 * The {@link #goTo(AddressTemplate)} convenience method combines a template lookup with navigation, falling back to the
 * fallback route when no dedicated route is registered.
 * <p>
 * The best-prefix matching algorithm picks the binding whose template matches the longest prefix of the input template. When
 * multiple bindings match with the same prefix length, the one with more exact (non-wildcard) value matches wins. For example,
 * given bindings for {@code subsystem=*} and {@code subsystem=logging}, navigating to
 * {@code subsystem=logging/logger=com.example} resolves to the more specific {@code subsystem=logging} binding.
 *
 * @see RouteBinding
 */
public class RouteRegistry {

    private final PlaceManager placeManager;
    private final StatementContext statementContext;
    private final String fallbackRoute;
    private final Map<String, RouteBinding> bindings;

    public RouteRegistry(PlaceManager placeManager, StatementContext statementContext, String fallbackRoute) {
        this.placeManager = placeManager;
        this.statementContext = statementContext;
        this.fallbackRoute = fallbackRoute;
        this.bindings = new HashMap<>();
    }

    /** Registers a route binding. */
    public void register(RouteBinding binding) {
        bindings.put(binding.route(), binding);
    }

    /** Looks up a binding by its exact route string. */
    public Optional<RouteBinding> byRoute(String route) {
        return Optional.ofNullable(bindings.get(route));
    }

    /**
     * Finds the best matching binding for the given address template using prefix matching. When multiple bindings match, the
     * one with the longest matching prefix wins; ties are broken by the number of exact (non-wildcard) value matches.
     */
    public Optional<RouteBinding> byTemplate(AddressTemplate template) {
        RouteBinding bestBinding = null;
        int bestSegments = -1;
        int bestExactValues = -1;

        for (RouteBinding binding : bindings.values()) {
            AddressTemplate resolved = new StatementContextResolver(statementContext).resolve(binding.template());
            Match match = match(resolved, template);
            if (match.matches) {
                if (match.segments > bestSegments ||
                        (match.segments == bestSegments && match.exactValues > bestExactValues)) {
                    bestBinding = binding;
                    bestSegments = match.segments;
                    bestExactValues = match.exactValues;
                }
            }
        }
        return Optional.ofNullable(bestBinding);
    }

    /** Navigates to the best matching route for the given template, falling back to the fallback route. */
    public void goTo(AddressTemplate template) {
        byTemplate(template).ifPresentOrElse(binding -> placeManager.goTo(binding.route(), binding.routeParams(template)),
                () -> placeManager.goTo(fallbackRoute, template.template));
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

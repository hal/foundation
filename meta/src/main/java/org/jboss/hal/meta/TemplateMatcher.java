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
package org.jboss.hal.meta;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Prefix-matching utility for {@link AddressTemplate}s.
 * <p>
 * Compares a pattern template's segments against an input template's segments left-to-right. When multiple patterns match, the
 * one with the longest matching prefix wins; ties are broken by the number of exact (non-wildcard) value matches.
 */
public final class TemplateMatcher {

    /**
     * The result of a prefix match between a pattern template and an input template.
     * <p>
     * Natural ordering is by {@link #segments} descending, then {@link #exactValues} descending, so the "best" match sorts
     * first. {@link #NO_MATCH} sorts last.
     */
    public record Match(boolean matches, int segments, int exactValues) implements Comparable<Match> {

        public static final Match NO_MATCH = new Match(false, 0, 0);

        @Override
        public int compareTo(Match other) {
            int cmp = Integer.compare(other.segments, this.segments);
            return cmp != 0 ? cmp : Integer.compare(other.exactValues, this.exactValues);
        }
    }

    /**
     * Compares the pattern's segments against the input's segments left-to-right. A wildcard value ({@code *}) in the pattern
     * matches any value in the input. Returns a {@link Match} describing how many segments matched and how many had exact
     * (non-wildcard) value equality, or {@link Match#NO_MATCH} if the pattern does not match.
     *
     * @param pattern the pattern template (may contain wildcards)
     * @param input   the concrete input template to match against
     * @return the match result
     */
    public static Match match(AddressTemplate pattern, AddressTemplate input) {
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

    /**
     * Finds the best matching candidate from an iterable of candidates. Each candidate's pattern template is extracted using
     * {@code templateFn} and matched against the input. The candidate with the best {@link Match} (most segments, then most
     * exact values) wins.
     *
     * @param candidates the candidates to search
     * @param templateFn extracts the pattern template from each candidate
     * @param input      the template to match against
     * @param <T>        the candidate type
     * @return the best matching candidate, or {@link Optional#empty()} if none match
     */
    public static <T> Optional<T> bestMatch(Iterable<T> candidates,
            Function<T, AddressTemplate> templateFn,
            AddressTemplate input) {
        T best = null;
        int bestSegments = -1;
        int bestExactValues = -1;

        for (T candidate : candidates) {
            Match match = match(templateFn.apply(candidate), input);
            if (match.matches) {
                if (match.segments > bestSegments ||
                        (match.segments == bestSegments && match.exactValues > bestExactValues)) {
                    best = candidate;
                    bestSegments = match.segments;
                    bestExactValues = match.exactValues;
                }
            }
        }
        return Optional.ofNullable(best);
    }

    /**
     * Finds the best matching candidate from an iterable of candidates, where each candidate may declare multiple pattern
     * templates. Each candidate's pattern templates are extracted using {@code templatesFn} and each is matched against the
     * input. The candidate with the best {@link Match} across all its templates (most segments, then most exact values) wins.
     *
     * @param candidates  the candidates to search
     * @param templatesFn extracts the pattern templates from each candidate
     * @param input       the template to match against
     * @param <T>         the candidate type
     * @return the best matching candidate, or {@link Optional#empty()} if none match
     */
    public static <T> Optional<T> bestMatchMultiple(Iterable<T> candidates,
            Function<T, ? extends Collection<AddressTemplate>> templatesFn,
            AddressTemplate input) {
        T best = null;
        int bestSegments = -1;
        int bestExactValues = -1;

        for (T candidate : candidates) {
            for (AddressTemplate pattern : templatesFn.apply(candidate)) {
                Match match = match(pattern, input);
                if (match.matches &&
                        (match.segments > bestSegments ||
                                (match.segments == bestSegments && match.exactValues > bestExactValues))) {
                    best = candidate;
                    bestSegments = match.segments;
                    bestExactValues = match.exactValues;
                }
            }
        }
        return Optional.ofNullable(best);
    }
}

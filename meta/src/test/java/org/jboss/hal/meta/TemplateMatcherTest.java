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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.jboss.hal.meta.TemplateMatcher.Match;
import org.junit.jupiter.api.Test;

import static org.jboss.hal.meta.TemplateMatcher.Match.NO_MATCH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateMatcherTest {

    // ------------------------------------------------------ match: no match

    @Test
    void noMatchWhenPatternEmpty() {
        Match result = TemplateMatcher.match(AddressTemplate.root(), AddressTemplate.ofTrusted("subsystem=datasources"));
        assertFalse(result.matches());
    }

    @Test
    void noMatchWhenKeysDiffer() {
        Match result = TemplateMatcher.match(
                AddressTemplate.ofTrusted("interface=*"),
                AddressTemplate.ofTrusted("subsystem=datasources"));
        assertFalse(result.matches());
    }

    @Test
    void noMatchWhenPatternLongerThanInput() {
        Match result = TemplateMatcher.match(
                AddressTemplate.ofTrusted("subsystem=datasources/data-source=*"),
                AddressTemplate.ofTrusted("subsystem=datasources"));
        assertFalse(result.matches());
    }

    @Test
    void noMatchWhenExactValueDiffers() {
        Match result = TemplateMatcher.match(
                AddressTemplate.ofTrusted("subsystem=logging"),
                AddressTemplate.ofTrusted("subsystem=elytron"));
        assertFalse(result.matches());
    }

    // ------------------------------------------------------ match: positive

    @Test
    void matchSingleSegmentWithWildcard() {
        Match result = TemplateMatcher.match(
                AddressTemplate.ofTrusted("interface=*"),
                AddressTemplate.ofTrusted("interface=public"));
        assertTrue(result.matches());
        assertEquals(1, result.segments());
        assertEquals(0, result.exactValues());
    }

    @Test
    void matchSingleSegmentWithExactValue() {
        Match result = TemplateMatcher.match(
                AddressTemplate.ofTrusted("subsystem=logging"),
                AddressTemplate.ofTrusted("subsystem=logging"));
        assertTrue(result.matches());
        assertEquals(1, result.segments());
        assertEquals(1, result.exactValues());
    }

    @Test
    void matchMultipleSegments() {
        Match result = TemplateMatcher.match(
                AddressTemplate.ofTrusted("subsystem=datasources/data-source=*"),
                AddressTemplate.ofTrusted("subsystem=datasources/data-source=ExampleDS"));
        assertTrue(result.matches());
        assertEquals(2, result.segments());
        assertEquals(1, result.exactValues());
    }

    @Test
    void matchPrefixOfLongerInput() {
        Match result = TemplateMatcher.match(
                AddressTemplate.ofTrusted("subsystem=*"),
                AddressTemplate.ofTrusted("subsystem=logging/logger=com.example"));
        assertTrue(result.matches());
        assertEquals(1, result.segments());
        assertEquals(0, result.exactValues());
    }

    @Test
    void matchMultipleWildcards() {
        Match result = TemplateMatcher.match(
                AddressTemplate.ofTrusted("subsystem=*/data-source=*"),
                AddressTemplate.ofTrusted("subsystem=datasources/data-source=ExampleDS"));
        assertTrue(result.matches());
        assertEquals(2, result.segments());
        assertEquals(0, result.exactValues());
    }

    @Test
    void matchDeeplyNestedInput() {
        Match result = TemplateMatcher.match(
                AddressTemplate.ofTrusted("subsystem=*"),
                AddressTemplate.ofTrusted("subsystem=logging/console-handler=CONSOLE/formatter=PATTERN"));
        assertTrue(result.matches());
        assertEquals(1, result.segments());
        assertEquals(0, result.exactValues());
    }

    // ------------------------------------------------------ match: compareTo

    @Test
    void compareToOrdersBySegmentsDescending() {
        Match shallow = new Match(true, 1, 0);
        Match deep = new Match(true, 2, 0);
        assertTrue(deep.compareTo(shallow) < 0);
        assertTrue(shallow.compareTo(deep) > 0);
    }

    @Test
    void compareToBreaksTiesByExactValuesDescending() {
        Match wildcard = new Match(true, 2, 0);
        Match exact = new Match(true, 2, 1);
        assertTrue(exact.compareTo(wildcard) < 0);
        assertTrue(wildcard.compareTo(exact) > 0);
    }

    @Test
    void compareToNoMatchSortsLast() {
        Match any = new Match(true, 1, 0);
        assertTrue(any.compareTo(NO_MATCH) < 0);
        assertTrue(NO_MATCH.compareTo(any) > 0);
    }

    // ------------------------------------------------------ bestMatch: no match

    @Test
    void bestMatchEmptyCandidates() {
        List<AddressTemplate> empty = List.of();
        Optional<AddressTemplate> result = TemplateMatcher.bestMatch(
                empty, Function.identity(), AddressTemplate.ofTrusted("subsystem=datasources"));
        assertFalse(result.isPresent());
    }

    @Test
    void bestMatchNoCandidatesMatch() {
        List<AddressTemplate> candidates = List.of(AddressTemplate.ofTrusted("interface=*"));
        Optional<AddressTemplate> result = TemplateMatcher.bestMatch(
                candidates, Function.identity(), AddressTemplate.ofTrusted("subsystem=datasources"));
        assertFalse(result.isPresent());
    }

    // ------------------------------------------------------ bestMatch: selection

    @Test
    void bestMatchSelectsLongestPrefix() {
        AddressTemplate shallow = AddressTemplate.ofTrusted("subsystem=*");
        AddressTemplate mid = AddressTemplate.ofTrusted("subsystem=datasources");
        AddressTemplate deep = AddressTemplate.ofTrusted("subsystem=datasources/data-source=*");
        List<AddressTemplate> candidates = List.of(shallow, mid, deep);

        Optional<AddressTemplate> result = TemplateMatcher.bestMatch(
                candidates, Function.identity(),
                AddressTemplate.ofTrusted("subsystem=datasources/data-source=ExampleDS"));
        assertTrue(result.isPresent());
        assertEquals(deep, result.get());
    }

    @Test
    void bestMatchExactValueBeatsWildcardAtSameDepth() {
        AddressTemplate wildcard = AddressTemplate.ofTrusted("subsystem=*");
        AddressTemplate exact = AddressTemplate.ofTrusted("subsystem=logging");
        List<AddressTemplate> candidates = List.of(wildcard, exact);

        Optional<AddressTemplate> result = TemplateMatcher.bestMatch(
                candidates, Function.identity(),
                AddressTemplate.ofTrusted("subsystem=logging/logger=com.example"));
        assertTrue(result.isPresent());
        assertEquals(exact, result.get());
    }

    @Test
    void bestMatchWildcardFallbackWhenExactValueDiffers() {
        AddressTemplate wildcard = AddressTemplate.ofTrusted("subsystem=*");
        AddressTemplate exact = AddressTemplate.ofTrusted("subsystem=logging");
        List<AddressTemplate> candidates = List.of(wildcard, exact);

        Optional<AddressTemplate> result = TemplateMatcher.bestMatch(
                candidates, Function.identity(),
                AddressTemplate.ofTrusted("subsystem=datasources/data-source=ExampleDS"));
        assertTrue(result.isPresent());
        assertEquals(wildcard, result.get());
    }

    @Test
    void bestMatchNoWildcardFallbackReturnsEmpty() {
        List<AddressTemplate> candidates = List.of(AddressTemplate.ofTrusted("subsystem=logging"));

        Optional<AddressTemplate> result = TemplateMatcher.bestMatch(
                candidates, Function.identity(),
                AddressTemplate.ofTrusted("subsystem=elytron"));
        assertFalse(result.isPresent());
    }

    @Test
    void bestMatchDeeperPrefixWinsOverShallowerExact() {
        AddressTemplate shallow = AddressTemplate.ofTrusted("subsystem=logging");
        AddressTemplate deep = AddressTemplate.ofTrusted("subsystem=logging/console-handler=*");
        List<AddressTemplate> candidates = List.of(shallow, deep);

        Optional<AddressTemplate> result = TemplateMatcher.bestMatch(
                candidates, Function.identity(),
                AddressTemplate.ofTrusted("subsystem=logging/console-handler=CONSOLE"));
        assertTrue(result.isPresent());
        assertEquals(deep, result.get());
    }

    @Test
    void bestMatchExactValuesPreferredOverMultipleWildcards() {
        AddressTemplate allWild = AddressTemplate.ofTrusted("subsystem=*/data-source=*");
        AddressTemplate partExact = AddressTemplate.ofTrusted("subsystem=datasources/data-source=*");
        List<AddressTemplate> candidates = List.of(allWild, partExact);

        Optional<AddressTemplate> result = TemplateMatcher.bestMatch(
                candidates, Function.identity(),
                AddressTemplate.ofTrusted("subsystem=datasources/data-source=ExampleDS"));
        assertTrue(result.isPresent());
        assertEquals(partExact, result.get());
    }

    @Test
    void bestMatchDeeplyNestedSelectsMostSpecificPrefix() {
        AddressTemplate shallow = AddressTemplate.ofTrusted("subsystem=*");
        AddressTemplate mid = AddressTemplate.ofTrusted("subsystem=logging");
        AddressTemplate deep = AddressTemplate.ofTrusted("subsystem=logging/console-handler=*");
        List<AddressTemplate> candidates = List.of(shallow, mid, deep);

        Optional<AddressTemplate> result = TemplateMatcher.bestMatch(
                candidates, Function.identity(),
                AddressTemplate.ofTrusted("subsystem=logging/console-handler=CONSOLE/formatter=PATTERN"));
        assertTrue(result.isPresent());
        assertEquals(deep, result.get());
    }

    @Test
    void bestMatchMixedWildcardAndExactAtDifferentDepths() {
        AddressTemplate anyAny = AddressTemplate.ofTrusted("subsystem=*/child=*");
        AddressTemplate loggingAny = AddressTemplate.ofTrusted("subsystem=logging/child=*");
        AddressTemplate anySpecific = AddressTemplate.ofTrusted("subsystem=*/child=foo");
        List<AddressTemplate> candidates = List.of(anyAny, loggingAny, anySpecific);

        Optional<AddressTemplate> result = TemplateMatcher.bestMatch(
                candidates, Function.identity(),
                AddressTemplate.ofTrusted("subsystem=logging/child=foo"));
        assertTrue(result.isPresent());
        // Both loggingAny and anySpecific match with 2 segments and 1 exact value — tied.
        // anyAny has 0 exact values, so it must not win.
        AddressTemplate winner = result.get();
        assertTrue(loggingAny.equals(winner) || anySpecific.equals(winner));
    }

    // ------------------------------------------------------ bestMatch: templateFn

    @Test
    void bestMatchWithTemplateFn() {
        record Named(String name, AddressTemplate template) {}
        List<Named> candidates = List.of(
                new Named("shallow", AddressTemplate.ofTrusted("subsystem=*")),
                new Named("deep", AddressTemplate.ofTrusted("subsystem=datasources/data-source=*")));

        Optional<Named> result = TemplateMatcher.bestMatch(
                candidates, Named::template,
                AddressTemplate.ofTrusted("subsystem=datasources/data-source=ExampleDS"));
        assertTrue(result.isPresent());
        assertEquals("deep", result.get().name());
    }
}

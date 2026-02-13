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

import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.jboss.hal.dmr.Expression.REG_EXP;
import static org.jboss.hal.dmr.Expression.containsExpression;
import static org.jboss.hal.dmr.Expression.extractExpressions;
import static org.jboss.hal.dmr.Expression.isExpression;
import static org.jboss.hal.dmr.Expression.splitExpression;
import static org.jboss.hal.dmr.Expression.startExpressionEnd;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExpressionTest {

    @BeforeAll
    static void beforeAll() {
        Expression.PREDICATE = name -> Pattern.matches(REG_EXP, name);
    }

    @Test
    public void nil() {
        assertFalse(isExpression(null));
    }

    @Test
    public void empty() {
        assertFalse(isExpression(""));
    }

    @Test
    public void blank() {
        assertFalse(isExpression("   "));
    }

    @Test
    public void noExpression() {
        assertFalse(isExpression("not an expression"));
        assertFalse(isExpression("${}"));
        assertFalse(isExpression("${ }"));
        assertFalse(isExpression("${  }"));
        assertFalse(isExpression("${:}"));
        assertFalse(isExpression("${.}"));
        assertFalse(isExpression("${-}"));
        assertFalse(isExpression("${{}"));
        assertFalse(isExpression("${}}"));
        assertFalse(isExpression("${0}"));
        assertFalse(isExpression("${a"));
        assertFalse(isExpression("a}"));
        assertFalse(isExpression("{a}"));
        assertFalse(isExpression("$a"));
    }

    @Test
    public void expression() {
        assertTrue(isExpression("${a}"));
        assertTrue(isExpression("${a:b}"));
        assertTrue(isExpression("${a:${b}}"));
        assertTrue(isExpression("${a:${b:c}}"));
        assertTrue(isExpression("${a:${b:${c}}}"));
        assertTrue(isExpression("${a:${b:${c:d}}}"));
    }

    @Test
    public void contains() {
        assertTrue(containsExpression("${a}"));
        assertTrue(containsExpression("start ${a}"));
        assertTrue(containsExpression("start ${a:b}"));
        assertTrue(containsExpression("${a} end"));
        assertTrue(containsExpression("${a:b} end"));
        assertTrue(containsExpression("start ${a} end"));
        assertTrue(containsExpression("start ${a:b} end"));
    }

    @Test
    public void see() {
        assertArrayEquals(new String[]{"", "${a}", ""}, startExpressionEnd("${a}"));
        assertArrayEquals(new String[]{"start ", "${a}", ""}, startExpressionEnd("start ${a}"));
        assertArrayEquals(new String[]{"start ", "${a:b}", ""}, startExpressionEnd("start ${a:b}"));
        assertArrayEquals(new String[]{"", "${a}", " end"}, startExpressionEnd("${a} end"));
        assertArrayEquals(new String[]{"", "${a:b}", " end"}, startExpressionEnd("${a:b} end"));
        assertArrayEquals(new String[]{"start ", "${a}", " end"}, startExpressionEnd("start ${a} end"));
        assertArrayEquals(new String[]{"start ", "${a:b}", " end"}, startExpressionEnd("start ${a:b} end"));

        assertArrayEquals(new String[]{"", "${a:${b:${c:d}}}", ""}, startExpressionEnd("${a:${b:${c:d}}}"));
        assertArrayEquals(new String[]{"start ", "${a:${b:${c:d}}}", " end"}, startExpressionEnd("start ${a:${b:${c:d}}} end"));
    }

    @Test
    public void split() {
        assertArrayEquals(null, splitExpression("${:}"));
        assertArrayEquals(null, splitExpression("notAnExpression"));
        assertArrayEquals(new String[]{"a", ""}, splitExpression("${a}"));
        assertArrayEquals(new String[]{"a", "b"}, splitExpression("${a:b}"));
        assertArrayEquals(new String[]{"a", "${b}"}, splitExpression("${a:${b}}"));
        assertArrayEquals(new String[]{"a", "${b:c}"}, splitExpression("${a:${b:c}}"));
        assertArrayEquals(new String[]{"a", "${b:${c}}"}, splitExpression("${a:${b:${c}}}"));
        assertArrayEquals(new String[]{"a", "${b:${c:d}}"}, splitExpression("${a:${b:${c:d}}}"));
    }

    @Test
    public void extract() {
        assertArrayEquals(null, extractExpressions("${:}"));
        assertArrayEquals(null, extractExpressions("notAnExpression"));
        assertArrayEquals(new String[]{"a"}, extractExpressions("${a}"));
        assertArrayEquals(new String[]{"a"}, extractExpressions("${a:b}"));
        assertArrayEquals(new String[]{"a", "b"}, extractExpressions("${a:${b}}"));
        assertArrayEquals(new String[]{"a", "b"}, extractExpressions("${a:${b:c}}"));
        assertArrayEquals(new String[]{"a", "b", "c"}, extractExpressions("${a:${b:${c}}}"));
        assertArrayEquals(new String[]{"a", "b", "c"}, extractExpressions("${a:${b:${c:d}}}"));
    }
}

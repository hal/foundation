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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import elemental2.core.JsRegExp;

/**
 * Utility for detecting and parsing WildFly management model expressions (e.g., {@code ${env.JAVA_HOME}}).
 *
 * <p>
 * Expressions in the WildFly management model allow for dynamic value resolution at runtime. They follow the format
 * {@code ${name}} or {@code ${name:default}}, where {@code name} is the expression identifier and {@code default} is an
 * optional fallback value.
 *
 * <p>
 * This class provides methods to check if a string is an expression, extract embedded expressions from strings, and split
 * expressions into their constituent parts.
 */
public class Expression {

    static final String REG_EXP = "^[a-zA-Z_$][a-zA-Z\\d_$\\.\\-]*$";
    static final JsRegExp JS_REG_EXP = new JsRegExp(REG_EXP);
    static Predicate<String> PREDICATE = JS_REG_EXP::test; // replaced in JVM unit tests

    /** Returns {@code true} if the given string is a complete expression in the format {@code ${name}} or {@code ${name:default}}. */
    public static boolean isExpression(String value) {
        if (value != null && value.length() > 3) {
            if (value.startsWith("${") && value.endsWith("}")) {
                String expression = value.substring(2, value.length() - 1);
                int index = expression.indexOf(':');
                String name = index > 0 ? expression.substring(0, index) : expression;
                return PREDICATE.test(name);
            }
        }
        return false;
    }

    /** Returns {@code true} if the given string contains an embedded expression. */
    public static boolean containsExpression(String value) {
        return startExpressionEnd(value) != null;
    }

    /**
     * Splits the given string around the first embedded expression. Returns a three-element array
     * {@code [prefix, expression, suffix]}, or {@code null} if no expression is found.
     */
    public static String[] startExpressionEnd(String value) {
        if (value != null && value.length() > 3) { // ${x}
            int startIndex = value.indexOf("${");
            if (startIndex >= 0) {
                int endIndex = value.lastIndexOf('}');
                if (endIndex > 0) {
                    String expression = value.substring(startIndex, endIndex + 1);
                    if (isExpression(expression)) {
                        String start = value.substring(0, startIndex);
                        String end = value.substring(endIndex + 1);
                        return new String[]{start, expression, end};
                    }
                }
            }
        }
        return null;
    }

    /**
     * Splits an expression into its name and default value. Returns a two-element array {@code [name, default]}, where
     * the default is an empty string if not specified. Returns {@code null} if the input is not a valid expression.
     */
    public static String[] splitExpression(String expression) {
        if (isExpression(expression)) {
            int colon = expression.indexOf(':');
            if (colon > 0) {
                String name = expression.substring(0, colon).substring(2); // ${
                String default_ = expression.substring(colon + 1);
                default_ = default_.substring(0, default_.length() - 1); // }
                return new String[]{name, default_};
            } else {
                String name = expression.substring(2, expression.length() - 1);
                return new String[]{name, ""};
            }
        } else {
            return null;
        }
    }

    /** Extracts all expression names from a possibly nested expression string. Returns {@code null} if no expressions are found. */
    public static String[] extractExpressions(String expression) {
        List<String> expressions = new ArrayList<>();
        String current = expression;
        String[] parts = splitExpression(current);
        while (parts != null && parts.length == 2) {
            expressions.add(parts[0]);
            if (!isExpression(parts[1])) {
                break;
            }
            current = parts[1];
            parts = splitExpression(current);
        }
        return expressions.isEmpty() ? null : expressions.toArray(new String[0]);
    }
}

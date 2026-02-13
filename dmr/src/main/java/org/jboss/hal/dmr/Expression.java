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

public class Expression {

    static final String REG_EXP = "^[a-zA-Z_$][a-zA-Z\\d_$\\.\\-]*$";
    static final JsRegExp JS_REG_EXP = new JsRegExp(REG_EXP);
    static Predicate<String> PREDICATE = JS_REG_EXP::test; // replaced in JVM unit tests

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

    public static boolean containsExpression(String value) {
        return startExpressionEnd(value) != null;
    }

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

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
package org.jboss.hal.ui.brick;

import java.util.function.Supplier;

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.hal.dmr.Expression;
import org.patternfly.icon.IconSets;
import org.patternfly.icon.PredefinedIcon;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.dmr.Expression.startExpressionEnd;
import static org.jboss.hal.resources.HalClasses.colon;
import static org.jboss.hal.resources.HalClasses.curlyBraces;
import static org.jboss.hal.resources.HalClasses.defaultValue;
import static org.jboss.hal.resources.HalClasses.dollar;
import static org.jboss.hal.resources.HalClasses.expression;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.name;
import static org.patternfly.style.Classes.end;
import static org.patternfly.style.Classes.start;

/**
 * Factory methods for rendering WildFly management model expressions ({@code ${name:default}}) as colour-coded HTML
 * elements, and for providing icon suppliers used by expression-related toggle buttons.
 * <p>
 * Expression rendering supports nested expressions (an expression whose default value is itself an expression) and
 * applies distinct CSS classes to each part: the {@code $} sigil, the curly braces, the expression name, the colon
 * separator, and the default value.
 */
public final class ExpressionBricks {

    /**
     * Renders a string value as a colour-coded expression element if it contains an expression, or as plain text
     * otherwise. Nested expressions are rendered recursively.
     *
     * @param value the string that may contain an expression
     * @return a span element with the rendered content
     */
    public static HTMLElement renderExpression(String value) {
        if (Expression.containsExpression(value)) {
            HTMLContainerBuilder<HTMLElement> span = span().css(halComponent(expression));
            internalRenderExpression(span, value);
            return span.element();
        } else {
            return span().text(value).element();
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private static void internalRenderExpression(HTMLContainerBuilder<HTMLElement> builder, String value) {
        String[] startExprEnd = startExpressionEnd(value);
        if (!startExprEnd[0].isEmpty()) {
            builder.add(span().css(halComponent(expression, start)).text(startExprEnd[0]));
        }
        String[] nameDefault = Expression.splitExpression(startExprEnd[1]);
        builder.add(span().css(halComponent(expression, dollar)))
                .add(span().css(halComponent(expression, curlyBraces, start)))
                .add(span().css(halComponent(expression, name)).text(nameDefault[0]));
        if (!nameDefault[1].isEmpty()) {
            builder.add(span().css(halComponent(expression, colon)));
            if (Expression.containsExpression(nameDefault[1])) {
                HTMLContainerBuilder<HTMLElement> nested = span().css(halComponent(expression));
                internalRenderExpression(nested, nameDefault[1]);
                builder.add(nested);
            } else {
                builder.add(span().css(halComponent(expression, defaultValue)).text(nameDefault[1]));
            }
        }
        builder.add(span().css(halComponent(expression, curlyBraces, end)));
        if (!startExprEnd[2].isEmpty()) {
            builder.add(span().css(halComponent(expression, end)).text(startExprEnd[2]));
        }
    }

    /** Returns a dollar-sign icon supplier, used to indicate expression editing mode. */
    public static Supplier<PredefinedIcon> expressionModeIcon() {
        return IconSets.fas::dollarSign;
    }

    /** Returns a terminal icon supplier, used to indicate normal (non-expression) editing mode. */
    public static Supplier<PredefinedIcon> normalModeIcon() {
        return IconSets.fas::terminal;
    }

    /** Returns a link icon supplier, used for the "resolve expression" action. */
    public static Supplier<PredefinedIcon> resolveExpressionIcon() {
        return IconSets.fas::link;
    }

    private ExpressionBricks() {
    }
}

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
package org.jboss.hal.op.task.statistics;

import java.util.List;
import java.util.Objects;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.jboss.hal.dmr.Expression.extractExpressions;
import static org.jboss.hal.dmr.ModelType.EXPRESSION;

/**
 * Holds the {@code statistics-enabled} attribute value for a single WildFly management resource. Tracks the resource's
 * address template, current attribute value, and whether the attribute supports expressions.
 */
class StatisticsEnabledState {

    /** The address template of the resource. */
    final AddressTemplate template;

    /** The current value of the {@code statistics-enabled} attribute. */
    final ModelNode value;

    /** Whether the {@code statistics-enabled} attribute allows expression values. */
    boolean expressionsAllowed;

    StatisticsEnabledState(AddressTemplate template, ModelNode value) {
        this.template = template;
        this.value = value;
        this.expressionsAllowed = false;
    }

    private StatisticsEnabledState(AddressTemplate template, ModelNode value, boolean expressionsAllowed) {
        this.template = template;
        this.value = value;
        this.expressionsAllowed = expressionsAllowed;
    }

    /** Returns a copy of this resource data with the given updated value. */
    StatisticsEnabledState copy(ModelNode value) {
        return new StatisticsEnabledState(template, value, expressionsAllowed);
    }

    /** Returns {@code true} if the current value is a DMR expression. */
    boolean isExpression() {
        return value.isDefined() && value.getType() == EXPRESSION;
    }

    /** Extracts and returns the expression keys from the value, or an empty list if the value is not an expression. */
    List<String> expressions() {
        if (isExpression()) {
            String[] expressions = extractExpressions(value.asString());
            if (expressions != null) {
                return asList(expressions);
            }
        }
        return emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {return false;}
        StatisticsEnabledState that = (StatisticsEnabledState) o;
        return Objects.equals(template, that.template);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(template);
    }
}

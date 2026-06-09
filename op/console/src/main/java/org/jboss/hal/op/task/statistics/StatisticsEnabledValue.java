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

/** Possible filter values for the {@code statistics-enabled} attribute in the statistics task UI. */
enum StatisticsEnabledValue {

    /** The attribute value is {@code true}. */
    true_("True", "true"),

    /** The attribute value is {@code false}. */
    false_("False", "false"),

    /** The attribute value is a DMR expression. */
    expression("Expression", "expression"),

    /** The attribute value is not a DMR expression. */
    noExpression("No expression", "no-expression");

    /** Unique identifier used for menu item registration and filter matching. */
    final String identifier;

    /** Display text shown in the multi-select dropdown. */
    final String text;

    /** Internal filter value string. */
    final String value;

    StatisticsEnabledValue(String text, String value) {
        this.identifier = StatisticsEnabledAttribute.NAME + "-" + value;
        this.text = text;
        this.value = value;
    }
}

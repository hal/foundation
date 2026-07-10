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
package org.jboss.hal.ui.resource.form;

/** Lists the input modes a {@link FormItem} can operate in: native controls, expression text input, or mixed. */
public enum FormItemInputMode {

    /** The form item uses its native control (switch, select, number input, etc.). */
    NATIVE,

    /** The form item shows a text input for entering WildFly expressions ({@code ${...}}). */
    EXPRESSION,

    /** The form item accepts both literal values and expressions in a single text input. */
    MIXED
}

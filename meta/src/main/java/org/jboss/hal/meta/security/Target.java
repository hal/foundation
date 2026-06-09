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
package org.jboss.hal.meta.security;

/**
 * The target type of a security {@link Constraint}: either an {@link #ATTRIBUTE} or an {@link #OPERATION}.
 */
public enum Target {
    /** Constraint targets a management operation. Symbol: {@code ":"} */
    OPERATION(":"),

    /** Constraint targets a management attribute. Symbol: {@code "@"} */
    ATTRIBUTE("@");

    static Target parse(String input) {
        if (OPERATION.symbol.equals(input)) {
            return OPERATION;
        } else if (ATTRIBUTE.symbol.equals(input)) {
            return ATTRIBUTE;
        } else {
            throw new IllegalArgumentException("Illegal symbol: '" + input + "'");
        }
    }

    /** The single-character symbol used in the constraint's string representation. */
    public final String symbol;

    Target(final String symbol) {
        this.symbol = symbol;
    }
}

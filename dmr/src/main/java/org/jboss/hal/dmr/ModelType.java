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

/**
 * Enumerates the types of values that a {@link ModelNode} can hold in the DMR type system. Each type is identified by a
 * single character used for binary serialization.
 */
public enum ModelType {

    /** Arbitrary-precision decimal number. */
    BIG_DECIMAL('d'),

    /** Arbitrary-precision integer. */
    BIG_INTEGER('i'),

    /** Boolean value ({@code true} or {@code false}). */
    BOOLEAN('Z'),

    /** Raw byte array. */
    BYTES('b'),

    /** Double-precision floating-point number. */
    DOUBLE('D'),

    /** An unresolved expression (e.g., {@code ${env.HOME}}). */
    EXPRESSION('e'),

    /** 32-bit signed integer. */
    INT('I'),

    /** Ordered list of {@link ModelNode} values. */
    LIST('l'),

    /** 64-bit signed integer. */
    LONG('J'),

    /** Unordered set of named {@link ModelNode} values (a map). */
    OBJECT('o'),

    /** A single name/value pair. */
    PROPERTY('p'),

    /** UTF-8 string value. */
    STRING('s'),

    /** A {@link ModelType} value itself (metatype). */
    TYPE('t'),

    /** No value defined. */
    UNDEFINED('u');

    /**
     * Returns the {@link ModelType} for the given serialization character.
     *
     * @param c the type character
     * @return the corresponding model type
     * @throws IllegalArgumentException if the character does not map to any type
     */
    public static ModelType forChar(char c) {
        switch (c) {
            case 'J':
                return LONG;
            case 'I':
                return INT;
            case 'Z':
                return BOOLEAN;
            case 's':
                return STRING;
            case 'D':
                return DOUBLE;
            case 'd':
                return BIG_DECIMAL;
            case 'i':
                return BIG_INTEGER;
            case 'b':
                return BYTES;
            case 'l':
                return LIST;
            case 't':
                return TYPE;
            case 'o':
                return OBJECT;
            case 'p':
                return PROPERTY;
            case 'e':
                return EXPRESSION;
            case 'u':
                return UNDEFINED;
            default:
                throw new IllegalArgumentException("Invalid type character '" + c + "'");
        }
    }

    /** The single character used for binary serialization of this type. */
    final char typeChar;

    ModelType(char typeChar) {
        this.typeChar = typeChar;
    }

    /** Returns the character used for binary serialization of this type. */
    public char getTypeChar() {
        return typeChar;
    }

    /** Returns {@code true} if this type represents a simple scalar value (not a collection, property, expression, or type). */
    public boolean simple() {
        return this == BOOLEAN || this == BIG_DECIMAL || this == BIG_INTEGER || this == INT || this == LONG || this == DOUBLE || this == STRING;
    }
}

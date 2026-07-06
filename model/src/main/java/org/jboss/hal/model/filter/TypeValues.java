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
package org.jboss.hal.model.filter;

import java.util.List;
import java.util.Objects;

import org.jboss.hal.dmr.ModelType;

import static java.util.stream.Collectors.joining;

/**
 * Represents a value type filter entry consisting of a DMR {@link ModelType} and a display label.
 */
public class TypeValues {

    /** Returns all predefined type filter values. */
    public static List<TypeValues> typeValues() {
        return List.of(
                new TypeValues("Boolean", ModelType.BOOLEAN),
                new TypeValues("Bytes", ModelType.BYTES),
                new TypeValues("Expression", ModelType.EXPRESSION),
                new TypeValues("Numeric", List.of(
                        ModelType.INT,
                        ModelType.LONG,
                        ModelType.DOUBLE,
                        ModelType.BIG_INTEGER,
                        ModelType.BIG_DECIMAL)),
                new TypeValues("List", ModelType.LIST),
                new TypeValues("Object", ModelType.OBJECT),
                new TypeValues("Property", ModelType.PROPERTY),
                new TypeValues("String", ModelType.STRING));
    }

    /** Human-readable display name. */
    public final String name;

    /** Unique identifier derived from the model type characters. */
    public final String identifier;

    /** The DMR model types this entry matches. */
    public final List<ModelType> types;

    /** Creates a new type value entry for a single model type. */
    public TypeValues(String name, ModelType type) {
        this(name, List.of(type));
    }

    /** Creates a new type value entry for multiple model types (e.g., all numeric types). */
    public TypeValues(String name, List<ModelType> types) {
        this.name = name;
        this.types = types;
        this.identifier = types.stream()
                .map(modelType -> String.valueOf(modelType.getTypeChar()))
                .collect(joining());
    }

    /** Returns a string like {@code Type(name:identifier)}. */
    @Override
    public String toString() {
        return "Type(" + name + ':' + identifier + ')';
    }

    /** Two type values are equal if they have the same name and identifier. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (!(o instanceof TypeValues)) {return false;}
        TypeValues that = (TypeValues) o;
        return Objects.equals(name, that.name) && Objects.equals(identifier, that.identifier);
    }

    /** Returns a hash code based on the name and identifier. */
    @Override
    public int hashCode() {
        return Objects.hash(name, identifier);
    }
}

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

import java.util.function.Function;

import org.patternfly.filter.FilterAttribute;

/**
 * Filter attribute matching the name of management model entries.
 */
public class NameAttribute<T> extends FilterAttribute<T, String> {

    /** Filter attribute name constant. */
    public static final String NAME = "name";

    /** Creates a new filter using the given function to extract the entry name. Matching is case-insensitive. */
    public NameAttribute(Function<T, String> nameFn) {
        super(NAME, (object, name) -> nameFn.apply(object).toLowerCase().contains(name.toLowerCase()));
    }
}

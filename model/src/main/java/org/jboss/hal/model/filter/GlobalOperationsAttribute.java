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

import org.jboss.hal.meta.description.OperationDescription;
import org.patternfly.filter.FilterAttribute;

/**
 * Filter attribute matching whether an operation is a global operation.
 */
public class GlobalOperationsAttribute<T> extends FilterAttribute<OperationDescription, Boolean> {

    /** Filter attribute name constant. */
    public static final String NAME = "global-operations";

    /** Creates a new filter that hides global operations when the filter value is {@code false}. */
    public GlobalOperationsAttribute() {
        super(NAME, (operation, value) -> value || !operation.global());
    }
}

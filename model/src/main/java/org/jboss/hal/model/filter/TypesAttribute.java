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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.patternfly.filter.FilterAttribute;

import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;

/**
 * Filter attribute matching the value type of management model entries.
 */
public class TypesAttribute<T> extends FilterAttribute<T, List<TypeValues>> {

    /** Filter attribute name constant. */
    public static final String NAME = "types";

    /** Creates a new filter using the given function to extract the model node containing the type. */
    public TypesAttribute(Function<T, ModelNode> modelNodeFn) {
        super(NAME, (object, types) -> {
            ModelType attributeType = modelNodeFn.apply(object).get(TYPE).asType();
            List<ModelType> modelTypes = new ArrayList<>();
            for (TypeValues type : types) {
                modelTypes.addAll(type.types);
            }
            return modelTypes.contains(attributeType);
        });
    }
}

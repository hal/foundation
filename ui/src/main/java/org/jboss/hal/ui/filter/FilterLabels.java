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
package org.jboss.hal.ui.filter;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.model.filter.AccessTypeAttribute;
import org.jboss.hal.model.filter.AccessTypeValue;
import org.jboss.hal.model.filter.DefinedAttribute;
import org.jboss.hal.model.filter.DeprecatedAttribute;
import org.jboss.hal.model.filter.ExpressionAttribute;
import org.jboss.hal.model.filter.ParametersAttribute;
import org.jboss.hal.model.filter.RequiredAttribute;
import org.jboss.hal.model.filter.ReturnValueAttribute;
import org.jboss.hal.model.filter.StorageAttribute;
import org.jboss.hal.model.filter.StorageValue;
import org.jboss.hal.model.filter.TypeValues;
import org.jboss.hal.model.filter.TypesAttribute;
import org.patternfly.component.label.Label;
import org.patternfly.filter.Filter;

import static org.patternfly.component.label.Label.label;
import static org.patternfly.filter.FilterAttributeModifier.collectionRemove;

public class FilterLabels {

    public static <T> List<Label> deReDeExLabels(Filter<T> filter) {
        List<Label> labels = new ArrayList<>();
        labels.addAll(booleanLabels(filter, DefinedAttribute.NAME, "Defined", "Undefined"));
        labels.addAll(requiredLabels(filter));
        labels.addAll(deprecatedLabels(filter));
        labels.addAll(expressionLabels(filter));
        return labels;
    }

    public static <T> List<Label> reDeExLabels(Filter<T> filter) {
        List<Label> labels = new ArrayList<>();
        labels.addAll(requiredLabels(filter));
        labels.addAll(deprecatedLabels(filter));
        labels.addAll(expressionLabels(filter));
        return labels;
    }

    public static <T> List<Label> requiredLabels(Filter<T> filter) {
        return booleanLabels(filter, RequiredAttribute.NAME, "Required", "Optional");
    }

    public static <T> List<Label> deprecatedLabels(Filter<T> filter) {
        return booleanLabels(filter, DeprecatedAttribute.NAME, "Deprecated", "Not deprecated");
    }

    public static <T> List<Label> expressionLabels(Filter<T> filter) {
        return booleanLabels(filter, ExpressionAttribute.NAME, "Expressions allowed", "No expressions allowed");
    }

    public static <T> List<Label> storageAccessTypeLabels(Filter<T> filter) {
        List<Label> labels = new ArrayList<>();
        labels.addAll(storageLabels(filter));
        labels.addAll(accessTypeLabels(filter));
        return labels;
    }

    public static <T> List<Label> storageLabels(Filter<T> filter) {
        List<Label> labels = new ArrayList<>();
        if (filter.defined(StorageAttribute.NAME)) {
            StorageValue storageValue = filter.<StorageValue>get(StorageAttribute.NAME).value();
            labels.add(label(storageValue.text)
                    .onClose((e, c) -> filter.reset(StorageAttribute.NAME)));
        }
        return labels;
    }

    public static <T> List<Label> accessTypeLabels(Filter<T> filter) {
        List<Label> labels = new ArrayList<>();
        if (filter.defined(AccessTypeAttribute.NAME)) {
            AccessTypeValue accessTypeValue = filter.<AccessTypeValue>get(AccessTypeAttribute.NAME).value();
            labels.add(label(accessTypeValue.text)
                    .onClose((e, c) -> filter.reset(AccessTypeAttribute.NAME)));
        }
        return labels;
    }

    public static <T> List<Label> typeLabels(Filter<T> filter) {
        List<Label> labels = new ArrayList<>();
        if (filter.defined(TypesAttribute.NAME)) {
            List<TypeValues> value = filter.<List<TypeValues>>get(TypesAttribute.NAME).value();
            for (TypeValues type : value) {
                labels.add(label(type.name).onClose((event, chip) ->
                        filter.set(TypesAttribute.NAME, List.of(type), collectionRemove(ArrayList::new))));
            }
        }
        return labels;
    }

    public static <T> List<Label> parametersReturnValueLabels(Filter<T> filter) {
        List<Label> labels = new ArrayList<>();
        labels.addAll(booleanLabels(filter, ParametersAttribute.NAME, "Parameters", "No parameters"));
        labels.addAll(booleanLabels(filter, ReturnValueAttribute.NAME, "Return value", "No return value"));
        return labels;
    }

    // ------------------------------------------------------ internal

    private static <T> List<Label> booleanLabels(Filter<T> filter, String filterAttribute, String true_, String false_) {
        List<Label> labels = new ArrayList<>();
        if (filter.defined(filterAttribute)) {
            Boolean value = filter.<Boolean>get(filterAttribute).value();
            labels.add(label(value ? true_ : false_).onClose((e, c) -> filter.reset(filterAttribute)));
        }
        return labels;
    }
}

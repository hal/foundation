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

import java.util.HashSet;
import java.util.List;

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.ui.resource.pipeline.ResolvedAttribute;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;

/**
 * Shared helper methods for {@link StringListFormItem} and {@link CapabilityReferencesFormItem} that handle modification
 * detection, default value extraction, and model node conversion for string list attributes.
 */
public class StringListSupport {

    static boolean isNewModified(ResolvedAttribute ra, List<String> values) {
        if (ra.description().hasDefault()) {
            return differentValues(defaultValues(ra), values);
        } else {
            return !values.isEmpty();
        }
    }

    static boolean isExistingModified(ResolvedAttribute ra, List<String> values, boolean wasDefined) {
        if (wasDefined) {
            return ra.expression() || differentValues(modelValues(ra), values);
        } else {
            return !values.isEmpty();
        }
    }

    /** Converts a list of string values to a DMR list model node, or returns an undefined node if the list is empty. */
    static ModelNode valuesModelNode(List<String> values) {
        if (values.isEmpty()) {
            return new ModelNode();
        } else {
            ModelNode modelNode = new ModelNode();
            for (String value : values) {
                modelNode.add(value);
            }
            return modelNode;
        }
    }

    static List<String> modelValues(ResolvedAttribute ra) {
        if (ra.value().isDefined()) {
            return ra.value().asList().stream()
                    .map(ModelNode::asString)
                    .collect(toList());
        }
        return emptyList();
    }

    static List<String> defaultValues(ResolvedAttribute ra) {
        if (ra.description().hasDefined(ModelDescriptionConstants.DEFAULT)) {
            return ra.description().get(DEFAULT).asList().stream()
                    .map(ModelNode::asString)
                    .collect(toList());
        }
        return emptyList();
    }

    static boolean differentValues(List<String> a, List<String> b) {
        if (a.isEmpty() && b.isEmpty()) {
            return false;
        }
        if (a.size() != b.size()) {
            return true;
        }
        return !new HashSet<>(a).equals(new HashSet<>(b));
    }
}

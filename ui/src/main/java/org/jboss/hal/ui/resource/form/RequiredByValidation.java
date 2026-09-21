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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jboss.hal.core.Humanize.sentenceCase;

/**
 * Form-level validation that enforces {@code requires} relationships. If attribute A declares {@code requires: [B]}, and A has a
 * non-empty value, then B must also have a non-empty value.
 * <p>
 * This validation builds a reverse mapping: for each required target attribute, it collects all attributes that require it. If
 * any requiring attribute is non-empty and the target is empty, validation fails.
 */
final class RequiredByValidation implements FormValidation {

    private final String targetName;
    private final Set<String> requiredBy;

    RequiredByValidation(String targetName, Set<String> requiredBy) {
        this.targetName = targetName;
        this.requiredBy = requiredBy;
    }

    @Override
    public Result validate(List<FormItem> items) {
        FormItem targetItem = FormValidation.findItem(items, targetName);
        if (targetItem == null) {
            return null;
        }

        List<String> nonEmptyRequirers = new ArrayList<>();
        for (String requirerName : requiredBy) {
            FormItem requirerItem = FormValidation.findItem(items, requirerName);
            if (requirerItem != null && !FormValidation.isEmptyOrDefault(requirerItem)) {
                nonEmptyRequirers.add(requirerName);
            }
        }

        if (!nonEmptyRequirers.isEmpty() && FormValidation.isEmptyOrDefault(targetItem)) {
            String message = sentenceCase(targetName) + " is required when " +
                    sentenceCase(nonEmptyRequirers, "or") + " is set.";
            Map<String, String> itemErrors = new HashMap<>();
            itemErrors.put(targetName, "Required because " +
                    sentenceCase(nonEmptyRequirers, "or") + " is set.");
            return new Result(message, itemErrors);
        }
        return null;
    }

    /**
     * Scans all form items for {@code requires} relationships and creates one {@link RequiredByValidation} per target attribute.
     */
    static List<FormValidation> fromItems(List<FormItem> items) {
        Map<String, Set<String>> reverseMap = new HashMap<>();
        for (FormItem item : items) {
            List<String> requires = item.attribute().description().requires();
            for (String targetName : requires) {
                FormItem targetItem = FormValidation.findItem(items, targetName);
                if (targetItem != null && !targetItem.attribute().description().required()) {
                    reverseMap.computeIfAbsent(targetName, k -> new HashSet<>())
                            .add(item.attribute().name());
                }
            }
        }

        List<FormValidation> validations = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : reverseMap.entrySet()) {
            validations.add(new RequiredByValidation(entry.getKey(), entry.getValue()));
        }
        return validations;
    }
}

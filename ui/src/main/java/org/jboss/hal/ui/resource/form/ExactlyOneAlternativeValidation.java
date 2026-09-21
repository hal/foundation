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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.jboss.hal.core.Humanize.sentenceCase;

/**
 * Form-level validation that enforces exactly one attribute from a set of required alternatives must be defined. This applies
 * only to alternatives groups where more than one member is marked as {@code required} in the management model metadata.
 */
final class ExactlyOneAlternativeValidation implements FormValidation {

    private final Set<String> requiredAlternatives;

    ExactlyOneAlternativeValidation(Set<String> requiredAlternatives) {
        this.requiredAlternatives = requiredAlternatives;
    }

    @Override
    public Result validate(List<FormItem> items) {
        List<String> emptyItems = new ArrayList<>();
        for (String name : requiredAlternatives) {
            FormItem item = FormValidation.findItem(items, name);
            if (item == null || FormValidation.isEmpty(item)) {
                emptyItems.add(name);
            }
        }
        if (emptyItems.size() == requiredAlternatives.size()) {
            String message = "One of " + sentenceCase(new ArrayList<>(requiredAlternatives), "or") +
                    " must be defined.";
            Map<String, String> itemErrors = new HashMap<>();
            String errorText = "One of " + sentenceCase(new ArrayList<>(requiredAlternatives), "or") +
                    " must be defined.";
            for (String name : emptyItems) {
                itemErrors.put(name, errorText);
            }
            return new Result(message, itemErrors);
        }
        return null;
    }

    /**
     * Scans all form items for alternatives groups where more than one member is required, and creates one validation per such
     * group.
     */
    static List<FormValidation> fromItems(List<FormItem> items) {
        List<Set<String>> processedGroups = new ArrayList<>();
        List<FormValidation> validations = new ArrayList<>();

        for (FormItem item : items) {
            List<String> alts = item.attribute().description().alternatives();
            if (!alts.isEmpty()) {
                Set<String> group = new TreeSet<>(alts);
                group.add(item.attribute().name());
                if (!processedGroups.contains(group)) {
                    processedGroups.add(group);

                    Set<String> requiredMembers = group.stream()
                            .filter(name -> {
                                FormItem member = FormValidation.findItem(items, name);
                                return member != null && member.attribute().description().required();
                            })
                            .collect(Collectors.toCollection(TreeSet::new));

                    if (requiredMembers.size() > 1) {
                        validations.add(new ExactlyOneAlternativeValidation(requiredMembers));
                    }
                }
            }
        }
        return validations;
    }
}

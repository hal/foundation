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

import static org.jboss.hal.core.Humanize.sentenceCase;

/**
 * Form-level validation that enforces at most one attribute from a set of alternatives may have a non-empty, non-default value.
 * Alternatives are declared in the WildFly management model metadata via the {@code alternatives} field.
 */
final class NotMoreThanOneAlternativeValidation implements FormValidation {

    private final Set<String> alternatives;

    NotMoreThanOneAlternativeValidation(Set<String> alternatives) {
        this.alternatives = alternatives;
    }

    @Override
    public Result validate(List<FormItem> items) {
        List<String> nonEmpty = new ArrayList<>();
        for (String name : alternatives) {
            FormItem item = FormValidation.findItem(items, name);
            if (item != null && !FormValidation.isEmptyOrDefault(item)) {
                nonEmpty.add(name);
            }
        }
        if (nonEmpty.size() > 1) {
            String message = "Only one of " + sentenceCase(new ArrayList<>(alternatives), "or") +
                    " may be set at the same time.";
            Map<String, String> itemErrors = new HashMap<>();
            for (String name : nonEmpty) {
                List<String> others = new ArrayList<>(nonEmpty);
                others.remove(name);
                itemErrors.put(name, "Mutually exclusive with " +
                        sentenceCase(others, "and") + ".");
            }
            return new Result(message, itemErrors);
        }
        return null;
    }

    /**
     * Scans all form items for {@code alternatives} relationships and creates one validation per unique alternatives group.
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
                    validations.add(new NotMoreThanOneAlternativeValidation(group));
                }
            }
        }
        return validations;
    }
}

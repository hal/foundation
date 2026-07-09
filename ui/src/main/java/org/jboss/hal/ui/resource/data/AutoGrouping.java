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
package org.jboss.hal.ui.resource.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.hal.ui.resource.ResourceAttribute;

/**
 * Groups attributes into alphabetical letter-range chunks (e.g. "A – D", "E – H"). Used for resources that have many
 * attributes but no metadata-defined attribute groups. Attributes starting with the same letter are never split across groups.
 */
class AutoGrouping implements GroupingStrategy {

    static final int AUTO_GROUPING_THRESHOLD = 20;
    private static final int TARGET_GROUP_SIZE = 10;

    @Override
    public Map<String, List<ResourceAttribute>> group(List<ResourceAttribute> attributes) {
        int size = attributes.size();
        int numberOfGroups = (int) Math.ceil((double) size / TARGET_GROUP_SIZE);
        int targetSize = (int) Math.ceil((double) size / numberOfGroups);

        LinkedHashMap<String, List<ResourceAttribute>> result = new LinkedHashMap<>();
        List<ResourceAttribute> currentChunk = new ArrayList<>();
        int i = 0;

        while (i < size) {
            currentChunk.add(attributes.get(i));
            i++;

            // only cut when we've reached the target size AND the next attribute starts a different letter
            if (currentChunk.size() >= targetSize && i < size) {
                char lastChar = firstChar(currentChunk.get(currentChunk.size() - 1));
                char nextChar = firstChar(attributes.get(i));
                if (lastChar != nextChar) {
                    result.put(groupName(currentChunk), currentChunk);
                    currentChunk = new ArrayList<>();
                }
                // same letter: keep adding to the current chunk until the letter changes
            }
        }

        if (!currentChunk.isEmpty()) {
            result.put(groupName(currentChunk), currentChunk);
        }

        return result;
    }

    private String groupName(List<ResourceAttribute> chunk) {
        char first = firstChar(chunk.get(0));
        char last = firstChar(chunk.get(chunk.size() - 1));
        if (first == last) {
            return String.valueOf(first);
        }
        return first + " – " + last;
    }

    // use fqn so nested attributes like "credential-reference.store" sort by their root name
    private char firstChar(ResourceAttribute attribute) {
        return Character.toUpperCase(attribute.fqn.charAt(0));
    }
}

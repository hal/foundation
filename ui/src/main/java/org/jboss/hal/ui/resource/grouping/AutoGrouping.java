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
package org.jboss.hal.ui.resource.grouping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.jboss.hal.core.Humanize.abbreviate;

/**
 * Groups items into alphabetical chunks based on first letter boundaries. Used for resources that have many items but no
 * metadata-defined attribute groups. Items starting with the same letter are never split across groups. Group names use
 * abbreviated attribute labels (e.g. "Allow EJB … – Disable default …").
 */
public class AutoGrouping {

    public static final int AUTO_GROUPING_THRESHOLD = 20;
    private static final int TARGET_GROUP_SIZE = 10;
    private static final int MAX_LABEL_LENGTH = 20;

    public static <T> Map<String, List<T>> group(List<T> items, Function<T, String> fqnFn, Function<T, String> labelFn) {
        int size = items.size();
        int numberOfGroups = (int) Math.ceil((double) size / TARGET_GROUP_SIZE);
        int targetSize = (int) Math.ceil((double) size / numberOfGroups);

        LinkedHashMap<String, List<T>> result = new LinkedHashMap<>();
        List<T> currentChunk = new ArrayList<>();
        int i = 0;

        while (i < size) {
            currentChunk.add(items.get(i));
            i++;
            if (currentChunk.size() >= targetSize && i < size) {
                char lastChar = firstChar(currentChunk.get(currentChunk.size() - 1), fqnFn);
                char nextChar = firstChar(items.get(i), fqnFn);
                if (lastChar != nextChar) {
                    result.put(groupName(currentChunk, labelFn), currentChunk);
                    currentChunk = new ArrayList<>();
                }
            }
        }

        if (!currentChunk.isEmpty()) {
            result.put(groupName(currentChunk, labelFn), currentChunk);
        }

        return result;
    }

    private static <T> String groupName(List<T> chunk, Function<T, String> labelFn) {
        String first = abbreviate(labelFn.apply(chunk.get(0)), MAX_LABEL_LENGTH);
        String last = abbreviate(labelFn.apply(chunk.get(chunk.size() - 1)), MAX_LABEL_LENGTH);
        if (first.equals(last)) {
            return first;
        }
        return first + " – " + last;
    }

    private static <T> char firstChar(T item, Function<T, String> fqnFn) {
        return Character.toUpperCase(fqnFn.apply(item).charAt(0));
    }
}

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
import java.util.TreeMap;

import org.jboss.hal.ui.resource.ResourceItem;

import static org.jboss.hal.core.Humanize.sentenceCase;

/**
 * Shared grouping logic for resource items (view items and form items). Handles metadata-based grouping, auto-grouping for
 * large item sets, and the decision between flat and grouped layouts.
 *
 * @see AutoGrouping
 * @see ResourceItem
 */
public final class GroupingSupport {

    public static final String UNGROUPED = "ungrouped";

    /** Returns {@code true} if any item has a metadata-defined attribute group. */
    public static <T extends ResourceItem> boolean hasGroups(List<T> items) {
        for (T item : items) {
            if (item.attribute().description().group() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves the grouping strategy for the given items. Returns {@code null} if items should be rendered flat (either
     * grouping is disabled or no grouping strategy applies). Otherwise, returns a map of group name to items, with the
     * {@link #UNGROUPED} key first (if present) followed by alphabetically sorted named groups.
     */
    public static <T extends ResourceItem> Map<String, List<T>> resolveGroups(List<T> items, boolean grouped) {
        if (grouped) {
            if (hasGroups(items)) {
                return groupByMetadata(items);
            } else if (items.size() >= AutoGrouping.AUTO_GROUPING_THRESHOLD) {
                return AutoGrouping.group(items,
                        item -> item.attribute().fqn(),
                        item -> sentenceCase(item.attribute().description().parent() != null
                                ? item.attribute().description().parent().name()
                                : item.attribute().description().name()));
            }
        }
        return null;
    }

    /** Partitions items by their metadata-defined group name. Ungrouped items come first, named groups are sorted. */
    static <T extends ResourceItem> Map<String, List<T>> groupByMetadata(List<T> items) {
        List<T> ungrouped = new ArrayList<>();
        TreeMap<String, List<T>> namedGroups = new TreeMap<>();
        for (T item : items) {
            String grp = item.attribute().description().group();
            if (grp == null) {
                ungrouped.add(item);
            } else {
                namedGroups.computeIfAbsent(grp, k -> new ArrayList<>()).add(item);
            }
        }
        LinkedHashMap<String, List<T>> result = new LinkedHashMap<>();
        if (!ungrouped.isEmpty()) {
            result.put(UNGROUPED, ungrouped);
        }
        result.putAll(namedGroups);
        return result;
    }

    private GroupingSupport() {
    }
}

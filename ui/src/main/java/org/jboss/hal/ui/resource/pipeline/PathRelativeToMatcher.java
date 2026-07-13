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
package org.jboss.hal.ui.resource.pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.hal.meta.description.AttributeDescription;

import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;

/**
 * Sibling matcher that claims pairs of top-level {@code path} + {@code relative-to} attributes. These are two separate STRING
 * attributes that are semantically coupled and should be rendered as one unit.
 * <p>
 * The matcher handles multiple naming variants by extracting a shared prefix:
 * <ul>
 *     <li>{@code path} + {@code relative-to} — most common (27 occurrences)</li>
 *     <li>{@code keystore-path} + {@code keystore-relative-to} — audit syslog TLS</li>
 *     <li>{@code object-store-path} + {@code object-store-relative-to} — transactions</li>
 *     <li>{@code directory} + {@code relative-to} — undertow access-log exception</li>
 * </ul>
 */
class PathRelativeToMatcher implements AttributeMatcher {

    private static final String RELATIVE_TO_SUFFIX = RELATIVE_TO;
    private static final String DIRECTORY = "directory";

    @Override
    public MatchResult match(List<AttributeDescription> pool) {
        List<AttributeGroup> groups = new ArrayList<>();
        Set<String> claimed = new HashSet<>();

        for (AttributeDescription ad : pool) {
            if (!ad.name().endsWith(RELATIVE_TO_SUFFIX)) {
                continue;
            }
            if (claimed.contains(ad.name())) {
                continue;
            }

            String prefix = ad.name().substring(0, ad.name().length() - RELATIVE_TO_SUFFIX.length());
            String siblingPathName = prefix.isEmpty() ? "path" : prefix + "path";

            AttributeDescription pathAttr = findInPool(pool, siblingPathName);
            if (pathAttr == null && prefix.isEmpty()) {
                pathAttr = findInPool(pool, DIRECTORY);
            }

            if (pathAttr != null && !claimed.contains(pathAttr.name())) {
                groups.add(AttributeGroup.of(pathAttr.name(),
                        Arrays.asList(pathAttr, ad)));
                claimed.add(pathAttr.name());
                claimed.add(ad.name());
            }
        }

        List<AttributeDescription> remaining = new ArrayList<>();
        for (AttributeDescription ad : pool) {
            if (!claimed.contains(ad.name())) {
                remaining.add(ad);
            }
        }

        return new MatchResult(groups, remaining);
    }

    private AttributeDescription findInPool(List<AttributeDescription> pool, String name) {
        for (AttributeDescription ad : pool) {
            if (ad.name().equals(name)) {
                return ad;
            }
        }
        return null;
    }
}

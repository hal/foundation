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
package org.jboss.hal.meta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.reverse;
import static org.jboss.hal.meta.WildcardResolver.Direction.RTL;

/**
 * A {@link TemplateResolver} that resolves wildcard segments ({@code *}) in address templates.
 */
public class WildcardResolver implements TemplateResolver {

    /** Direction in which wildcards are resolved: left-to-right or right-to-left. */
    public enum Direction {
        /** Resolve wildcards from left to right. */
        LTR,
        /** Resolve wildcards from right to left. */
        RTL
    }

    private final Direction direction;
    private final Iterator<String> iterator;

    public WildcardResolver(Direction direction, String first, String... more) {
        this.direction = direction;
        List<String> values = new ArrayList<>();
        if (valid(first)) {
            values.add(first);
        }
        if (more != null) {
            for (String value : more) {
                if (valid(value)) {
                    values.add(value);
                }
            }
        }
        iterator = values.iterator();
    }

    private boolean valid(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Override
    public AddressTemplate resolve(AddressTemplate template) {
        List<Segment> resolved = new ArrayList<>();
        List<Segment> segments = new ArrayList<>(template.segments());
        if (direction == RTL) {
            reverse(segments);
        }
        for (Segment segment : segments) {
            if (!segment.containsPlaceholder() && segment.hasKey() && "*".equals(segment.value)) {
                if (iterator.hasNext()) {
                    resolved.add(new Segment(segment.key, iterator.next()));
                } else {
                    resolved.add(segment);
                }
            } else {
                resolved.add(segment);
            }
        }
        if (direction == RTL) {
            reverse(resolved);
        }
        return AddressTemplate.of(resolved);
    }
}

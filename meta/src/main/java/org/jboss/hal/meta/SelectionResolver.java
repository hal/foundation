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
import java.util.List;
import java.util.function.Supplier;

import static org.jboss.hal.meta.Placeholder.SELECTED_RESOURCE;

/**
 * A {@link TemplateResolver} that resolves the {@code {selection}} placeholder to the currently selected resource name.
 */
public class SelectionResolver implements TemplateResolver {

    private final Supplier<String> selection;

    public SelectionResolver(Supplier<String> selection) {
        if (selection == null) {
            throw new IllegalArgumentException("Selection provider must not be null");
        }
        this.selection = selection;
    }

    @Override
    public AddressTemplate resolve(AddressTemplate template) {
        List<Segment> resolved = new ArrayList<>();
        for (Segment segment : template) {
            if (segment.containsPlaceholder() && segment.hasKey() && SELECTED_RESOURCE.equals(segment.placeholder())) {
                resolved.add(new Segment(segment.key, selection.get()));
            } else {
                resolved.add(segment);
            }
        }
        return AddressTemplate.of(resolved);
    }
}

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

/**
 * A {@link TemplateResolver} that resolves placeholders using the values stored in the {@link StatementContext}.
 */
public class StatementContextResolver implements TemplateResolver {

    private final StatementContext statementContext;

    public StatementContextResolver(StatementContext statementContext) {
        if (statementContext == null) {
            throw new IllegalArgumentException("Statement context must not be null");
        }
        this.statementContext = statementContext;
    }

    @Override
    public AddressTemplate resolve(AddressTemplate template) {
        List<Segment> resolved = new ArrayList<>();
        for (Segment segment : template) {
            if (segment.containsPlaceholder()) {
                Placeholder segmentPlaceholder = segment.placeholder();
                if (statementContext.environment.standalone() && segmentPlaceholder.domainOnly) {
                    continue;
                }
                Placeholder statementPlaceholder = statementContext.placeholder(segmentPlaceholder.name);
                if (statementPlaceholder != null) {
                    String resolvedValue = statementContext.value(statementPlaceholder);
                    if (resolvedValue != null) {
                        if (segment.hasKey()) {
                            // key={placeholder}
                            resolved.add(new Segment(segment.key, resolvedValue));
                        } else {
                            // {placeholder}
                            resolved.add(new Segment(statementPlaceholder.resource, resolvedValue));
                        }
                    } else {
                        resolved.add(segment);
                    }
                } else {
                    resolved.add(segment);
                }
            } else {
                resolved.add(segment);
            }
        }
        return AddressTemplate.of(resolved);
    }
}

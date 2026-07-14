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

import java.util.List;

import org.jboss.hal.meta.description.AttributeDescription;

import static org.jboss.hal.ui.resource.pipeline.AttributeMatcher.hasSimpleValueType;
import static org.jboss.hal.ui.resource.pipeline.AttributeMatcher.partition;

/**
 * Matcher that claims OBJECT attributes with a simple VALUE_TYPE (STRING, INT, LONG, etc.) — free-form key-value maps.
 * These are arbitrary {@code {String → String}} maps like {@code properties}, {@code params}, {@code configuration}, etc.
 * <p>
 * Must run after all composite matchers (which claim known structured OBJECTs) and before {@link FlatteningProvider}
 * (which handles simpleRecord OBJECTs with structured VALUE_TYPE).
 */
class MapMatcher implements AttributeMatcher {

    @Override
    public MatchResult match(List<AttributeDescription> pool) {
        return partition(pool, ad -> hasSimpleValueType(ad));
    }
}

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

import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;
import static org.jboss.hal.ui.resource.pipeline.AttributeMatcher.hasObjectValueType;
import static org.jboss.hal.ui.resource.pipeline.AttributeMatcher.partition;

/**
 * Composite matcher that claims OBJECT attributes with the file structure: a value-type containing {@code path} (STRING)
 * and {@code relative-to} (STRING) sub-attributes. Covers the {@code file} attribute in logging subsystem resources.
 * <p>
 * This matcher runs after {@link CredentialReferenceMatcher} and {@link TimeUnitMatcher} in the priority chain, so those
 * patterns are already claimed and cannot produce false positives here.
 */
class FileMatcher implements AttributeMatcher {

    @Override
    public MatchResult match(List<AttributeDescription> pool) {
        return partition(pool, ad -> hasObjectValueType(ad, PATH, RELATIVE_TO));
    }
}

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

import static org.jboss.hal.dmr.ModelDescriptionConstants.ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAR_TEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORE;
import static org.jboss.hal.ui.resource.pipeline.AttributeMatcher.hasObjectValueType;
import static org.jboss.hal.ui.resource.pipeline.AttributeMatcher.partition;

/**
 * Composite matcher that claims OBJECT attributes with the credential reference structure: a value-type containing
 * {@code store}, {@code alias}, and {@code clear-text} sub-attributes.
 * <p>
 * Covers all 9 name variants across the WildFly management model (credential-reference, shared-secret-reference,
 * key-credential-reference, etc.).
 */
class CredentialReferenceMatcher implements AttributeMatcher {

    @Override
    public MatchResult match(List<AttributeDescription> pool) {
        return partition(pool, ad -> hasObjectValueType(ad, STORE, ALIAS, CLEAR_TEXT));
    }
}

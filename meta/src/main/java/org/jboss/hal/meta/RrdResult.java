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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;

/**
 * Accumulates the results of parsing {@code read-resource-description} responses: resource descriptions, security
 * contexts, and processed address mappings.
 */
class RrdResult {

    final Map<String, ResourceDescription> resourceDescriptions;
    final Map<String, SecurityContext> securityContexts;
    final Map<String, Set<String>> processedAddresses;

    RrdResult() {
        resourceDescriptions = new HashMap<>();
        securityContexts = new HashMap<>();
        processedAddresses = new HashMap<>();
    }

    boolean noResourceDescription(String address) {
        return !resourceDescriptions.containsKey(address);
    }

    void addResourceDescription(String address, ResourceDescription resourceDescription) {
        if (noResourceDescription(address)) {
            resourceDescriptions.put(address, resourceDescription);
        }
    }

    boolean noSecurityContext(String address) {
        return !securityContexts.containsKey(address);
    }

    void addSecurityContext(String address, SecurityContext securityContext) {
        if (noSecurityContext(address)) {
            securityContexts.put(address, securityContext);
        }
    }

    boolean shouldUpdate() {
        return !resourceDescriptions.isEmpty() || !processedAddresses.isEmpty();
    }
}

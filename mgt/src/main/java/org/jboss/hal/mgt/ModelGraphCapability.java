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
package org.jboss.hal.mgt;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.elemento.logger.Logger;

import elemental2.dom.RequestInit;
import elemental2.promise.Promise;

import static elemental2.dom.DomGlobal.fetch;

/**
 * Fetches capability reference data from the MGT capability-references API.
 */
@ApplicationScoped
public class ModelGraphCapability {

    private static final Logger logger = Logger.getLogger(ModelGraphCapability.class.getName());

    private final ModelGraphEndpoint endpoint;

    @Inject
    public ModelGraphCapability(ModelGraphEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Fetches the resources that reference a given capability from the MGT capability-references API. Returns a promise that
     * resolves to the array of capability references or an empty array if the request fails.
     */
    public Promise<CapabilityReference[]> capabilityReferences(String capability) {
        String url = endpoint.api().append("capability/").append(capability).append("/references").toString();

        RequestInit init = RequestInit.create();
        init.setMethod("GET");
        init.setMode("cors");

        logger.debug("Fetching capability references for '%s' at %s", capability, url);
        return fetch(url, init)
                .then(response -> {
                    if (response.ok) {
                        return response.json()
                                .then(json -> {
                                    CapabilityReferencesResponse crr = (CapabilityReferencesResponse) json;
                                    CapabilityReference[] references = crr.references;
                                    logger.debug("Model graph returned %d references for capability '%s'",
                                            references != null ? references.length : 0, capability);
                                    return Promise.resolve(references != null ? references : new CapabilityReference[0]);
                                })
                                .catch_(error -> {
                                    logger.error("Failed to parse capability references response for '%s': %s",
                                            capability, String.valueOf(error));
                                    return Promise.resolve(new CapabilityReference[0]);
                                });
                    } else {
                        logger.error("Capability references request for '%s' failed: %d", capability, response.status);
                        return Promise.resolve(new CapabilityReference[0]);
                    }
                })
                .catch_(error -> {
                    logger.error("Capability references request for '%s' failed: %s", capability, error);
                    return Promise.resolve(new CapabilityReference[0]);
                });
    }
}

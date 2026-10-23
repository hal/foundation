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
package org.jboss.hal.core.mgt;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.elemento.logger.Logger;
import org.jboss.hal.env.Environment;

import elemental2.dom.RequestInit;
import elemental2.promise.Promise;

import static elemental2.dom.DomGlobal.fetch;

/**
 * Manages connectivity to the MGT sidecar container. Handles availability checks and ping.
 *
 * <p>The MGT container port is derived from the WildFly product version: {@code 7000 + major * 10 + minor}. For example,
 * WildFly 41.0 maps to port 7410, and the REST API is reachable at {@code http://localhost:7410/api/}.
 */
@ApplicationScoped
public class ModelGraphConnection {

    private static final Logger logger = Logger.getLogger(ModelGraphConnection.class.getName());

    private final Environment environment;
    private final ModelGraphEndpoint endpoint;
    private boolean available;

    @Inject
    public ModelGraphConnection(Environment environment, ModelGraphEndpoint endpoint) {
        this.environment = environment;
        this.endpoint = endpoint;
        this.available = false;
    }

    /** Returns whether a matching MGT container was found during the last {@link #ping()} check. */
    public boolean available() {
        return available;
    }

    /**
     * Checks whether a matching MGT container is running by sending a GET request to the {@code /api/identity} endpoint.
     * Updates the {@link #available()} flag and returns a promise that resolves to {@code true} if the container responds with
     * a 200 status, or {@code false} otherwise.
     */
    public Promise<Boolean> ping() {
        String url = endpoint.api().append("identity").toString();

        RequestInit init = RequestInit.create();
        init.setMethod("GET");
        init.setMode("cors");

        logger.debug("Checking model graph availability at %s", url);
        return fetch(url, init)
                .then(response -> {
                    if (response.ok) {
                        return response.json()
                                .then(identity -> {
                                    logger.info("Model graph for WildFly %s available: %o",
                                            environment.productVersion(), identity);
                                    available = true;
                                    return Promise.resolve(true);
                                })
                                .catch_(error -> {
                                    logger.error("Failed to parse model graph identity for WildFly %s: %s",
                                            environment.productVersion(), String.valueOf(error));
                                    available = false;
                                    return Promise.resolve(false);
                                });
                    } else {
                        logger.info("Model graph for WildFly %s not available: %d",
                                environment.productVersion(), response.status);
                        available = false;
                        return Promise.resolve(false);
                    }
                })
                .catch_(error -> {
                    logger.info("Model graph for WildFly %s not available: %s",
                            environment.productVersion(), error);
                    available = false;
                    return Promise.resolve(false);
                });
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

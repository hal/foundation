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
package org.jboss.hal.op.mgt;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.elemento.logger.Logger;
import org.jboss.hal.env.Environment;

import elemental2.dom.RequestInit;
import elemental2.promise.Promise;

import static elemental2.dom.DomGlobal.fetch;

/**
 * CDI service for interacting with the model graph tools (MGT) sidecar container. The MGT container provides a REST API backed
 * by a Neo4j graph database containing the static metadata of a specific WildFly release.
 *
 * <p>The MGT container port is derived from the WildFly product version: {@code 7000 + major * 10 + minor}. For example,
 * WildFly 41.0 maps to port 7410, and the REST API is reachable at {@code http://localhost:7410/api/}.
 */
@ApplicationScoped
public class ModelGraphTools {

    private static final Logger logger = Logger.getLogger(ModelGraphTools.class.getName());
    private static final int HTTP_PORT_BASE = 7000;

    private final Environment environment;

    @Inject
    public ModelGraphTools(Environment environment) {
        this.environment = environment;
    }

    /**
     * Checks whether a matching MGT container is running by sending a GET request to the {@code /api/identity} endpoint.
     * Returns a promise that resolves to {@code true} if the container responds with a 200 status, or {@code false} otherwise.
     */
    public Promise<Boolean> ping() {
        int port = HTTP_PORT_BASE + environment.productVersion().major() * 10 + environment.productVersion().minor();
        String url = "http://localhost:" + port + "/api/identity";

        RequestInit init = RequestInit.create();
        init.setMethod("GET");
        init.setMode("cors");

        logger.debug("Checking model graph tools availability at %s", url);
        return fetch(url, init)
                .then(response -> {
                    if (response.ok) {
                        response.json()
                                .then(identity -> {
                                    logger.info("Model graph tools for WildFly %s available: %o",
                                            environment.productVersion(), identity);
                                    return Promise.resolve(true);
                                })
                                .catch_(error -> {
                                    logger.error("Failed to parse model graph tools identity for WildFly %s: %s",
                                            environment.productVersion(), String.valueOf(error));
                                    return Promise.reject(error);
                                });
                    } else {
                        logger.info("Model graph tools for WildFly %s not available: %d",
                                environment.productVersion(), response.status);
                    }
                    return Promise.resolve(false);
                })
                .catch_(error -> {
                    logger.info("Model graph tools for WildFly %s not available: %s",
                            environment.productVersion(), error);
                    return Promise.reject(error);
                });
    }
}

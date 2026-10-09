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
import org.jboss.hal.env.Version;

import elemental2.dom.RequestInit;
import elemental2.promise.Promise;

import static elemental2.dom.DomGlobal.fetch;
import static org.jboss.hal.resources.Urls.MODEL_GRAPH_TOOLS_IDENTITY;
import static org.jboss.hal.resources.Urls.MODEL_GRAPH_TOOLS_SEARCH;
import static org.jboss.hal.resources.Urls.replaceVersion;

/**
 * Service for interacting with the model graph tools (MGT) sidecar container. The MGT container provides a REST API backed by a
 * Neo4j graph database containing the static metadata of a specific WildFly release.
 *
 * <p>The MGT container port is derived from the WildFly product version: {@code 7000 + major * 10 + minor}. For example,
 * WildFly 41.0 maps to port 7410, and the REST API is reachable at {@code http://localhost:7410/api/}.
 */
@ApplicationScoped
public class ModelGraphTools {

    private static final Logger logger = Logger.getLogger(ModelGraphTools.class.getName());
    private static final int HTTP_PORT_BASE = 7000;
    private static final int LIMIT = 100;

    private final Environment environment;
    private boolean available;

    @Inject
    public ModelGraphTools(Environment environment) {
        this.environment = environment;
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
        String url = replaceVersion(MODEL_GRAPH_TOOLS_IDENTITY, String.valueOf(port(environment.productVersion())));

        RequestInit init = RequestInit.create();
        init.setMethod("GET");
        init.setMode("cors");

        logger.debug("Checking model graph tools availability at %s", url);
        return fetch(url, init)
                .then(response -> {
                    if (response.ok) {
                        return response.json()
                                .then(identity -> {
                                    logger.info("Model graph tools for WildFly %s available: %o",
                                            environment.productVersion(), identity);
                                    available = true;
                                    return Promise.resolve(true);
                                })
                                .catch_(error -> {
                                    logger.error("Failed to parse model graph tools identity for WildFly %s: %s",
                                            environment.productVersion(), String.valueOf(error));
                                    available = false;
                                    return Promise.resolve(false);
                                });
                    } else {
                        logger.info("Model graph tools for WildFly %s not available: %d",
                                environment.productVersion(), response.status);
                        available = false;
                        return Promise.resolve(false);
                    }
                })
                .catch_(error -> {
                    logger.info("Model graph tools for WildFly %s not available: %s",
                            environment.productVersion(), error);
                    available = false;
                    return Promise.resolve(false);
                });
    }

    /**
     * Queries the MGT search API for resources, attributes, operations, and capabilities matching the given term. Returns a
     * promise that resolves to the array of search results or an empty array if the request fails.
     */
    public Promise<SearchResult[]> search(String term) {
        return search(term, LIMIT);
    }

    /**
     * Queries the MGT search API for resources, attributes, operations, and capabilities matching the given term. Returns a
     * promise that resolves to the array of search results or an empty array if the request fails.
     */
    public Promise<SearchResult[]> search(String term, int limit) {
        String url = replaceVersion(MODEL_GRAPH_TOOLS_SEARCH, String.valueOf(port(environment.productVersion())))
                + "?q=" + term + "&limit=" + limit;

        RequestInit init = RequestInit.create();
        init.setMethod("GET");
        init.setMode("cors");

        logger.debug("Searching model graph tools at %s", url);
        return fetch(url, init)
                .then(response -> {
                    if (response.ok) {
                        return response.json()
                                .then(json -> {
                                    SearchResponse searchResponse = (SearchResponse) json;
                                    SearchResult[] results = searchResponse.results;
                                    logger.debug("Model graph tools returned %d results for '%s'",
                                            results != null ? results.length : 0, term);
                                    return Promise.resolve(results != null ? results : new SearchResult[0]);
                                })
                                .catch_(error -> {
                                    logger.error("Failed to parse model graph tools search response: %s",
                                            String.valueOf(error));
                                    return Promise.resolve(new SearchResult[0]);
                                });
                    } else {
                        logger.error("Model graph tools search failed: %d", response.status);
                        return Promise.resolve(new SearchResult[0]);
                    }
                })
                .catch_(error -> {
                    logger.error("Model graph tools search failed: %s", error);
                    return Promise.resolve(new SearchResult[0]);
                });
    }

    private int port(Version version) {
        return HTTP_PORT_BASE + version.major() * 10 + version.minor();
    }
}

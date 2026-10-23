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

import elemental2.dom.RequestInit;
import elemental2.promise.Promise;

import static elemental2.dom.DomGlobal.fetch;

/**
 * Queries the MGT search API for resources, attributes, operations, and capabilities matching a given term.
 */
@ApplicationScoped
public class ModelGraphSearch {

    private static final Logger logger = Logger.getLogger(ModelGraphSearch.class.getName());
    private static final int LIMIT = 100;

    private final ModelGraphEndpoint endpoint;

    @Inject
    public ModelGraphSearch(ModelGraphEndpoint endpoint) {
        this.endpoint = endpoint;
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
        String url = endpoint.api().append("search?q=").append(term).append("&limit=").append(limit).toString();

        RequestInit init = RequestInit.create();
        init.setMethod("GET");
        init.setMode("cors");

        logger.debug("Searching model graph at %s", url);
        return fetch(url, init)
                .then(response -> {
                    if (response.ok) {
                        return response.json()
                                .then(json -> {
                                    SearchResponse searchResponse = (SearchResponse) json;
                                    SearchResult[] results = searchResponse.results;
                                    logger.debug("Model graph returned %d results for '%s'",
                                            results != null ? results.length : 0, term);
                                    return Promise.resolve(results != null ? results : new SearchResult[0]);
                                })
                                .catch_(error -> {
                                    logger.error("Failed to parse model graph search response: %s",
                                            String.valueOf(error));
                                    return Promise.resolve(new SearchResult[0]);
                                });
                    } else {
                        logger.error("Model graph search failed: %d", response.status);
                        return Promise.resolve(new SearchResult[0]);
                    }
                })
                .catch_(error -> {
                    logger.error("Model graph search failed: %s", error);
                    return Promise.resolve(new SearchResult[0]);
                });
    }
}

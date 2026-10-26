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
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import static elemental2.dom.DomGlobal.fetch;

/**
 * Queries the MGT catalog API for faceted search across resources, attributes, operations, and capabilities.
 */
@ApplicationScoped
public class ModelGraphCatalog {

    private static final Logger logger = Logger.getLogger(ModelGraphCatalog.class.getName());

    private final ModelGraphEndpoint endpoint;

    @Inject
    public ModelGraphCatalog(ModelGraphEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    /** Queries the MGT catalog API with the given filter. Returns a promise that resolves to the catalog response. */
    public Promise<CatalogResponse> search(CatalogFilter filter) {
        StringBuilder url = endpoint.api().append("catalog");
        String queryString = filter.toQueryString();
        if (!queryString.isEmpty()) {
            url.append("?").append(queryString);
        }

        RequestInit init = RequestInit.create();
        init.setMethod("GET");
        init.setMode("cors");

        logger.debug("Searching model graph catalog at %s", url);
        return fetch(url.toString(), init)
                .then(response -> {
                    if (response.ok) {
                        return response.json()
                                .then(json -> {
                                    CatalogResponse catalogResponse = (CatalogResponse) json;
                                    logger.debug("Model graph catalog returned %d of %d results",
                                            catalogResponse.results != null ? catalogResponse.results.length : 0,
                                            catalogResponse.total);
                                    return Promise.resolve(catalogResponse);
                                })
                                .catch_(error -> {
                                    logger.error("Failed to parse model graph catalog response: %s",
                                            String.valueOf(error));
                                    return Promise.resolve(emptyCatalogResponse());
                                });
                    } else {
                        logger.error("Model graph catalog search failed: %d", response.status);
                        return Promise.resolve(emptyCatalogResponse());
                    }
                })
                .catch_(error -> {
                    logger.error("Model graph catalog search failed: %s", error);
                    return Promise.resolve(emptyCatalogResponse());
                });
    }

    private CatalogResponse emptyCatalogResponse() {
        JsPropertyMap<Object> map = JsPropertyMap.of();
        map.set("results", new CatalogResult[0]);
        map.set("total", 0);
        map.set("limit", 0);
        map.set("offset", 0);
        return Js.uncheckedCast(map);
    }
}

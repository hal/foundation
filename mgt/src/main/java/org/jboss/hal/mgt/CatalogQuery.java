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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record CatalogQuery(
        String q,
        ModelGraphType type,
        int limit,
        int offset,
        Map<CatalogFacet, FacetValue> facets) {

    public CatalogQuery {
        facets = Map.copyOf(facets);
    }

    private static final int DEFAULT_LIMIT = 50;
    private static final int DEFAULT_OFFSET = 0;

    public String toQueryString() {
        List<String> params = new ArrayList<>();
        if (q != null && !q.isEmpty()) {
            params.add("q=" + q);
        }
        if (type != ModelGraphType.ALL) {
            params.add("type=" + type.type);
        }
        for (Map.Entry<CatalogFacet, FacetValue> entry : facets.entrySet()) {
            params.add(entry.getKey().key + "=" + entry.getValue().toQueryParam());
        }
        if (limit != DEFAULT_LIMIT) {
            params.add("limit=" + limit);
        }
        if (offset != DEFAULT_OFFSET) {
            params.add("offset=" + offset);
        }
        return String.join("&", params);
    }
}

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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogQueryTest {

    @Test
    void emptyQueryProducesEmptyString() {
        var q = new CatalogQuery(null, ModelGraphType.ALL, 50, 0, Map.of());
        assertEquals("", q.toQueryString());
    }

    @Test
    void allTypeNotInQueryString() {
        var q = new CatalogQuery(null, ModelGraphType.ALL, 50, 0, Map.of());
        assertFalse(q.toQueryString().contains("type="));
    }

    @Test
    void specificTypeInQueryString() {
        var q = new CatalogQuery(null, ModelGraphType.ATTRIBUTE, 50, 0, Map.of());
        assertTrue(q.toQueryString().contains("type=attribute"));
    }

    @Test
    void queryTextIncluded() {
        var q = new CatalogQuery("datasource", ModelGraphType.ALL, 50, 0, Map.of());
        assertTrue(q.toQueryString().contains("q=datasource"));
    }

    @Test
    void nonDefaultPaginationIncluded() {
        var q = new CatalogQuery(null, ModelGraphType.ALL, 25, 10, Map.of());
        assertTrue(q.toQueryString().contains("limit=25"));
        assertTrue(q.toQueryString().contains("offset=10"));
    }

    @Test
    void defaultPaginationOmitted() {
        var q = new CatalogQuery(null, ModelGraphType.ALL, 50, 0, Map.of());
        assertFalse(q.toQueryString().contains("limit="));
        assertFalse(q.toQueryString().contains("offset="));
    }

    @Test
    void facetsRendered() {
        var facets = Map.of(CatalogFacet.REQUIRED, (FacetValue) new FacetValue.Bool(true));
        var q = new CatalogQuery(null, ModelGraphType.ALL, 50, 0, facets);
        assertTrue(q.toQueryString().contains("required=true"));
    }

    @Test
    void combinedQueryString() {
        var facets = Map.of(
                CatalogFacet.REQUIRED, (FacetValue) new FacetValue.Bool(true),
                CatalogFacet.STABILITY,
                (FacetValue) new FacetValue.Multi(new LinkedHashSet<>(List.of("default", "community"))));
        var q = new CatalogQuery("test", ModelGraphType.ATTRIBUTE, 50, 0, facets);
        String qs = q.toQueryString();
        assertTrue(qs.contains("q=test"));
        assertTrue(qs.contains("type=attribute"));
        assertTrue(qs.contains("required=true"));
        assertTrue(qs.contains("stability="));
    }
}

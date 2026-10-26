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

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * JsInterop type for the MGT catalog API response ({@code GET /api/catalog}).
 *
 * <p>Contains paginated results, facet counts, and pagination metadata. The {@link #facets} field is a dynamic object
 * whose keys are facet names and values are objects mapping facet values to counts. Use
 * {@code Js.asPropertyMap(facets)} to iterate.
 *
 * @see <a href="https://github.com/model-graph-tools">Model Graph Tools</a>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class CatalogResponse {

    /** The catalog results for the current page. */
    public CatalogResult[] results;

    /** Facet counts as a dynamic object. Keys are facet names, values are objects mapping facet values to counts. */
    public Object facets;

    /** Total number of results matching the current filters. */
    public int total;

    /** Page size used for this response. */
    public int limit;

    /** Pagination offset. */
    public int offset;
}

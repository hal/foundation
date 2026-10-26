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

/**
 * Represents the query parameters for the MGT catalog API ({@code GET /api/catalog}). Use builder-style setters to
 * configure filters, then call {@link #toQueryString()} to produce the URL query string.
 *
 * <p>Only non-null/non-default values are included in the query string. Multi-value parameters are rendered as
 * comma-separated values.
 */
public class CatalogFilter {

    private static final int DEFAULT_LIMIT = 50;
    private static final int DEFAULT_OFFSET = 0;

    // general
    private String q;
    private List<String> type;
    private List<String> stability;
    private String deprecated;
    private String scope;
    private String sort;
    private String order;
    private int limit = DEFAULT_LIMIT;
    private int offset = DEFAULT_OFFSET;

    // resource-specific
    private Boolean singleton;
    private String declaresCap;
    private Boolean definesAttrGroups;

    // attribute-specific
    private List<String> dataType;
    private List<String> accessType;
    private List<String> storage;
    private List<String> restart;
    private Boolean required;
    private Boolean nillable;
    private Boolean expressions;
    private Boolean sensitive;
    private Boolean hasAllowed;
    private List<String> unit;
    private Boolean hasRelationships;
    private String refsCap;

    // operation-specific
    private Boolean readOnly;
    private Boolean runtimeOnly;
    private Boolean global;
    private Boolean hasParams;
    private Boolean hasReturnValue;

    // ------------------------------------------------------ general setters

    public CatalogFilter q(String q) {
        this.q = q;
        return this;
    }

    public CatalogFilter type(String... values) {
        this.type = List.of(values);
        return this;
    }

    public CatalogFilter stability(String... values) {
        this.stability = List.of(values);
        return this;
    }

    public CatalogFilter deprecated(String deprecated) {
        this.deprecated = deprecated;
        return this;
    }

    public CatalogFilter scope(String scope) {
        this.scope = scope;
        return this;
    }

    public CatalogFilter sort(String sort) {
        this.sort = sort;
        return this;
    }

    public CatalogFilter order(String order) {
        this.order = order;
        return this;
    }

    public CatalogFilter limit(int limit) {
        this.limit = limit;
        return this;
    }

    public CatalogFilter offset(int offset) {
        this.offset = offset;
        return this;
    }

    // ------------------------------------------------------ resource setters

    public CatalogFilter singleton(boolean singleton) {
        this.singleton = singleton;
        return this;
    }

    public CatalogFilter declaresCap(String declaresCap) {
        this.declaresCap = declaresCap;
        return this;
    }

    public CatalogFilter definesAttrGroups(boolean definesAttrGroups) {
        this.definesAttrGroups = definesAttrGroups;
        return this;
    }

    // ------------------------------------------------------ attribute setters

    public CatalogFilter dataType(String... values) {
        this.dataType = List.of(values);
        return this;
    }

    public CatalogFilter accessType(String... values) {
        this.accessType = List.of(values);
        return this;
    }

    public CatalogFilter storage(String... values) {
        this.storage = List.of(values);
        return this;
    }

    public CatalogFilter restart(String... values) {
        this.restart = List.of(values);
        return this;
    }

    public CatalogFilter required(boolean required) {
        this.required = required;
        return this;
    }

    public CatalogFilter nillable(boolean nillable) {
        this.nillable = nillable;
        return this;
    }

    public CatalogFilter expressions(boolean expressions) {
        this.expressions = expressions;
        return this;
    }

    public CatalogFilter sensitive(boolean sensitive) {
        this.sensitive = sensitive;
        return this;
    }

    public CatalogFilter hasAllowed(boolean hasAllowed) {
        this.hasAllowed = hasAllowed;
        return this;
    }

    public CatalogFilter unit(String... values) {
        this.unit = List.of(values);
        return this;
    }

    public CatalogFilter hasRelationships(boolean hasRelationships) {
        this.hasRelationships = hasRelationships;
        return this;
    }

    public CatalogFilter refsCap(String refsCap) {
        this.refsCap = refsCap;
        return this;
    }

    // ------------------------------------------------------ operation setters

    public CatalogFilter readOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    public CatalogFilter runtimeOnly(boolean runtimeOnly) {
        this.runtimeOnly = runtimeOnly;
        return this;
    }

    public CatalogFilter global(boolean global) {
        this.global = global;
        return this;
    }

    public CatalogFilter hasParams(boolean hasParams) {
        this.hasParams = hasParams;
        return this;
    }

    public CatalogFilter hasReturnValue(boolean hasReturnValue) {
        this.hasReturnValue = hasReturnValue;
        return this;
    }

    // ------------------------------------------------------ query string

    /** Builds the URL query string from the current filter state. Returns an empty string if no filters are set. */
    public String toQueryString() {
        List<String> params = new ArrayList<>();
        appendString(params, "q", q);
        appendMulti(params, "type", type);
        appendMulti(params, "stability", stability);
        appendString(params, "deprecated", deprecated);
        appendString(params, "scope", scope);

        // resource
        appendBoolean(params, "singleton", singleton);
        appendString(params, "declaresCap", declaresCap);
        appendBoolean(params, "definesAttrGroups", definesAttrGroups);

        // attribute
        appendMulti(params, "dataType", dataType);
        appendMulti(params, "accessType", accessType);
        appendMulti(params, "storage", storage);
        appendMulti(params, "restart", restart);
        appendBoolean(params, "required", required);
        appendBoolean(params, "nillable", nillable);
        appendBoolean(params, "expressions", expressions);
        appendBoolean(params, "sensitive", sensitive);
        appendBoolean(params, "hasAllowed", hasAllowed);
        appendMulti(params, "unit", unit);
        appendBoolean(params, "hasRelationships", hasRelationships);
        appendString(params, "refsCap", refsCap);

        // operation
        appendBoolean(params, "readOnly", readOnly);
        appendBoolean(params, "runtimeOnly", runtimeOnly);
        appendBoolean(params, "global", global);
        appendBoolean(params, "hasParams", hasParams);
        appendBoolean(params, "hasReturnValue", hasReturnValue);

        // pagination & sorting
        appendString(params, "sort", sort);
        appendString(params, "order", order);
        if (limit != DEFAULT_LIMIT) {
            params.add("limit=" + limit);
        }
        if (offset != DEFAULT_OFFSET) {
            params.add("offset=" + offset);
        }

        return params.isEmpty() ? "" : String.join("&", params);
    }

    private void appendString(List<String> params, String key, String value) {
        if (value != null && !value.isEmpty()) {
            params.add(key + "=" + value);
        }
    }

    private void appendBoolean(List<String> params, String key, Boolean value) {
        if (value != null) {
            params.add(key + "=" + value);
        }
    }

    private void appendMulti(List<String> params, String key, List<String> values) {
        if (values != null && !values.isEmpty()) {
            params.add(key + "=" + String.join(",", values));
        }
    }
}

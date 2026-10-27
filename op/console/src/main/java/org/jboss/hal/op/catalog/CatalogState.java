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
package org.jboss.hal.op.catalog;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.hal.mgt.CatalogFacet;
import org.jboss.hal.mgt.CatalogFacet.Group;
import org.jboss.hal.mgt.CatalogFilter;
import org.jboss.hal.mgt.ModelGraphType;

import static org.jboss.hal.mgt.CatalogFacet.TYPE;

/**
 * Mutable state machine for the Model Catalog page. Tracks active facet selections and builds a {@link CatalogFilter} for
 * querying the MGT catalog API.
 *
 * <p>Handles implicit type constraints: when a type-specific facet (e.g., {@link CatalogFacet#REQUIRED}) is applied without an
 * explicit type selection, the type is implicitly constrained to the facet's group. Removing the last type-specific facet while
 * implicit removes the type constraint.
 */
public class CatalogState {

    private String query;
    private final Map<CatalogFacet, Set<String>> selections;
    private boolean implicitType;

    public CatalogState() {
        this.selections = new EnumMap<>(CatalogFacet.class);
    }

    // ------------------------------------------------------ mutations

    /** Adds a value to the given facet's selection. Handles implicit type constraints for type-specific facets. */
    public void apply(CatalogFacet facet, String value) {
        if (facet == TYPE) {
            implicitType = false;
        } else if (facet.group != Group.GENERAL && !hasExplicitType()) {
            ModelGraphType impliedType = facet.group.implicitType();
            if (impliedType != null) {
                selections.computeIfAbsent(TYPE, k -> new LinkedHashSet<>()).clear();
                selections.get(TYPE).add(impliedType.type);
                implicitType = true;
            }
        }
        selections.computeIfAbsent(facet, k -> new LinkedHashSet<>()).add(value);
    }

    /** Removes a value from the given facet's selection. Cleans up implicit type constraints when appropriate. */
    public void remove(CatalogFacet facet, String value) {
        Set<String> values = selections.get(facet);
        if (values == null) {
            return;
        }
        values.remove(value);
        if (values.isEmpty()) {
            selections.remove(facet);
        }

        if (facet == TYPE) {
            removeTypeSpecificFacets(value);
        } else if (facet.group != Group.GENERAL && implicitType && !hasTypeSpecificFacets()) {
            selections.remove(TYPE);
            implicitType = false;
        }
    }

    /** Resets all facet selections but preserves the text query. */
    public void clearAll() {
        selections.clear();
        implicitType = false;
    }

    /** Clears the text query. */
    public void clearQuery() {
        this.query = null;
    }

    /** Sets the text search query. */
    public void query(String q) {
        this.query = q;
    }

    // ------------------------------------------------------ queries

    /** Builds a {@link CatalogFilter} from the current state. */
    public CatalogFilter currentFilter() {
        CatalogFilter filter = new CatalogFilter();
        if (query != null && !query.isEmpty()) {
            filter.q(query);
        }
        for (Map.Entry<CatalogFacet, Set<String>> entry : selections.entrySet()) {
            CatalogFacet facet = entry.getKey();
            Set<String> values = entry.getValue();
            if (values.isEmpty()) {
                continue;
            }
            applyToFilter(filter, facet, values);
        }
        return filter;
    }

    /** Returns whether the given facet value is currently selected. */
    public boolean isActive(CatalogFacet facet, String value) {
        Set<String> values = selections.get(facet);
        return values != null && values.contains(value);
    }

    /** Returns whether any facet selections are active (excludes the text query). */
    public boolean hasActiveFilters() {
        return !selections.isEmpty();
    }

    /** Returns the currently selected type values. */
    public Set<String> activeTypes() {
        Set<String> types = selections.get(TYPE);
        return types != null ? Set.copyOf(types) : Set.of();
    }

    /** Returns whether the current type constraint was implicitly inferred from a type-specific facet. */
    public boolean isImplicitType() {
        return implicitType;
    }

    // ------------------------------------------------------ internal

    private boolean hasExplicitType() {
        return selections.containsKey(TYPE) && !implicitType;
    }

    private boolean hasTypeSpecificFacets() {
        for (CatalogFacet facet : selections.keySet()) {
            if (facet != TYPE && facet.group != Group.GENERAL) {
                return true;
            }
        }
        return false;
    }

    private void removeTypeSpecificFacets(String typeValue) {
        for (Group group : Group.values()) {
            ModelGraphType impliedType = group.implicitType();
            if (impliedType != null && typeValue.equals(impliedType.type)) {
                selections.entrySet().removeIf(e -> e.getKey().group == group);
                break;
            }
        }
    }

    private void applyToFilter(CatalogFilter filter, CatalogFacet facet, Set<String> values) {
        String[] arr = values.toArray(new String[0]);
        switch (facet) {
            // general
            case TYPE -> filter.type(arr);
            case STABILITY -> filter.stability(arr);
            case DEPRECATED -> filter.deprecated(arr[0]);

            // resource
            case SINGLETON -> filter.singleton(toBoolean(arr[0]));
            case DECLARES_CAP -> filter.declaresCap(arr[0]);
            case DEFINES_ATTR_GROUPS -> filter.definesAttrGroups(toBoolean(arr[0]));

            // attribute
            case DATA_TYPE -> filter.dataType(arr);
            case ACCESS_TYPE -> filter.accessType(arr);
            case STORAGE -> filter.storage(arr);
            case RESTART -> filter.restart(arr);
            case REQUIRED -> filter.required(toBoolean(arr[0]));
            case NILLABLE -> filter.nillable(toBoolean(arr[0]));
            case EXPRESSIONS -> filter.expressions(toBoolean(arr[0]));
            case SENSITIVE -> filter.sensitive(toBoolean(arr[0]));
            case HAS_ALLOWED -> filter.hasAllowed(toBoolean(arr[0]));
            case UNIT -> filter.unit(arr);
            case HAS_RELATIONSHIPS -> filter.hasRelationships(toBoolean(arr[0]));
            case REFS_CAP -> filter.refsCap(arr[0]);

            // operation
            case READ_ONLY -> filter.readOnly(toBoolean(arr[0]));
            case RUNTIME_ONLY -> filter.runtimeOnly(toBoolean(arr[0]));
            case GLOBAL -> filter.global(toBoolean(arr[0]));
            case HAS_PARAMS -> filter.hasParams(toBoolean(arr[0]));
            case HAS_RETURN_VALUE -> filter.hasReturnValue(toBoolean(arr[0]));
        }
    }

    private static boolean toBoolean(String value) {
        return "yes".equals(value);
    }
}

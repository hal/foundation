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
import java.util.Map;

import org.jboss.hal.mgt.CatalogFacet;
import org.jboss.hal.mgt.CatalogFacet.Group;
import org.jboss.hal.mgt.CatalogQuery;
import org.jboss.hal.mgt.FacetValue;
import org.jboss.hal.mgt.ModelGraphType;
import org.jboss.hal.mgt.TypeSelection;
import org.patternfly.component.pagination.PageInfo;

import static org.patternfly.component.pagination.PageInfo.UNKNOWN;
import static org.patternfly.component.pagination.PageSize._50;

/**
 * Mutable state machine for the Model Catalog page. Tracks the selected type, active facet selections, text query, and
 * pagination. Builds a {@link CatalogQuery} for querying the MGT catalog API.
 *
 * <p>Handles implicit type constraints: when a type-specific facet is applied without an explicit type selection, the type is
 * implicitly constrained to the facet's group. Removing the last type-specific facet while implicit removes the type
 * constraint.
 */
class CatalogState implements CatalogStateInfo {

    private final Map<CatalogFacet, FacetValue> facets;
    private String query;
    private PageInfo pageInfo;
    private TypeSelection typeSelection;

    CatalogState() {
        this.facets = new EnumMap<>(CatalogFacet.class);
        this.pageInfo = PageInfo.of(_50.value, UNKNOWN);
        this.typeSelection = new TypeSelection(ModelGraphType.ALL, false);
    }

    // ------------------------------------------------------ mutations

    void selectType(ModelGraphType type) {
        ModelGraphType previousType = typeSelection.type();
        typeSelection = new TypeSelection(type, false);
        if (previousType != type) {
            facets.entrySet().removeIf(e -> e.getKey().group != Group.GENERAL && !groupMatchesType(e.getKey(), type));
        }
    }

    void applyFacetValue(CatalogFacet facet, FacetValue value) {
        if (facet.group != Group.GENERAL && typeSelection.type() == ModelGraphType.ALL) {
            ModelGraphType impliedType = facet.group.implicitType();
            if (impliedType != null) {
                typeSelection = new TypeSelection(impliedType, true);
            }
        }
        facets.put(facet, value);
    }

    void removeFacet(CatalogFacet facet) {
        facets.remove(facet);
        if (typeSelection.implicit() && !hasTypeSpecificFacets()) {
            typeSelection = new TypeSelection(ModelGraphType.ALL, false);
        }
    }

    void reset() {
        query = null;
        pageInfo = PageInfo.of(pageInfo.pageSize(), UNKNOWN);
        typeSelection = new TypeSelection(ModelGraphType.ALL, false);
        facets.clear();
    }

    void clearQuery() {
        this.query = null;
    }

    void query(String q) {
        this.query = q;
    }

    void navigate(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    void updateTotal(int total) {
        this.pageInfo = pageInfo.withTotal(total);
    }

    // ------------------------------------------------------ queries

    @Override
    public TypeSelection typeSelection() {
        return typeSelection;
    }

    @Override
    public ModelGraphType effectiveType() {
        return typeSelection.type();
    }

    @Override
    public boolean isActive(CatalogFacet facet) {
        return facets.containsKey(facet);
    }

    @Override
    @SuppressWarnings({"DeconstructionCanBeUsed", "IfCanBeSwitch"})
    public boolean isActive(CatalogFacet facet, String value) {
        FacetValue fv = facets.get(facet);
        if (fv == null) {
            return false;
        }
        if (fv instanceof FacetValue.Bool b) {
            return b.value() == "true".equals(value);
        } else if (fv instanceof FacetValue.BoolText bt) {
            return bt.value() == "true".equals(value);
        } else if (fv instanceof FacetValue.Multi m) {
            return m.values().contains(value);
        }
        return false;
    }

    @Override
    public FacetValue facetValue(CatalogFacet facet) {
        return facets.get(facet);
    }

    @Override
    public boolean hasActiveFilters() {
        return !facets.isEmpty();
    }

    @Override
    public PageInfo pageInfo() {
        return pageInfo;
    }

    CatalogQuery buildQuery() {
        return new CatalogQuery(
                query,
                typeSelection.type(),
                pageInfo.pageSize(),
                pageInfo.from() - 1,
                Map.copyOf(facets));
    }

    // ------------------------------------------------------ internal

    private boolean groupMatchesType(CatalogFacet facet, ModelGraphType type) {
        if (type == ModelGraphType.ALL) {
            return true;
        }
        ModelGraphType groupType = facet.group.implicitType();
        return groupType != null && groupType == type;
    }

    private boolean hasTypeSpecificFacets() {
        for (CatalogFacet facet : facets.keySet()) {
            if (facet.group != Group.GENERAL) {
                return true;
            }
        }
        return false;
    }
}

package org.jboss.hal.op.catalog;

import org.jboss.hal.mgt.CatalogFacet;
import org.jboss.hal.mgt.FacetValue;
import org.jboss.hal.mgt.ModelGraphType;
import org.jboss.hal.mgt.TypeSelection;
import org.patternfly.component.pagination.PageInfo;

public interface CatalogStateInfo {

    TypeSelection typeSelection();

    ModelGraphType effectiveType();

    boolean isActive(CatalogFacet facet);

    boolean isActive(CatalogFacet facet, String value);

    boolean hasActiveFilters();

    FacetValue facetValue(CatalogFacet facet);

    String facetText(CatalogFacet facet);

    PageInfo pageInfo();
}

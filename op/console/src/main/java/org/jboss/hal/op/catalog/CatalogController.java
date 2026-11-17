package org.jboss.hal.op.catalog;

import org.jboss.hal.mgt.CatalogFacet;
import org.jboss.hal.mgt.FacetValue;
import org.jboss.hal.mgt.ModelGraphType;
import org.patternfly.component.pagination.PageInfo;

public interface CatalogController {

    void search(String query);

    void clearSearch();

    void reset();

    void selectType(ModelGraphType type);

    void applyFacetValue(CatalogFacet facet, FacetValue value);

    void removeFacet(CatalogFacet facet);

    void navigate(PageInfo pageInfo);

    CatalogStateInfo state();
}

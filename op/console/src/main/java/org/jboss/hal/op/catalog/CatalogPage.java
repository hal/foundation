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

import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.elemento.router.LoadedData;
import org.jboss.elemento.router.Page;
import org.jboss.elemento.router.Parameter;
import org.jboss.elemento.router.Place;
import org.jboss.elemento.router.Route;
import org.jboss.hal.mgt.CatalogFacet;
import org.jboss.hal.mgt.CatalogFilter;
import org.jboss.hal.mgt.CatalogResponse;
import org.jboss.hal.mgt.ModelGraphCatalog;
import org.jboss.hal.mgt.ModelGraphType;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static org.jboss.hal.resources.OuiaIds.PAGE_CATALOG;
import static org.patternfly.component.divider.Divider.divider;
import static org.patternfly.component.divider.DividerType.hr;
import static org.patternfly.component.page.PageSection.pageSection;
import static org.patternfly.layout.split.Split.split;
import static org.patternfly.layout.split.SplitItem.splitItem;

/**
 * The Model Catalog page provides faceted search over the WildFly management model via the MGT sidecar. The layout follows the
 * PatternFly catalog view pattern: a toolbar section with search and result count, then a split layout with a filter sidebar
 * and a dynamic results table.
 */
@Dependent
@Route("/model-catalog")
public class CatalogPage implements Page {

    private final ModelGraphCatalog catalog;
    private final CatalogState state;
    private final CatalogToolbar catalogToolbar;
    private final CatalogSidebar catalogSidebar;
    private final CatalogTable catalogTable;
    private ModelGraphType selectedType;

    @Inject
    public CatalogPage(ModelGraphCatalog catalog) {
        this.catalog = catalog;
        this.state = new CatalogState();
        this.catalogToolbar = new CatalogToolbar(this::onSearch, this::onClear);
        this.catalogSidebar = new CatalogSidebar(this::onTypeSelect, this::onFacetToggle);
        this.catalogTable = new CatalogTable();
    }

    @Override
    public Iterable<HTMLElement> elements(Place place, Parameter parameter, LoadedData data) {
        HTMLElement toolbarSection = pageSection().ouiaId(PAGE_CATALOG).limitWidth()
                .add(catalogToolbar)
                .add(divider(hr))
                .element();

        HTMLElement contentSection = pageSection().limitWidth()
                .add(split().gutter()
                        .addItem(splitItem()
                                .style("min-width", "200px")
                                .style("max-width", "250px")
                                .add(catalogSidebar))
                        .addItem(splitItem().fill()
                                .add(catalogTable)))
                .element();

        return asList(toolbarSection, contentSection);
    }

    @Override
    public void attach() {
        search();
    }

    // ------------------------------------------------------ event handlers

    private void onSearch(String query) {
        state.query(query);
        search();
    }

    private void onClear() {
        state.clearQuery();
        state.clearAll();
        selectedType = null;
        search();
    }

    private void onTypeSelect(ModelGraphType type) {
        selectedType = type;
        state.clearAll();
        if (type != null) {
            state.apply(CatalogFacet.TYPE, type.type);
        }
        search();
    }

    private void onFacetToggle(CatalogFacet facet, String value) {
        if (state.isActive(facet, value)) {
            state.remove(facet, value);
        } else {
            state.apply(facet, value);
        }
        search();
    }

    // ------------------------------------------------------ search

    private void search() {
        CatalogFilter filter = state.currentFilter();
        catalog.search(filter).then(response -> {
            handleResponse(response);
            return null;
        });
    }

    private void handleResponse(CatalogResponse response) {
        ModelGraphType effectiveType = deriveEffectiveType();
        catalogTable.update(effectiveType, response.results);
        catalogSidebar.update(response.facets, state);
        catalogToolbar.updateResultCount(response.total, response.limit);
    }

    private ModelGraphType deriveEffectiveType() {
        if (selectedType != null) {
            return selectedType;
        }
        Set<String> activeTypes = state.activeTypes();
        if (activeTypes.size() == 1) {
            String typeStr = activeTypes.iterator().next();
            try {
                return ModelGraphType.of(typeStr);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}

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

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.elemento.router.LoadedData;
import org.jboss.elemento.router.Page;
import org.jboss.elemento.router.Parameter;
import org.jboss.elemento.router.Place;
import org.jboss.elemento.router.Route;
import org.jboss.hal.mgt.CatalogFacet;
import org.jboss.hal.mgt.CatalogQuery;
import org.jboss.hal.mgt.CatalogResponse;
import org.jboss.hal.mgt.FacetValue;
import org.jboss.hal.mgt.ModelGraphCatalog;
import org.jboss.hal.mgt.ModelGraphType;
import org.patternfly.component.pagination.PageInfo;
import org.patternfly.component.pagination.Pagination;
import org.patternfly.layout.split.SplitItem;

import elemental2.dom.Event;
import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
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
    private final CatalogWelcome catalogWelcome;
    private final CatalogTable catalogTable;
    private final SplitItem mainContent;

    @Inject
    public CatalogPage(ModelGraphCatalog catalog) {
        this.catalog = catalog;
        this.state = new CatalogState();
        this.catalogToolbar = new CatalogToolbar(state.pageInfo(), this::onSearch, this::onClearSearch, this::onClear,
                this::onNavigate);
        this.catalogSidebar = new CatalogSidebar(this::onTypeSelect, this::onFacetToggle);
        this.catalogWelcome = new CatalogWelcome();
        this.catalogTable = new CatalogTable();
        this.mainContent = splitItem().fill();
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
                        .addItem(mainContent
                                .add(catalogWelcome)))
                .element();

        return asList(toolbarSection, contentSection);
    }

    @Override
    public void attach() {
        catalogToolbar.showPagination(false);
        initialSearch();
    }

    // ------------------------------------------------------ event handlers

    private void onSearch(String query) {
        state.query(query);
        state.resetPageInfo();
        search();
    }

    private void onClearSearch() {
        state.clearQuery();
        state.resetPageInfo();
        search();
    }

    private void onClear() {
        state.clearQuery();
        state.clearAll();
        state.resetPageInfo();
        showWelcome();
        initialSearch();
    }

    private void onTypeSelect(ModelGraphType type) {
        state.selectType(type != null ? type : ModelGraphType.ALL);
        state.resetPageInfo();
        search();
    }

    private void onFacetToggle(CatalogFacet facet, String value) {
        // TODO adapt to new FacetValue API during UI refactoring
        if (state.isActive(facet)) {
            state.remove(facet);
        } else {
            state.apply(facet, new FacetValue.Bool("yes".equals(value)));
        }
        state.resetPageInfo();
        search();
    }

    private void onNavigate(Event event, Pagination pagination, PageInfo previous, PageInfo update) {
        state.navigate(update);
        search();
    }

    // ------------------------------------------------------ welcome / results

    private void showWelcome() {
        failSafeRemoveFromParent(catalogTable);
        mainContent.add(catalogWelcome);
        catalogToolbar.showPagination(false);
    }

    private void showResults() {
        failSafeRemoveFromParent(catalogWelcome);
        mainContent.add(catalogTable);
        catalogToolbar.showPagination(true);
    }

    // ------------------------------------------------------ search

    private void initialSearch() {
        CatalogQuery query = state.buildFilter();
        catalog.search(query).then(response -> {
            catalogSidebar.update(state, response);
            return null;
        });
    }

    private void search() {
        CatalogQuery query = state.buildFilter();
        catalog.search(query).then(response -> {
            handleResponse(response);
            return null;
        });
    }

    private void handleResponse(CatalogResponse response) {
        ModelGraphType effectiveType = state.effectiveType();
        state.updateTotal(response.total);
        showResults();
        catalogSidebar.update(state, response);
        catalogTable.update(effectiveType, response.results);
        catalogToolbar.updatePageInfo(state.pageInfo());
    }
}

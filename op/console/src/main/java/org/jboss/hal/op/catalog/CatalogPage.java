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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.elemento.HTMLContainerBuilder;
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

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.modelCatalog;
import static org.jboss.hal.resources.HalClasses.results;
import static org.jboss.hal.resources.OuiaIds.PAGE_CATALOG;
import static org.patternfly.component.divider.Divider.divider;
import static org.patternfly.component.divider.DividerType.hr;
import static org.patternfly.component.page.PageSection.pageSection;

/**
 * The Model Catalog page provides faceted search over the WildFly management model via the MGT sidecar. The layout follows the
 * PatternFly catalog view pattern: a toolbar section with search and result count, then a split layout with a filter sidebar
 * and a dynamic results table.
 */
@Dependent
@Route("/model-catalog")
public class CatalogPage implements CatalogController, Page {

    private final ModelGraphCatalog catalog;
    private final CatalogState state;
    private final CatalogToolbar toolbar;
    private final CatalogSidebar sidebar;
    private final CatalogWelcome welcome;
    private final CatalogTable table;
    private final HTMLContainerBuilder<HTMLDivElement> mainContent;

    @Inject
    public CatalogPage(ModelGraphCatalog catalog) {
        this.catalog = catalog;
        this.state = new CatalogState();
        this.toolbar = new CatalogToolbar(this);
        this.sidebar = new CatalogSidebar(this);
        this.welcome = new CatalogWelcome();
        this.table = new CatalogTable();
        this.mainContent = div().css(halComponent(modelCatalog, results));
    }

    @Override
    public Iterable<HTMLElement> elements(Place place, Parameter parameter, LoadedData data) {
        HTMLElement toolbarSection = pageSection().ouiaId(PAGE_CATALOG).limitWidth()
                .add(toolbar)
                .add(divider(hr))
                .element();

        HTMLElement contentSection = pageSection().limitWidth()
                .add(div().css(halComponent(modelCatalog))
                        .add(sidebar)
                        .add(mainContent
                                .add(welcome)))
                .element();

        return asList(toolbarSection, contentSection);
    }

    @Override
    public void attach() {
        reset();
    }

    // ------------------------------------------------------ api

    @Override
    public void search(String query) {
        state.query(query);
        internalSearch(this::internalUpdate);
    }

    @Override
    public void clearSearch() {
        state.clearQuery();
        internalSearch(this::internalUpdate);
    }

    @Override
    public void reset() {
        showWelcome();
        state.reset();
        internalSearch(response -> {
            toolbar.reset();
            sidebar.reset(response);
        });
    }

    @Override
    public void selectType(ModelGraphType type) {
        state.selectType(type);
        internalSearch(this::internalUpdate);
    }

    @Override
    @SuppressWarnings("DeconstructionCanBeUsed")
    public void applyFacetValue(CatalogFacet facet, String value) {
        if (facet.exclusive) {
            if (state.isActive(facet, value)) {
                state.removeFacet(facet);
            } else {
                state.applyFacetValue(facet, new FacetValue.Bool("true".equals(value)));
            }
        } else {
            FacetValue current = state.facetValue(facet);
            if (current instanceof FacetValue.Multi multi && multi.values().contains(value)) {
                Set<String> remaining = new LinkedHashSet<>(multi.values());
                remaining.remove(value);
                if (remaining.isEmpty()) {
                    state.removeFacet(facet);
                } else {
                    state.applyFacetValue(facet, new FacetValue.Multi(remaining));
                }
            } else {
                Set<String> values = current instanceof FacetValue.Multi multi
                        ? new LinkedHashSet<>(multi.values())
                        : new LinkedHashSet<>();
                values.add(value);
                state.applyFacetValue(facet, new FacetValue.Multi(values));
            }
        }
        internalSearch(this::internalUpdate);
    }

    @Override
    public void navigate(PageInfo pageInfo) {
        state.navigate(pageInfo);
        internalSearch(this::internalUpdate);
    }

    @Override
    public CatalogStateInfo state() {
        return state;
    }

    // ------------------------------------------------------ internal

    private void internalSearch(Consumer<CatalogResponse> consumer) {
        CatalogQuery query = state.buildQuery();
        catalog.search(query).then(response -> {
            consumer.accept(response);
            return null;
        });
    }

    private void internalUpdate(CatalogResponse response) {
        ModelGraphType effectiveType = state.effectiveType();
        state.updateTotal(response.total);
        showResults();
        sidebar.update(response);
        table.update(effectiveType, response.results);
        toolbar.update(state.pageInfo());
    }

    private void showWelcome() {
        failSafeRemoveFromParent(table);
        mainContent.add(welcome);
    }

    private void showResults() {
        failSafeRemoveFromParent(welcome);
        mainContent.add(table);
    }
}

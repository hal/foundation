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

import java.util.List;
import java.util.function.Consumer;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.router.LoadedData;
import org.jboss.elemento.router.Page;
import org.jboss.elemento.router.Parameter;
import org.jboss.elemento.router.Place;
import org.jboss.elemento.router.Route;
import org.jboss.hal.core.Notifications;
import org.jboss.hal.dmr.ResourceCheck;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.tree.ModelTree;
import org.jboss.hal.mgt.CatalogFacet;
import org.jboss.hal.mgt.CatalogQuery;
import org.jboss.hal.mgt.CatalogResponse;
import org.jboss.hal.mgt.CatalogResult;
import org.jboss.hal.mgt.FacetValue;
import org.jboss.hal.mgt.ModelGraphCatalog;
import org.jboss.hal.mgt.ModelGraphType;
import org.jboss.hal.ui.navigation.RouteRegistry;
import org.patternfly.component.pagination.PageInfo;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.elemento.flow.Flow.parallel;
import static org.jboss.hal.core.Notification.error;
import static org.jboss.hal.core.Notification.info;
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

    private final Dispatcher dispatcher;
    private final ModelGraphCatalog catalog;
    private final ModelTree modelTree;
    private final Notifications notifications;
    private final RouteRegistry routeRegistry;
    private final StatementContext statementContext;
    private final CatalogState state;
    private final CatalogToolbar toolbar;
    private final CatalogSidebar sidebar;
    private final CatalogWelcome welcome;
    private final CatalogTable table;
    private final HTMLContainerBuilder<HTMLDivElement> mainContent;

    @Inject
    public CatalogPage(Dispatcher dispatcher,
            Environment environment,
            ModelGraphCatalog catalog,
            ModelTree modelTree,
            Notifications notifications,
            RouteRegistry routeRegistry,
            StatementContext statementContext) {
        this.dispatcher = dispatcher;
        this.catalog = catalog;
        this.modelTree = modelTree;
        this.notifications = notifications;
        this.routeRegistry = routeRegistry;
        this.statementContext = statementContext;
        this.state = new CatalogState();
        this.toolbar = new CatalogToolbar(this);
        this.sidebar = new CatalogSidebar(this);
        this.welcome = new CatalogWelcome();
        this.table = new CatalogTable(this, environment.builtInStability());
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
        state.resetPage();
        internalSearch(this::internalUpdate);
    }

    @Override
    public void applyFacetValue(CatalogFacet facet, FacetValue value) {
        state.applyFacetValue(facet, value);
        state.resetPage();
        internalSearch(this::internalUpdate);
    }

    @Override
    public void removeFacet(CatalogFacet facet) {
        state.removeFacet(facet);
        internalSearch(this::internalUpdate);
    }

    @Override
    public void navigate(PageInfo pageInfo) {
        state.navigate(pageInfo);
        internalSearch(this::internalUpdate);
    }

    @Override
    public void verifyAndGo(CatalogResult result) {
        verifyAndGo(result, result.resourceAddress);
    }

    @Override
    public void verifyAndGo(CatalogResult result, String resource) {
        AddressTemplate.ofUntrusted(resource).ifPresentOrElse(template -> {
                    if (template.fullyQualified()) {
                        goTo(result, template);
                    } else {
                        modelTree.resolveWildcards(template).then(resolved -> {
                            if (resolved.isEmpty()) {
                                // TODO Let the user know that no matching resources were found
                                notifications.send(info("No matching resources found",
                                        "The address " + resource + " does not resolve do any resources."));
                            } else if (resolved.size() == 1) {
                                goTo(result, resolved.get(0));
                            } else {
                                table.showPickList(result, resolved);
                            }
                            return null;
                        });
                    }
                },
                () -> notifications.send(error("Invalid address", "The address " + resource + " is invalid.")));
    }

    @Override
    public void goTo(CatalogResult result, AddressTemplate template) {
        String highlight = switch (result.type()) {
            case ATTRIBUTE -> "a:" + result.name;
            case OPERATION -> "o:" + result.name;
            case CAPABILITY -> "c:" + result.name;
            default -> null;
        };
        routeRegistry.goTo(template, highlight);
    }

    @Override
    public void verifyOnServer() {
        List<Task<FlowContext>> tasks = table.unverifiedResults().stream()
                .map(result -> (Task<FlowContext>) context -> {
                    if (result.type() == ModelGraphType.CAPABILITY) {
                        // TODO check if declaring resources exist
                        return context.resolve();
                    } else {
                        return AddressTemplate.ofUntrusted(result.resourceAddress)
                                .map(template -> {
                                    if (template.fullyQualified()) {
                                        return new ResourceCheck(dispatcher, template.resolve(statementContext))
                                                .apply(new FlowContext())
                                                .then(innerContext -> {
                                                    int status = innerContext.pop(404);
                                                    if (status == 200) {
                                                        table.markVerified(result, VerificationStatus.EXISTS_ONCE, template);
                                                    } else {
                                                        table.markVerified(result, VerificationStatus.DOES_NOT_EXIST, null);
                                                    }
                                                    return innerContext.resolve();
                                                });
                                    } else {
                                        return modelTree.resolveWildcards(template).then(resolved -> {
                                            if (resolved.isEmpty()) {
                                                table.markVerified(result, VerificationStatus.DOES_NOT_EXIST, null);
                                            } else if (resolved.size() == 1) {
                                                table.markVerified(result, VerificationStatus.EXISTS_ONCE, template);
                                            } else {
                                                table.markVerified(result, VerificationStatus.EXISTS_MULTIPLE, null);
                                            }
                                            return context.resolve();
                                        });
                                    }
                                })
                                .orElseGet(() -> {
                                    table.markVerified(result, VerificationStatus.INVALID, null);
                                    return context.resolve();
                                });
                    }
                })
                .collect(toList());

        if (tasks.isEmpty()) {
            notifications.send(info("Verification done", "All resources have already been verified."));
        } else {
            parallel(new FlowContext(), tasks).subscribe(context ->
                    notifications.send(info("Verification complete", tasks.size() + " resources verified.")));
        }
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

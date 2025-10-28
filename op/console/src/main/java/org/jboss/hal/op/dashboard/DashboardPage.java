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
package org.jboss.hal.op.dashboard;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.elemento.router.LoadedData;
import org.jboss.elemento.router.Page;
import org.jboss.elemento.router.Parameter;
import org.jboss.elemento.router.Place;
import org.jboss.elemento.router.Route;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Environment;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.model.deployment.Deployments;
import org.patternfly.layout.flex.FlexItem;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static org.jboss.elemento.Elements.p;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.page.PageSection.pageSection;
import static org.patternfly.component.title.Title.title;
import static org.patternfly.layout.flex.Direction.column;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexShorthand._1;
import static org.patternfly.layout.flex.Gap.md;
import static org.patternfly.layout.grid.Grid.grid;
import static org.patternfly.layout.grid.GridItem.gridItem;
import static org.patternfly.style.Size._3xl;

@Dependent
@Route("/")
public class DashboardPage implements Page {

    private static final double REFRESH_INTERVAL = 5_000;

    private final Environment environment;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final Deployments deployments;
    private final List<DashboardCard> cards;
    private double refreshHandle;

    @Inject
    public DashboardPage(Environment environment,
            StatementContext statementContext,
            Dispatcher dispatcher,
            Deployments deployments) {
        this.environment = environment;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.deployments = deployments;
        this.cards = new ArrayList<>();
    }

    @Override
    public Iterable<HTMLElement> elements(Place place, Parameter parameter, LoadedData data) {
        DashboardCard deploymentCard = new DeploymentCard(environment, deployments);
        DashboardCard documentationCard = new DocumentationCard(environment);
        DashboardCard healthCard = new HealthCard(dispatcher);
        DashboardCard logCard = new LogCard(dispatcher);
        DashboardCard productInfoCard = new ProductInfoCard(environment);
        DashboardCard runtimeCard = new RuntimeCard(statementContext, dispatcher);

        if (environment.standalone()) {
            cards.addAll(asList(
                    deploymentCard,
                    documentationCard,
                    healthCard,
                    logCard,
                    productInfoCard,
                    runtimeCard));
        } else {
            cards.addAll(asList(
                    deploymentCard,
                    documentationCard,
                    productInfoCard,
                    runtimeCard));
        }

        HTMLElement header = pageSection().limitWidth()
                        .add(content()
                                .add(title(1, _3xl).text("WildFly Application Server"))
                                .add(p().text("Dashboard")))
                .element();
        HTMLElement dashboard = pageSection().limitWidth()
                .add(grid().gutter().run(grid -> {
                            if (environment.standalone()) {
                                grid
                                        .addItem(gridItem().span(12)
                                                .add(productInfoCard))
                                        .addItem(gridItem().span(8)
                                                .add(runtimeCard))
                                        .addItem(gridItem().span(4).rowSpan(3)
                                                .add(flex().direction(column).gap(md)
                                                        .addItem(FlexItem.flexItem().flex(_1).add(logCard))
                                                        .addItem(FlexItem.flexItem().flex(_1).add(healthCard))))
                                        .addItem(gridItem().span(8)
                                                .add(deploymentCard))
                                        .addItem(gridItem().span(8)
                                                .add(documentationCard));
                            } else {
                                grid
                                        .addItem(gridItem().span(12)
                                                .add(productInfoCard))
                                        .addItem(gridItem().span(6)
                                                .add(runtimeCard))
                                        .addItem(gridItem().span(6)
                                                .add(deploymentCard))
                                        .addItem(gridItem().span(12)
                                                .add(documentationCard));
                            }
                        })
                )
                .element();
        return asList(header, dashboard);
    }

    @Override
    public void attach() {
        refresh();
        // refreshHandle = setInterval(__ -> refresh(), REFRESH_INTERVAL);
    }

    @Override
    public void detach() {
        // clearInterval(refreshHandle);
    }

    private void refresh() {
        for (DashboardCard card : cards) {
            card.refresh();
        }
    }
}

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

import java.util.Iterator;
import java.util.Map;

import org.jboss.hal.env.Environment;
import org.jboss.hal.model.deployment.Deployment;
import org.jboss.hal.model.deployment.DeploymentStatus;
import org.jboss.hal.model.deployment.Deployments;
import org.patternfly.component.card.CardBody;
import org.patternfly.component.card.CardTitle;
import org.patternfly.icon.IconSets;
import org.patternfly.icon.PredefinedIcon;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.hal.op.dashboard.DashboardCard.dashboardEmptyState;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardBody.cardBody;
import static org.patternfly.component.card.CardHeader.cardHeader;
import static org.patternfly.component.card.CardTitle.cardTitle;
import static org.patternfly.component.divider.Divider.divider;
import static org.patternfly.component.divider.DividerType.hr;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateHeader.emptyStateHeader;
import static org.patternfly.icon.IconSets.fas.checkCircle;
import static org.patternfly.icon.IconSets.fas.exclamationCircle;
import static org.patternfly.icon.IconSets.fas.pauseCircle;
import static org.patternfly.icon.IconSets.fas.question;
import static org.patternfly.icon.IconSets.fas.timesCircle;
import static org.patternfly.layout.flex.Display.inlineFlex;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.SpaceItems.sm;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Orientation.vertical;
import static org.patternfly.token.Token.globalColorStatusDanger100;
import static org.patternfly.token.Token.globalColorStatusSuccess100;
import static org.patternfly.token.Token.globalColorStatusWarning100;

class DeploymentCard implements DashboardCard {

    private final Environment environment;
    private final Deployments deployments;
    private final CardTitle cardTitle;
    private final CardBody cardBody;
    private final HTMLElement root;

    DeploymentCard(Environment environment, Deployments deployments) {
        this.environment = environment;
        this.deployments = deployments;
        this.root = card().css(util("h-100"))
                .addHeader(cardHeader()
                        .addTitle(cardTitle = cardTitle())
                        .addActions(refreshActions()))
                .addBody(cardBody = cardBody().style("text-align", "center"))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void refresh() {
        removeChildrenFrom(cardBody);

        if (environment.standalone()) {
            deployments.readStandaloneDeployments().then(deployments -> {
                if (deployments.isEmpty()) {
                    cardBody.add(dashboardEmptyState()
                            .addHeader(emptyStateHeader()
                                    .icon(IconSets.fas.ban())
                                    .text("No deployments"))
                            .addBody(emptyStateBody().text("This server contains no deployments.")));
                } else {
                    if (deployments.size() == 1) {
                        cardTitle.text("1 Deployment");
                    } else {
                        cardTitle.text(deployments.size() + " Deployments");
                    }
                    Map<DeploymentStatus, Long> status = deployments.stream()
                            .collect(groupingBy(Deployment::status, counting()));
                    if (status.size() == 1) {
                        cardBody.add(status(status.keySet().iterator().next()));
                    } else {
                        cardBody.add(flex().display(inlineFlex)
                                .run(flex -> {
                                    for (Iterator<Map.Entry<DeploymentStatus, Long>> iterator = status.entrySet().iterator();
                                            iterator.hasNext(); ) {
                                        Map.Entry<DeploymentStatus, Long> entry = iterator.next();
                                        flex.add(flex().spaceItems(sm)
                                                .add(div().add(status(entry.getKey())))
                                                .add(div().text(String.valueOf(entry.getValue()))));
                                        if (iterator.hasNext()) {
                                            flex.add(divider(hr).orientation(vertical));
                                        }
                                    }
                                }));
                    }
                }
                return null;
            });
        } else {
            // TODO Add support for domain mode
            cardBody.add(dashboardEmptyState()
                    .addHeader(emptyStateHeader()
                            .icon(exclamationCircle())
                            .text("Domain mode"))
                    .addBody(emptyStateBody().text("Domain mode is not supported yet.")));
        }
    }

    private PredefinedIcon status(DeploymentStatus status) {
        switch (status) {
            case OK:
                return checkCircle().attr("color", globalColorStatusSuccess100.var);
            case FAILED:
                return timesCircle().attr("color", globalColorStatusDanger100.var);
            case STOPPED:
                return pauseCircle();
            case UNDEFINED:
            default:
                return question().attr("color", globalColorStatusWarning100.var);
        }
    }
}

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

import java.util.List;

import org.jboss.elemento.Id;
import org.jboss.elemento.flow.Flow;
import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.ResourceCheck;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.patternfly.component.Severity;
import org.patternfly.component.card.CardBody;
import org.patternfly.component.icon.Icon;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHECKS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATUS;
import static org.jboss.hal.op.dashboard.Dashboard.dashboardEmptyState;
import static org.jboss.hal.ui.BuildingBlocks.errorCode;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardBody.cardBody;
import static org.patternfly.component.card.CardHeader.cardHeader;
import static org.patternfly.component.card.CardTitle.cardTitle;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.icon.Icon.icon;
import static org.patternfly.component.list.DataList.dataList;
import static org.patternfly.component.list.DataListCell.dataListCell;
import static org.patternfly.component.list.DataListItem.dataListItem;
import static org.patternfly.component.title.Title.title;
import static org.patternfly.icon.IconSets.fas.arrowDown;
import static org.patternfly.icon.IconSets.fas.arrowUp;
import static org.patternfly.icon.IconSets.fas.exclamationTriangle;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Size.xl;
import static org.patternfly.style.Status.danger;
import static org.patternfly.style.Status.success;
import static org.patternfly.style.Status.warning;

class HealthCard implements DashboardCard {

    private final Dispatcher dispatcher;
    private final CardBody cardBody;
    private final HTMLElement root;

    HealthCard(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.root = card()
                .addHeader(cardHeader()
                        .addTitle(cardTitle()
                                .run(ct -> ct.textDelegate().appendChild(title(2, xl, "Health").element())))
                        .addActions(refreshActions()))
                .add(cardBody = cardBody())
                .element();
    }

    @Override
    public void refresh() {
        removeChildrenFrom(cardBody);

        ResourceAddress address = AddressTemplate.of("/subsystem=microprofile-health-smallrye").resolve();
        Task<FlowContext> resourceCheck = new ResourceCheck(dispatcher, address);
        Task<FlowContext> healthCheck = context -> {
            int status = context.pop(404);
            if (status == 200) {
                return dispatcher.execute(new Operation.Builder(address, "check").build())
                        .then(context::resolve)
                        .catch_(context::reject);
            } else {
                return context.resolve(new ModelNode());
            }
        };
        Flow.sequential(new FlowContext(), List.of(resourceCheck, healthCheck))
                .then(context -> {
                    ModelNode result = context.pop(new ModelNode());
                    if (result.isDefined()) {
                        if (result.hasDefined(CHECKS)) {
                            List<ModelNode> checks = result.get(CHECKS).asList();
                            if (!checks.isEmpty()) {
                                cardBody.add(dataList().css(modifier("grid-none"))
                                        .addItems(checks, check -> {
                                            String name = check.get(NAME).asString();
                                            String nameId = Id.build(name);
                                            return dataListItem(nameId)
                                                    .addCell(dataListCell().add(span().id(nameId).text(name)))
                                                    .addCell(dataListCell().alignRight().noFill()
                                                            .add(statusIcon(check)));
                                        }));
                            } else {
                                cardBody.add(dashboardEmptyState()
                                        .status(Severity.warning)
                                        .text("No checks found"));
                            }
                        } else {
                            cardBody.add(dashboardEmptyState()
                                    .status(Severity.warning)
                                    .text("No checks found"));
                        }
                    } else {
                        cardBody.add(dashboardEmptyState()
                                .status(Severity.warning)
                                .text("MicroProfile Health not present"));
                    }
                    return null;
                }).catch_(error -> {
                    cardBody.add(dashboardEmptyState()
                            .status(Severity.warning)
                            .text("MicroProfile Health error")
                            .addBody(emptyStateBody().add(errorCode(String.valueOf(error)))));
                    return null;
                });
    }

    private Icon statusIcon(ModelNode check) {
        ModelNode status = check.get(STATUS);
        if ("UP".equals(status.asString())) {
            return icon(arrowUp()).status(success);
        } else if ("DOWN".equals(status.asString())) {
            return icon(arrowDown()).status(danger);
        } else {
            return icon(exclamationTriangle()).status(warning);
        }
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}

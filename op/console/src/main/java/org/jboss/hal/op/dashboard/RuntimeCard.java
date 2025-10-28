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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.patternfly.component.card.Card;
import org.patternfly.layout.gallery.Gallery;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PRODUCT_INFO;
import static org.jboss.hal.op.dashboard.DashboardCard.dashboardEmptyState;
import static org.jboss.hal.ui.BuildingBlocks.errorCode;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardBody.cardBody;
import static org.patternfly.component.card.CardTitle.cardTitle;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;
import static org.patternfly.layout.gallery.Gallery.gallery;

class RuntimeCard implements DashboardCard {

    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final Gallery gallery;

    RuntimeCard(StatementContext statementContext, Dispatcher dispatcher) {
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.gallery = gallery().gutter().style("--pf-v5-l-gallery--GridTemplateColumns--min: 400px");
    }

    @Override
    public HTMLElement element() {
        return gallery.element();
    }

    @Override
    public void refresh() {
        removeChildrenFrom(gallery);

        AddressTemplate template = AddressTemplate.of("{domain.controller}");
        Operation productInfo = new Operation.Builder(template.resolve(statementContext), PRODUCT_INFO).build();
        dispatcher.execute(productInfo)
                .then(result -> {
                    ModelNode summary = result.asList().get(0).get("summary");
                    gallery.add(hostInfo(summary));
                    gallery.add(jvmInfo(summary));
                    return null;
                }).catch_(error -> {
                    gallery.add(dashboardEmptyState()
                            .status(danger)
                            .text("Runtime error")
                            .addBody(emptyStateBody().add(errorCode(String.valueOf(error)))));
                    return null;
                });
    }

    private Card hostInfo(ModelNode result) {
        return card().addTitle(cardTitle().text("Host"))
                .addBody(cardBody().add(descriptionList()
                        .addItem(descriptionListGroup("host-name")
                                .addTerm(descriptionListTerm("Name"))
                                .addDescription(descriptionListDescription(
                                        result.get("host-operating-system").asString())))
                        .addItem(descriptionListGroup("host-arch")
                                .addTerm(descriptionListTerm("Architecture"))
                                .addDescription(descriptionListDescription(
                                        ModelNodeHelper.nested(result, "host-cpu.host-cpu-arch").asString())))
                        .addItem(descriptionListGroup("host-cores")
                                .addTerm(descriptionListTerm("Cores"))
                                .addDescription(descriptionListDescription(
                                        ModelNodeHelper.nested(result, "host-cpu.host-core-count").asString())))));
    }

    private Card jvmInfo(ModelNode result) {
        return card().addTitle(cardTitle().text("JVM"))
                .addBody(cardBody().add(descriptionList()
                        .addItem(descriptionListGroup("jvm-name")
                                .addTerm(descriptionListTerm("Name"))
                                .addDescription(descriptionListDescription(
                                        ModelNodeHelper.nested(result, "jvm.name").asString())))
                        .addItem(descriptionListGroup("jvm-version")
                                .addTerm(descriptionListTerm("Version"))
                                .addDescription(descriptionListDescription(
                                        ModelNodeHelper.nested(result, "jvm.jvm-version").asString())))
                        .addItem(descriptionListGroup("jvm-vendor")
                                .addTerm(descriptionListTerm("Vendor"))
                                .addDescription(descriptionListDescription(
                                        ModelNodeHelper.nested(result, "jvm.jvm-vendor").asString())))));
    }
}

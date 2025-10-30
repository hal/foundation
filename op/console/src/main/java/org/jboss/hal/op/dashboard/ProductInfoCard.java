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

import org.jboss.hal.env.Environment;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardBody.cardBody;
import static org.patternfly.component.card.CardTitle.cardTitle;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;
import static org.patternfly.component.title.Title.title;
import static org.patternfly.style.Breakpoint.default_;
import static org.patternfly.style.Breakpoints.breakpoints;
import static org.patternfly.style.Size.xl;

class ProductInfoCard implements DashboardCard {

    private final HTMLElement root;

    ProductInfoCard(Environment environment) {
        this.root = card()
                .addTitle(cardTitle().run(ct -> ct.textDelegate().appendChild(title(2, xl, "Details").element())))
                .addBody(cardBody()
                        .add(descriptionList().columns(breakpoints(default_, 2))
                                .addItem(descriptionListGroup("name")
                                        .addTerm(descriptionListTerm("Name"))
                                        .addDescription(descriptionListDescription(environment.productName())))
                                .addItem(descriptionListGroup("operation=mode")
                                        .addTerm(descriptionListTerm("Operation mode"))
                                        .addDescription(descriptionListDescription(environment.operationMode().toString())))
                                .addItem(descriptionListGroup("version")
                                        .addTerm(descriptionListTerm("Version"))
                                        .addDescription(descriptionListDescription(environment.productVersion().toString())))
                                .addItem(descriptionListGroup("management-version")
                                        .addTerm(descriptionListTerm("Management version"))
                                        .addDescription(
                                                descriptionListDescription(environment.managementVersion().toString())))
                                .addItem(descriptionListGroup("console-version")
                                        .addTerm(descriptionListTerm("Console version"))
                                        .addDescription(
                                                descriptionListDescription(environment.applicationVersion().toString())))
                                .addItem(descriptionListGroup("stability")
                                        .addTerm(descriptionListTerm("Stability"))
                                        .addDescription(descriptionListDescription()
                                                .add(stabilityLabel(environment.serverStability()))))))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void refresh() {
        // nop
    }
}

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

import static org.jboss.elemento.Elements.a;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardBody.cardBody;
import static org.patternfly.component.card.CardFooter.cardFooter;
import static org.patternfly.component.card.CardTitle.cardTitle;
import static org.patternfly.component.divider.Divider.divider;
import static org.patternfly.component.divider.DividerType.hr;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;
import static org.patternfly.style.Breakpoint.default_;
import static org.patternfly.style.Breakpoints.breakpoints;

class EnvironmentCard implements DashboardCard {

    private final HTMLElement root;

    EnvironmentCard(Environment environment) {
        this.root = card()
                .addTitle(cardTitle().textContent("Details"))
                .addBody(cardBody()
                        .add(descriptionList().columns(breakpoints(default_, 2))
                                .addGroup(descriptionListGroup()
                                        .addTerm(descriptionListTerm("Name"))
                                        .addDescription(descriptionListDescription(environment.productName())))
                                .addGroup(descriptionListGroup()
                                        .addTerm(descriptionListTerm("Operation mode"))
                                        .addDescription(descriptionListDescription(environment.operationMode().toString())))
                                .addGroup(descriptionListGroup()
                                        .addTerm(descriptionListTerm("Version"))
                                        .addDescription(descriptionListDescription(environment.productVersion().toString())))
                                .addGroup(descriptionListGroup()
                                        .addTerm(descriptionListTerm("Management version"))
                                        .addDescription(
                                                descriptionListDescription(environment.managementVersion().toString())))
                                .addGroup(descriptionListGroup()
                                        .addTerm(descriptionListTerm("Console version"))
                                        .addDescription(
                                                descriptionListDescription(environment.applicationVersion().toString())))))
                .add(divider(hr))
                .addFooter(cardFooter()
                        .add(a("#").textContent("View settings")))
                .element();
    }

    @Override
    public void refresh() {
        // nop
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}

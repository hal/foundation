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
import org.jboss.hal.env.Environment;
import org.patternfly.layout.flex.AlignItems;
import org.patternfly.layout.flex.AlignSelf;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static org.jboss.elemento.Elements.a;
import static org.jboss.hal.resources.Urls.BROWSE_ISSUES;
import static org.jboss.hal.resources.Urls.DEVELOPER_MAILING_LIST;
import static org.jboss.hal.resources.Urls.FORUM;
import static org.jboss.hal.resources.Urls.GETTING_STARTED;
import static org.jboss.hal.resources.Urls.LATEST_NEWS;
import static org.jboss.hal.resources.Urls.WILDFLY_CATALOG;
import static org.jboss.hal.resources.Urls.WILDFLY_DOCUMENTATION;
import static org.jboss.hal.resources.Urls.WILDFLY_GUIDES;
import static org.jboss.hal.resources.Urls.WILDFLY_HOMEPAGE;
import static org.jboss.hal.resources.Urls.ZULIP_CHAT;
import static org.jboss.hal.resources.Urls.replaceVersion;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardBody.cardBody;
import static org.patternfly.component.card.CardHeader.cardHeader;
import static org.patternfly.component.card.CardTitle.cardTitle;
import static org.patternfly.component.divider.Divider.divider;
import static org.patternfly.component.divider.DividerType.hr;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.component.title.Title.title;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.FlexShorthand._1;
import static org.patternfly.style.Breakpoint.md;
import static org.patternfly.style.Breakpoints.breakpoints;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Orientation.vertical;
import static org.patternfly.style.Size.xl;

class DocumentationCard implements DashboardCard {

    private static final List<String[]> GENERAL_RESOURCES = asList(
            new String[]{"WildFly homepage", WILDFLY_HOMEPAGE},
            new String[]{"WildFly documentation", WILDFLY_DOCUMENTATION},
            new String[]{"WildFly catalog", WILDFLY_CATALOG},
            new String[]{"Latest news", LATEST_NEWS},
            new String[]{"Browse issues", BROWSE_ISSUES}
    );

    private static final List<String[]> GET_HELP = asList(
            new String[]{"Getting started", GETTING_STARTED},
            new String[]{"WildFly guides", WILDFLY_GUIDES},
            new String[]{"Join the forum", FORUM},
            new String[]{"Join Zulip chat", ZULIP_CHAT},
            new String[]{"Developer mailing list", DEVELOPER_MAILING_LIST}
    );

    private final HTMLElement root;

    DocumentationCard(Environment environment) {
        this.root = card().fullHeight().add(flex().css(util("h-100"))
                        .alignItems(AlignItems.stretch)
                        .alignSelf(AlignSelf.stretch)
                        .addItem(flexItem().flex(_1)
                                .add(card().fullHeight().plain()
                                        .addHeader(cardHeader().addTitle(cardTitle().run(ct -> ct.textDelegate()
                                                .appendChild(title(2, xl, "General Resources").element()))))
                                        .addBody(cardBody().add(list().plain()
                                                .addItems(GENERAL_RESOURCES, nu ->
                                                        listItem(Id.build("general-resources", nu[0]))
                                                                .add(a(replaceVersion(nu[1], environment.productVersionLink()),
                                                                        "_blank")
                                                                        .text(nu[0])))))))
                        .add(divider(hr).orientation(breakpoints(md, vertical)))
                        .addItem(flexItem().flex(_1)
                                .add(card().fullHeight().plain()
                                        .addHeader(cardHeader().addTitle(cardTitle().run(ct -> ct.textDelegate()
                                                .appendChild(title(2, xl, "Get Help").element()))))
                                        .addBody(cardBody().add(list().plain()
                                                .addItems(GET_HELP, nu ->
                                                        listItem(Id.build("get-help", nu[0]))
                                                                .add(a(replaceVersion(nu[1], environment.productVersionLink()),
                                                                        "_blank")
                                                                        .text(nu[0]))))))))
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

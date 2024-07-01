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

import org.jboss.hal.env.Environment;
import org.patternfly.layout.flex.AlignItems;
import org.patternfly.layout.flex.AlignSelf;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static org.jboss.elemento.Elements.a;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardBody.cardBody;
import static org.patternfly.component.card.CardHeader.cardHeader;
import static org.patternfly.component.card.CardTitle.cardTitle;
import static org.patternfly.component.divider.Divider.divider;
import static org.patternfly.component.divider.DividerType.hr;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.FlexShorthand._1;
import static org.patternfly.style.Breakpoint.md;
import static org.patternfly.style.Breakpoints.breakpoints;
import static org.patternfly.style.Orientation.vertical;

class DocumentationCard implements DashboardCard {

    private static final List<String[]> GENERAL_RESOURCES = asList(
            new String[]{"WildFly homepage", "https://www.wildfly.org"},
            new String[]{"WildFly documentation", "https://docs.wildfly.org/%v/"},
            new String[]{"Model reference", "https://docs.wildfly.org/%v/wildscribe"},
            new String[]{"Latest news", "https://www.wildfly.org/news/"},
            new String[]{"Browse issues", "https://issues.jboss.org/browse/WFLY"}
    );

    private static final List<String[]> GET_HELP = asList(
            new String[]{"Getting started", "https://www.wildfly.org/get-started/"},
            new String[]{"WildFly Guides", "https://www.wildfly.org/guides/"},
            new String[]{"Join the forum", "https://groups.google.com/forum/#!forum/wildfly"},
            new String[]{"Join Zulip chat", "https://wildfly.zulipchat.com/"},
            new String[]{"Developer mailing list", "https://lists.jboss.org/archives/list/wildfly-dev@lists.jboss.org/"}
    );

    private final String version;
    private final HTMLElement root;

    DocumentationCard(Environment environment) {
        this.version = environment.productVersionLink();
        this.root = card()
                .add(flex()
                        .alignItems(AlignItems.stretch)
                        .alignSelf(AlignSelf.stretch)
                        .addItem(flexItem().flex(_1)
                                .add(card().fullHeight().plain()
                                        .addHeader(cardHeader().addTitle(cardTitle().textContent("General Resources")))
                                        .addBody(cardBody()
                                                .add(list().plain()
                                                        .addItems(GENERAL_RESOURCES, tuple -> listItem()
                                                                .add(a(replaceVersion(tuple[1]), "_blank")
                                                                        .textContent(tuple[0])))))))
                        .add(divider(hr).orientation(breakpoints(md, vertical)))
                        .addItem(flexItem().flex(_1)
                                .add(card().fullHeight().plain()
                                        .addHeader(cardHeader().addTitle(cardTitle().textContent("Get Help")))
                                        .addBody(cardBody()
                                                .add(list().plain()
                                                        .addItems(GET_HELP, tuple -> listItem()
                                                                .add(a(replaceVersion(tuple[1]), "_blank")
                                                                        .textContent(tuple[0]))))))))
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

    private String replaceVersion(String url) {
        return url.replace("%v", version);
    }
}

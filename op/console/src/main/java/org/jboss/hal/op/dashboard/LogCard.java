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
import java.util.function.Supplier;

import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.patternfly.component.card.CardBody;
import org.patternfly.component.card.CardTitle;
import org.patternfly.icon.PredefinedIcon;
import org.patternfly.layout.flex.Flex;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.alert;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LINES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_LOG_FILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TAIL;
import static org.jboss.hal.op.dashboard.DashboardCard.dashboardEmptyState;
import static org.patternfly.component.Severity.warning;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardBody.cardBody;
import static org.patternfly.component.card.CardHeader.cardHeader;
import static org.patternfly.component.card.CardTitle.cardTitle;
import static org.patternfly.component.divider.Divider.divider;
import static org.patternfly.component.divider.DividerType.hr;
import static org.patternfly.component.emptystate.EmptyStateActions.emptyStateActions;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;
import static org.patternfly.component.title.Title.title;
import static org.patternfly.icon.IconSets.fas.checkCircle;
import static org.patternfly.icon.IconSets.fas.exclamationTriangle;
import static org.patternfly.icon.IconSets.fas.timesCircle;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.JustifyContent.center;
import static org.patternfly.layout.flex.SpaceItems.md;
import static org.patternfly.layout.flex.SpaceItems.sm;
import static org.patternfly.style.Orientation.vertical;
import static org.patternfly.style.Size.xl;
import static org.patternfly.token.Token.chartGlobalDangerColor100;
import static org.patternfly.token.Token.chartGlobalWarningColor100;
import static org.patternfly.token.Token.globalColorStatusSuccess100;

class LogCard implements DashboardCard {

    private enum Status {
        ERROR("errors", () -> timesCircle().attr("color", chartGlobalDangerColor100.var)),
        WARN("warnings", () -> exclamationTriangle().attr("color", chartGlobalWarningColor100.var)),
        SKIP(null, null);

        final String text;
        final Supplier<PredefinedIcon> icon;

        Status(String text, Supplier<PredefinedIcon> icon) {
            this.text = text;
            this.icon = icon;
        }

        static Status parse(final String line) {
            if (line.contains("ERROR")) {
                return Status.ERROR;
            } else if (line.contains("WARN")) {
                return Status.WARN;
            } else {
                return Status.SKIP;
            }
        }
    }

    private final Dispatcher dispatcher;
    private final CardTitle cardTitle;
    private final CardBody cardBody;
    private final HTMLElement root;

    LogCard(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.root = card()
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
        String logFile = "server.log";
        cardTitle.run(ct -> ct.textDelegate().appendChild(title(2, xl, logFile).element()));
        removeChildrenFrom(cardBody);

        ResourceAddress address = AddressTemplate.of("subsystem=logging/log-file=" + logFile).resolve();
        Operation operation = new Operation.Builder(address, READ_LOG_FILE)
                .param(LINES, 100)
                .param(TAIL, true)
                .build();
        dispatcher.execute(operation,
                result -> {
                    Map<Status, Long> statusMap = result.asList()
                            .stream()
                            .map(node -> Status.parse(node.asString()))
                            .filter(status -> status != Status.SKIP)
                            .collect(groupingBy(identity(), counting()));
                    if (statusMap.isEmpty()) {
                        cardBody.add(flex().justifyContent(center).spaceItems(md)
                                .add(flex().spaceItems(sm)
                                        .add(flexItem()
                                                .add(checkCircle().attr("color", globalColorStatusSuccess100.var)))
                                        .add(div().text("No errors or warnings"))));
                    } else if (statusMap.size() == 1) {
                        Map.Entry<Status, Long> entry = statusMap.entrySet().iterator().next();
                        cardBody.add(flex().justifyContent(center).spaceItems(md)
                                .add(flex().spaceItems(sm)
                                        .add(flexItem().add(entry.getKey().icon.get()))
                                        .add(div().text(entry.getValue() + " " + entry.getKey().text))));
                    } else {
                        Flex flex = flex();
                        cardBody.add(flex.justifyContent(center).spaceItems(md));
                        for (Iterator<Map.Entry<Status, Long>> iterator = statusMap.entrySet().iterator();
                                iterator.hasNext(); ) {
                            Map.Entry<Status, Long> entry = iterator.next();
                            flex.add(flex().spaceItems(sm)
                                    .add(flexItem().add(entry.getKey().icon.get())));
                            flex.add(div().text(entry.getValue() + " " + entry.getKey().text));
                            if (iterator.hasNext()) {
                                flex.add(divider(hr).orientation(vertical));
                            }
                        }
                    }
                },
                (op, error) -> cardBody.add(dashboardEmptyState()
                        .status(warning)
                        .text("Log file not found")
                        .addBody(emptyStateBody()
                                .add("The log file ")
                                .add(code().text("server.log"))
                                .add(" was not found!"))
                        .addFooter(emptyStateFooter()
                                .addActions(emptyStateActions()
                                        .add(button("Choose log file")
                                                .link()
                                                .onClick((event, component) -> chooseLogFile()))))));
    }

    private void chooseLogFile() {
        // TODO Implement choose log file
        alert("Not yet implemented");
    }
}

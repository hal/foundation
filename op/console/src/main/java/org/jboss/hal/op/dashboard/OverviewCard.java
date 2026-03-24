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

import org.jboss.elemento.Attachable;
import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRepository;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.description.AttributeDescriptions;
import org.patternfly.component.card.Card;
import org.patternfly.component.card.CardBody;
import org.patternfly.component.list.DescriptionListDescription;
import org.patternfly.component.list.DescriptionListGroup;
import org.patternfly.style.Size;

import elemental2.dom.HTMLElement;
import elemental2.dom.MutationRecord;

import static java.util.Arrays.asList;
import static org.jboss.elemento.Elements.br;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.flow.Flow.parallel;
import static org.jboss.hal.core.LabelBuilder.labelBuilder;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.op.dashboard.Dashboard.dashboardEmptyState;
import static org.jboss.hal.op.dashboard.Dashboard.dlg;
import static org.jboss.hal.ui.BuildingBlocks.attributeDescriptionPopover;
import static org.jboss.hal.ui.BuildingBlocks.errorCode;
import static org.jboss.hal.ui.BuildingBlocks.AttributeDescriptionContent.descriptionOnly;
import static org.jboss.hal.ui.Format.duration;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardBody.cardBody;
import static org.patternfly.component.card.CardTitle.cardTitle;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;
import static org.patternfly.component.title.Title.title;
import static org.patternfly.icon.IconSets.fas.arrowUp;
import static org.patternfly.icon.IconSets.fas.cog;
import static org.patternfly.popper.Placement.auto;

class OverviewCard implements Attachable, AutoRefresh, DashboardCard {

    private static final String ROOT_KEY = "root";
    private static final String ENV_KEY = "env";
    private static final String RUNTIME_KEY = "runtime";
    private static final String PAYLOAD_KEY = "payload";

    private final Environment environment;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final MetadataRepository metadataRepository;
    private final Card card;
    private final CardBody cardBody;
    private DescriptionListDescription uptimeDld;
    private double intervalHandle;

    OverviewCard(Environment environment, StatementContext statementContext, Dispatcher dispatcher,
            MetadataRepository metadataRepository) {
        this.environment = environment;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.metadataRepository = metadataRepository;
        this.card = card()
                .addTitle(cardTitle()
                        .run(ct -> ct.textDelegate().appendChild(title(2, Size.xl, "Overview").element())))
                .addBody(cardBody = cardBody());
        Attachable.register(this, this);
    }

    @Override
    public void attach(MutationRecord mutationRecord) {
        // nop
    }

    @Override
    public void detach(MutationRecord mutationRecord) {
        stopAutoRefresh();
    }

    @Override
    public HTMLElement element() {
        return card.element();
    }

    // ------------------------------------------------------ refresh

    @Override
    public void refresh() {
        //noinspection DuplicatedCode
        removeChildrenFrom(cardBody);

        AddressTemplate rootTmpl = AddressTemplate.of("{domain.controller}");
        Task<FlowContext> rootTask = context -> metadataRepository.lookup(rootTmpl)
                .then(metadata -> context.resolve(ROOT_KEY, metadata));
        Operation rootOp = new Operation.Builder(rootTmpl.resolve(statementContext), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();

        AddressTemplate envTmpl = environment.standalone()
                ? AddressTemplate.of("core-service=server-environment")
                : AddressTemplate.of("{domain.controller}/core-service=host-environment");
        Task<FlowContext> envTask = context -> metadataRepository.lookup(envTmpl)
                .then(value -> context.resolve(ENV_KEY, value));
        Operation envOp = new Operation.Builder(envTmpl.resolve(statementContext), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();

        AddressTemplate runtimeTmpl = AddressTemplate.of("{domain.controller}/core-service=platform-mbean/type=runtime");
        Task<FlowContext> runtimeTask = context -> metadataRepository.lookup(runtimeTmpl)
                .then(value -> context.resolve(RUNTIME_KEY, value));
        Operation runtimeOp = new Operation.Builder(runtimeTmpl.resolve(statementContext), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();

        Task<FlowContext> payloadTask = context -> dispatcher
                .execute(new Composite(rootOp, envOp, runtimeOp))
                .then(result -> context.resolve(PAYLOAD_KEY, result));

        parallel(new FlowContext(), asList(rootTask, envTask, runtimeTask, payloadTask)).subscribe(context -> {
            if (context.isSuccessful()) {
                Metadata rootMeta = context.get(ROOT_KEY);
                Metadata envMeta = context.get(ENV_KEY);
                Metadata runtimeMeta = context.get(RUNTIME_KEY);
                CompositeResult payload = context.get(PAYLOAD_KEY);
                ModelNode rootNode = payload.step(0).get(RESULT);
                ModelNode envNode = payload.step(1).get(RESULT);
                ModelNode runtimeNode = payload.step(2).get(RESULT);
                AttributeDescriptions rootAttributes = rootMeta.resourceDescription().attributes();
                AttributeDescriptions envAttributes = envMeta.resourceDescription().attributes();
                AttributeDescriptions runtimeAttributes = runtimeMeta.resourceDescription().attributes();

                cardBody.add(descriptionList()
                        .autoFit()
                        .addItem(dlg(rootAttributes, rootNode, "product-name"))
                        .addItem(dlg(rootAttributes, rootNode, "product-version"))
                        .addItem(dlg(rootAttributes, rootNode, "name"))
                        .addItem(descriptionListGroup("console-version")
                                .addTerm(descriptionListTerm(labelBuilder("console-version")))
                                .addDescription(descriptionListDescription(environment.applicationVersion().toString())))
                        .addItem(dlg(envAttributes, "stability", dld ->
                                dld.add(stabilityLabel(environment.serverStability()))))
                        .addItem(descriptionListGroup("operation-mode")
                                .addTerm(descriptionListTerm(labelBuilder("operation-mode")))
                                .addDescription(descriptionListDescription(environment.operationMode().name())))
                        .addItem(configFileDlg(envAttributes, envNode))
                        .addItem(dlg(runtimeAttributes, "uptime", arrowUp(), dld -> {
                            uptimeDld = dld;
                            dld.text(duration(runtimeNode.get("uptime").asLong()));
                        })));
                intervalHandle = startAutoRefresh();

            } else {
                cardBody.add(dashboardEmptyState()
                        .status(danger)
                        .text("Runtime error")
                        .addBody(emptyStateBody().add(errorCode(context.failureReason())))
                        .element());
            }
        });
    }

    // ------------------------------------------------------ auto refresh

    @Override
    public double interval() {
        return 10_000;
    }

    @Override
    public double handle() {
        return intervalHandle;
    }

    @Override
    public void autoRefresh() {
        if (uptimeDld != null) {
            AddressTemplate template = AddressTemplate.of("{domain.controller}/core-service=platform-mbean/type=runtime");
            Operation operation = new Operation.Builder(template.resolve(statementContext), READ_RESOURCE_OPERATION)
                    .param(ATTRIBUTES_ONLY, true)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            dispatcher.execute(operation).then(result -> {
                uptimeDld.text(duration(result.get("uptime").asLong()));
                return null;
            });
        }
    }

    // ------------------------------------------------------ internals

    private DescriptionListGroup configFileDlg(AttributeDescriptions envAttributes, ModelNode envNode) {
        if (environment.standalone()) {
            return dlg(envAttributes, "config-file", cog(), dld -> {
                dld.text(filename(envNode.get("config-file").asString()));
            });
        } else {
            String domainConfig = filename(envNode.get("domain-config-file").asString());
            String hostConfig = filename(envNode.get("host-config-file").asString());
            String label = labelBuilder("config-file");
            return descriptionListGroup("config-file")
                    .addTerm(descriptionListTerm(label).icon(cog())
                            .help(attributeDescriptionPopover(label, envAttributes.get("domain-config-file"), descriptionOnly)
                                    .placement(auto)))
                    .addDescription(descriptionListDescription()
                            .add(domainConfig)
                            .add(br())
                            .add(hostConfig));
        }
    }

    private String filename(String value) {
        int lastSlash = value.lastIndexOf('/');
        int lastBackslash = value.lastIndexOf('\\');
        int index = Math.max(lastSlash, lastBackslash);
        return index != -1 ? value.substring(index + 1) : value;
    }
}

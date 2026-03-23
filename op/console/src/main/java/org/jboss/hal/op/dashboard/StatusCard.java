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
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRepository;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.description.AttributeDescriptions;
import org.jboss.hal.model.RunningMode;
import org.jboss.hal.model.RunningState;
import org.jboss.hal.model.RuntimeConfigurationState;
import org.jboss.hal.model.SuspendState;
import org.jboss.hal.model.host.Host;
import org.jboss.hal.model.server.Server;
import org.patternfly.component.card.Card;
import org.patternfly.component.card.CardBody;
import org.patternfly.component.list.DescriptionListDescription;
import org.patternfly.popper.Placement;

import elemental2.dom.HTMLElement;
import elemental2.dom.MutationRecord;

import static java.util.Arrays.asList;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.flow.Flow.parallel;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.op.dashboard.Dashboard.dashboardEmptyState;
import static org.jboss.hal.op.dashboard.Dashboard.dlg;
import static org.jboss.hal.ui.BuildingBlocks.AttributeDescriptionContent.descriptionOnly;
import static org.jboss.hal.ui.BuildingBlocks.attributeDescriptionPopover;
import static org.jboss.hal.ui.BuildingBlocks.errorCode;
import static org.jboss.hal.ui.BuildingBlocks.runningModeLabel;
import static org.jboss.hal.ui.BuildingBlocks.runningStateLabel;
import static org.jboss.hal.ui.BuildingBlocks.runtimeConfigurationStateLabel;
import static org.jboss.hal.ui.BuildingBlocks.suspendStateLabel;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardBody.cardBody;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;

class StatusCard implements Attachable, AutoRefresh, DashboardCard {

    private static final String METADATA_KEY = "metadata";
    private static final String PAYLOAD_KEY = "payload";

    private final Environment environment;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final MetadataRepository metadataRepository;
    private final Card card;
    private final CardBody cardBody;
    private DescriptionListDescription stateDld;
    private DescriptionListDescription runningModeDld;
    private DescriptionListDescription hostOrServerStateDld;
    private DescriptionListDescription suspendStateDld;
    private double intervalHandle;

    StatusCard(Environment environment, StatementContext statementContext, Dispatcher dispatcher,
            MetadataRepository metadataRepository) {
        this.environment = environment;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.metadataRepository = metadataRepository;
        this.card = card().addBody(cardBody = cardBody());
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

    @Override
    public void refresh() {
        //noinspection DuplicatedCode
        removeChildrenFrom(cardBody);

        AddressTemplate template = AddressTemplate.of("{domain.controller}");
        Task<FlowContext> metadataTask = context -> metadataRepository.lookup(template)
                .then(metadata -> context.resolve(METADATA_KEY, metadata));
        Operation operation = new Operation.Builder(template.resolve(statementContext), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Task<FlowContext> payloadTask = context -> dispatcher.execute(operation)
                .then(result -> context.resolve(PAYLOAD_KEY, result));

        parallel(new FlowContext(), asList(metadataTask, payloadTask)).subscribe(context -> {
            if (context.isSuccessful()) {
                Metadata metadata = context.get(METADATA_KEY);
                ModelNode payload = context.get(PAYLOAD_KEY);
                AttributeDescriptions rootAttributes = metadata.resourceDescription().attributes();
                RuntimeConfigurationState runtimeConfigurationState;
                String hostOrServerStateName;
                RunningState hostOrServerStateValue;
                RunningMode runningMode;
                SuspendState suspendState;
                if (environment.standalone()) {
                    Server server = Server.standalone().addServerAttributes(payload);
                    hostOrServerStateName = "server-state";
                    runtimeConfigurationState = server.getRuntimeConfigurationState();
                    hostOrServerStateValue = server.getServerState();
                    runningMode = server.getRunningMode();
                    suspendState = server.getSuspendState();
                } else {
                    Host host = new Host(payload);
                    hostOrServerStateName = "host-state";
                    runtimeConfigurationState = host.getRuntimeConfigurationState();
                    hostOrServerStateValue = host.getHostState();
                    runningMode = host.getRunningMode();
                    suspendState = host.getSuspendState();
                }
                cardBody.add(descriptionList()
                        .autoFit()
                        .addItem(descriptionListGroup("runtime-configuration-state")
                                .addTerm(descriptionListTerm("Configuration state")
                                        .help(attributeDescriptionPopover("Configuration state",
                                                rootAttributes.get("runtime-configuration-state"), descriptionOnly)
                                                .placement(Placement.auto)))
                                .addDescription(stateDld = descriptionListDescription()
                                        .add(runtimeConfigurationStateLabel(runtimeConfigurationState))))
                        .addItem(dlg(rootAttributes, "running-mode", dld -> {
                            runningModeDld = dld;
                            dld.add(runningModeLabel(runningMode));
                        }))
                        .addItem(dlg(rootAttributes, hostOrServerStateName, dld -> {
                            hostOrServerStateDld = dld;
                            dld.add(runningStateLabel(hostOrServerStateValue));
                        }))
                        .addItem(dlg(rootAttributes, "suspend-state", dld -> {
                            suspendStateDld = dld;
                            dld.add(suspendStateLabel(suspendState));
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
        return 5_000;
    }

    @Override
    public double handle() {
        return intervalHandle;
    }

    @Override
    public void autoRefresh() {
        AddressTemplate template = AddressTemplate.of("{domain.controller}");
        Operation operation = new Operation.Builder(template.resolve(statementContext), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        dispatcher.execute(operation)
                .then(result -> {
                    RuntimeConfigurationState runtimeConfigurationState;
                    RunningMode runningMode;
                    RunningState hostOrServerStateValue;
                    SuspendState suspendState;
                    if (environment.standalone()) {
                        Server server = Server.standalone().addServerAttributes(result);
                        runtimeConfigurationState = server.getRuntimeConfigurationState();
                        runningMode = server.getRunningMode();
                        hostOrServerStateValue = server.getServerState();
                        suspendState = server.getSuspendState();
                    } else {
                        Host host = new Host(result);
                        runtimeConfigurationState = host.getRuntimeConfigurationState();
                        hostOrServerStateValue = host.getHostState();
                        runningMode = host.getRunningMode();
                        suspendState = host.getSuspendState();
                    }
                    if (stateDld != null) {
                        removeChildrenFrom(stateDld);
                        stateDld.add(runtimeConfigurationStateLabel(runtimeConfigurationState));
                    }
                    if (runningModeDld != null) {
                        removeChildrenFrom(runningModeDld);
                        runningModeDld.add(runningModeLabel(runningMode));
                    }
                    if (hostOrServerStateDld != null) {
                        removeChildrenFrom(hostOrServerStateDld);
                        hostOrServerStateDld.add(runningStateLabel(hostOrServerStateValue));
                    }
                    if (suspendStateDld != null) {
                        removeChildrenFrom(suspendStateDld);
                        suspendStateDld.add(suspendStateLabel(suspendState));
                    }
                    return null;
                });
    }
}

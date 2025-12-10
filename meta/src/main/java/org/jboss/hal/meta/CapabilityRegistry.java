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
package org.jboss.hal.meta;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.elemento.flow.Flow;
import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.ParallelTasks;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.resources.Keys;

import elemental2.promise.Promise;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPENDENT_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.GET_PROVIDER_POINTS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUGGEST_CAPABILITIES;

@ApplicationScoped
public class CapabilityRegistry {

    private static final Logger logger = Logger.getLogger(CapabilityRegistry.class.getName());
    private static final AddressTemplate TEMPLATE = AddressTemplate.of("{domain.controller}/core-service=capability-registry");

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;

    @Inject
    public CapabilityRegistry(Dispatcher dispatcher, StatementContext statementContext) {
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
    }

    public Promise<List<String>> suggestCapabilities(AddressTemplate address, String capability) {
        ResourceAddress resourceAddress = address.resolve(statementContext);
        Operation operation = new Operation.Builder(TEMPLATE.resolve(statementContext), SUGGEST_CAPABILITIES)
                .param(NAME, capability)
                .param(DEPENDENT_ADDRESS, resourceAddress)
                .build();
        return dispatcher.execute(operation)
                .then(result -> {
                    if (result.isDefined()) {
                        List<String> capabilities = result.asList().stream().map(ModelNode::asString).collect(toList());
                        logger.debug("Capabilities for %s at %s: %s", capability, address, capabilities);
                        return Promise.resolve(capabilities);
                    } else {
                        return Promise.resolve(emptyList());
                    }
                });
    }

    public Promise<List<String>> providerPoints(String capability) {
        Operation operation = new Operation.Builder(TEMPLATE.resolve(statementContext), GET_PROVIDER_POINTS)
                .param(NAME, capability)
                .build();
        return dispatcher.execute(operation)
                .then(result -> {
                    if (result.isDefined()) {
                        List<String> providerPoints = result.asList().stream().map(ModelNode::asString).collect(toList());
                        logger.debug("Provider points for %s: %s", capability, providerPoints);
                        return Promise.resolve(providerPoints);
                    }
                    return Promise.resolve(emptyList());
                });
    }

    public Promise<List<AddressTemplate>> findResources(String capability, String value) {
        List<Task<FlowContext>> tasks = List.of(
                context -> providerPoints(capability).then(pps -> context.resolve(Keys.PROVIDER_POINTS, pps)),
                context -> {
                    List<String> providerPoints = context.get(Keys.PROVIDER_POINTS);
                    List<Task<FlowContext>> nestedTasks = providerPoints.stream()
                            .map(pp -> new ReadProviderPoint(dispatcher, capability, value, pp))
                            .collect(toList());
                    // nested parallel tasks to read the provider points
                    return new ParallelTasks<>(nestedTasks, false).apply(context);
                });

        List<AddressTemplate> templates = new ArrayList<>();
        FlowContext flowContext = new FlowContext();
        flowContext.set(Keys.ADDRESS_TEMPLATES, templates);
        return Flow.sequential(flowContext, tasks)
                .failFast(false)
                .then(context -> Promise.resolve(context.get(Keys.ADDRESS_TEMPLATES, emptyList())))
                .catch_(error -> {
                    logger.error("Unable to find resources for capability %s and value %s: %s", capability, value,
                            String.valueOf(error));
                    return Promise.resolve(emptyList());
                });
    }

    private static class ReadProviderPoint implements Task<FlowContext> {

        private final Dispatcher dispatcher;
        private final String capability;
        private final String providerPoint;
        private final String value;

        private ReadProviderPoint(Dispatcher dispatcher, String capability, String value, String providerPoint) {
            this.dispatcher = dispatcher;
            this.capability = capability;
            this.value = value;
            this.providerPoint = providerPoint;
        }

        @Override
        public Promise<FlowContext> apply(FlowContext context) {
            logger.debug("Read provider point %s for %s and %s", providerPoint, capability, value);
            List<AddressTemplate> foundTemplates = context.get(Keys.ADDRESS_TEMPLATES);
            ResourceAddress address = AddressTemplate.of(providerPoint).resolve();
            Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .param(ATTRIBUTES_ONLY, true)
                    .build();
            return dispatcher.execute(operation, false)
                    .then(result -> {
                        if (result.isDefined()) {
                            if (result.getType() == ModelType.LIST) {
                                List<AddressTemplate> templates = result.asList().stream()
                                        .filter(node -> node.hasDefined(ADDRESS))
                                        .map(node -> AddressTemplate.of(new ResourceAddress(node.get(ADDRESS))))
                                        .filter(template -> value.equals(template.last().value))
                                        .collect(toList());
                                if (!templates.isEmpty()) {
                                    foundTemplates.addAll(templates);
                                    logger.debug("Add %s for %s and %s to found templates", templates, capability, value);
                                }
                            } else if (result.getType() == ModelType.OBJECT) {
                                AddressTemplate template = AddressTemplate.of(new ResourceAddress(result.get(ADDRESS)));
                                foundTemplates.add(template);
                                logger.debug("Add %s for %s and %s to found templates", template, capability, value);
                            }
                        }
                        return context.resolve();
                    })
                    .catch_(error -> {
                        logger.debug("%s is not a valid resource for %s and %s", address, capability, value);
                        return context.resolve();  // ignore errors
                    });
        }
    }
}

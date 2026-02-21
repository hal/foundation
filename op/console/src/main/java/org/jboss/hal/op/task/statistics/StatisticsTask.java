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
package org.jboss.hal.op.task.statistics;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.elemento.flow.Flow;
import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.Notifications;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRepository;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.tree.ModelTree;
import org.jboss.hal.meta.tree.TraverseContinuation;
import org.jboss.hal.meta.tree.TraverseOperation;
import org.jboss.hal.meta.tree.TraverseType;
import org.jboss.hal.task.Task;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.op.task.statistics.ExpressionsSection.expressionsSection;
import static org.jboss.hal.op.task.statistics.ResourcesSection.resourcesSection;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.icon.IconSets.fas.chartLine;

// TODO Support domain mode
@Dependent
public class StatisticsTask implements Task {

    private static final Logger logger = Logger.getLogger(StatisticsTask.class.getName());
    public static final String TASK_ID = StatisticsTask.class.getName();

    final Set<String> expressions;
    final Map<AddressTemplate, ResourceData> resources;
    private final Dispatcher dispatcher;
    private final MetadataRepository metadataRepository;
    private final ModelTree modelTree;
    private final ExpressionsSection expressionsSection;
    private final ResourcesSection resourcesSection;

    @Inject
    public StatisticsTask(Dispatcher dispatcher,
            CrudOperations crud,
            MetadataRepository metadataRepository,
            ModelTree modelTree,
            Notifications notifications) {
        this.dispatcher = dispatcher;
        this.metadataRepository = metadataRepository;
        this.modelTree = modelTree;
        this.resources = new HashMap<>();
        this.expressions = new HashSet<>();
        this.expressionsSection = expressionsSection(this, dispatcher, crud);
        this.resourcesSection = resourcesSection(this, dispatcher, notifications);
    }

    @Override
    public String id() {
        return TASK_ID;
    }

    @Override
    public String title() {
        return "Statistics";
    }

    @Override
    public Element icon() {
        return chartLine().element();
    }

    @Override
    public HTMLElement summary() {
        return content(p)
                .add("Enable / disable statistics for all or a selection of subsystems.")
                .element();
    }

    @Override
    public Iterable<HTMLElement> elements() {
        return asList(header().element(), expressionsSection.element(), resourcesSection.element());
    }

    @Override
    public void run() {
        TraverseOperation<ModelNode> operation = (template, context) -> {
            if (template.fullyQualified()) {
                return dispatcher.execute(new Operation.Builder(template.resolve(context), READ_RESOURCE_OPERATION)
                                .param(ATTRIBUTES_ONLY, true)
                                .param(INCLUDE_RUNTIME, true)
                                .build())
                        .then(result -> Promise.resolve(result.asPropertyList().stream()
                                .filter(property -> STATISTICS_ENABLED.equals(property.getName()))
                                .map(Property::getValue)
                                .findFirst()
                                .orElse(new ModelNode())))
                        .catch_(error -> {
                            logger.error("Failed to read attributes of %s: %s", template, error);
                            return Promise.resolve(new ModelNode());
                        });
            } else {
                return Promise.resolve(new ModelNode());
            }
        };
        modelTree.traverse(new TraverseContinuation(), AddressTemplate.root(), singleton("/core-service"),
                        EnumSet.noneOf(TraverseType.class), operation,
                        (template, statisticsEnabled, context) -> {
                            if (template.fullyQualified() && statisticsEnabled.isDefined()) {
                                // Already add expressions and resources to the tables.
                                // Update the dropdowns later if we know whether the attributes support expressions.
                                ResourceData rd = new ResourceData(template, statisticsEnabled);
                                for (String expression : rd.expressions()) {
                                    addExpression(expression, false);
                                }
                                resources.put(rd.template, rd);
                                resourcesSection.addResource(rd);
                            }
                        })
                .then(context -> {
                    // We have collected all expressions and resources with a statistics-enabled attribute.
                    // We can update the count and the bulk expression dropdown now.
                    resourcesSection.count(resources.size());
                    resourcesSection.updateBulkExpressionDropdown(expressions);

                    // We don't know whether the attributes support expressions.
                    // So let's find out by looking at the metadata.
                    List<org.jboss.elemento.flow.Task<FlowContext>> tasks = resources.values().stream()
                            .map(rd -> (org.jboss.elemento.flow.Task<FlowContext>) fc ->
                                    metadataRepository.lookup(rd.template).then(md -> fc.resolve()))
                            .collect(toList());
                    return Flow.parallel(new FlowContext(), tasks)
                            .then(__ -> {
                                resources.values().forEach(rd -> {
                                    Metadata metadata = metadataRepository.get(rd.template); // get is safe now
                                    AttributeDescription description = metadata.resourceDescription().attributes()
                                            .get(STATISTICS_ENABLED);
                                    rd.expressionsAllowed = description.expressionAllowed();
                                    if (rd.expressionsAllowed) {
                                        // Now we can add dropdowns for those resources that support expressions.
                                        resourcesSection.addExpressionDropdown(rd, expressions);
                                    }
                                });
                                return null;
                            })
                            .catch_(error -> {
                                // TODO Error handling
                                return null;
                            });
                });
    }

    void addExpression(String expression, boolean updateDropdowns) {
        if (!expressions.contains(expression)) {
            expressions.add(expression);
            expressionsSection.addExpression(expression);
            if (updateDropdowns) {
                resourcesSection.updateExpressionDropdowns(expression);
            }
        }
    }
}

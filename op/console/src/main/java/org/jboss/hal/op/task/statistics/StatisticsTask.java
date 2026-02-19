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
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.elemento.logger.Logger;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.Notifications;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
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
import static org.jboss.hal.dmr.Expression.extractExpressions;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelType.EXPRESSION;
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

    final Set<String> distinctExpressions;
    final Set<String> distinctResources;
    private final Dispatcher dispatcher;
    private final ModelTree modelTree;
    private final ExpressionsSection expressionsSection;
    private final ResourcesSection resourcesSection;

    @Inject
    public StatisticsTask(Dispatcher dispatcher, CrudOperations crud, ModelTree modelTree, Notifications notifications) {
        this.dispatcher = dispatcher;
        this.modelTree = modelTree;
        this.distinctExpressions = new HashSet<>();
        this.distinctResources = new HashSet<>();
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
        clear();
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
                                if (statisticsEnabled.getType() == EXPRESSION) {
                                    String[] expressions = extractExpressions(statisticsEnabled.asString());
                                    if (expressions != null) {
                                        for (String expression : expressions) {
                                            expressionsSection.addExpression(expression);
                                        }
                                    }
                                }
                                resourcesSection.addResource(template, statisticsEnabled);
                            }
                        })
                .then(context -> {
                    resourcesSection.count();
                    // Now that we know all expressions, it's time to update the expression dropdowns
                    for (String expression : distinctExpressions) {
                        resourcesSection.updateExpressionMenus(expression);
                    }
                    return null;
                });
    }

    void updateExpressionMenus(String expression) {
        resourcesSection.updateExpressionMenus(expression);
    }

    private void clear() {
        distinctResources.clear();
        distinctExpressions.clear();
        expressionsSection.clear();
        resourcesSection.clear();
    }
}

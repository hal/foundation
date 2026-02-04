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
package org.jboss.hal.ui.resource;

import java.util.ArrayList;
import java.util.List;

import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.ParallelTasks;
import org.jboss.elemento.flow.Task;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Segment;
import org.patternfly.component.AsyncItems;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MenuList;

import elemental2.promise.IThenable.ThenOnFulfilledCallbackFn;
import elemental2.promise.Promise;

import static elemental2.promise.Promise.reject;
import static elemental2.promise.Promise.resolve;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.flow.Flow.sequential;
import static org.jboss.hal.core.Notification.error;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.resource.ResourceDialogs.addResourceModal;
import static org.jboss.hal.ui.resource.ResourceDialogs.addResourceWizard;
import static org.patternfly.component.menu.MenuItem.menuItem;

class CapabilityReferenceSupport {

    static AsyncItems<MenuList, MenuItem> capabilityItems(AddressTemplate template, String capability) {
        return menuList -> uic().capabilityRegistry().suggestCapabilities(template, capability)
                .then(capabilities -> resolve(capabilities.stream()
                        .sorted()
                        // make sure identifier == text/value
                        // this is important because we need to select menu items based on values
                        .map(c -> menuItem(c, c))
                        .collect(toList())));
    }

    static Promise<MenuItem> newItem(String value, String capability) {
        Task<FlowContext> providerPointsTask = context -> uic().capabilityRegistry().providerPoints(capability)
                .then(context::resolve);
        Task<FlowContext> resolveWildcardsTask = context -> {
            List<String> providerPoints = context.pop(emptyList());
            List<Task<FlowContext>> resolveWildcardsTasks = providerPoints.stream()
                    .map(CapabilityReferenceSupport::resolveWildcards)
                    .collect(toList());
            return new ParallelTasks<>(resolveWildcardsTasks, false).apply(context);
        };

        FlowContext initialContext = new FlowContext();
        initialContext.set("templates", new ArrayList<AddressTemplate>());
        return sequential(initialContext, asList(providerPointsTask, resolveWildcardsTask)).then(context -> {
            List<AddressTemplate> templates = context.get("templates");
            List<AddressTemplate> sortedTemplates = templates.stream()
                    .distinct()
                    .sorted(comparing(AddressTemplate::toString))
                    .collect(toList());
            if (sortedTemplates.isEmpty()) {
                uic().notifications().send(error("No provider points",
                        "No provider points found for " + capability + "."));
                return reject("No provider points");
            } else {
                ThenOnFulfilledCallbackFn<ModelNode, MenuItem> processNewResource = modelNode -> {
                    if (modelNode.isDefined()) {
                        String name = modelNode.get(NAME).asString();
                        return resolve(menuItem(name, name));
                    } else {
                        return reject("Add operation canceled");
                    }
                };
                if (sortedTemplates.size() == 1) {
                    return addResourceModal(sortedTemplates.get(0), value, false).then(processNewResource);
                } else {
                    return addResourceWizard(sortedTemplates, value).then(processNewResource);
                }
            }
        });
    }

    private static Task<FlowContext> resolveWildcards(String providerPoint) {
        AddressTemplate ppTemplate = AddressTemplate.of(providerPoint);
        if (ppTemplate.parent().fullyQualified()) {
            return context -> {
                List<AddressTemplate> templates = context.get("templates");
                templates.add(ppTemplate);
                return context.resolve();
            };
        } else {
            return context -> {
                AddressTemplate parent = ppTemplate.parent();
                Segment last = ppTemplate.last();
                return uic().modelTree().resolveWildcards(parent)
                        .then(currentTemplates -> {
                            List<AddressTemplate> allTemplates = context.get("templates");
                            for (AddressTemplate template : currentTemplates) {
                                allTemplates.add(template.append(last));
                            }
                            return context.resolve();
                        });
            };
        }
    }
}

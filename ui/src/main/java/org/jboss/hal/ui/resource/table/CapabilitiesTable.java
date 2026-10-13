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
package org.jboss.hal.ui.resource.table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.elemento.Attachable;
import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.flow.Flow;
import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.hal.core.mgt.CapabilityReference;
import org.jboss.hal.dmr.ResourceCheck;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.CapabilityDescription;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.resources.Dataset;
import org.jboss.hal.resources.Keys;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.SelectInTree;
import org.jboss.hal.ui.navigation.Highlight;
import org.patternfly.component.table.Table;
import org.patternfly.component.table.TableType;
import org.patternfly.component.table.Tbody;
import org.patternfly.component.table.Tr;
import org.patternfly.layout.flex.Flex;

import elemental2.dom.HTMLElement;
import elemental2.dom.MutationRecord;
import elemental2.promise.Promise;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.closest;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.brick.EmptyStateBricks.noItems;
import static org.jboss.hal.ui.component.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.navigation.Highlight.Type.ATTRIBUTE;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.table.Tbody.tbody;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.Th.th;
import static org.patternfly.component.table.Thead.thead;
import static org.patternfly.component.table.TitleCell.titleCell;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.layout.bullseye.Bullseye.bullseye;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.SpaceItems.sm;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Width.width10;
import static org.patternfly.style.Width.width30;
import static org.patternfly.style.Width.width60;

/**
 * Displays a table of capabilities provided by a resource in the model browser detail panel.
 * <p>
 * Each row shows the capability name, whether it is dynamic, and its dynamic elements. When MGT (model graph tools) is
 * available, each row becomes expandable and lazily loads the attributes and resources that reference the capability. When no
 * capabilities are available, an empty state is displayed.
 */
public class CapabilitiesTable implements Attachable, IsElement<HTMLElement> {

    private final Table table;
    private final Tbody tbody;

    public CapabilitiesTable(Metadata metadata, boolean mgtAvailable) {
        this.table = table(mgtAvailable ? TableType.treeTable : TableType.table)
                .addHead(thead().css(util("mt-sm"))
                        .addRow(tr("capabilities-head")
                                .addItem(th("name").width(width60).text("Name"))
                                .addItem(th("dynamic").width(width10).text("Dynamic"))
                                .addItem(th("dynamic-elements").width(width30).text("Dynamic elements"))))
                .addBody(tbody = tbody()
                        .run(tbody -> {
                            if (metadata.resourceDescription().capabilities().isEmpty()) {
                                tbody.addRow(tr(Id.unique("empty"))
                                        .addItem(td().colSpan(3)
                                                .add(bullseye()
                                                        .add(noItems("No capabilities",
                                                                "This resource provides no capabilities.")))));
                            } else {
                                tbody.addRows(metadata.resourceDescription().capabilities(),
                                        capability -> capabilityRow(metadata.resourceDescription(), capability,
                                                mgtAvailable));
                            }
                        }));
        Attachable.register(this, this);
    }

    @Override
    public void attach(MutationRecord mutationRecord) {
        HTMLElement ancestor = closest(table.element(), By.data(Dataset.highlight));
        if (ancestor != null) {
            String value = ancestor.dataset.get(Dataset.highlight);
            Highlight.parse(value).ifPresent(h -> {
                if (h.type() == Highlight.Type.CAPABILITY) {
                    for (Tr tr : tbody.items()) {
                        CapabilityDescription cd = tr.get(Keys.CAPABILITY_DESCRIPTION);
                        if (cd != null && h.name().equals(cd.name())) {
                            h.flash(ancestor, tr.element());
                            return;
                        }
                    }
                }
            });
        }
    }

    @Override
    public HTMLElement element() {
        return table.element();
    }

    private Tr capabilityRow(ResourceDescription resource, CapabilityDescription capability, boolean mgtAvailable) {
        Tr tr = tr(capability.name())
                .store(Keys.CAPABILITY_DESCRIPTION, capability);
        if (mgtAvailable) {
            tr.add(titleCell().add(capabilityName(resource, capability)));
            tr.addChildren(parent -> loadReferences(capability));
        } else {
            tr.addItem(td("Name").add(capabilityName(resource, capability)));
        }
        tr.addItem(td("Dynamic").text(String.valueOf(capability.dynamic())))
                .addItem(td("Dynamic elements").add(dynamicElements(capability)));
        return tr;
    }

    private Promise<Iterable<Tr>> loadReferences(CapabilityDescription capability) {
        return uic().modelGraphTools().capabilityReferences(capability.name())
                .then(references -> {
                    if (references.length == 0) {
                        return Promise.resolve(emptyList());
                    }

                    // group references by attribute name, preserving insertion order
                    Map<String, List<AddressTemplate>> byAttribute = new LinkedHashMap<>();
                    for (CapabilityReference ref : references) {
                        byAttribute.computeIfAbsent(ref.attributeName, k -> new ArrayList<>())
                                .add(AddressTemplate.ofTrusted(ref.resourceAddress));
                    }

                    // resolve wildcards per attribute group in parallel
                    String key = "groups";
                    List<String> attributeNames = new ArrayList<>(byAttribute.keySet());
                    List<Task<FlowContext>> tasks = attributeNames.stream()
                            .map(attrName -> (Task<FlowContext>) context -> {
                                List<AddressTemplate> templates = byAttribute.get(attrName);
                                return uic().modelTree().resolveWildcards(templates).then(resolved -> {
                                    Map<String, List<AddressTemplate>> groups = context.get(key);
                                    if (!resolved.isEmpty()) {
                                        groups.put(attrName, resolved);
                                    }
                                    return context.resolve();
                                });
                            })
                            .collect(toList());

                    FlowContext initialContext = new FlowContext();
                    initialContext.set(key, new LinkedHashMap<String, List<AddressTemplate>>());
                    return Flow.parallel(initialContext, tasks)
                            .then(context -> {
                                Map<String, List<AddressTemplate>> groups = context.get(key);
                                return checkExistence(groups);
                            });
                });
    }

    private Promise<Iterable<Tr>> checkExistence(Map<String, List<AddressTemplate>> groups) {
        // deduplicate templates within each group
        for (List<AddressTemplate> templates : groups.values()) {
            Set<String> seen = new HashSet<>();
            templates.removeIf(t -> !seen.add(t.template));
        }

        // check existence of all templates in parallel
        String key = "existing";
        FlowContext initialContext = new FlowContext();
        initialContext.set(key, new HashSet<String>());
        return Flow.parallel(initialContext, existsTasks(groups, key))
                .then(context -> {
                    Set<String> existing = context.get(key);
                    List<Tr> children = new ArrayList<>();
                    children.add(tr(Id.unique("ref", "desc"))
                            .addItem(td().colSpan(3)
                                    .text("This capability is referenced by the following attributes and resources. Existing resources are linked; non-existent resources are listed here for reference.")));
                    groups.entrySet().stream()
                            .filter(entry -> !entry.getValue().isEmpty())
                            .map(entry -> referenceRow(entry.getKey(), entry.getValue(), existing))
                            .forEach(children::add);
                    return Promise.resolve(children);
                });
    }

    private static List<Task<FlowContext>> existsTasks(Map<String, List<AddressTemplate>> groups, String key) {
        List<Task<FlowContext>> tasks = new ArrayList<>();
        for (List<AddressTemplate> templates : groups.values()) {
            for (AddressTemplate template : templates) {
                tasks.add(context -> new ResourceCheck(uic().dispatcher(), template.resolve()).apply(context)
                        .then(c -> {
                            int status = c.pop(404);
                            if (status == 200) {
                                Set<String> existing = c.get(key);
                                existing.add(template.template);
                            }
                            return c.resolve();
                        }));
            }
        }
        return tasks;
    }

    private Tr referenceRow(String attributeName, List<AddressTemplate> templates, Set<String> existing) {
        return tr(Id.unique("ref", attributeName))
                .addItem(td().colSpan(3)
                        .add(flex().spaceItems(sm)
                                .addItem(flexItem().text(attributeName + ":"))
                                .addItem(flexItem()
                                        .add(list().plain()
                                                .addItems(templates, template -> listItem()
                                                        .run(li -> {
                                                            if (existing.contains(template.template)) {
                                                                li.add(button().link().inline()
                                                                        .text(template.toString())
                                                                        .onClick((e, b) -> SelectInTree.dispatch(
                                                                                b.element(), template, new Highlight(ATTRIBUTE,
                                                                                        attributeName).toString())));
                                                            } else {
                                                                li.text(template.toString());
                                                            }
                                                        }))))));
    }

    private Flex capabilityName(ResourceDescription resource, CapabilityDescription capability) {
        if (uic().environment().highlightStability(resource.stability(), capability.stability())) {
            return flex().spaceItems(sm)
                    .addItem(flexItem().text(capability.name()))
                    .add(flexItem().add(stabilityLabel(capability.stability())));
        } else {
            return flex().add(capability.name());
        }
    }

    private org.patternfly.component.list.List dynamicElements(CapabilityDescription capability) {
        return list().plain()
                .addItems(capability.dynamicElements(), dynamicElement -> listItem().text(dynamicElement));
    }
}

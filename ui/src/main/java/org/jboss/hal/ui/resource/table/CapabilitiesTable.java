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
import java.util.SortedMap;
import java.util.TreeMap;

import org.jboss.elemento.Attachable;
import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.flow.Flow;
import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.hal.mgt.CapabilityReference;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Identifiers;
import org.jboss.hal.meta.description.CapabilityDescription;
import org.jboss.hal.meta.description.CapabilityDescriptions;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.resources.Dataset;
import org.jboss.hal.resources.Keys;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.SelectInTree;
import org.jboss.hal.ui.navigation.Highlight;
import org.patternfly.component.skeleton.Shape;
import org.patternfly.component.table.ExpandableRowContent;
import org.patternfly.component.table.Table;
import org.patternfly.component.table.TableType;
import org.patternfly.component.table.Tbody;
import org.patternfly.component.table.Tr;
import org.patternfly.core.Timeouts;
import org.patternfly.layout.flex.Flex;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.MutationRecord;
import elemental2.promise.Promise;

import static elemental2.dom.DomGlobal.clearTimeout;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.closest;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.brick.EmptyStateBricks.noItems;
import static org.jboss.hal.ui.component.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.navigation.Highlight.Type.ATTRIBUTE;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.component.skeleton.Skeleton.skeleton;
import static org.patternfly.component.table.ExpandableRowContent.expandableRowContent;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.table.TableCaption.tableCaption;
import static org.patternfly.component.table.Tbody.tbody;
import static org.patternfly.component.table.Td.expandableTd;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.Th.th;
import static org.patternfly.component.table.Thead.thead;
import static org.patternfly.component.table.Tr.expandableTr;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.layout.bullseye.Bullseye.bullseye;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.SpaceItems.sm;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Size.xs;
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
    private final Set<String> expandedCapabilities;

    public CapabilitiesTable(Metadata metadata, boolean mgtAvailable) {
        this.expandedCapabilities = new HashSet<>();
        this.table = table(mgtAvailable ? TableType.expandable : TableType.table)
                .addHead(thead().css(util("mt-sm"))
                        .addRow(tr(Identifiers.CAPABILITIES_HEAD)
                                .run(tr -> {
                                    if (mgtAvailable) {
                                        tr.addItem(th().screenReader("Row expansion"));
                                    }
                                })
                                .addItem(th("name").width(width60).text("Name"))
                                .addItem(th("dynamic").width(width10).text("Dynamic"))
                                .addItem(th("dynamic-elements").width(width30).text("Dynamic elements"))))
                .run(table -> {
                    int colspan = mgtAvailable ? 4 : 3;
                    if (metadata.resourceDescription().capabilities().isEmpty()) {
                        table.addBody(tbody().addRow(tr(Id.unique("empty"))
                                .addItem(td().colSpan(colspan)
                                        .add(bullseye()
                                                .add(noItems("No capabilities",
                                                        "This resource provides no capabilities.").size((xs)))))));
                    } else {
                        if (mgtAvailable) {
                            addBodies(metadata.resourceDescription(), metadata.resourceDescription().capabilities(), table);
                        } else {
                            addRows(metadata.resourceDescription(), metadata.resourceDescription().capabilities(), table);
                        }
                    }
                });
        Attachable.register(this, this);
    }

    @Override
    public void attach(MutationRecord mutationRecord) {
        HTMLElement ancestor = closest(table.element(), By.data(Dataset.highlight));
        if (ancestor != null) {
            String value = ancestor.dataset.get(Dataset.highlight);
            Highlight.parse(value).ifPresent(h -> {
                if (h.type() == Highlight.Type.CAPABILITY) {
                    for (Tbody tbody : table.items()) {
                        for (Tr tr : tbody.items()) {
                            CapabilityDescription cd = tr.get(Keys.CAPABILITY_DESCRIPTION);
                            if (cd != null && h.name().equals(cd.name())) {
                                h.flash(ancestor, tr.element());
                                return;
                            }
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

    // ------------------------------------------------------ capabilities

    private void addBodies(ResourceDescription resource, CapabilityDescriptions capabilities, Table table) {
        table.addItems(capabilities, capability -> {
            ExpandableRowContent expandableRowContent = expandableRowContent();
            return tbody()
                    .addRow(tr(capability.name())
                            .store(Keys.CAPABILITY_DESCRIPTION, capability)
                            .onToggle((e, tr, expanded) -> toggleReferences(capability, expanded, expandableRowContent))
                            .addItem(expandableTd())
                            .addItem(td("Name").add(capabilityName(resource, capability)))
                            .addItem(td("Dynamic").text(String.valueOf(capability.dynamic())))
                            .addItem(td("Dynamic elements").add(dynamicElements(capability))))
                    .addRow(expandableTr(Id.build(capability.name(), "references"))
                            .addItem(td().text(""))
                            .addItem(td().colSpan(3)
                                    .addExpandableRowContent(expandableRowContent.noPadding())));
        });
    }

    private void addRows(ResourceDescription resource, CapabilityDescriptions capabilities, Table table) {
        table.addBody(tbody().addRows(capabilities, capability -> tr(capability.name())
                .store(Keys.CAPABILITY_DESCRIPTION, capability)
                .addItem(td("Name").add(capabilityName(resource, capability)))
                .addItem(td("Dynamic").text(String.valueOf(capability.dynamic())))
                .addItem(td("Dynamic elements").add(dynamicElements(capability)))
        ));
    }

    // ------------------------------------------------------ references

    private void toggleReferences(CapabilityDescription capability, boolean expanded, ExpandableRowContent content) {
        if (expanded && !expandedCapabilities.contains(capability.name())) {
            double handle = DomGlobal.setTimeout(__ -> content.add(skeleton()
                    .shape(Shape.square)
                    .screenReaderText("Loading references")), Timeouts.LOADING_TIMEOUT);

            loadReferences(capability).then(groups -> {
                clearTimeout(handle);
                removeChildrenFrom(content);
                expandedCapabilities.add(capability.name());

                if (groups.isEmpty()) {
                    // TODO Remove first empty td and adjust colspan from 3 to 4
                    content.add(noItems("No references", "No references found for this capability").size(xs));
                } else {
                    content.add(table().compact().noBorders()
                            .addCaption(tableCaption().text(
                                    "This capability can be referenced by the following attributes and resources."))
                            .addHead(thead().addRow(tr(Id.build(capability.name(), "references", "head"))
                                    .addItem(th("attribute").text("Attribute"))
                                    .addItem(th("resources").text("Resources"))))
                            .addBody(tbody().addRows(groups.entrySet(), entry -> {
                                String attribute = entry.getKey();
                                List<AddressTemplate> templates = entry.getValue();
                                return tr(Id.build(capability.name(), "references", attribute))
                                        .addItem(td("attribute").text(attribute))
                                        .addItem(td("resources")
                                                .add(list().plain().addItems(templates, template ->
                                                        listItem().add(button().link().inline().text(template.toString())
                                                                .onClick((e, b) ->
                                                                        SelectInTree.dispatch(b.element(), template,
                                                                                new Highlight(ATTRIBUTE,
                                                                                        attribute).toString()))))));
                            })));
                }
                return null;
            });
        }
    }

    private Promise<Map<String, List<AddressTemplate>>> loadReferences(CapabilityDescription capability) {
        return uic().modelGraphCapability().capabilityReferences(capability.name()).then(references -> {
            if (references.length == 0) {
                return Promise.resolve(emptyMap());
            }

            // group references by attribute name, preserving insertion order
            SortedMap<String, List<AddressTemplate>> byAttribute = new TreeMap<>();
            for (CapabilityReference ref : references) {
                byAttribute.computeIfAbsent(ref.attributeName, k -> new ArrayList<>())
                        .add(AddressTemplate.ofTrusted(ref.resourceAddress));
            }

            // resolve wildcards per attribute group in parallel
            String key = "groups";
            List<Task<FlowContext>> tasks = byAttribute.keySet().stream()
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
            return Flow.parallel(initialContext, tasks).then(context -> {
                Map<String, List<AddressTemplate>> groups = context.get(key);
                // deduplicate templates within each group
                for (List<AddressTemplate> templates : groups.values()) {
                    Set<String> seen = new HashSet<>();
                    templates.removeIf(t -> !seen.add(t.template));
                    templates.sort(comparing(AddressTemplate::toString));
                }
                return Promise.resolve(groups);
            });
        });
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

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
package org.jboss.hal.ui.resource.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.ui.resource.data.AutoGrouping;
import org.patternfly.component.expandable.ExpandableSection;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.core.Humanize.capitalCase;
import static org.jboss.hal.core.Humanize.sentenceCase;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.patternfly.component.expandable.ExpandableSection.expandableSection;
import static org.patternfly.component.expandable.ExpandableSectionContent.expandableSectionContent;
import static org.patternfly.component.expandable.ExpandableSectionToggle.expandableSectionToggle;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.style.Breakpoint._2xl;
import static org.patternfly.style.Breakpoint.lg;
import static org.patternfly.style.Breakpoint.md;
import static org.patternfly.style.Breakpoint.sm;
import static org.patternfly.style.Breakpoint.xl;
import static org.patternfly.style.Breakpoints.breakpoints;
import static org.patternfly.style.Classes.group;
import static org.patternfly.style.Orientation.horizontal;
import static org.patternfly.style.Orientation.vertical;

/**
 * Builds the read-only view of resource attributes as a PatternFly description list. Handles both flat and grouped layouts
 * using expandable sections for attribute groups.
 */
public class ResourceView {

    private static final String UNGROUPED = "ungrouped";

    private final List<HTMLElement> groupContainers;

    public ResourceView() {
        this.groupContainers = new ArrayList<>();
    }

    public HTMLElement build(List<ViewItem> items, boolean grouped) {
        groupContainers.clear();
        if (grouped) {
            Map<String, List<ViewItem>> itemGroups;
            if (hasGroups(items)) {
                itemGroups = groupByMetadata(items);
            } else if (items.size() >= AutoGrouping.AUTO_GROUPING_THRESHOLD) {
                itemGroups = AutoGrouping.group(items,
                        item -> item.attribute().fqn(),
                        item -> sentenceCase(item.attribute().name()));
            } else {
                itemGroups = null;
            }
            if (itemGroups != null) {
                return buildGrouped(itemGroups);
            }
        }
        HTMLElement dl = createDescriptionList();
        for (ViewItem item : items) {
            dl.appendChild(item.element());
        }
        return dl;
    }

    private HTMLElement buildGrouped(Map<String, List<ViewItem>> itemGroups) {
        HTMLContainerBuilder<HTMLDivElement> container = div()
                .css(halComponent(HalClasses.resource, HalClasses.groups));
        for (Map.Entry<String, List<ViewItem>> entry : itemGroups.entrySet()) {
            String groupName = entry.getKey();
            List<ViewItem> groupItems = entry.getValue();
            HTMLElement dl = createDescriptionList();
            for (ViewItem item : groupItems) {
                dl.appendChild(item.element());
            }
            if (UNGROUPED.equals(groupName)) {
                container.add(dl);
            } else {
                ExpandableSection es = expandableSection()
                        .css(halComponent(HalClasses.resource, group))
                        .addToggle(expandableSectionToggle(capitalCase(groupName)))
                        .addContent(expandableSectionContent().add(dl));
                container.add(es);
                groupContainers.add(es.element());
            }
        }
        return container.element();
    }

    public List<HTMLElement> groupContainers() {
        return groupContainers;
    }

    // ------------------------------------------------------ static helpers

    public static boolean hasGroups(List<ViewItem> items) {
        for (ViewItem item : items) {
            if (item.attribute().description().group() != null) {
                return true;
            }
        }
        return false;
    }

    public static HTMLElement createDescriptionList() {
        return descriptionList()
                .orientation(breakpoints(sm, vertical, md, horizontal, lg, horizontal, xl, horizontal, _2xl, horizontal))
                .css(halComponent(HalClasses.resource, HalClasses.view))
                .element();
    }

    // ------------------------------------------------------ internal

    private static Map<String, List<ViewItem>> groupByMetadata(List<ViewItem> items) {
        List<ViewItem> ungrouped = new ArrayList<>();
        TreeMap<String, List<ViewItem>> namedGroups = new TreeMap<>();
        for (ViewItem item : items) {
            String grp = item.attribute().description().group();
            if (grp == null) {
                ungrouped.add(item);
            } else {
                namedGroups.computeIfAbsent(grp, k -> new ArrayList<>()).add(item);
            }
        }
        LinkedHashMap<String, List<ViewItem>> result = new LinkedHashMap<>();
        if (!ungrouped.isEmpty()) {
            result.put(UNGROUPED, ungrouped);
        }
        result.putAll(namedGroups);
        return result;
    }
}

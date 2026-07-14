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
import java.util.List;
import java.util.Map;

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.ui.resource.GroupingSupport;
import org.jboss.hal.ui.resource.pipeline.ResolvedAttribute;
import org.patternfly.component.expandable.ExpandableSection;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.hal.core.Humanize.capitalCase;
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
import static org.patternfly.style.Classes.filtered;
import static org.patternfly.style.Classes.group;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Orientation.horizontal;
import static org.patternfly.style.Orientation.vertical;

/**
 * Builds the read-only view of resource attributes as a PatternFly description list. Handles both flat and grouped layouts
 * using expandable sections for attribute groups.
 */
public class ResourceView {

    private final List<ViewItem> items;
    private final List<HTMLElement> groupContainers;

    public ResourceView() {
        this.items = new ArrayList<>();
        this.groupContainers = new ArrayList<>();
    }

    public HTMLElement build(List<ViewItem> viewItems, boolean grouped) {
        items.clear();
        items.addAll(viewItems);
        groupContainers.clear();

        Map<String, List<ViewItem>> itemGroups = GroupingSupport.resolveGroups(viewItems, grouped);
        if (itemGroups != null) {
            return buildGrouped(itemGroups);
        }
        HTMLElement dl = createDescriptionList();
        for (ViewItem item : viewItems) {
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
            if (GroupingSupport.UNGROUPED.equals(groupName)) {
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

    // ------------------------------------------------------ filtering

    /** Applies the filter to all items, toggling visibility. Returns the number of matching items. */
    public int applyFilter(Filter<ResolvedAttribute> filter) {
        int matchingItems = 0;
        for (ViewItem item : items) {
            boolean match = filter.match(item.attribute());
            item.element().classList.toggle(modifier(filtered), !match);
            if (match) {
                matchingItems++;
            }
        }
        for (HTMLElement container : groupContainers) {
            boolean hasVisibleItem = false;
            for (ViewItem item : items) {
                if (container.contains(item.element())
                        && !item.element().classList.contains(modifier(filtered))) {
                    hasVisibleItem = true;
                    break;
                }
            }
            setVisible(container, hasVisibleItem);
        }
        return matchingItems;
    }

    /** Clears all filter state, making all items and group containers visible. */
    public void clearFilter() {
        for (ViewItem item : items) {
            item.element().classList.remove(modifier(filtered));
        }
        for (HTMLElement container : groupContainers) {
            setVisible(container, true);
        }
    }

    // ------------------------------------------------------ static helpers

    public static HTMLElement createDescriptionList() {
        return descriptionList()
                .orientation(breakpoints(sm, vertical, md, horizontal, lg, horizontal, xl, horizontal, _2xl, horizontal))
                .css(halComponent(HalClasses.resource, HalClasses.view))
                .element();
    }
}

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
package org.jboss.hal.ui.resource.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.ui.resource.grouping.GroupingSupport;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.patternfly.component.alert.Alert;
import org.patternfly.component.expandable.ExpandableSection;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.hal.core.Humanize.capitalCase;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.alert.Alert.alert;
import static org.patternfly.component.expandable.ExpandableSection.expandableSection;
import static org.patternfly.component.expandable.ExpandableSectionContent.expandableSectionContent;
import static org.patternfly.component.expandable.ExpandableSectionToggle.expandableSectionToggle;
import static org.patternfly.component.form.Form.form;
import static org.patternfly.style.Classes.filtered;
import static org.patternfly.style.Classes.group;
import static org.patternfly.style.Classes.modifier;

/**
 * Builds and manages the editable form for resource attributes. Handles both flat and grouped layouts using expandable
 * sections for attribute groups. Provides validation, model node collection, and alert display.
 * <p>
 * Used by dialog classes (add resource, execute operation) and by {@code ResourceData} for inline editing.
 */
public class ResourceForm implements IsElement<HTMLElement> {

    private final List<FormItem> items;
    private final List<HTMLElement> groupContainers;
    private final org.patternfly.component.form.Form pfForm;

    public ResourceForm() {
        this.items = new ArrayList<>();
        this.groupContainers = new ArrayList<>();
        this.pfForm = form().css(halComponent(resource, HalClasses.form)).horizontal();
    }

    // ------------------------------------------------------ add items

    public ResourceForm addItem(FormItem item) {
        items.add(item);
        pfForm.add(item.element());
        return this;
    }

    public ResourceForm addItems(List<FormItem> formItems, boolean grouped) {
        items.addAll(formItems);
        groupContainers.clear();

        Map<String, List<FormItem>> itemGroups = GroupingSupport.resolveGroups(formItems, grouped);
        if (itemGroups != null) {
            addGrouped(itemGroups);
            return this;
        }
        for (FormItem item : formItems) {
            pfForm.add(item.element());
        }
        return this;
    }

    private void addGrouped(Map<String, List<FormItem>> itemGroups) {
        for (Map.Entry<String, List<FormItem>> entry : itemGroups.entrySet()) {
            String groupName = entry.getKey();
            List<FormItem> groupItems = entry.getValue();
            if (GroupingSupport.UNGROUPED.equals(groupName)) {
                for (FormItem item : groupItems) {
                    pfForm.add(item.element());
                }
            } else {
                HTMLContainerBuilder<HTMLDivElement> groupContent = div()
                        .css(halComponent(HalClasses.resource, HalClasses.groupBody));
                for (FormItem item : groupItems) {
                    groupContent.add(item.element());
                }
                ExpandableSection es = expandableSection()
                        .css(halComponent(HalClasses.resource, group))
                        .addToggle(expandableSectionToggle(capitalCase(groupName)))
                        .addContent(expandableSectionContent().add(groupContent));
                pfForm.add(es);
                groupContainers.add(es.element());
            }
        }
    }

    // ------------------------------------------------------ validation and data

    public void resetValidation() {
        items.forEach(FormItem::resetValidation);
    }

    public boolean validate() {
        return items.stream().allMatch(FormItem::validate);
    }

    public ModelNode modelNode() {
        ModelNode modelNode = new ModelNode();
        for (FormItem item : items) {
            if (item.isModified()) {
                ModelNode value = item.modelNode();
                if (value.isDefined()) {
                    modelNode.get(item.attribute().fqn()).set(value);
                }
            }
        }
        return modelNode;
    }

    public List<Operation> operations(ResourceAddress address) {
        return items.stream()
                .filter(FormItem::isModified)
                .flatMap(fi -> fi.operations(address).stream())
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------ alerts

    public void addAlert(Alert alert) {
        pfForm.add(alert);
    }

    public void validationAlert(String title) {
        pfForm.add(alert(danger, title).inline()
                .addDescription("Please fix the validation errors before saving."));
    }

    // ------------------------------------------------------ filtering

    /** Applies the filter to all items, toggling visibility. Returns the number of matching items. */
    public int applyFilter(Filter<ResolvedAttribute> filter) {
        int matchingItems = 0;
        for (FormItem item : items) {
            boolean match = filter.match(item.attribute());
            item.element().classList.toggle(modifier(filtered), !match);
            if (match) {
                matchingItems++;
            }
        }
        for (HTMLElement container : groupContainers) {
            boolean hasVisibleItem = false;
            for (FormItem item : items) {
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
        for (FormItem item : items) {
            item.element().classList.remove(modifier(filtered));
        }
        for (HTMLElement container : groupContainers) {
            setVisible(container, true);
        }
    }

    // ------------------------------------------------------ accessors

    public List<FormItem> items() {
        return items;
    }

    @Override
    public HTMLElement element() {
        return pfForm.element();
    }
}

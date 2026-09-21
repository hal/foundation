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
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.grouping.GroupingSupport;
import org.patternfly.component.alert.Alert;
import org.patternfly.component.expandable.ExpandableSection;
import org.patternfly.component.form.Form;
import org.patternfly.filter.Filter;
import org.patternfly.style.Classes;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLUListElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.li;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.ul;
import static org.jboss.hal.core.Humanize.capitalCase;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.alert.Alert.alert;
import static org.patternfly.component.alert.AlertDescription.alertDescription;
import static org.patternfly.component.expandable.ExpandableSection.expandableSection;
import static org.patternfly.component.expandable.ExpandableSectionContent.expandableSectionContent;
import static org.patternfly.component.expandable.ExpandableSectionToggle.expandableSectionToggle;
import static org.patternfly.component.form.Form.form;
import static org.patternfly.style.Classes.filtered;
import static org.patternfly.style.Classes.group;
import static org.patternfly.style.Classes.modifier;

/**
 * Builds and manages the editable form for resource attributes. Handles both flat and grouped layouts using expandable sections
 * for attribute groups. Provides validation, model node collection, and alert display.
 * <p>
 * Used by dialog classes (add resource, execute operation) and by {@code ResourceData} for inline editing.
 */
public class ResourceForm implements IsElement<HTMLElement> {

    private final Form form;
    private final List<FormItem> items;
    private final List<FormValidation> formValidations;
    private final List<FormValidation.Result> formValidationResults;
    private final List<HTMLElement> groupContainers;

    public ResourceForm() {
        this.form = form().css(halComponent(resource, Classes.form)).horizontal();
        this.items = new ArrayList<>();
        this.formValidations = new ArrayList<>();
        this.formValidationResults = new ArrayList<>();
        this.groupContainers = new ArrayList<>();
    }

    // ------------------------------------------------------ add items

    public ResourceForm addItem(FormItem item) {
        items.add(item);
        form.add(item.element());
        detectFormValidations();
        return this;
    }

    public ResourceForm addItems(List<FormItem> formItems, boolean grouped) {
        items.addAll(formItems);
        groupContainers.clear();

        Map<String, List<FormItem>> itemGroups = GroupingSupport.resolveGroups(formItems, grouped);
        if (itemGroups != null) {
            addGrouped(itemGroups);
        } else {
            for (FormItem item : formItems) {
                form.add(item.element());
            }
        }
        detectFormValidations();
        return this;
    }

    private void addGrouped(Map<String, List<FormItem>> itemGroups) {
        for (Map.Entry<String, List<FormItem>> entry : itemGroups.entrySet()) {
            String groupName = entry.getKey();
            List<FormItem> groupItems = entry.getValue();
            if (GroupingSupport.UNGROUPED.equals(groupName)) {
                for (FormItem item : groupItems) {
                    form.add(item.element());
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
                form.add(es);
                groupContainers.add(es.element());
            }
        }
    }

    // ------------------------------------------------------ validation

    /**
     * Resets all validation state: per-item validation on each {@link FormItem}, and form-level alerts. Called before each new
     * validation cycle.
     */
    public void resetValidation() {
        items.forEach(FormItem::resetValidation);
        form.clearAlerts();
    }

    /**
     * Validates the form in two phases:
     * <ol>
     *   <li><b>Per-item validation</b> — calls {@link FormItem#validate()} on each item to check individual field constraints
     *       (required fields, numeric ranges, expression syntax).</li>
     *   <li><b>Form-level validation</b> — runs each {@link FormValidation} to check cross-field constraints (requires,
     *       alternatives). Failed items are marked via {@link FormItem#showError(String)} with per-item error messages.</li>
     * </ol>
     * Returns {@code true} only if both phases pass with no errors. Form-level error messages are collected and rendered by
     * {@link #validationAlert(String)}.
     */
    public boolean validate() {
        boolean itemsValid = items.stream().allMatch(FormItem::validate);
        formValidationResults.clear();
        for (FormValidation validation : formValidations) {
            FormValidation.Result result = validation.validate(items);
            if (result != null) {
                formValidationResults.add(result);
                for (Map.Entry<String, String> entry : result.itemErrors().entrySet()) {
                    FormItem item = FormValidation.findItem(items, entry.getKey());
                    if (item != null) {
                        item.showError(entry.getValue());
                    }
                }
            }
        }
        return itemsValid && formValidationResults.isEmpty();
    }

    private void detectFormValidations() {
        formValidations.clear();
        formValidations.addAll(RequiredByValidation.fromItems(items));
        formValidations.addAll(NotMoreThanOneAlternativeValidation.fromItems(items));
        formValidations.addAll(ExactlyOneAlternativeValidation.fromItems(items));
    }

    // ------------------------------------------------------ data

    public ModelNode modelNode() {
        ModelNode payload = new ModelNode();
        for (FormItem item : items) {
            if (item.isModified()) {
                item.contributeToPayload(payload);
            }
        }
        return payload;
    }

    public List<Operation> operations(ResourceAddress address) {
        return items.stream()
                .filter(FormItem::isModified)
                .flatMap(fi -> fi.operations(address).stream())
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------ alerts

    public void addAlert(Alert alert) {
        form.addAlert(alert);
    }

    public void validationAlert(String title) {
        if (formValidationResults.isEmpty()) {
            addAlert(alert(danger, title).inline()
                    .addDescription("Please fix the validation errors before saving."));
        } else if (formValidationResults.size() == 1) {
            addAlert(alert(danger, title).inline()
                    .addDescription(formValidationResults.get(0).message()));
        } else {
            Alert validationAlert = alert(danger, title).inline();
            HTMLContainerBuilder<HTMLUListElement> list = ul();
            for (FormValidation.Result result : formValidationResults) {
                list.add(li().text(result.message()));
            }
            validationAlert.addDescription(alertDescription().add(list));
            addAlert(validationAlert);
        }
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

    public FormItem item(String identifier) {
        return items.stream()
                .filter(item -> item.identifier().equals(identifier))
                .findFirst()
                .orElse(null);
    }

    public List<FormItem> items() {
        return items;
    }

    @Override
    public HTMLElement element() {
        return form.element();
    }
}

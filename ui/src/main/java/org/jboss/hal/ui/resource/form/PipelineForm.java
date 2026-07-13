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
import java.util.stream.Collectors;

import org.jboss.elemento.IsElement;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.resources.HalClasses;
import org.patternfly.component.alert.Alert;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.alert.Alert.alert;
import static org.patternfly.component.form.Form.form;

/**
 * Lightweight form wrapper for pipeline {@link FormItem}s. Provides the form element, validation, model node collection,
 * and alert display. Used by dialog classes and ResourceData as a replacement for the old ResourceForm.
 */
public class PipelineForm implements IsElement<HTMLElement> {

    private final List<FormItem> items;
    private final org.patternfly.component.form.Form pfForm;

    public PipelineForm() {
        this.items = new ArrayList<>();
        this.pfForm = form().css(halComponent(resource, HalClasses.form)).horizontal();
    }

    public PipelineForm addItem(FormItem item) {
        items.add(item);
        pfForm.add(item.element());
        return this;
    }

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

    public void addAlert(Alert alert) {
        pfForm.add(alert);
    }

    public void validationAlert(String title) {
        pfForm.add(alert(danger, title).inline()
                .addDescription("Please fix the validation errors before saving."));
    }

    @Override
    public HTMLElement element() {
        return pfForm.element();
    }
}

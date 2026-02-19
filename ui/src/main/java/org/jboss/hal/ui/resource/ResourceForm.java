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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.elemento.IsElement;
import org.jboss.elemento.TypedBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.resources.HalClasses;
import org.patternfly.component.AddItemHandler;
import org.patternfly.component.AurHandler;
import org.patternfly.component.HasItems;
import org.patternfly.component.RemoveItemHandler;
import org.patternfly.component.UpdateItemHandler;
import org.patternfly.component.alert.Alert;
import org.patternfly.component.form.Form;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelNodeHelper.nested;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.alert.Alert.alert;
import static org.patternfly.component.form.Form.form;
import static org.patternfly.component.form.FormAlert.formAlert;

/** Form to modify an existing resource */
class ResourceForm implements
        TypedBuilder<HTMLElement, ResourceForm>,
        IsElement<HTMLElement>,
        HasItems<HTMLElement, ResourceForm, FormItem> {

    private final AddressTemplate template;
    private final Map<String, FormItem> items;
    private final AurHandler<ResourceForm, FormItem> aur;
    private final Form form;

    ResourceForm(AddressTemplate template) {
        this.template = template;
        this.items = new LinkedHashMap<>();
        this.aur = new AurHandler<>(this);
        this.form = form().css(halComponent(resource, HalClasses.form))
                .horizontal();
    }

    @Override
    public HTMLElement element() {
        return form.element();
    }

    @Override
    public ResourceForm that() {
        return this;
    }

    // ------------------------------------------------------ add

    @Override
    public ResourceForm add(FormItem item) {
        items.put(item.identifier(), item);
        form.addItem(item.formGroup);
        return aur.added(item);
    }

    // ------------------------------------------------------ api

    @Override
    public Iterator<FormItem> iterator() {
        return items.values().iterator();
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean contains(String identifier) {
        return items.containsKey(identifier);
    }

    @Override
    public FormItem item(String identifier) {
        return items.get(identifier);
    }

    @Override
    public void updateItem(FormItem item) {
        replaceItemElement(item, (oldItem, newItem) -> {
            items.put(newItem.identifier(), newItem);
            aur.updated(oldItem, newItem);
        });
    }

    @Override
    public void removeItem(String identifier) {
        FormItem item = items.remove(identifier);
        failSafeRemoveFromParent(item);
        aur.removed(item);
    }

    @Override
    public void clear() {
        form.clear();
        Iterator<FormItem> iterator = items.values().iterator();
        while (iterator.hasNext()) {
            FormItem item = iterator.next();
            iterator.remove();
            aur.removed(item);
        }
    }

    // ------------------------------------------------------ events

    @Override
    public ResourceForm onAdd(AddItemHandler<ResourceForm, FormItem> onAdd) {
        return aur.onAdd(onAdd);
    }

    @Override
    public ResourceForm onUpdate(UpdateItemHandler<ResourceForm, FormItem> onUpdate) {
        return aur.onUpdate(onUpdate);
    }

    @Override
    public ResourceForm onRemove(RemoveItemHandler<ResourceForm, FormItem> onRemove) {
        return aur.onRemove(onRemove);
    }

    // ------------------------------------------------------ validation

    void resetValidation() {
        form.clearAlerts();
        items.values().forEach(FormItem::resetValidation);
    }

    boolean validate() {
        boolean valid = true;
        for (FormItem formItem : items.values()) {
            if (!formItem.ra.description.readOnly()) {
                // don't refactor to: `valid = valid && formItem.validate();` !!
                // the short circuit optimization prevents the validation of all form items
                valid = formItem.validate() && valid;
            }
        }
        return valid;
    }

    void validationAlert(String title) {
        addAlert(alert(danger, title).inline()
                .addDescription("There are validation errors. Please fix them and try again."));
    }

    void addAlert(Alert alert) {
        form.addAlert(formAlert().addAlert(alert));
    }

    // ------------------------------------------------------ data

    ModelNode modelNode() {
        ModelNode modelNode = new ModelNode();
        items.values().stream()
                .filter(FormItem::isModified)
                .filter(formItem -> formItem.modelNode().isDefined())
                .forEach(formItem -> nested(modelNode, formItem.ra.fqn, true).set(formItem.modelNode()));
        return modelNode;
    }

    List<Operation> attributeOperations() {
        return items.values().stream()
                .filter(FormItem::isModified)
                .map(formItem -> {
                    Operation operation;
                    ResourceAddress address = template.resolve();
                    ModelNode currentValue = formItem.modelNode();
                    if (currentValue.isDefined()) {
                        operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                                .param(NAME, formItem.ra.fqn)
                                .param(VALUE, currentValue)
                                .build();
                    } else {
                        operation = new Operation.Builder(address, UNDEFINE_ATTRIBUTE_OPERATION)
                                .param(NAME, formItem.ra.fqn)
                                .build();
                    }
                    return operation;
                })
                .collect(toList());
    }
}

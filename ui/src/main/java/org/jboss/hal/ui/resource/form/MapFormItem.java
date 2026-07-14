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

import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.label.Label;
import org.patternfly.component.textinputgroup.FilterInput;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KEY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAP_PUT_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAP_REMOVE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.textinputgroup.FilterInput.filterInput;

/**
 * Form item for free-form key-value map attributes. Uses a {@link FilterInput} with {@code key=value} parsing.
 * Each entry appears as a removable label. Produces granular {@code map-put} / {@code map-remove} DMR operations
 * for changed entries.
 */
public class MapFormItem extends AbstractFormItem {

    private final Map<String, String> originalEntries;
    private /*final*/ FilterInput filterInput;

    public MapFormItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        super(identifier, attribute, context);
        this.originalEntries = new LinkedHashMap<>();
        if (attribute.value().isDefined()) {
            for (Property property : attribute.value().asPropertyList()) {
                originalEntries.put(property.getName(), property.getValue().asString());
            }
        }
        defaultSetup();
    }

    // ------------------------------------------------------ setup

    @Override
    FormGroupControl readOnlyGroup() {
        return readOnlyGroupWithExpressionSwitch();
    }

    @Override
    FormGroupControl nativeGroup() {
        return formGroupControl().add(mapControl());
    }

    @Override
    HTMLElement nativeContainer() {
        if (nativeContainer == null) {
            nativeContainer = inputGroup()
                    .addItem(inputGroupItem().addButton(switchToExpressionModeButton()))
                    .addItem(inputGroupItem().fill().add(mapControl()))
                    .element();
        }
        return nativeContainer;
    }

    private FilterInput mapControl() {
        filterInput = filterInput(identifier)
                .applyTo(inputElement -> {
                    inputElement.autocomplete("off");
                    inputElement.placeholder("key=value");
                })
                .textToLabel(this::parseKeyValue)
                .allowDuplicates(false);
        if (attribute.value().isDefined()) {
            setLabels(originalEntries);
        } else if (attribute.description().nillable()) {
            filterInput.placeholder(UNDEFINED);
        }
        return filterInput;
    }

    private Label parseKeyValue(String text) {
        int eqIndex = text.indexOf('=');
        if (eqIndex <= 0) {
            return null;
        }
        String key = text.substring(0, eqIndex);
        String value = text.substring(eqIndex + 1);
        if (value.isEmpty()) {
            return null;
        }
        return Label.label(key + "=" + value).compact().closable();
    }

    // ------------------------------------------------------ validation

    @Override
    public void resetValidation() {
        super.resetValidation();
        if (filterInput != null) {
            filterInput.resetValidation();
        }
    }

    @Override
    public boolean validate() {
        if (requiredOnItsOwn()) {
            if (inputMode == InputMode.NATIVE) {
                if (currentEntries().isEmpty()) {
                    filterInput.validated(error);
                    formGroupControl.addHelperText(requiredHelperText());
                    return false;
                }
            }
        } else if (inputMode == InputMode.EXPRESSION) {
            return validateExpressionMode();
        }
        return true;
    }

    // ------------------------------------------------------ modification tracking

    @Override
    boolean isNativeModifiedForNew() {
        return !currentEntries().isEmpty();
    }

    @Override
    boolean isNativeModifiedForExisting(boolean wasDefined) {
        Map<String, String> current = currentEntries();
        if (wasDefined) {
            return !originalEntries.equals(current);
        } else {
            return !current.isEmpty();
        }
    }

    // ------------------------------------------------------ data

    @Override
    public ModelNode modelNode() {
        if (inputMode == InputMode.NATIVE) {
            Map<String, String> entries = currentEntries();
            if (entries.isEmpty()) {
                return new ModelNode();
            }
            ModelNode modelNode = new ModelNode();
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                modelNode.get(entry.getKey()).set(entry.getValue());
            }
            return modelNode;
        } else if (inputMode == InputMode.EXPRESSION) {
            return expressionModelNode();
        }
        return new ModelNode();
    }

    // ------------------------------------------------------ operations

    @Override
    public List<Operation> operations(ResourceAddress address) {
        if (!isModified()) {
            return java.util.Collections.emptyList();
        }
        Map<String, String> current = currentEntries();
        List<Operation> operations = new ArrayList<>();

        // Entries to remove: in original but not in current
        for (String key : originalEntries.keySet()) {
            if (!current.containsKey(key)) {
                operations.add(new Operation.Builder(address, MAP_REMOVE_OPERATION)
                        .param(NAME, attribute.name())
                        .param(KEY, key)
                        .build());
            }
        }

        // Entries to add or update: in current but not in original, or value changed
        for (Map.Entry<String, String> entry : current.entrySet()) {
            String originalValue = originalEntries.get(entry.getKey());
            if (originalValue == null || !originalValue.equals(entry.getValue())) {
                operations.add(new Operation.Builder(address, MAP_PUT_OPERATION)
                        .param(NAME, attribute.name())
                        .param(KEY, entry.getKey())
                        .param(VALUE, entry.getValue())
                        .build());
            }
        }

        return operations;
    }

    // ------------------------------------------------------ events

    @Override
    void afterSwitchedToNativeMode() {
        if (attribute.value().isDefined() && !attribute.expression()) {
            setLabels(originalEntries);
        } else if (attribute.description().nillable()) {
            filterInput.placeholder(UNDEFINED);
        }
    }

    // ------------------------------------------------------ internal

    private void setLabels(Map<String, String> entries) {
        filterInput.labelGroup().clear();
        List<String> labelTexts = entries.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(toList());
        filterInput.labelGroup().addItems(labelTexts,
                text -> filterInput.textToLabel().apply(text));
    }

    private Map<String, String> currentEntries() {
        Map<String, String> entries = new LinkedHashMap<>();
        for (Label label : filterInput.labelGroup().items()) {
            String text = label.text();
            int eqIndex = text.indexOf('=');
            if (eqIndex > 0) {
                entries.put(text.substring(0, eqIndex), text.substring(eqIndex + 1));
            }
        }
        return entries;
    }
}

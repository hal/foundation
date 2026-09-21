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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.ui.resource.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.help.HelperText;
import org.patternfly.component.label.Label;
import org.patternfly.component.textinputgroup.FilterInput;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.textinputgroup.FilterInput.filterInput;

/**
 * {@link NativeControl} for LIST attributes with all-simple-record value-type. Each list entry is a record of simple fields.
 * Records are encoded as comma-separated {@code key=value} pairs in a {@link FilterInput}, appearing as removable labels.
 */
public final class ListSimpleRecordControl implements NativeControl<FilterInput> {

    private static final String PAIR_SEP = ", ";
    private static final String KV_SEP = "=";

    private List<String> fieldNames;
    private List<String> originalLabels;

    @Override
    public FilterInput create(PipelineContext context, String identifier, ResolvedAttribute attribute) {
        fieldNames = extractFieldNames(attribute.description());
        originalLabels = new ArrayList<>();

        FilterInput fi = filterInput(identifier)
                .applyTo(inputElement -> {
                    inputElement.autocomplete("off");
                    inputElement.placeholder(fieldNames.stream()
                            .collect(Collectors.joining(PAIR_SEP, "", "")));
                })
                .textToLabel(this::parseRecord)
                .allowDuplicates(false);

        if (attribute.value().isDefined()) {
            List<ModelNode> items = attribute.value().asList();
            for (ModelNode item : items) {
                String labelText = recordToLabel(item);
                originalLabels.add(labelText);
            }
            setLabels(fi, originalLabels);
        } else if (attribute.description().nillable()) {
            fi.placeholder(UNDEFINED);
        }
        return fi;
    }

    @Override
    public HTMLElement element(FilterInput control) {
        return control.element();
    }

    @Override
    public ModelNode modelNode(FilterInput control, ResolvedAttribute attribute) {
        List<String> labels = currentLabels(control);
        if (labels.isEmpty()) {
            return new ModelNode();
        }
        ModelNode list = new ModelNode();
        list.setEmptyList();
        for (String labelText : labels) {
            ModelNode record = labelToRecord(labelText);
            if (record.isDefined()) {
                list.add(record);
            }
        }
        return list;
    }

    @Override
    public boolean isModifiedForNew(FilterInput control, ResolvedAttribute attribute) {
        return !currentLabels(control).isEmpty();
    }

    @Override
    public boolean isModifiedForExisting(FilterInput control, ResolvedAttribute attribute, boolean wasDefined) {
        List<String> current = currentLabels(control);
        if (wasDefined) {
            return !originalLabels.equals(current);
        }
        return !current.isEmpty();
    }

    @Override
    public boolean validate(FilterInput control, ResolvedAttribute attribute, FormGroupControl formGroupControl) {
        if (FormItemBricks.requiredOnItsOwn(attribute) && currentLabels(control).isEmpty()) {
            control.validated(error);
            formGroupControl.addHelperText(FormItemBricks.requiredHelperText(attribute));
            return false;
        }
        return true;
    }

    @Override
    public void resetValidation(FilterInput control) {
        control.resetValidation();
    }

    @Override
    public void disable(FilterInput control) {
        control.disabled();
    }

    @Override
    public void afterSwitchedToNativeMode(FilterInput control, ResolvedAttribute attribute) {
        if (attribute.value().isDefined() && !attribute.expression()) {
            setLabels(control, originalLabels);
        } else if (attribute.description().nillable()) {
            control.placeholder(UNDEFINED);
        }
    }

    @Override
    public HelperText nativeHelperText() {
        if (fieldNames != null && !fieldNames.isEmpty()) {
            String format = fieldNames.stream()
                    .map(f -> f + "=<value>")
                    .collect(Collectors.joining(PAIR_SEP));
            return HelperText.helperText("Format: " + format);
        }
        return HelperText.helperText("Add records as comma-separated key=value pairs.");
    }

    // ------------------------------------------------------ internal

    private List<String> extractFieldNames(AttributeDescription description) {
        List<String> names = new ArrayList<>();
        if (description.hasDefined(VALUE_TYPE) &&
                description.get(VALUE_TYPE).getType() == ModelType.OBJECT) {
            for (Property property : description.get(VALUE_TYPE).asPropertyList()) {
                names.add(property.getName());
            }
        }
        return names;
    }

    private String recordToLabel(ModelNode record) {
        if (record.isDefined() && record.getType() == ModelType.OBJECT) {
            return record.asPropertyList().stream()
                    .filter(p -> p.getValue().isDefined())
                    .map(p -> p.getName() + KV_SEP + p.getValue().asString())
                    .collect(Collectors.joining(PAIR_SEP));
        }
        return record.asString();
    }

    private ModelNode labelToRecord(String labelText) {
        ModelNode record = new ModelNode();
        String[] pairs = labelText.split(PAIR_SEP);
        for (String pair : pairs) {
            String trimmed = pair.trim();
            int eqIndex = trimmed.indexOf('=');
            if (eqIndex > 0) {
                String key = trimmed.substring(0, eqIndex);
                String value = trimmed.substring(eqIndex + 1);
                record.get(key).set(value);
            }
        }
        return record.isDefined() ? record : new ModelNode();
    }

    private Label parseRecord(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (!trimmed.contains("=")) {
            return null;
        }
        return Label.label(trimmed).compact().closable();
    }

    private List<String> currentLabels(FilterInput control) {
        List<String> labels = new ArrayList<>();
        for (Label label : control.labelGroup().items()) {
            labels.add(label.text());
        }
        return labels;
    }

    private void setLabels(FilterInput fi, List<String> labels) {
        fi.labelGroup().clear();
        fi.labelGroup().addItems(labels, text -> fi.textToLabel().apply(text));
    }
}

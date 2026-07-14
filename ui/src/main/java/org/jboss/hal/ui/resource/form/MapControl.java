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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.help.HelperText;
import org.patternfly.component.label.Label;
import org.patternfly.component.textinputgroup.FilterInput;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.patternfly.component.ValidationStatus.error;
import org.patternfly.component.help.HelperText;

import static org.patternfly.component.textinputgroup.FilterInput.filterInput;

/**
 * {@link NativeControl} for free-form key-value map attributes. Uses a {@link FilterInput} with {@code key=value} parsing.
 * Each entry appears as a removable label.
 */
public final class MapControl implements NativeControl<FilterInput> {

    private Map<String, String> originalEntries;

    /** Returns the original entries captured at creation time. Used by {@link MapOperationStrategy}. */
    Map<String, String> originalEntries() {
        return originalEntries;
    }

    @Override
    public FilterInput create(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        originalEntries = new LinkedHashMap<>();
        if (attribute.value().isDefined()) {
            for (Property property : attribute.value().asPropertyList()) {
                originalEntries.put(property.getName(), property.getValue().asString());
            }
        }
        FilterInput fi = filterInput(identifier)
                .applyTo(inputElement -> {
                    inputElement.autocomplete("off");
                    inputElement.placeholder("key=value");
                })
                .textToLabel(this::parseKeyValue)
                .allowDuplicates(false);
        if (attribute.value().isDefined()) {
            setLabels(fi, originalEntries);
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
        Map<String, String> entries = currentEntries(control);
        if (entries.isEmpty()) {
            return new ModelNode();
        }
        ModelNode modelNode = new ModelNode();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            modelNode.get(entry.getKey()).set(entry.getValue());
        }
        return modelNode;
    }

    @Override
    public boolean isModifiedForNew(FilterInput control, ResolvedAttribute attribute) {
        return !currentEntries(control).isEmpty();
    }

    @Override
    public boolean isModifiedForExisting(FilterInput control, ResolvedAttribute attribute, boolean wasDefined) {
        Map<String, String> current = currentEntries(control);
        if (wasDefined) {
            return !originalEntries.equals(current);
        }
        return !current.isEmpty();
    }

    @Override
    public boolean validate(FilterInput control, ResolvedAttribute attribute, FormGroupControl formGroupControl) {
        if (FormItemBricks.requiredOnItsOwn(attribute) && currentEntries(control).isEmpty()) {
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
    public void afterSwitchedToNativeMode(FilterInput control, ResolvedAttribute attribute) {
        if (attribute.value().isDefined() && !attribute.expression()) {
            setLabels(control, originalEntries);
        } else if (attribute.description().nillable()) {
            control.placeholder(UNDEFINED);
        }
    }

    @Override
    public HelperText helperText() {
        return HelperText.helperText("Use <key>=<value> to add new entries.");
    }

    // ------------------------------------------------------ internal

    Map<String, String> currentEntries(FilterInput control) {
        Map<String, String> entries = new LinkedHashMap<>();
        for (Label label : control.labelGroup().items()) {
            String text = label.text();
            int eqIndex = text.indexOf('=');
            if (eqIndex > 0) {
                entries.put(text.substring(0, eqIndex), text.substring(eqIndex + 1));
            }
        }
        return entries;
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

    private void setLabels(FilterInput fi, Map<String, String> entries) {
        fi.labelGroup().clear();
        List<String> labelTexts = entries.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(toList());
        fi.labelGroup().addItems(labelTexts, text -> fi.textToLabel().apply(text));
    }
}

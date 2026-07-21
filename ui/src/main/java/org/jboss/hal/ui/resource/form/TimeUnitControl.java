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

import java.util.List;

import org.jboss.elemento.Id;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.AttributeDescriptions;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.pipeline.TimeUnitProvider;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormSelect;
import org.patternfly.component.form.TextInput;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNIT;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.FormSelect.formSelect;
import static org.patternfly.component.form.FormSelectOption.formSelectOption;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.form.TextInputType.number;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;

/**
 * {@link NativeControl} for time-unit composite attributes. Combines a number input (time) with a unit dropdown.
 */
public final class TimeUnitControl implements NativeControl<HTMLElement> {

    private static final List<String> DEFAULT_UNITS = List.of(
            "NANOSECONDS", "MICROSECONDS", "MILLISECONDS", "SECONDS", "MINUTES", "HOURS", "DAYS");

    private TextInput timeInput;
    private FormSelect unitSelect;
    private long originalTime;
    private String originalUnit;

    @Override
    public HTMLElement create(PipelineContext context, String identifier, ResolvedAttribute attribute) {
        originalTime = TimeUnitProvider.time(attribute.value());
        originalUnit = TimeUnitProvider.unit(attribute.value());

        timeInput = textInput(number, Id.build(identifier, "time"))
                .run(ti -> {
                    ti.input().autocomplete("off");
                    ti.input().min(0);
                    ti.input().apply(e -> e.step = "1");
                    if (originalTime >= 0) {
                        ti.value(String.valueOf(originalTime));
                    }
                });

        List<String> allowedUnits = allowedUnits(attribute);
        unitSelect = formSelect(Id.build(identifier, "unit"))
                .run(fs -> fs.selectElement().attr("autocomplete", "off"))
                .addOptions(allowedUnits, u -> formSelectOption(u))
                .run(fs -> {
                    if (originalUnit != null) {
                        fs.value(originalUnit);
                    }
                });

        return inputGroup()
                .addItem(inputGroupItem().fill().addControl(timeInput))
                .addItem(inputGroupItem().addControl(unitSelect))
                .element();
    }

    @Override
    public HTMLElement element(HTMLElement control) {
        return control;
    }

    @Override
    public ModelNode modelNode(HTMLElement control, ResolvedAttribute attribute) {
        String timeStr = timeValue();
        if (timeStr.isEmpty()) {
            return new ModelNode();
        }
        ModelNode result = new ModelNode();
        result.get(TIME).set(Long.parseLong(timeStr));
        result.get(UNIT).set(unitValue());
        return result;
    }

    @Override
    public boolean isModifiedForNew(HTMLElement control, ResolvedAttribute attribute) {
        return !timeValue().isEmpty();
    }

    @Override
    public boolean isModifiedForExisting(HTMLElement control, ResolvedAttribute attribute, boolean wasDefined) {
        if (!wasDefined) {
            return !timeValue().isEmpty();
        }
        String currentTimeStr = timeValue();
        String currentUnit = unitValue();
        long currentTime = currentTimeStr.isEmpty() ? -1 : Long.parseLong(currentTimeStr);
        return currentTime != originalTime || !currentUnit.equals(originalUnit != null ? originalUnit : "");
    }

    @Override
    public boolean validate(HTMLElement control, ResolvedAttribute attribute, FormGroupControl formGroupControl) {
        String timeStr = timeValue();
        if (FormItemBricks.requiredOnItsOwn(attribute) && timeStr.isEmpty()) {
            timeInput.validated(error);
            formGroupControl.addHelperText(FormItemBricks.requiredHelperText(attribute));
            return false;
        }
        if (!timeStr.isEmpty()) {
            try {
                Long.parseLong(timeStr);
            } catch (NumberFormatException e) {
                timeInput.validated(error);
                formGroupControl.addHelperText(FormItemBricks.requiredHelperText(attribute));
                return false;
            }
        }
        return true;
    }

    @Override
    public void resetValidation(HTMLElement control) {
        timeInput.resetValidation();
        unitSelect.resetValidation();
    }

    private static List<String> allowedUnits(ResolvedAttribute attribute) {
        AttributeDescriptions nested = attribute.description().valueTypeAttributeDescriptions();
        AttributeDescription unitDescription = nested.get(UNIT);
        if (unitDescription != null && unitDescription.hasDefined(ALLOWED)) {
            return unitDescription.get(ALLOWED).asList().stream()
                    .map(ModelNode::asString)
                    .collect(toList());
        }
        return DEFAULT_UNITS;
    }

    private String timeValue() {
        return FormItemBricks.safeValue(timeInput);
    }

    private String unitValue() {
        return unitSelect.value() != null ? unitSelect.value() : "";
    }
}

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
import org.jboss.hal.ui.resource.pipeline.ResolvedAttribute;

import java.util.List;

import org.jboss.elemento.Id;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.AttributeDescriptions;
import org.jboss.hal.ui.resource.composite.TimeUnitAttribute;
import org.patternfly.component.form.FormSelect;
import org.patternfly.component.form.TextInput;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNIT;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.FormSelect.formSelect;
import static org.patternfly.component.form.FormSelectOption.formSelectOption;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.form.TextInputType.number;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;

/** Form item for time-unit composite attributes. Combines a number input (time) with a unit dropdown. */
public class TimeUnitFormItem extends AbstractFormItem {

    private static final List<String> DEFAULT_UNITS = List.of(
            "NANOSECONDS", "MICROSECONDS", "MILLISECONDS", "SECONDS", "MINUTES", "HOURS", "DAYS");

    private final TextInput timeInput;
    private final FormSelect unitSelect;
    private final long originalTime;
    private final String originalUnit;

    public TimeUnitFormItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        super(identifier, attribute, context);
        this.originalTime = TimeUnitAttribute.time(attribute.value());
        this.originalUnit = TimeUnitAttribute.unit(attribute.value());

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

        formGroupControl = formGroupControl()
                .addInputGroup(inputGroup()
                        .addItem(inputGroupItem().fill().addControl(timeInput))
                        .addItem(inputGroupItem().addControl(unitSelect)));

        formGroup = formGroup(identifier)
                .required(attribute.description().required())
                .addLabel(label())
                .addControl(formGroupControl);
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

    // ------------------------------------------------------ validation

    @Override
    public void resetValidation() {
        timeInput.resetValidation();
        unitSelect.resetValidation();
        if (formGroupControl != null) {
            formGroupControl.removeHelperText();
        }
    }

    @Override
    public boolean validate() {
        String timeValue = timeValue();
        if (requiredOnItsOwn() && timeValue.isEmpty()) {
            timeInput.validated(error);
            formGroupControl.addHelperText(requiredHelperText());
            return false;
        }
        if (!timeValue.isEmpty()) {
            try {
                Long.parseLong(timeValue);
            } catch (NumberFormatException e) {
                timeInput.validated(error);
                formGroupControl.addHelperText(requiredHelperText());
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------ data

    @Override
    boolean isNativeModifiedForNew() {
        return !timeValue().isEmpty();
    }

    @Override
    boolean isNativeModifiedForExisting(boolean wasDefined) {
        if (!wasDefined) {
            return !timeValue().isEmpty();
        }
        String currentTimeStr = timeValue();
        String currentUnit = unitValue();
        long currentTime = currentTimeStr.isEmpty() ? -1 : Long.parseLong(currentTimeStr);
        return currentTime != originalTime || !currentUnit.equals(originalUnit != null ? originalUnit : "");
    }

    @Override
    public ModelNode modelNode() {
        String timeStr = timeValue();
        if (timeStr.isEmpty()) {
            return new ModelNode();
        }
        ModelNode result = new ModelNode();
        result.get(TIME).set(Long.parseLong(timeStr));
        result.get(UNIT).set(unitValue());
        return result;
    }

    private String timeValue() {
        return timeInput.value() != null ? timeInput.value() : "";
    }

    private String unitValue() {
        return unitSelect.value() != null ? unitSelect.value() : "";
    }
}

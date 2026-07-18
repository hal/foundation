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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormSelect;
import org.patternfly.component.form.FormSelectOption;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.FormSelect.formSelect;
import static org.patternfly.component.form.FormSelectOption.formSelectOption;

/**
 * {@link NativeControl} for string attributes with a fixed set of allowed values, rendered as a select dropdown.
 */
public final class SelectControl implements NativeControl<FormSelect> {

    @Override
    public FormSelect create(PipelineContext context, String identifier, ResolvedAttribute attribute) {
        List<String> allowedValues = attribute.description().get(ALLOWED)
                .asList()
                .stream()
                .map(ModelNode::asString)
                .collect(toList());
        return formSelect(identifier)
                .run(fs -> {
                    fs.selectElement().attr("autocomplete", "off");
                    if (attribute.description().nillable() && !attribute.description().hasDefault()) {
                        fs.addOption(formSelectOption(UNDEFINED));
                    }
                })
                .addOptions(allowedValues, lbl -> FormSelectOption.formSelectOption(lbl, lbl))
                .run(fs -> {
                    if (attribute.value().isDefined()) {
                        fs.value(attribute.value().asString());
                    } else if (attribute.description().hasDefault()) {
                        fs.value(attribute.description().get(DEFAULT).asString());
                    } else if (attribute.description().nillable()) {
                        fs.value(UNDEFINED);
                    }
                });
    }

    @Override
    public HTMLElement element(FormSelect control) {
        return control.element();
    }

    @Override
    public ModelNode modelNode(FormSelect control, ResolvedAttribute attribute) {
        String value = control.value();
        if (UNDEFINED.equals(value)) {
            return new ModelNode();
        }
        return new ModelNode().set(value);
    }

    @Override
    public boolean isModifiedForNew(FormSelect control, ResolvedAttribute attribute) {
        String selectedValue = control.value();
        if (attribute.description().hasDefault()) {
            return !attribute.description().get(DEFAULT).asString().equals(selectedValue);
        }
        return !UNDEFINED.equals(selectedValue);
    }

    @Override
    public boolean isModifiedForExisting(FormSelect control, ResolvedAttribute attribute, boolean wasDefined) {
        String selectedValue = control.value();
        if (wasDefined) {
            return attribute.expression() || !attribute.value().asString().equals(selectedValue);
        }
        return !UNDEFINED.equals(selectedValue);
    }

    @Override
    public boolean validate(FormSelect control, ResolvedAttribute attribute, FormGroupControl formGroupControl) {
        if (FormItemBricks.requiredOnItsOwn(attribute) && UNDEFINED.equals(control.value())) {
            control.validated(error);
            formGroupControl.addHelperText(FormItemBricks.requiredHelperText(attribute));
            return false;
        }
        return true;
    }

    @Override
    public void resetValidation(FormSelect control) {
        control.resetValidation();
    }

    @Override
    public void afterSwitchedToNativeMode(FormSelect control, ResolvedAttribute attribute) {
        boolean wasDefined = attribute.value().isDefined();
        if (wasDefined && !attribute.expression()) {
            failSafeSelectValue(control, attribute.value().asString());
        } else {
            if (attribute.description().hasDefault()) {
                failSafeSelectValue(control, attribute.description().get(DEFAULT).asString());
            } else if (attribute.description().nillable()) {
                failSafeSelectValue(control, UNDEFINED);
            } else {
                control.selectFirstValue(false);
            }
        }
    }

    private void failSafeSelectValue(FormSelect control, String value) {
        if (control.containsValue(value)) {
            control.value(value, false);
        } else {
            control.selectFirstValue(false);
        }
    }
}

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

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.component.form.FormSelect;
import org.patternfly.component.form.FormSelectOption;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.ui.resource.FormItemFlags.Scope.EXISTING_RESOURCE;
import static org.jboss.hal.ui.resource.FormItemFlags.Scope.NEW_RESOURCE;
import static org.jboss.hal.ui.resource.FormItemInputMode.EXPRESSION;
import static org.jboss.hal.ui.resource.FormItemInputMode.NATIVE;
import static org.jboss.hal.ui.resource.HelperTexts.required;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.FormSelect.formSelect;
import static org.patternfly.component.form.FormSelectOption.formSelectOption;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;

class SelectFormItem extends FormItem {

    // The select control is created in the constructor by defaultSetup() -> nativeContainer() -> selectControl().
    // It's, so to speak, final and never null!
    private /*final*/ FormSelect selectControl;

    SelectFormItem(String identifier, ResourceAttribute ra, FormGroupLabel label, FormItemFlags flags) {
        super(identifier, ra, label, flags);
        defaultSetup();
    }

    @Override
    FormGroupControl readOnlyGroup() {
        return readOnlyGroupWithExpressionSwitch();
    }

    @Override
    FormGroupControl nativeGroup() {
        return formGroupControl().addControl(selectControl());
    }

    @Override
    HTMLElement nativeContainer() {
        if (nativeContainer == null) {
            nativeContainer = inputGroup()
                    .addItem(inputGroupItem().addButton(switchToExpressionModeButton()))
                    .addItem(inputGroupItem().fill().addControl(selectControl()))
                    .element();
        }
        return nativeContainer;
    }

    private FormSelect selectControl() {
        List<String> allowedValues = ra.description.get(ALLOWED)
                .asList()
                .stream()
                .map(ModelNode::asString)
                .collect(toList());
        selectControl = formSelect(identifier)
                .run(fs -> {
                    fs.selectElement().attr("autocomplete", "off");
                    if (ra.description.nillable() && !ra.description.hasDefault()) {
                        fs.addOption(formSelectOption(UNDEFINED));
                    }
                })
                .addOptions(allowedValues, lbl -> FormSelectOption.formSelectOption(lbl, lbl))
                .run(fs -> {
                    if (ra.value.isDefined()) {
                        fs.value(ra.value.asString());
                    } else if (ra.description.hasDefault()) {
                        fs.value(ra.description.get(DEFAULT).asString());
                    } else if (ra.description.nillable()) {
                        fs.value(UNDEFINED);
                    }
                });
        return selectControl;
    }

    // ------------------------------------------------------ validation

    @Override
    void resetValidation() {
        super.resetValidation();
        selectControl.resetValidation();
    }

    @Override
    boolean validate() {
        if (inputMode == NATIVE) {
            if (requiredOnItsOwn() && UNDEFINED.equals(selectControl.value())) {
                selectControl.validated(error);
                formGroupControl.addHelperText(required(ra));
                return false;
            }
        } else if (inputMode == EXPRESSION) {
            return validateExpressionMode();
        }
        return true;
    }

    // ------------------------------------------------------ data

    @Override
    boolean isModified() {
        if (ra.readable && !ra.description.readOnly()) {
            if (flags.scope == NEW_RESOURCE) {
                if (inputMode == NATIVE) {
                    String selectedValue = selectControl.value();
                    if (ra.description.hasDefault()) {
                        return !ra.description.get(DEFAULT).asString().equals(selectedValue);
                    } else {
                        return !UNDEFINED.equals(selectedValue);
                    }
                } else if (inputMode == EXPRESSION) {
                    return isExpressionModified();
                }
            } else if (flags.scope == EXISTING_RESOURCE) {
                boolean wasDefined = ra.value.isDefined();
                if (inputMode == NATIVE) {
                    String selectedValue = selectControl.value();
                    if (wasDefined) {
                        // modified if the original value was an expression or is different from the current user input
                        String originalValue = ra.value.asString();
                        return ra.expression || !originalValue.equals(selectedValue);
                    } else {
                        return !UNDEFINED.equals(selectedValue);
                    }
                } else if (inputMode == EXPRESSION) {
                    return isExpressionModified();
                }
            } else {
                unknownScope();
            }
        }
        return false;
    }

    @Override
    ModelNode modelNode() {
        if (inputMode == NATIVE) {
            String selectedValue = selectControl.value();
            if (UNDEFINED.equals(selectedValue)) {
                return new ModelNode();
            } else {
                return new ModelNode().set(selectedValue);
            }
        } else if (inputMode == EXPRESSION) {
            return expressionModelNode();
        }
        return new ModelNode();
    }

    // ------------------------------------------------------ events

    @Override
    @SuppressWarnings("DuplicatedCode")
    void afterSwitchedToNativeMode() {
        boolean wasDefined = ra.value.isDefined();
        if (wasDefined && !ra.expression) {
            String originalValue = ra.value.asString();
            failSafeSelectValue(originalValue);
        } else {
            if (ra.description.hasDefault()) {
                failSafeSelectValue(ra.description.get(DEFAULT).asString());
            } else if (ra.description.nillable()) {
                failSafeSelectValue(UNDEFINED);
            } else {
                selectControl.selectFirstValue(false);
            }
        }
    }

    private void failSafeSelectValue(String value) {
        if (selectControl.containsValue(value)) {
            selectControl.value(value, false);
        } else {
            selectControl.selectFirstValue(false);
        }
    }
}

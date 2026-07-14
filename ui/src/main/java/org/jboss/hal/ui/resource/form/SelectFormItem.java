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

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormSelect;
import org.patternfly.component.form.FormSelectOption;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.ui.resource.form.InputMode.EXPRESSION;
import static org.jboss.hal.ui.resource.form.InputMode.NATIVE;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.FormSelect.formSelect;
import static org.patternfly.component.form.FormSelectOption.formSelectOption;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;

/** Form item for editing string attributes with a fixed set of allowed values, rendered as a select dropdown. */
public class SelectFormItem extends AbstractFormItem {

    private /*final*/ FormSelect selectControl;

    public SelectFormItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        super(identifier, attribute, context);
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
        List<String> allowedValues = attribute.description().get(ALLOWED)
                .asList()
                .stream()
                .map(ModelNode::asString)
                .collect(toList());
        selectControl = formSelect(identifier)
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
        return selectControl;
    }

    // ------------------------------------------------------ validation

    @Override
    public void resetValidation() {
        super.resetValidation();
        selectControl.resetValidation();
    }

    @Override
    public boolean validate() {
        if (inputMode == NATIVE) {
            if (requiredOnItsOwn() && UNDEFINED.equals(selectControl.value())) {
                selectControl.validated(error);
                formGroupControl.addHelperText(requiredHelperText());
                return false;
            }
        } else if (inputMode == EXPRESSION) {
            return validateExpressionMode();
        }
        return true;
    }

    // ------------------------------------------------------ data

    @Override
    boolean isNativeModifiedForNew() {
        String selectedValue = selectControl.value();
        if (attribute.description().hasDefault()) {
            return !attribute.description().get(DEFAULT).asString().equals(selectedValue);
        } else {
            return !UNDEFINED.equals(selectedValue);
        }
    }

    @Override
    boolean isNativeModifiedForExisting(boolean wasDefined) {
        String selectedValue = selectControl.value();
        if (wasDefined) {
            return attribute.expression() || !attribute.value().asString().equals(selectedValue);
        } else {
            return !UNDEFINED.equals(selectedValue);
        }
    }

    @Override
    public ModelNode modelNode() {
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
    void afterSwitchedToNativeMode() {
        boolean wasDefined = attribute.value().isDefined();
        if (wasDefined && !attribute.expression()) {
            failSafeSelectValue(attribute.value().asString());
        } else {
            if (attribute.description().hasDefault()) {
                failSafeSelectValue(attribute.description().get(DEFAULT).asString());
            } else if (attribute.description().nillable()) {
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

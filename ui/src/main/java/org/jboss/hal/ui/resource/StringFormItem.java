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

import org.jboss.elemento.By;
import org.jboss.hal.dmr.ModelNode;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;

import static org.jboss.hal.dmr.Expression.containsExpression;
import static org.jboss.hal.ui.resource.FormItemInputMode.MIXED;
import static org.jboss.hal.ui.resource.HelperTexts.required;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.tooltip.Tooltip.tooltip;

class StringFormItem extends FormItem {

    StringFormItem(String identifier, ResourceAttribute ra, FormGroupLabel label, FormItemFlags flags) {
        super(identifier, ra, label, flags);
        inputMode = MIXED;

        if (ra.description.readOnly()) {
            formGroupControl = readOnlyGroup();
        } else {
            if (ra.description.expressionAllowed()) {
                formGroupControl = expressionGroup();
            } else {
                formGroupControl = normalGroup();
            }
        }
        formGroup = formGroup(identifier)
                .required(ra.description.required())
                .addLabel(label)
                .addControl(formGroupControl);
    }

    FormGroupControl readOnlyGroup() {
        return readOnlyGroupWithExpressionSwitch();
    }

    private FormGroupControl expressionGroup() {
        return formGroupControl()
                .addInputGroup(inputGroup()
                        .addItem(inputGroupItem().fill().addControl(textControl()))
                        .addItem(inputGroupItem().addButton(resolveExpressionButton()))
                        .add(tooltip(By.id(resolveExpressionId), "Resolve expression")));
    }

    private FormGroupControl normalGroup() {
        return formGroupControl().addControl(textControl());
    }

    // ------------------------------------------------------ validation

    @Override
    boolean validate() {
        if (requiredOnItsOwn() && emptyTextControl()) {
            textControl.validated(error);
            formGroupControl.addHelperText(required(ra));
            return false;
        }
        return true;
    }

    // ------------------------------------------------------ data

    @Override
    boolean isModified() {
        if (ra.readable && !ra.description.readOnly()) {
            // StringFormItem runs in mixed mode, so it's safe to delegate to isExpressionModified()
            return isExpressionModified();
        }
        return false;
    }

    @Override
    ModelNode modelNode() {
        String value = textControlValue();
        if (value.isEmpty()) {
            return new ModelNode();
        } else {
            if (containsExpression(value)) {
                return expressionModelNode();
            } else {
                return new ModelNode().set(value);
            }
        }
    }
}

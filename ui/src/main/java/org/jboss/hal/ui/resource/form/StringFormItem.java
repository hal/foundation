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

import org.jboss.elemento.By;
import org.jboss.hal.dmr.ModelNode;
import org.patternfly.component.form.FormGroupControl;

import static org.jboss.hal.dmr.Expression.containsExpression;
import static org.jboss.hal.ui.resource.form.InputMode.MIXED;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.tooltip.Tooltip.tooltip;

/** Form item for editing plain string attributes, rendered as a text input in mixed mode (literals and expressions). */
public class StringFormItem extends AbstractFormItem {

    public StringFormItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        super(identifier, attribute, context);
        inputMode = MIXED;

        if (attribute.description().readOnly()) {
            formGroupControl = readOnlyGroup();
        } else {
            if (attribute.description().expressionAllowed()) {
                formGroupControl = expressionGroup();
            } else {
                formGroupControl = normalGroup();
            }
        }
        formGroup = formGroup(identifier)
                .required(attribute.description().required())
                .addLabel(label())
                .addControl(formGroupControl);
    }

    @Override
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
    public boolean validate() {
        if (requiredOnItsOwn() && emptyTextControl()) {
            textControl.validated(error);
            formGroupControl.addHelperText(requiredHelperText());
            return false;
        }
        return true;
    }

    // ------------------------------------------------------ data

    @Override
    public boolean isModified() {
        if (attribute.readable() && !attribute.description().readOnly()) {
            return isExpressionModified();
        }
        return false;
    }

    @Override
    boolean isNativeModifiedForNew() {
        return false;
    }

    @Override
    boolean isNativeModifiedForExisting(boolean wasDefined) {
        return false;
    }

    @Override
    public ModelNode modelNode() {
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

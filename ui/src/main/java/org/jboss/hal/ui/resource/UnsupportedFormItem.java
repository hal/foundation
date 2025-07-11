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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.patternfly.component.form.FormControl;
import org.patternfly.component.form.FormGroupLabel;

import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.ui.resource.HelperTexts.unsupported;
import static org.patternfly.component.ValidationStatus.warning;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.TextArea.textArea;

class UnsupportedFormItem extends FormItem {

    UnsupportedFormItem(String identifier, ResourceAttribute ra, FormGroupLabel label, FormItemFlags flags) {
        super(identifier, ra, label, flags);

        FormControl<?, ?> control;
        ModelType type = ra.description.get(TYPE).asType();
        if (type == ModelType.LIST || type == ModelType.OBJECT) {
            String value = ra.value.toJSONString().replace("\\/", "/");
            int newLines = 0;
            for (int i = 0; i < value.length(); i++) {
                if (value.charAt(i) == '\n') {
                    newLines++;
                }
            }
            int rows;
            if (newLines == 0) {
                rows = 1;
            } else if (newLines > 1 && newLines < 10) {
                rows = 3;
            } else {
                rows = 5;
            }
            control = textArea(identifier)
                    .readonly() // the fallback form item is always read-only!
                    .validated(warning)
                    // TODO
                    // .applyTo(ta -> ta.element().rows = rows)
                    .run(ta -> {
                        if (ra.value.isDefined()) {
                            ta.value(value);
                        } else {
                            ta.placeholder("undefined");
                        }
                    });
        } else {
            control = textControl()
                    .readonly() // the fallback form item is always read-only!
                    .validated(warning);
        }
        formGroup = formGroup(identifier)
                .required(ra.description.required())
                .addLabel(label)
                .addControl(formGroupControl()
                        .addControl(control)
                        .addHelperText(unsupported()));
    }

    @Override
    boolean isModified() {
        return false;
    }

    @Override
    ModelNode modelNode() {
        return new ModelNode();
    }
}

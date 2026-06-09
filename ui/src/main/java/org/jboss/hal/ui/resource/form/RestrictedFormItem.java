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
import org.jboss.hal.ui.resource.ResourceAttribute;

import org.jboss.hal.dmr.ModelNode;
import org.patternfly.component.form.FormGroupLabel;

import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.inputgroup.InputGroupText.inputGroupText;
import static org.patternfly.icon.IconSets.fas.lock;

/** Form item displayed when the current user lacks read permission for the attribute, showing a locked "restricted" indicator. */
class RestrictedFormItem extends FormItem {

    RestrictedFormItem(String identifier, ResourceAttribute ra, FormGroupLabel label, FormItemFlags flags) {
        super(identifier, ra, label, flags);

        formGroup = formGroup(identifier)
                .required(ra.description.required())
                .addLabel(label)
                .addControl(formGroupControl()
                        .add(inputGroup()
                                .addItem(inputGroupItem().fill()
                                        .addControl(textInput(identifier)
                                                .value("restricted")
                                                .disabled()))
                                .addText(inputGroupText().icon(lock()).plain())));
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

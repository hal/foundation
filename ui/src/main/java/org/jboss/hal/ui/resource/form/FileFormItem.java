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

import org.jboss.elemento.Id;
import org.jboss.hal.dmr.ModelNode;
import org.patternfly.component.form.TextInput;

import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.inputgroup.InputGroupText.inputGroupText;

/**
 * Form item for file composite attributes ({@code {path, relative-to}}). Text input for path + text input for relative-to.
 * Produces a single write-attribute with the OBJECT value.
 */
public class FileFormItem extends AbstractFormItem {

    private final TextInput pathInput;
    private final TextInput relativeToInput;
    private final String originalPath;
    private final String originalRelativeTo;

    public FileFormItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        super(identifier, attribute, context);
        this.originalPath = attribute.value().hasDefined(PATH) ? attribute.value().get(PATH).asString() : "";
        this.originalRelativeTo = attribute.value().hasDefined(RELATIVE_TO)
                ? attribute.value().get(RELATIVE_TO).asString() : "";

        pathInput = textInput(Id.build(identifier, "path"))
                .run(ti -> {
                    ti.input().autocomplete("off");
                    if (!originalPath.isEmpty()) {
                        ti.value(originalPath);
                    }
                });

        relativeToInput = textInput(Id.build(identifier, "relative-to"))
                .run(ti -> {
                    ti.input().autocomplete("off");
                    ti.placeholder("relative to...");
                    if (!originalRelativeTo.isEmpty()) {
                        ti.value(originalRelativeTo);
                    }
                });

        formGroupControl = formGroupControl()
                .addInputGroup(inputGroup()
                        .addItem(inputGroupItem().fill().addControl(pathInput))
                        .addText(inputGroupText().plain().text("relative to"))
                        .addItem(inputGroupItem().fill().addControl(relativeToInput)));

        formGroup = formGroup(identifier)
                .required(attribute.description().required())
                .addLabel(label())
                .addControl(formGroupControl);
    }

    // ------------------------------------------------------ validation

    @Override
    public void resetValidation() {
        pathInput.resetValidation();
        relativeToInput.resetValidation();
        if (formGroupControl != null) {
            formGroupControl.removeHelperText();
        }
    }

    // ------------------------------------------------------ data

    @Override
    boolean isNativeModifiedForNew() {
        return !pathValue().isEmpty() || !relativeToValue().isEmpty();
    }

    @Override
    boolean isNativeModifiedForExisting(boolean wasDefined) {
        if (!wasDefined) {
            return !pathValue().isEmpty() || !relativeToValue().isEmpty();
        }
        return !originalPath.equals(pathValue()) || !originalRelativeTo.equals(relativeToValue());
    }

    @Override
    public ModelNode modelNode() {
        String path = pathValue();
        String relativeTo = relativeToValue();
        if (path.isEmpty() && relativeTo.isEmpty()) {
            return new ModelNode();
        }
        ModelNode result = new ModelNode();
        if (!path.isEmpty()) {
            result.get(PATH).set(path);
        }
        if (!relativeTo.isEmpty()) {
            result.get(RELATIVE_TO).set(relativeTo);
        }
        return result;
    }

    private String pathValue() {
        return pathInput.value() != null ? pathInput.value() : "";
    }

    private String relativeToValue() {
        return relativeToInput.value() != null ? relativeToInput.value() : "";
    }
}

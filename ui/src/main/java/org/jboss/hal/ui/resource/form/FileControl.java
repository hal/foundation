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

import org.jboss.elemento.Id;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.TextInput;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.inputgroup.InputGroupText.inputGroupText;

/**
 * {@link NativeControl} for file composite attributes ({@code {path, relative-to}}). Two text inputs side-by-side in an input
 * group.
 */
public final class FileControl implements NativeControl<HTMLElement> {

    private TextInput pathInput;
    private TextInput relativeToInput;
    private String originalPath;
    private String originalRelativeTo;

    @Override
    public HTMLElement create(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        originalPath = attribute.value().hasDefined(PATH) ? attribute.value().get(PATH).asString() : "";
        originalRelativeTo = attribute.value().hasDefined(RELATIVE_TO)
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

        return inputGroup()
                .addItem(inputGroupItem().fill().addControl(pathInput))
                .addText(inputGroupText().plain().text("relative to"))
                .addItem(inputGroupItem().fill().addControl(relativeToInput))
                .element();
    }

    @Override
    public HTMLElement element(HTMLElement control) {
        return control;
    }

    @Override
    public ModelNode modelNode(HTMLElement control, ResolvedAttribute attribute) {
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

    @Override
    public boolean isModifiedForNew(HTMLElement control, ResolvedAttribute attribute) {
        return !pathValue().isEmpty() || !relativeToValue().isEmpty();
    }

    @Override
    public boolean isModifiedForExisting(HTMLElement control, ResolvedAttribute attribute, boolean wasDefined) {
        if (!wasDefined) {
            return !pathValue().isEmpty() || !relativeToValue().isEmpty();
        }
        return !originalPath.equals(pathValue()) || !originalRelativeTo.equals(relativeToValue());
    }

    @Override
    public void resetValidation(HTMLElement control) {
        pathInput.resetValidation();
        relativeToInput.resetValidation();
    }

    private String pathValue() {
        return pathInput.value() != null ? pathInput.value() : "";
    }

    private String relativeToValue() {
        return relativeToInput.value() != null ? relativeToInput.value() : "";
    }
}

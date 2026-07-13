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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.elemento.Id;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.patternfly.component.form.TextInput;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.inputgroup.InputGroupText.inputGroupText;

/**
 * Form item for sibling path + relative-to attribute groups. Two text inputs rendered as a single form group. Produces 2
 * separate write-attribute/undefine-attribute operations (one per attribute).
 */
public class PathRelativeToFormItem implements FormItem {

    private final String identifier;
    private final List<ResolvedAttribute> attributes;
    private final ResolvedAttribute pathAttr;
    private final ResolvedAttribute relativeToAttr;
    private final TextInput pathInput;
    private final TextInput relativeToInput;
    private final String originalPath;
    private final String originalRelativeTo;
    private final HTMLElement root;

    public PathRelativeToFormItem(String identifier, List<ResolvedAttribute> attributes, PipelineContext context) {
        this.identifier = identifier;
        this.attributes = attributes;
        this.pathAttr = findPath(attributes);
        this.relativeToAttr = findRelativeTo(attributes);

        this.originalPath = pathAttr != null && pathAttr.value().isDefined() ? pathAttr.value().asString() : "";
        this.originalRelativeTo = relativeToAttr != null && relativeToAttr.value().isDefined()
                ? relativeToAttr.value().asString() : "";

        pathInput = textInput(Id.build(identifier, "path"))
                .run(ti -> {
                    ti.input().autocomplete("off");
                    if (!originalPath.isEmpty()) {
                        ti.value(originalPath);
                    }
                });

        // TODO Replace with typeahead populated from /path=* and standard paths
        relativeToInput = textInput(Id.build(identifier, "relative-to"))
                .run(ti -> {
                    ti.input().autocomplete("off");
                    ti.placeholder("relative to...");
                    if (!originalRelativeTo.isEmpty()) {
                        ti.value(originalRelativeTo);
                    }
                });

        String label = pathAttr != null
                ? org.jboss.hal.core.Humanize.sentenceCase(pathAttr.name())
                : "Path";

        this.root = formGroup(identifier)
                .addLabel(org.patternfly.component.form.FormGroupLabel.formGroupLabel(label))
                .addControl(formGroupControl()
                        .addInputGroup(inputGroup()
                                .addItem(inputGroupItem().fill().addControl(pathInput))
                                .addText(inputGroupText().plain().text("relative to"))
                                .addItem(inputGroupItem().fill().addControl(relativeToInput))))
                .element();
    }

    // ------------------------------------------------------ FormItem

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public ResolvedAttribute attribute() {
        return pathAttr != null ? pathAttr : attributes.get(0);
    }

    @Override
    public boolean isModified() {
        return !originalPath.equals(pathValue()) || !originalRelativeTo.equals(relativeToValue());
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public void resetValidation() {
        pathInput.resetValidation();
        relativeToInput.resetValidation();
    }

    @Override
    public ModelNode modelNode() {
        ModelNode result = new ModelNode();
        String path = pathValue();
        String relativeTo = relativeToValue();
        if (!path.isEmpty() && pathAttr != null) {
            result.get(pathAttr.name()).set(path);
        }
        if (!relativeTo.isEmpty() && relativeToAttr != null) {
            result.get(relativeToAttr.name()).set(relativeTo);
        }
        return result;
    }

    @Override
    public List<Operation> operations(ResourceAddress address) {
        if (!isModified()) {
            return Collections.emptyList();
        }
        List<Operation> ops = new ArrayList<>();
        if (pathAttr != null && !originalPath.equals(pathValue())) {
            ops.add(attributeOperation(address, pathAttr.name(), pathValue()));
        }
        if (relativeToAttr != null && !originalRelativeTo.equals(relativeToValue())) {
            ops.add(attributeOperation(address, relativeToAttr.name(), relativeToValue()));
        }
        return ops;
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ internal

    private Operation attributeOperation(ResourceAddress address, String name, String value) {
        if (value.isEmpty()) {
            return new Operation.Builder(address, UNDEFINE_ATTRIBUTE_OPERATION)
                    .param(NAME, name)
                    .build();
        } else {
            return new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                    .param(NAME, name)
                    .param(VALUE, new ModelNode().set(value))
                    .build();
        }
    }

    private String pathValue() {
        return pathInput.value() != null ? pathInput.value() : "";
    }

    private String relativeToValue() {
        return relativeToInput.value() != null ? relativeToInput.value() : "";
    }

    private static ResolvedAttribute findPath(List<ResolvedAttribute> attributes) {
        return attributes.stream()
                .filter(ra -> !ra.name().endsWith(RELATIVE_TO))
                .findFirst().orElse(null);
    }

    private static ResolvedAttribute findRelativeTo(List<ResolvedAttribute> attributes) {
        return attributes.stream()
                .filter(ra -> ra.name().endsWith(RELATIVE_TO))
                .findFirst().orElse(null);
    }
}

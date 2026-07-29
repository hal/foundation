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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.menu.SingleTypeahead;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.patternfly.component.ValidationStatus.error;

/**
 * {@link NativeControl} for standalone {@code relative-to} attributes not paired with a sibling path attribute.
 */
public final class RelativeToControl implements NativeControl<SingleTypeahead> {

    private TextInput input;
    private PipelineFlags flags;

    /** Returns the underlying text input, e.g. to pre-fill a value before the form is shown. */
    public TextInput textInput() {
        return input;
    }

    @Override
    public boolean handlesMixedExpressions() {
        return true;
    }

    @Override
    public SingleTypeahead create(PipelineContext context, String identifier, ResolvedAttribute attribute) {
        return FormItemBricks.singleTypeahead(identifier, attribute, PathSupport::newPath, PathSupport.paths());
    }

    @Override
    public HTMLElement element(SingleTypeahead control) {
        return control.element();
    }

    @Override
    public ModelNode modelNode(SingleTypeahead control, ResolvedAttribute attribute) {
        String v = value(control);
        if (v == null || v.isEmpty()) {
            return new ModelNode();
        }
        return new ModelNode().set(v);
    }

    @Override
    public boolean isModifiedForNew(SingleTypeahead control, ResolvedAttribute attribute) {
        String v = value(control);
        if (attribute.description().hasDefault()) {
            return !attribute.description().get(DEFAULT).asString().equals(v);
        }
        return v != null && !v.isEmpty();
    }

    @Override
    public boolean isModifiedForExisting(SingleTypeahead control, ResolvedAttribute attribute, boolean wasDefined) {
        String v = value(control);
        if (wasDefined) {
            return attribute.expression() || !attribute.value().asString().equals(v);
        }
        return v != null && !v.isEmpty();
    }

    @Override
    public boolean validate(SingleTypeahead control, ResolvedAttribute attribute, FormGroupControl formGroupControl) {
        if (FormItemBricks.requiredOnItsOwn(attribute) && value(control).isEmpty()) {
            control.menuToggle().validated(error);
            formGroupControl.addHelperText(FormItemBricks.requiredHelperText(attribute));
            return false;
        }
        return true;
    }

    @Override
    public void resetValidation(SingleTypeahead control) {
        control.menuToggle().resetValidation();
    }

    @Override
    public void afterSwitchedToNativeMode(SingleTypeahead control, ResolvedAttribute attribute) {
        FormItemBricks.afterSwitchedToSingleTypeahead(control, attribute);
    }

    private String value(SingleTypeahead control) {
        return control != null ? control.menuToggle().searchInput().value() : "";
    }
}

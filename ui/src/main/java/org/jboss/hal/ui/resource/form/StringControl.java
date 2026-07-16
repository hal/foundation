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

import org.jboss.elemento.By;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.TextInput;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.Expression.containsExpression;
import static org.jboss.hal.ui.brick.ExpressionBricks.resolveExpressionIcon;
import static org.jboss.hal.ui.resource.pipeline.PipelineFlags.Scope.EXISTING_RESOURCE;
import static org.jboss.hal.ui.resource.pipeline.PipelineFlags.Scope.NEW_RESOURCE;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.tooltip.Tooltip.tooltip;

/**
 * {@link NativeControl} for plain string attributes, rendered as a text input in mixed mode (accepts both literals and
 * expressions in a single control). When expressions are allowed, the text input is wrapped in an InputGroup with a
 * resolve-expression button.
 */
public final class StringControl implements NativeControl<HTMLElement> {

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
    public HTMLElement create(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        flags = context.flags();
        input = TextInput.textInput(identifier)
                .run(ti -> {
                    ti.input().autocomplete("off");
                    if (attribute.value().isDefined()) {
                        ti.value(attribute.value().asString());
                    }
                    FormItemBricks.applyPlaceholder(ti.input(), attribute, flags);
                });

        if (attribute.description().expressionAllowed()) {
            String resolveId = identifier + "-resolve-expression";
            return inputGroup()
                    .addItem(inputGroupItem().fill().addControl(input))
                    .addItem(inputGroupItem().addButton(
                            button().id(resolveId).control().icon(resolveExpressionIcon().get())))
                    .add(tooltip(By.id(resolveId), "Resolve expression"))
                    .element();
        } else {
            return input.element();
        }
    }

    @Override
    public HTMLElement element(HTMLElement control) {
        return control;
    }

    @Override
    public ModelNode modelNode(HTMLElement control, ResolvedAttribute attribute) {
        String value = textValue();
        if (value.isEmpty()) {
            return new ModelNode();
        } else if (containsExpression(value)) {
            return new ModelNode().setExpression(value);
        } else {
            return new ModelNode().set(value);
        }
    }

    @Override
    public boolean isModifiedForNew(HTMLElement control, ResolvedAttribute attribute) {
        return !textValue().isEmpty();
    }

    @Override
    public boolean isModifiedForExisting(HTMLElement control, ResolvedAttribute attribute, boolean wasDefined) {
        if (flags.scope() == NEW_RESOURCE) {
            return !textValue().isEmpty();
        } else if (flags.scope() == EXISTING_RESOURCE) {
            if (attribute.value().isDefined()) {
                return !attribute.value().asString().equals(textValue());
            } else {
                return !textValue().isEmpty();
            }
        }
        return false;
    }

    @Override
    public boolean validate(HTMLElement control, ResolvedAttribute attribute, FormGroupControl formGroupControl) {
        if (FormItemBricks.requiredOnItsOwn(attribute) && textValue().isEmpty()) {
            input.validated(error);
            formGroupControl.addHelperText(FormItemBricks.requiredHelperText(attribute));
            return false;
        }
        return true;
    }

    @Override
    public void resetValidation(HTMLElement control) {
        input.resetValidation();
    }

    private String textValue() {
        return input != null && input.value() != null ? input.value() : "";
    }
}

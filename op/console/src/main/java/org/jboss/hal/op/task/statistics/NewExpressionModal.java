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
package org.jboss.hal.op.task.statistics;

import java.util.function.Consumer;

import org.jboss.hal.dmr.Expression;
import org.patternfly.component.ValidationStatus;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.help.HelperText;
import org.patternfly.component.modal.Modal;

import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.EventType.keydown;
import static org.jboss.elemento.Key.Enter;
import static org.jboss.hal.ui.BuildingBlocks.renderExpression;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.form.Form.form;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.FormGroupLabel.formGroupLabel;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.help.HelperText.helperText;
import static org.patternfly.component.modal.Modal.modal;
import static org.patternfly.component.modal.ModalBody.modalBody;
import static org.patternfly.component.modal.ModalFooter.modalFooter;
import static org.patternfly.component.modal.ModalHeader.modalHeader;
import static org.patternfly.component.modal.ModalHeaderDescription.modalHeaderDescription;
import static org.patternfly.style.Size.md;

class NewExpressionModal {

    // ------------------------------------------------------ factory

    static NewExpressionModal newExpressionModal(Consumer<String> callback) {
        return new NewExpressionModal(callback);
    }

    // ------------------------------------------------------ instance

    private final Consumer<String> callback;
    private final Modal modal;
    private final TextInput textInput;
    private final HelperText helperText;

    NewExpressionModal(Consumer<String> callback) {
        this.callback = callback;
        this.modal = modal().size(md);
        this.textInput = textInput("new-expression").applyTo(input ->
                input.on(keydown, event -> {
                    if (Enter.match(event)) {
                        event.preventDefault();
                        validate(input.element().value);
                    }
                }));
        this.helperText = helperText("Not a valid expression", ValidationStatus.error);
        setVisible(helperText, false);
    }

    void open() {
        modal.addHeader(modalHeader()
                        .addTitle("New expression")
                        .addDescription(modalHeaderDescription()
                                .add("Please enter a valid expression as ")
                                .add(renderExpression("${expression:default}"))
                                .add(".")))
                .addBody(modalBody()
                        .add(form().addGroup(formGroup("new-expression").required()
                                .addLabel(formGroupLabel("Expression"))
                                .addControl(formGroupControl()
                                        .addControl(textInput)
                                        .addHelperText(helperText)))))
                .addFooter(modalFooter()
                        .addButton(button("Ok").primary(), (e, m) -> validate(textInput.value()))
                        .addButton(button("Cancel").link(), (e, m) -> m.close()))
                .appendToBody()
                .open();
        textInput.input().element().focus();
    }

    private void validate(String value) {
        boolean expression = Expression.isExpression(value);
        setVisible(helperText, !expression);
        if (expression) {
            textInput.resetValidation();
            modal.close();
            callback.accept(value);
        } else {
            textInput.validated(ValidationStatus.error);
        }
    }
}

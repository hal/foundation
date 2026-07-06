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
package org.jboss.hal.ui.resource.dialog;

import org.jboss.hal.ui.resource.ResourceAttribute;

import java.util.List;

import org.jboss.hal.dmr.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.OperationDescription;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.resources.OuiaIds;
import org.jboss.hal.ui.resource.form.FormItemFlags;
import org.jboss.hal.ui.resource.form.FormItemFlags.Placeholder;
import org.jboss.hal.ui.resource.form.FormItemFlags.Scope;
import org.jboss.hal.ui.resource.form.ResourceForm;
import org.patternfly.layout.stack.StackItem;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.brick.CodeBricks.errorCode;
import static org.jboss.hal.ui.brick.CodeBricks.modelNodeCode;
import static org.jboss.hal.ui.resource.ResourceAttribute.notDeprecated;
import static org.jboss.hal.ui.resource.ResourceAttribute.resourceAttributes;
import static org.jboss.hal.ui.resource.form.FormItemFactory.formItem;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.Severity.success;
import static org.patternfly.component.alert.Alert.alert;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.modal.Modal.modal;
import static org.patternfly.component.modal.ModalBody.modalBody;
import static org.patternfly.component.modal.ModalFooter.modalFooter;
import static org.patternfly.component.modal.ModalHeader.modalHeader;
import static org.patternfly.layout.stack.Stack.stack;
import static org.patternfly.layout.stack.StackItem.stackItem;
import static org.patternfly.style.Size.lg;

import static org.jboss.hal.core.Notification.error;

/**
 * Dialog for executing a management operation with parameter inputs and result display.
 * <p>
 * Opens a modal that generates form inputs from the operation's parameter metadata. If the operation has no parameters, it
 * executes immediately. Results and errors are displayed inline below the form.
 */
class ExecuteOperationDialogs {

    static void executeOperationModal(AddressTemplate template, String operation) {
        uic().metadataRepository().lookup(template)
                .then(metadata -> {
                    OperationDescription operationDescription = metadata.resourceDescription().operations().get(operation);
                    if (operationDescription.isDefined()) {
                        String title = template.template + ":" + operation + "()";
                        boolean parameters = !operationDescription.parameters().isEmpty();
                        StackItem resultContainer = stackItem();
                        ResourceForm resourceForm = operationForm(template, metadata, operationDescription);
                        modal().size(lg).top()
                                .ouiaId(OuiaIds.EXECUTE_MODAL)
                                .addHeader(modalHeader()
                                        .addTitle(title)
                                        .addDescription(operationDescription.description()))
                                .addBody(modalBody()
                                        .add(stack().gutter()
                                                .addItem(stackItem().fill(parameters)
                                                        .add(div().css(halComponent(HalClasses.resource))
                                                                .add(resourceForm)))
                                                .addItem(resultContainer)))
                                .addFooter(modalFooter()
                                        .addButton(button("Execute").primary()
                                                .ouiaId(OuiaIds.EXECUTE_BTN), (__, m) ->
                                                executeOperation(template, operationDescription, resourceForm, resultContainer))
                                        .addButton(button("Close").link()
                                                .ouiaId(OuiaIds.CLOSE_BTN), (__, m) -> m.close()))
                                .appendToBody()
                                .open();
                        if (!parameters) {
                            executeOperation(template, operationDescription, resourceForm, resultContainer);
                        }
                    } else {
                        uic().notifications().send(error("Operation failed", "No operation definition found for " + operation));
                    }
                    return null;
                })
                .catch_(err -> {
                    uic().notifications().send(error("Operation failed", String.valueOf(err)));
                    return null;
                });
    }

    private static ResourceForm operationForm(AddressTemplate template, org.jboss.hal.meta.Metadata metadata,
            OperationDescription operationDescription) {
        List<ResourceAttribute> resourceAttributes = resourceAttributes(operationDescription, notDeprecated());
        ResourceForm resourceForm = new ResourceForm(template);
        for (ResourceAttribute ra : resourceAttributes) {
            resourceForm.addItem(formItem(template, metadata, ra,
                    new FormItemFlags(Scope.NEW_RESOURCE, Placeholder.DEFAULT_VALUE)));
        }
        return resourceForm;
    }

    private static void executeOperation(AddressTemplate template, OperationDescription operationDescription,
            ResourceForm resourceForm, StackItem resultContainer) {
        boolean execute = true;
        boolean parameters = !operationDescription.parameters().isEmpty();
        int lines = parameters ? 7 : 5;

        resourceForm.resetValidation();
        removeChildrenFrom(resultContainer);
        if (parameters) {
            if (!resourceForm.validate()) {
                execute = false;
                resourceForm.validationAlert("Operation failed");
            }
        }

        if (execute) {
            Operation.Builder builder = new Operation.Builder(template.resolve(uic().statementContext()),
                    operationDescription.name());
            if (parameters) {
                builder.payload(resourceForm.modelNode());
            }
            uic().dispatcher().execute(builder.build())
                    .then(result -> {
                        resourceForm.addAlert(alert(success, "Operation successfully executed").inline());
                        setVisible(resultContainer, result.isDefined());
                        if (result.isDefined()) {
                            resultContainer.add(modelNodeCode(result, lines));
                        }
                        return null;
                    })
                    .catch_(err -> {
                        resourceForm.addAlert(alert(danger, "Operation failed").inline());
                        resultContainer.add(errorCode(String.valueOf(err), lines));
                        return null;
                    });
        }
    }
}

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

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.OperationDescription;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.resources.OuiaIds;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.form.PipelineForm;
import org.jboss.hal.ui.resource.pipeline.Pipeline;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags.Placeholder;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags.Scope;
import org.patternfly.layout.stack.StackItem;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.brick.CodeBricks.errorCode;
import static org.jboss.hal.ui.brick.CodeBricks.modelNodeCode;
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

class ExecuteOperationDialogs {

    static void executeOperationModal(AddressTemplate template, String operation) {
        uic().metadataRepository().lookup(template)
                .then(metadata -> {
                    OperationDescription operationDescription = metadata.resourceDescription().operations().get(operation);
                    if (operationDescription.isDefined()) {
                        String title = template.template + ":" + operation + "()";
                        boolean parameters = !operationDescription.parameters().isEmpty();
                        StackItem resultContainer = stackItem();
                        PipelineForm pipelineForm = operationForm(template, metadata, operationDescription);
                        modal().size(lg).top()
                                .ouiaId(OuiaIds.EXECUTE_MODAL)
                                .addHeader(modalHeader()
                                        .addTitle(title)
                                        .addDescription(operationDescription.description()))
                                .addBody(modalBody()
                                        .add(stack().gutter()
                                                .addItem(stackItem().fill(parameters)
                                                        .add(div().css(halComponent(HalClasses.resource))
                                                                .add(pipelineForm)))
                                                .addItem(resultContainer)))
                                .addFooter(modalFooter()
                                        .addButton(button("Execute").primary()
                                                .ouiaId(OuiaIds.EXECUTE_BTN), (__, m) ->
                                                executeOperation(template, operationDescription, pipelineForm,
                                                        resultContainer))
                                        .addButton(button("Close").link()
                                                .ouiaId(OuiaIds.CLOSE_BTN), (__, m) -> m.close()))
                                .appendToBody()
                                .open();
                        if (!parameters) {
                            executeOperation(template, operationDescription, pipelineForm, resultContainer);
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

    private static PipelineForm operationForm(AddressTemplate template, Metadata metadata,
            OperationDescription operationDescription) {
        PipelineContext context = new PipelineContext(template, metadata, new ModelNode(),
                new PipelineFlags(Scope.NEW_RESOURCE, Placeholder.DEFAULT_VALUE));
        List<FormItem> items = Pipeline.create().formItems(context, operationDescription.parameters());
        PipelineForm pipelineForm = new PipelineForm();
        for (FormItem item : items) {
            if (!item.attribute().description().deprecation().isDefined()) {
                pipelineForm.addItem(item);
            }
        }
        return pipelineForm;
    }

    private static void executeOperation(AddressTemplate template, OperationDescription operationDescription,
            PipelineForm pipelineForm, StackItem resultContainer) {
        boolean execute = true;
        boolean parameters = !operationDescription.parameters().isEmpty();
        int lines = parameters ? 7 : 5;

        pipelineForm.resetValidation();
        removeChildrenFrom(resultContainer);
        if (parameters) {
            if (!pipelineForm.validate()) {
                execute = false;
                pipelineForm.validationAlert("Operation failed");
            }
        }

        if (execute) {
            Operation.Builder builder = new Operation.Builder(template.resolve(uic().statementContext()),
                    operationDescription.name());
            if (parameters) {
                builder.payload(pipelineForm.modelNode());
            }
            uic().dispatcher().execute(builder.build())
                    .then(result -> {
                        pipelineForm.addAlert(alert(success, "Operation successfully executed").inline());
                        setVisible(resultContainer, result.isDefined());
                        if (result.isDefined()) {
                            resultContainer.add(modelNodeCode(result, lines));
                        }
                        return null;
                    })
                    .catch_(err -> {
                        pipelineForm.addAlert(alert(danger, "Operation failed").inline());
                        resultContainer.add(errorCode(String.valueOf(err), lines));
                        return null;
                    });
        }
    }
}

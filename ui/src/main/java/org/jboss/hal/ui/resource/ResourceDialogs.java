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

import java.util.List;

import org.jboss.hal.core.LabelBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContextResolver;
import org.jboss.hal.meta.WildcardResolver;
import org.jboss.hal.meta.description.OperationDescription;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.ui.resource.FormItemFlags.Placeholder;
import org.jboss.hal.ui.resource.FormItemFlags.Scope;
import org.patternfly.component.modal.Modal;
import org.patternfly.layout.stack.StackItem;

import elemental2.promise.IThenable;
import elemental2.promise.Promise;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.RejectCallbackFn;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.ResolveCallbackFn;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.core.Notification.error;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.meta.WildcardResolver.Direction.LTR;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.ui.BuildingBlocks.errorCode;
import static org.jboss.hal.ui.BuildingBlocks.modelNodeCode;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.resource.FormItemFactory.formItem;
import static org.jboss.hal.ui.resource.FormItemFactory.nameFormItem;
import static org.jboss.hal.ui.resource.ResourceAttribute.notDeprecated;
import static org.jboss.hal.ui.resource.ResourceAttribute.resourceAttributes;
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
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Size.lg;
import static org.patternfly.style.Size.sm;

public class ResourceDialogs {

    // ------------------------------------------------------ add

    public static Promise<ModelNode> addResource(AddressTemplate template, String resource, boolean singleton) {
        String title;
        AddressTemplate resolved;
        if (singleton) {
            title = "Add " + resource;
            resolved = new WildcardResolver(LTR, resource).resolve(template);
        } else {
            title = new LabelBuilder().label(template.last().key);
            resolved = AddressTemplate.of(template);
        }
        return new Promise<>((resolve, reject) -> uic().metadataRepository().lookup(resolved)
                .then(Promise::resolve)
                .then(metadata -> {
                    OperationDescription operationDescription = metadata.resourceDescription().operations().get(ADD);
                    if (operationDescription.isDefined()) {
                        ResourceForm resourceForm = addForm(resolved, metadata, operationDescription, singleton);
                        modal().size(lg).top()
                                .addHeader(modalHeader()
                                        .addTitle(title)
                                        .addDescription(metadata.resourceDescription().description()))
                                .addBody(modalBody()
                                        .add(div().css(halComponent(HalClasses.resource))
                                                .add(resourceForm)))
                                .addFooter(modalFooter()
                                        .addButton(button("Add").primary(),
                                                (__, modal) -> addResource(resolved, modal, resourceForm, resolve))
                                        .addButton(button("Cancel").link(), (__, modal) -> onCancel(modal, resolve)))
                                .appendToBody()
                                .open();
                    } else {
                        reject.onInvoke("No add operation defined for " + resolved);
                    }
                    return null;
                })
                .catch_(error -> {
                    reject.onInvoke(error);
                    return null;
                }));
    }

    private static ResourceForm addForm(AddressTemplate template, Metadata metadata, OperationDescription operationDescription,
            boolean singleton) {
        List<ResourceAttribute> resourceAttributes = resourceAttributes(operationDescription, notDeprecated());
        ResourceForm resourceForm = new ResourceForm(template);
        if (!singleton) {
            resourceForm.addItem(nameFormItem(metadata));
        }
        for (ResourceAttribute ra : resourceAttributes) {
            resourceForm.addItem(formItem(template, metadata, ra,
                    new FormItemFlags(Scope.NEW_RESOURCE, Placeholder.DEFAULT_VALUE)));
        }
        return resourceForm;
    }

    private static void addResource(AddressTemplate template, Modal modal, ResourceForm resourceForm,
            ResolveCallbackFn<ModelNode> resolve) {
        resourceForm.resetValidation();
        if (resourceForm.validate()) {
            AddressTemplate resolved;
            ModelNode payload = resourceForm.modelNode();
            if (payload.has(NAME)) {
                ModelNode nameModelNode = payload.remove(NAME);
                resolved = new WildcardResolver(LTR, nameModelNode.asString()).resolve(template);
            } else {
                resolved = AddressTemplate.of(template);
            }
            uic().crud().create(resolved, payload)
                    .then(result -> onSuccess(modal, result, resolve))
                    .catch_(error -> {
                        resourceForm.addAlert(
                                alert(danger, "Failed to add resource").inline()
                                        .addDescription(String.valueOf(error)));
                        return null;
                    });
        } else {
            resourceForm.validationAlert("Failed to add resource");
        }
    }

    // ------------------------------------------------------ execute operation

    public static void executeOperation(AddressTemplate template, String operation) {
        uic().metadataRepository().lookup(template)
                .then(metadata -> {
                    OperationDescription operationDescription = metadata.resourceDescription().operations().get(operation);
                    if (operationDescription.isDefined()) {
                        String title = template.template + ":" + operation + "()";
                        boolean parameters = !operationDescription.parameters().isEmpty();
                        StackItem resultContainer = stackItem();
                        ResourceForm resourceForm = operationForm(template, metadata, operationDescription);
                        modal().size(lg).top()
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
                                        .addButton(button("Execute").primary(), (__, modal) ->
                                                executeOperation(template, operationDescription, resourceForm, resultContainer))
                                        .addButton(button("Close").link(), (__, modal) -> modal.close()))
                                .appendToBody()
                                .open();
                        if (!parameters) {
                            // execute immediately
                            executeOperation(template, operationDescription, resourceForm, resultContainer);
                        }
                    } else {
                        uic().notifications().send(error("Operation failed", "No operation definition found for " + operation));
                    }
                    return null;
                })
                .catch_(error -> {
                    uic().notifications().send(error("Operation failed", String.valueOf(error)));
                    return null;
                });
    }

    private static ResourceForm operationForm(AddressTemplate template, Metadata metadata,
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
                    .catch_(error -> {
                        resourceForm.addAlert(alert(danger, "Operation failed").inline());
                        resultContainer.add(errorCode(String.valueOf(error), lines));
                        return null;
                    });
        }
    }

    // ------------------------------------------------------ delete

    public static Promise<ModelNode> deleteResource(AddressTemplate template) {
        AddressTemplate resolvedTemplate = new StatementContextResolver(uic().statementContext()).resolve(template);
        String name = resolvedTemplate.last().value;
        return new Promise<>((resolve, reject) -> modal().size(sm)
                .addHeader("Delete resource")
                .addBody(modalBody()
                        .add("Do you really want to delete ")
                        .add(span().css(util("font-weight-bold")).text(name))
                        .add("?"))
                .addFooter(modalFooter()
                        .addButton(button("Delete").primary(), (__, modal) -> uic().crud().delete(resolvedTemplate)
                                .then(result -> onSuccess(modal, result, resolve))
                                .catch_(error -> onError(modal, error, reject)))
                        .addButton(button("Cancel").link(), (__, modal) -> onCancel(modal, resolve)))
                .appendToBody()
                .open());
    }

    // ------------------------------------------------------ internal

    private static IThenable<ModelNode> onSuccess(Modal modal, ModelNode modelNode, ResolveCallbackFn<ModelNode> resolve) {
        modal.close();
        resolve.onInvoke(modelNode);
        return null;
    }

    private static IThenable<ModelNode> onError(Modal modal, Object error, RejectCallbackFn reject) {
        modal.close();
        reject.onInvoke(error);
        return null;
    }

    private static void onCancel(Modal modal, ResolveCallbackFn<ModelNode> resolve) {
        modal.close();
        resolve.onInvoke(new ModelNode());
    }
}

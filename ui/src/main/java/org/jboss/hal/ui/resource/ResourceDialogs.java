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
import org.patternfly.component.modal.ModalHeaderTitle;
import org.patternfly.component.wizard.Wizard;
import org.patternfly.component.wizard.WizardStep;
import org.patternfly.layout.stack.StackItem;

import elemental2.promise.Promise;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.ResolveCallbackFn;

import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.core.LabelBuilder.labelBuilder;
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
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.list.SimpleList.simpleList;
import static org.patternfly.component.list.SimpleListItem.simpleListItem;
import static org.patternfly.component.modal.Modal.modal;
import static org.patternfly.component.modal.ModalBody.modalBody;
import static org.patternfly.component.modal.ModalFooter.modalFooter;
import static org.patternfly.component.modal.ModalHeader.modalHeader;
import static org.patternfly.component.modal.ModalHeaderTitle.modalHeaderTitle;
import static org.patternfly.component.wizard.Wizard.wizard;
import static org.patternfly.component.wizard.WizardFooterButtons.next;
import static org.patternfly.component.wizard.WizardHeader.wizardHeader;
import static org.patternfly.component.wizard.WizardHeaderDescription.wizardHeaderDescription;
import static org.patternfly.component.wizard.WizardHeaderTitle.wizardHeaderTitle;
import static org.patternfly.component.wizard.WizardStep.wizardStep;
import static org.patternfly.layout.flex.Direction.column;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.Gap.none;
import static org.patternfly.layout.stack.Stack.stack;
import static org.patternfly.layout.stack.StackItem.stackItem;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Size.lg;
import static org.patternfly.style.Size.sm;
import static org.patternfly.token.Token.globalFontSizeXs;
import static org.patternfly.token.Token.globalTextColorSubtle;

public class ResourceDialogs {

    // ------------------------------------------------------ add

    /**
     * Initiates a wizard for adding a new resource based on the given templates and resource name. This method guides the user
     * through the necessary steps, such as selecting a location and providing required details for adding the resource.
     *
     * @param templates a list of {@link AddressTemplate} objects representing the possible locations where the resource can be
     *                  added
     * @param resource  the name of the resource to be added
     * @return a {@link Promise} that resolves to a {@link ModelNode} object representing the added resource if the operation is
     * successful or to an undefined {@link ModelNode} if the operation is canceled
     */
    public static Promise<ModelNode> addResourceWizard(List<AddressTemplate> templates, String resource) {
        Wizard wizard = wizard()
                .visitRequired()
                .addHeader(wizardHeader()
                        .addTitle(wizardHeaderTitle().run(wht -> h(wht.textDelegate())
                                .add("Add ")
                                .add(code(resource))))
                        .addDescription(wizardHeaderDescription()
                                .add("Choose where to add ")
                                .add(code(resource))
                                .add(" and fill in the required fields.")));

        WizardStep selectLocation = wizardStep("select-location", "Select location")
                .onEnter((wzd, step) -> wzd.footer().nextButton().disabled(!wzd.context().has("template")))
                .add(stack().gutter()
                        .addItem(stackItem()
                                .add(content(p).editorial()
                                        .add("Please select the location where you want to add ")
                                        .add(code(resource))
                                        .add(".")))
                        .addItem(stackItem().fill(true)
                                .add(simpleList()
                                        .addItems(templates, template -> {
                                            String resolved = new WildcardResolver(LTR, resource).resolve(template).toString();
                                            return simpleListItem(resolved)
                                                    .store("template", template)
                                                    .add(flex().direction(column).gap(none)
                                                            .addItem(flexItem().add(resolved))
                                                            .addItem(flexItem()
                                                                    .style("color", globalTextColorSubtle.var)
                                                                    .style("font-size", globalFontSizeXs.var)
                                                                    .run(flexItem -> uic().metadataRepository()
                                                                            .lookup(template, metadata ->
                                                                                    flexItem.text(metadata.resourceDescription()
                                                                                            .description())))));
                                        })
                                        .onSelect((e, item, selected) -> {
                                            wizard.footer().nextButton().disabled(!selected);
                                            wizard.context().store("template", item.get("template"));
                                        }))))
                .nextIfPromised((wzd, current, next) -> {
                    AddressTemplate template = wzd.context().get("template");
                    return uic().metadataRepository()
                            .lookup(template)
                            .then(metadata -> {
                                wzd.context().store("metadata", metadata);
                                return Promise.resolve(true);
                            });
                });

        return new Promise<>((resolve, reject) -> modal().size(lg).top()
                .addWizard(wizard
                        .addItem(selectLocation)
                        .addItem(wizardStep("add-resource", "Add resource")
                                .customButtonName(next, "Add")
                                .onEnter((wzd, step) -> {
                                    AddressTemplate template = wizard.context().get("template");
                                    Metadata metadata = wizard.context().get("metadata");
                                    OperationDescription operationDescription = metadata.resourceDescription()
                                            .operations()
                                            .get(ADD);
                                    removeChildrenFrom(step);
                                    if (operationDescription.isDefined()) {
                                        ResourceForm resourceForm = resourceForm(template, metadata, operationDescription,
                                                resource, false);
                                        step.add(div().css(halComponent(HalClasses.resource)).add(resourceForm));
                                        wzd.context().store("resourceForm", resourceForm);
                                    } else {
                                        step.add(emptyState()
                                                .status(danger)
                                                .text("No add operation")
                                                .addBody(emptyStateBody()
                                                        .add("There's no add operation defined for ")
                                                        .add(code(template.toString()))
                                                        .add(". Please select another template.")));
                                    }
                                })
                                .nextIfPromised((wzd, current, next) -> {
                                    AddressTemplate template = wizard.context().get("template");
                                    ResourceForm resourceForm = wizard.context().get("resourceForm");
                                    return addResource(template, resourceForm)
                                            .then(modelNode -> {
                                                wzd.context().store("modelNode", modelNode);
                                                return Promise.resolve(modelNode.isDefined());
                                            });
                                }))
                        .onCancel((event, wzd) -> resolve.onInvoke(new ModelNode())) // undefined means canceled
                        .onFinish((event, wzd) -> {
                            ModelNode modelNode = wzd.context().get("modelNode");
                            resolve.onInvoke(modelNode);
                        }))
                .appendToBody()
                .open());
    }

    /**
     * Opens a modal dialog for adding a new resource based on the specified address template and resource name. The method
     * dynamically determines whether the resource is a singleton and adjusts the behavior accordingly.
     *
     * @param template  the {@link AddressTemplate} specifying the address location where the resource will be added
     * @param resource  the name of the resource to be added
     * @param singleton a boolean flag indicating whether the resource to be added is a singleton
     * @return a {@link Promise} that resolves to a {@link ModelNode} representing the added resource if the operation is
     * successful, or is undefined if the operation was rejected or canceled
     */
    public static Promise<ModelNode> addResourceModal(AddressTemplate template, String resource, boolean singleton) {
        ModalHeaderTitle title;
        AddressTemplate resolved;
        if (singleton) {
            title = modalHeaderTitle().run(mht -> span(mht.textDelegate())
                    .add("Add ")
                    .add(code(resource)));
            resolved = new WildcardResolver(LTR, resource).resolve(template);
        } else {
            title = modalHeaderTitle().run(mht -> span(mht.textDelegate())
                    .add("Add " + labelBuilder(template.last().key) + " ")
                    .run(span -> {
                        if (resource != null) {
                            span.add(code(resource));
                        }
                    }));
            resolved = AddressTemplate.of(template);
        }
        return new Promise<>((resolve, reject) -> uic().metadataRepository().lookup(resolved)
                .then(Promise::resolve)
                .then(metadata -> {
                    OperationDescription operationDescription = metadata.resourceDescription().operations().get(ADD);
                    if (operationDescription.isDefined()) {
                        ResourceForm resourceForm = resourceForm(resolved, metadata, operationDescription, resource, singleton);
                        modal().size(lg).top()
                                .addHeader(modalHeader()
                                        .addTitle(title)
                                        .addDescription(metadata.resourceDescription().description()))
                                .addBody(modalBody()
                                        .add(div().css(halComponent(HalClasses.resource))
                                                .add(resourceForm)))
                                .addFooter(modalFooter()
                                        .addButton(button("Add").primary(), (__, modal) ->
                                                addResource(resolved, resourceForm).then(modelNode -> {
                                                    if (modelNode.isDefined()) {
                                                        modal.close();
                                                        resolve.onInvoke(modelNode);
                                                    }
                                                    return null;
                                                }))
                                        .addButton(button("Cancel").link(), (__, modal) ->
                                                closeAndResolveWithUndefined(modal, resolve)))
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

    private static ResourceForm resourceForm(AddressTemplate template, Metadata metadata,
            OperationDescription operationDescription,
            String value, boolean singleton) {
        List<ResourceAttribute> resourceAttributes = resourceAttributes(operationDescription, notDeprecated());
        ResourceForm resourceForm = new ResourceForm(template);
        if (!singleton) {
            resourceForm.addItem(nameFormItem(metadata, value));
        }
        for (ResourceAttribute ra : resourceAttributes) {
            // TODO Support attribute groups
            resourceForm.addItem(formItem(template, metadata, ra,
                    new FormItemFlags(Scope.NEW_RESOURCE, Placeholder.DEFAULT_VALUE)));
        }
        return resourceForm;
    }

    private static Promise<ModelNode> addResource(AddressTemplate template, ResourceForm resourceForm) {
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
            return uic().crud().create(resolved, payload).catch_(error -> {
                resourceForm.addAlert(
                        alert(danger, "Failed to add resource").inline()
                                .addDescription(String.valueOf(error)));
                return Promise.resolve(new ModelNode()); // undefined means error
            });
        } else {
            resourceForm.validationAlert("Failed to add resource");
            return Promise.resolve(new ModelNode()); // undefined means error
        }
    }

    // ------------------------------------------------------ execute operation

    /**
     * Opens a modal dialog to execute a specific operation on a resource identified by the provided address template. The
     * dialog dynamically generates its content based on the operation's metadata, allowing the user to provide input parameters
     * if required and view the resulting output.
     *
     * @param template  the {@link AddressTemplate} representing the address location of the resource on which the operation
     *                  will be executed
     * @param operation the name of the operation to execute on the resource
     */
    public static void executeOperationModal(AddressTemplate template, String operation) {
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

    /**
     * Opens a modal dialog to confirm the deletion of a resource identified by the specified address template. The dialog
     * provides a user-friendly interface to either confirm or cancel the deletion operation.
     *
     * @param template the {@link AddressTemplate} representing the address location of the resource to be deleted
     * @return a {@link Promise} that resolves to a {@link ModelNode} representing the result of the deletion operation if
     * successful, or rejects with an error or resolves to an undefined {@link ModelNode} if the operation is canceled
     */
    public static Promise<ModelNode> deleteResourceModal(AddressTemplate template) {
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
                                .then(result -> {
                                    modal.close();
                                    resolve.onInvoke(result);
                                    return null;
                                })
                                .catch_(error -> {
                                    modal.close();
                                    reject.onInvoke(error);
                                    return null;
                                }))
                        .addButton(button("Cancel").link(), (__, modal) -> closeAndResolveWithUndefined(modal, resolve)))
                .appendToBody()
                .open());
    }

    // ------------------------------------------------------ internal

    private static void closeAndResolveWithUndefined(Modal modal, ResolveCallbackFn<ModelNode> resolve) {
        modal.close();
        resolve.onInvoke(new ModelNode()); // undefined means canceled
    }
}

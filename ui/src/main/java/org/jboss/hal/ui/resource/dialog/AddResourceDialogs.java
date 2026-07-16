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

import org.jboss.hal.core.Humanize;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.WildcardResolver;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.OperationDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.resources.Keys;
import org.jboss.hal.resources.OuiaIds;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.form.ResourceForm;
import org.jboss.hal.ui.resource.form.StandardFormItem;
import org.jboss.hal.ui.resource.form.StringControl;
import org.jboss.hal.ui.resource.pipeline.Pipeline;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags.Placeholder;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags.Scope;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.patternfly.component.modal.Modal;
import org.patternfly.component.modal.ModalHeaderTitle;
import org.patternfly.component.wizard.Wizard;
import org.patternfly.component.wizard.WizardStep;

import elemental2.promise.Promise;

import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ACCESS_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPRESSIONS_ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_WRITE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.meta.WildcardResolver.Direction.LTR;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.Severity.danger;
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
import static org.patternfly.style.Size.lg;
import static org.patternfly.token.Token.globalFontSizeXs;
import static org.patternfly.token.Token.globalTextColorSubtle;

class AddResourceDialogs {

    static Promise<ModelNode> addResourceWizard(List<AddressTemplate> templates, String resource) {
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
                .onEnter((wzd, step) -> wzd.footer().nextButton().disabled(!wzd.context().has(Keys.FINDER_TEMPLATE)))
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
                                                    .store(Keys.FINDER_TEMPLATE, template)
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
                                            wizard.context().store(Keys.FINDER_TEMPLATE, item.get(Keys.FINDER_TEMPLATE));
                                        }))))
                .nextIfPromised((wzd, current, next) -> {
                    AddressTemplate template = wzd.context().get(Keys.FINDER_TEMPLATE);
                    return uic().metadataRepository()
                            .lookup(template)
                            .then(metadata -> {
                                wzd.context().store(Keys.METADATA, metadata);
                                return Promise.resolve(true);
                            });
                });

        return new Promise<>((resolve, reject) -> modal().size(lg).top()
                .addWizard(wizard
                        .addItem(selectLocation)
                        .addItem(wizardStep("add-resource", "Add resource")
                                .customButtonName(next, "Add")
                                .onEnter((wzd, step) -> {
                                    AddressTemplate template = wizard.context().get(Keys.FINDER_TEMPLATE);
                                    Metadata metadata = wizard.context().get(Keys.METADATA);
                                    OperationDescription operationDescription = metadata.resourceDescription()
                                            .operations()
                                            .get(ADD);
                                    removeChildrenFrom(step);
                                    if (operationDescription.isDefined()) {
                                        ResourceForm pipelineForm = resourceForm(template, metadata, operationDescription,
                                                resource, false);
                                        step.add(div().css(halComponent(HalClasses.resource)).add(pipelineForm));
                                        wzd.context().store(Keys.RESOURCE_FORM, pipelineForm);
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
                                    AddressTemplate template = wizard.context().get(Keys.FINDER_TEMPLATE);
                                    ResourceForm pipelineForm = wizard.context().get(Keys.RESOURCE_FORM);
                                    return addResource(template, pipelineForm)
                                            .then(modelNode -> {
                                                wzd.context().store(Keys.MODEL_NODE, modelNode);
                                                return Promise.resolve(modelNode.isDefined());
                                            });
                                }))
                        .onCancel((event, wzd) -> resolve.onInvoke(new ModelNode()))
                        .onFinish((event, wzd) -> {
                            ModelNode modelNode = wzd.context().get(Keys.MODEL_NODE);
                            resolve.onInvoke(modelNode);
                        }))
                .appendToBody()
                .open());
    }

    static Promise<ModelNode> addResourceModal(AddressTemplate template, String resource, boolean singleton) {
        ModalHeaderTitle title;
        AddressTemplate resolved;
        if (singleton) {
            title = modalHeaderTitle().run(mht -> span(mht.textDelegate())
                    .add("Add ")
                    .add(code(resource)));
            resolved = new WildcardResolver(LTR, resource).resolve(template);
        } else {
            title = modalHeaderTitle().run(mht -> span(mht.textDelegate())
                    .add("Add " + Humanize.sentenceCase(template.last().key) + " ")
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
                        ResourceForm pipelineForm = resourceForm(resolved, metadata, operationDescription, resource, singleton);
                        modal().size(lg).top()
                                .ouiaId(OuiaIds.ADD_MODAL)
                                .addHeader(modalHeader()
                                        .addTitle(title)
                                        .addDescription(metadata.resourceDescription().description()))
                                .addBody(modalBody()
                                        .add(div().css(halComponent(HalClasses.resource))
                                                .add(pipelineForm)))
                                .addFooter(modalFooter()
                                        .addButton(button("Add").primary()
                                                .ouiaId(OuiaIds.ADD_BTN), (__, m) ->
                                                addResource(resolved, pipelineForm).then(modelNode -> {
                                                    if (modelNode.isDefined()) {
                                                        m.close();
                                                        resolve.onInvoke(modelNode);
                                                    }
                                                    return null;
                                                }))
                                        .addButton(button("Cancel").link()
                                                .ouiaId(OuiaIds.CANCEL_BTN), (__, m) -> {
                                            m.close();
                                            resolve.onInvoke(new ModelNode());
                                        }))
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
        PipelineContext context = new PipelineContext(template, metadata, new ModelNode(),
                new PipelineFlags(Scope.NEW_RESOURCE, Placeholder.DEFAULT_VALUE));
        List<FormItem> items = Pipeline.instance().formItems(context, operationDescription.parameters());
        ResourceForm pipelineForm = new ResourceForm();
        if (!singleton) {
            pipelineForm.addItem(nameFormItem(metadata, value, context));
        }
        for (FormItem item : items) {
            if (!item.attribute().description().deprecation().isDefined()) {
                pipelineForm.addItem(item);
            }
        }
        return pipelineForm;
    }

    static FormItem nameFormItem(Metadata metadata, String value, PipelineContext context) {
        AttributeDescription nameDescription = metadata.resourceDescription().attributes().get(NAME);
        if (!nameDescription.isDefined()) {
            ModelNode modelNode = new ModelNode();
            modelNode.get(DESCRIPTION).set("The name of the resource");
            modelNode.get(TYPE).set(ModelType.STRING);
            nameDescription = new AttributeDescription(new org.jboss.hal.dmr.Property(NAME, modelNode));
        }
        nameDescription.get(REQUIRED).set(true);
        nameDescription.get(ACCESS_TYPE).set(READ_WRITE);
        nameDescription.get(EXPRESSIONS_ALLOWED).set(false);

        ResolvedAttribute ra = new ResolvedAttribute(nameDescription, new ModelNode(), true, true);
        StringControl nameControl = new StringControl();
        StandardFormItem<elemental2.dom.HTMLElement> nameItem = new StandardFormItem<>(NAME, ra, context, nameControl);
        if (value != null) {
            nameControl.textInput().value(value);
        }
        return nameItem;
    }

    private static Promise<ModelNode> addResource(AddressTemplate template, ResourceForm pipelineForm) {
        pipelineForm.resetValidation();
        if (pipelineForm.validate()) {
            AddressTemplate resolved;
            ModelNode payload = pipelineForm.modelNode();
            if (payload.has(NAME)) {
                ModelNode nameModelNode = payload.remove(NAME);
                resolved = new WildcardResolver(LTR, nameModelNode.asString()).resolve(template);
            } else {
                resolved = AddressTemplate.of(template);
            }
            return uic().crud().create(resolved, payload).catch_(error -> {
                pipelineForm.addAlert(
                        alert(danger, "Failed to add resource").inline()
                                .addDescription(String.valueOf(error)));
                return Promise.resolve(new ModelNode());
            });
        } else {
            pipelineForm.validationAlert("Failed to add resource");
            return Promise.resolve(new ModelNode());
        }
    }
}

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

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.ui.resource.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.CredentialReferenceHandler;
import org.jboss.hal.ui.resource.pipeline.CredentialReferenceHandler.Mode;
import org.jboss.hal.ui.resource.pipeline.Pipeline;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.menu.SingleTypeahead;
import org.patternfly.component.togglegroup.ToggleGroup;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAR_TEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.resources.HalClasses.clearText;
import static org.jboss.hal.resources.HalClasses.credentialReference;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.storeReference;
import static org.patternfly.component.SelectionMode.single;
import static org.patternfly.component.togglegroup.ToggleGroup.toggleGroup;
import static org.patternfly.component.togglegroup.ToggleGroupItem.toggleGroupItem;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.form;

/**
 * {@link NativeControl} for credential reference attributes. Toggle group selection (Undefined / Clear text / Credential store)
 * with mode-specific fields.
 */
public final class CredentialReferenceControl implements NativeControl<HTMLElement> {

    private static final String MODE_KEY = "credential-reference-mode";

    private Mode originalMode;
    private Mode selectedMode;
    private FormItem storeFormItem;
    private FormItem aliasFormItem;
    private FormItem clearText0FormItem;
    private FormItem clearText1FormItem;
    private FormItem typeFormItem;
    private CredentialStoreAliasControl aliasControl;
    private HTMLContainerBuilder<HTMLDivElement> clearTextContainer;
    private HTMLContainerBuilder<HTMLDivElement> storeReferenceContainer;


    @Override
    public HTMLElement create(PipelineContext context, String identifier, ResolvedAttribute attribute) {
        originalMode = CredentialReferenceHandler.mode(attribute.value());

        // Without detachFromParent() the label would be "Credential store / ..."
        Pipeline pipeline = Pipeline.instance();
        storeFormItem = pipeline.formItem(context, attribute.child(STORE).detachFromParent());
        aliasControl = new CredentialStoreAliasControl();
        aliasFormItem = new DefaultFormItem<>(context, ALIAS, attribute.child(ALIAS).detachFromParent(), aliasControl);
        clearText0FormItem = pipeline.formItem(context, attribute.child(CLEAR_TEXT).detachFromParent());
        clearText1FormItem = pipeline.formItem(context, attribute.child(CLEAR_TEXT).detachFromParent());
        typeFormItem = pipeline.formItem(context, attribute.child(TYPE).detachFromParent());

        clearTextContainer = div().css(component(form), halComponent(credentialReference, clearText))
                .add(clearText0FormItem);
        storeReferenceContainer = div().css(component(form), halComponent(credentialReference, storeReference))
                .add(storeFormItem)
                .add(aliasFormItem)
                .add(clearText1FormItem)
                .add(typeFormItem);

        ToggleGroup toggleGroup = toggleGroup(single)
                .onSingleSelect((e, item, selected) -> selectMode(item.get(MODE_KEY)))
                .addItem(toggleGroupItem(Mode.UNDEFINED.name())
                        .store(MODE_KEY, Mode.UNDEFINED)
                        .iconAndText(Mode.UNDEFINED.icon, "Undefined"))
                .addItem(toggleGroupItem(Mode.CLEAR_TEXT.name())
                        .store(MODE_KEY, Mode.CLEAR_TEXT)
                        .iconAndText(Mode.CLEAR_TEXT.icon, "Clear text"))
                .addItem(toggleGroupItem(Mode.STORE_REFERENCE.name())
                        .store(MODE_KEY, Mode.STORE_REFERENCE)
                        .iconAndText(Mode.STORE_REFERENCE.icon, "Credential store"));

        SingleTypeahead storeTypeahead = (SingleTypeahead) storeFormItem.editableControl().control();
        storeTypeahead.menuToggle().searchInput().onClear((e, c) -> aliasControl.update(null));
        storeTypeahead.menuToggle().searchInput().onInput((e, c, value) -> aliasControl.update(value));
        storeTypeahead.menu().onSingleSelect((e, item, selected) -> {
            if (selected) {
                aliasControl.update(item.identifier());
            }
        });
        selectMode(originalMode);
        toggleGroup.select(originalMode.name(), true, false);

        return div().css(halComponent(credentialReference))
                .add(toggleGroup)
                .add(clearTextContainer)
                .add(storeReferenceContainer)
                .element();
    }

    @Override
    public HTMLElement element(HTMLElement control) {
        return control;
    }

    @Override
    public ModelNode modelNode(HTMLElement control, ResolvedAttribute attribute) {
        ModelNode result = new ModelNode();
        switch (selectedMode) {
            case UNDEFINED:
                break;
            case CLEAR_TEXT:
                ModelNode clearText0ModelNode = clearText0FormItem.editableControl().modelNode();
                if (clearText0ModelNode.isDefined()) {
                    result.get(CLEAR_TEXT).set(clearText0ModelNode);
                }
                break;
            case STORE_REFERENCE:
                ModelNode storeModelNode = storeFormItem.editableControl().modelNode();
                ModelNode aliasModelNode = aliasFormItem.editableControl().modelNode();
                ModelNode clearText1ModelNode = clearText1FormItem.editableControl().modelNode();
                ModelNode typeModelNode = typeFormItem.editableControl().modelNode();
                if (storeModelNode.isDefined()) {
                    result.get(STORE).set(storeModelNode);
                }
                if (aliasModelNode.isDefined()) {
                    result.get(ALIAS).set(aliasModelNode);
                }
                if (clearText1ModelNode.isDefined()) {
                    result.get(CLEAR_TEXT).set(clearText1ModelNode);
                }
                if (typeModelNode.isDefined()) {
                    result.get(TYPE).set(typeModelNode);
                }
                break;
        }
        return result;
    }

    @Override
    public boolean isModifiedForNew(HTMLElement control, ResolvedAttribute attribute) {
        return selectedMode != Mode.UNDEFINED;
    }

    @Override
    public boolean isModifiedForExisting(HTMLElement control, ResolvedAttribute attribute, boolean wasDefined) {
        if (selectedMode != originalMode) {
            return true;
        }

        return switch (selectedMode) {
            case CLEAR_TEXT -> clearText0FormItem.isModified();
            case STORE_REFERENCE -> storeFormItem.isModified() ||
                    aliasFormItem.isModified() ||
                    clearText1FormItem.isModified() ||
                    typeFormItem.isModified();
            default -> false;
        };
    }

    @Override
    public boolean validate(HTMLElement control, ResolvedAttribute attribute, FormGroupControl fgc) {
        return switch (selectedMode) {
            case CLEAR_TEXT -> clearText0FormItem.validate();
            case STORE_REFERENCE -> storeFormItem.validate() &&
                    aliasFormItem.validate() &&
                    clearText1FormItem.validate() &&
                    typeFormItem.validate();
            default -> true;
        };
    }

    @Override
    public void resetValidation(HTMLElement control) {
        storeFormItem.resetValidation();
        aliasFormItem.resetValidation();
        clearText0FormItem.resetValidation();
        clearText1FormItem.resetValidation();
        typeFormItem.resetValidation();
    }

    // ------------------------------------------------------ internal

    private void selectMode(Mode mode) {
        this.selectedMode = mode;
        setVisible(clearTextContainer, selectedMode == Mode.CLEAR_TEXT);
        setVisible(storeReferenceContainer, selectedMode == Mode.STORE_REFERENCE);
        resetValidation(null);
    }
}

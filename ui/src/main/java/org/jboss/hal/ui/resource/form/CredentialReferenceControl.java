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

import org.jboss.elemento.Id;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.AttributeDescriptions;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.CredentialReferenceProvider;
import org.jboss.hal.ui.resource.pipeline.CredentialReferenceProvider.Mode;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.menu.SingleTypeahead;
import org.patternfly.style.Modifiers.FullWidth;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITY_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAR_TEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.ui.resource.form.CapabilityReferenceSupport.capabilityItems;
import static org.jboss.hal.ui.resource.form.CapabilityReferenceSupport.newItem;
import static org.jboss.hal.ui.resource.form.SearchReloadInput.searchReloadInput;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.Radio.radio;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.form.TextInputType.password;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.SingleSelectMenu.singleSelectMenu;
import static org.patternfly.component.menu.SingleTypeahead.singleTypeahead;

/**
 * {@link NativeControl} for credential reference attributes. Radio mode selection (Not configured / Clear text / Credential
 * store) with mode-specific fields.
 */
public final class CredentialReferenceControl implements NativeControl<HTMLElement> {

    private enum SelectedMode {
        NOT_CONFIGURED, CLEAR_TEXT, CREDENTIAL_STORE
    }

    private static final String RADIO_GROUP = "credential-reference-mode";

    private Mode originalMode;
    private SelectedMode selectedMode;
    private String capability;
    private TextInput clearTextInput;
    private HTMLElement clearTextPanel;
    private SingleTypeahead storeTypeahead;
    private TextInput aliasInput;
    private TextInput storePasswordInput;
    private TextInput typeInput;
    private HTMLElement storePanel;
    private FormGroupControl formGroupControl;

    @Override
    public HTMLElement create(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        originalMode = CredentialReferenceProvider.mode(attribute.value());

        AttributeDescriptions nested = attribute.description().valueTypeAttributeDescriptions();
        AttributeDescription storeDescription = nested.get(STORE);
        if (storeDescription != null && storeDescription.hasDefined(CAPABILITY_REFERENCE)) {
            capability = storeDescription.get(CAPABILITY_REFERENCE).asString();
        }
        if (capability == null) {
            capability = "org.wildfly.security.credential-store";
        }

        String radioGroup = Id.build(identifier, RADIO_GROUP);

        // "Not configured" radio
        HTMLElement noneRadioContainer = div()
                .add(radio(Id.build(identifier, "none"), radioGroup, "Not configured")
                        .value(originalMode == Mode.UNDEFINED)
                        .onChange((e, r, checked) -> {
                            if (checked) {
                                switchMode(SelectedMode.NOT_CONFIGURED);
                            }
                        }))
                .element();
        if (attribute.description().required()) {
            setVisible(noneRadioContainer, false);
        }

        // "Clear text" controls
        clearTextInput = textInput(password, Id.build(identifier, "clear-text"));
        if (originalMode == Mode.CLEAR_TEXT && attribute.value().hasDefined(CLEAR_TEXT)) {
            clearTextInput.value(attribute.value().get(CLEAR_TEXT).asString());
        }
        clearTextPanel = div().css("cr-nested-fields")
                .add(fieldRow("Password", clearTextInput.element()))
                .element();

        // "Credential store" controls
        aliasInput = textInput(Id.build(identifier, "alias"));
        storePasswordInput = textInput(password, Id.build(identifier, "store-password"));
        storePasswordInput.placeholder("New password for alias...");
        typeInput = textInput(Id.build(identifier, "type"));
        typeInput.placeholder("PasswordCredential");

        if (originalMode == Mode.STORE_REFERENCE) {
            if (attribute.value().hasDefined(ALIAS)) {
                aliasInput.value(attribute.value().get(ALIAS).asString());
            }
            if (attribute.value().hasDefined(TYPE)) {
                typeInput.value(attribute.value().get(TYPE).asString());
            }
        }

        storeTypeahead = singleTypeahead(searchReloadInput(Id.build(identifier, "store"))
                .plain()
                .placeholder("Select or create store...")
                .onReload((e, c) -> storeTypeahead.menu().reload()))
                .applyToMenuToggle(FullWidth::fullWidth)
                .allowNewItems(value -> "Add \"" + value + "\"...",
                        value -> newItem(value, capability))
                .addMenu(singleSelectMenu()
                        .addContent(menuContent()
                                .addList(menuList()
                                        .addItems(capabilityItems(context.template(), capability)))));
        if (originalMode == Mode.STORE_REFERENCE && attribute.value().hasDefined(STORE)) {
            failSafeSelectStore(attribute.value().get(STORE).asString());
        }

        storePanel = div().css("cr-nested-fields")
                .add(fieldRow("Store", storeTypeahead.element()))
                .add(fieldRow("Alias", aliasInput.element()))
                .add(fieldRow("Password", storePasswordInput.element(),
                        span().css("cr-optional-label").text("(optional)").element()))
                .add(fieldRow("Type", typeInput.element(),
                        span().css("cr-optional-label").text("(optional)").element()))
                .element();

        // initial mode
        switch (originalMode) {
            case STORE_REFERENCE:
                selectedMode = SelectedMode.CREDENTIAL_STORE;
                break;
            case CLEAR_TEXT:
                selectedMode = SelectedMode.CLEAR_TEXT;
                break;
            default:
                selectedMode = SelectedMode.NOT_CONFIGURED;
                break;
        }
        setVisible(clearTextPanel, selectedMode == SelectedMode.CLEAR_TEXT);
        setVisible(storePanel, selectedMode == SelectedMode.CREDENTIAL_STORE);

        return div()
                .add(noneRadioContainer)
                .add(radio(Id.build(identifier, "clear-text-radio"), radioGroup, "Clear text")
                        .value(originalMode == Mode.CLEAR_TEXT)
                        .onChange((e, r, checked) -> {
                            if (checked) {
                                switchMode(SelectedMode.CLEAR_TEXT);
                            }
                        }))
                .add(clearTextPanel)
                .add(radio(Id.build(identifier, "store-radio"), radioGroup, "Credential store")
                        .value(originalMode == Mode.STORE_REFERENCE)
                        .onChange((e, r, checked) -> {
                            if (checked) {
                                switchMode(SelectedMode.CREDENTIAL_STORE);
                            }
                        }))
                .add(storePanel)
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
            case NOT_CONFIGURED:
                break;
            case CLEAR_TEXT:
                result.get(CLEAR_TEXT).set(clearTextValue());
                break;
            case CREDENTIAL_STORE:
                result.get(STORE).set(storeValue());
                result.get(ALIAS).set(aliasValue());
                String pw = storePasswordValue();
                if (!pw.isEmpty()) {
                    result.get(CLEAR_TEXT).set(pw);
                }
                String type = typeValue();
                if (!type.isEmpty()) {
                    result.get(TYPE).set(type);
                }
                break;
        }
        return result;
    }

    @Override
    public boolean isModifiedForNew(HTMLElement control, ResolvedAttribute attribute) {
        return selectedMode != SelectedMode.NOT_CONFIGURED;
    }

    @Override
    public boolean isModifiedForExisting(HTMLElement control, ResolvedAttribute attribute, boolean wasDefined) {
        Mode currentMode = selectedModeToMode();
        if (currentMode != originalMode) {
            return true;
        }
        switch (selectedMode) {
            case CLEAR_TEXT:
                String originalClearText = attribute.value().hasDefined(CLEAR_TEXT)
                        ? attribute.value().get(CLEAR_TEXT).asString() : "";
                return !originalClearText.equals(clearTextValue());
            case CREDENTIAL_STORE:
                String originalStore = attribute.value().hasDefined(STORE) ? attribute.value().get(STORE).asString() : "";
                String originalAlias = attribute.value().hasDefined(ALIAS) ? attribute.value().get(ALIAS).asString() : "";
                String originalType = attribute.value().hasDefined(TYPE) ? attribute.value().get(TYPE).asString() : "";
                if (!originalStore.equals(storeValue()) || !originalAlias.equals(aliasValue())) {
                    return true;
                }
                if (!originalType.equals(typeValue())) {
                    return true;
                }
                return !storePasswordValue().isEmpty();
            default:
                return wasDefined;
        }
    }

    @Override
    public boolean validate(HTMLElement control, ResolvedAttribute attribute, FormGroupControl fgc) {
        this.formGroupControl = fgc;
        switch (selectedMode) {
            case CLEAR_TEXT:
                if (clearTextValue().isEmpty()) {
                    clearTextInput.validated(error);
                    fgc.addHelperText(FormItemBricks.requiredHelperText(attribute));
                    return false;
                }
                return true;
            case CREDENTIAL_STORE:
                boolean valid = true;
                if (storeValue().isEmpty()) {
                    storeTypeahead.menuToggle().validated(error);
                    valid = false;
                }
                if (aliasValue().isEmpty()) {
                    aliasInput.validated(error);
                    valid = false;
                }
                if (!valid) {
                    fgc.addHelperText(FormItemBricks.requiredHelperText(attribute));
                }
                return valid;
            default:
                return true;
        }
    }

    @Override
    public void resetValidation(HTMLElement control) {
        clearTextInput.resetValidation();
        aliasInput.resetValidation();
        if (storeTypeahead != null) {
            storeTypeahead.menuToggle().resetValidation();
        }
    }

    // ------------------------------------------------------ internal

    private void failSafeSelectStore(String value) {
        if (storeTypeahead.menu().hasAsyncItems()) {
            storeTypeahead.menuToggle().text(value);
            storeTypeahead.onLoaded((__, st) -> st.select(value));
        } else {
            storeTypeahead.select(value);
        }
    }

    private void switchMode(SelectedMode mode) {
        selectedMode = mode;
        setVisible(clearTextPanel, mode == SelectedMode.CLEAR_TEXT);
        setVisible(storePanel, mode == SelectedMode.CREDENTIAL_STORE);
        resetValidation(null);
        if (formGroupControl != null) {
            formGroupControl.removeHelperText();
        }
    }

    private Mode selectedModeToMode() {
        switch (selectedMode) {
            case CLEAR_TEXT:
                return Mode.CLEAR_TEXT;
            case CREDENTIAL_STORE:
                return Mode.STORE_REFERENCE;
            default:
                return Mode.UNDEFINED;
        }
    }

    private String clearTextValue() {
        return clearTextInput.value() != null ? clearTextInput.value() : "";
    }

    private String storeValue() {
        return storeTypeahead != null
                ? (storeTypeahead.menuToggle().searchInput().value() != null
                        ? storeTypeahead.menuToggle().searchInput().value() : "")
                : "";
    }

    private String aliasValue() {
        return aliasInput.value() != null ? aliasInput.value() : "";
    }

    private String storePasswordValue() {
        return storePasswordInput.value() != null ? storePasswordInput.value() : "";
    }

    private String typeValue() {
        return typeInput.value() != null ? typeInput.value() : "";
    }

    private static HTMLElement fieldRow(String labelText, HTMLElement fieldControl) {
        return fieldRow(labelText, fieldControl, null);
    }

    private static HTMLElement fieldRow(String labelText, HTMLElement fieldControl, HTMLElement suffix) {
        HTMLElement row = div().css("cr-field-row")
                .add(span().css("cr-field-label").text(labelText))
                .add(div().css("cr-field-input").add(fieldControl))
                .element();
        if (suffix != null) {
            row.appendChild(suffix);
        }
        return row;
    }
}

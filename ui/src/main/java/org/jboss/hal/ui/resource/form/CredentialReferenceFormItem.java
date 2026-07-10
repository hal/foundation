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
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.AttributeDescriptions;
import org.jboss.hal.ui.resource.CredentialReference;
import org.jboss.hal.ui.resource.CredentialReference.Mode;
import org.jboss.hal.ui.resource.ResourceAttribute;
import org.patternfly.component.form.FormGroupLabel;
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
import static org.jboss.hal.ui.resource.form.HelperTexts.required;
import static org.jboss.hal.ui.resource.form.SearchReloadInput.searchReloadInput;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.Radio.radio;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.form.TextInputType.password;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.SingleSelectMenu.singleSelectMenu;
import static org.patternfly.component.menu.SingleTypeahead.singleTypeahead;

/**
 * Form item for editing credential reference attributes. Presents three radio buttons for mode selection (Not configured /
 * Clear text / Credential store) with mode-specific fields:
 * <ul>
 *     <li><b>Not configured</b> — no fields, produces {@code undefine-attribute}</li>
 *     <li><b>Clear text</b> — password input</li>
 *     <li><b>Credential store</b> — store typeahead (with inline create), alias input, optional password for auto-provision,
 *         optional type</li>
 * </ul>
 * The "Not configured" radio is hidden when the attribute is required.
 */
class CredentialReferenceFormItem extends FormItem {

    private enum SelectedMode {
        NOT_CONFIGURED, CLEAR_TEXT, CREDENTIAL_STORE
    }

    private static final String RADIO_GROUP = "credential-reference-mode";

    private final AddressTemplate template;
    private final Mode originalMode;
    private SelectedMode selectedMode;
    private String capability;

    // clear-text mode controls
    private final TextInput clearTextInput;
    private final HTMLElement clearTextPanel;

    // credential-store mode controls
    private SingleTypeahead storeTypeahead;
    private final TextInput aliasInput;
    private final TextInput storePasswordInput;
    private final TextInput typeInput;
    private final HTMLElement storePanel;

    // radio for "not configured" (may be hidden)
    private final HTMLElement noneRadioContainer;

    CredentialReferenceFormItem(String identifier, ResourceAttribute ra, FormGroupLabel label, FormItemFlags flags,
            AddressTemplate template) {
        super(identifier, ra, label, flags);
        this.template = template;
        this.originalMode = CredentialReference.mode(ra.value);

        // resolve capability from nested store description
        AttributeDescriptions nested = ra.description.valueTypeAttributeDescriptions();
        AttributeDescription storeDescription = nested.get(STORE);
        if (storeDescription.hasDefined(CAPABILITY_REFERENCE)) {
            capability = storeDescription.get(CAPABILITY_REFERENCE).asString();
        }

        String radioGroup = Id.build(identifier, RADIO_GROUP);

        // -- "Not configured" radio
        noneRadioContainer = div()
                .add(radio(Id.build(identifier, "none"), radioGroup, "Not configured")
                        .value(originalMode == Mode.UNDEFINED)
                        .onChange((e, radio, checked) -> {
                            if (checked) {
                                switchMode(SelectedMode.NOT_CONFIGURED);
                            }
                        }))
                .element();
        if (ra.description.required()) {
            setVisible(noneRadioContainer, false);
        }

        // -- "Clear text" radio + panel
        clearTextInput = textInput(password, Id.build(identifier, "clear-text"));
        if (originalMode == Mode.CLEAR_TEXT && ra.value.hasDefined(CLEAR_TEXT)) {
            clearTextInput.value(ra.value.get(CLEAR_TEXT).asString());
        }
        clearTextPanel = div().css("cr-nested-fields")
                .add(fieldRow("Password", clearTextInput.element()))
                .element();

        // -- "Credential store" radio + panel
        aliasInput = textInput(Id.build(identifier, "alias"));
        storePasswordInput = textInput(password, Id.build(identifier, "store-password"));
        storePasswordInput.placeholder("New password for alias...");
        typeInput = textInput(Id.build(identifier, "type"));
        typeInput.placeholder("PasswordCredential");

        if (originalMode == Mode.STORE_REFERENCE) {
            if (ra.value.hasDefined(ALIAS)) {
                aliasInput.value(ra.value.get(ALIAS).asString());
            }
            if (ra.value.hasDefined(TYPE)) {
                typeInput.value(ra.value.get(TYPE).asString());
            }
        }

        storePanel = div().css("cr-nested-fields")
                .add(fieldRow("Store", storeTypeaheadControl().element()))
                .add(fieldRow("Alias", aliasInput.element()))
                .add(fieldRow("Password", storePasswordInput.element(),
                        span().css("cr-optional-label").text("(optional)").element()))
                .add(fieldRow("Type", typeInput.element(),
                        span().css("cr-optional-label").text("(optional)").element()))
                .element();

        // -- determine initial mode and visibility
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

        // -- build the form group
        formGroupControl = formGroupControl()
                .add(noneRadioContainer)
                .add(radio(Id.build(identifier, "clear-text-radio"), radioGroup, "Clear text")
                        .value(originalMode == Mode.CLEAR_TEXT)
                        .onChange((e, radio, checked) -> {
                            if (checked) {
                                switchMode(SelectedMode.CLEAR_TEXT);
                            }
                        }))
                .add(clearTextPanel)
                .add(radio(Id.build(identifier, "store-radio"), radioGroup, "Credential store")
                        .value(originalMode == Mode.STORE_REFERENCE)
                        .onChange((e, radio, checked) -> {
                            if (checked) {
                                switchMode(SelectedMode.CREDENTIAL_STORE);
                            }
                        }))
                .add(storePanel);

        formGroup = formGroup(identifier)
                .required(ra.description.required())
                .addLabel(label)
                .addControl(formGroupControl);
    }

    // ------------------------------------------------------ controls

    private SingleTypeahead storeTypeaheadControl() {
        if (capability == null) {
            capability = "org.wildfly.security.credential-store";
        }
        SearchReloadInput searchReloadInput = searchReloadInput(Id.build(identifier, "store"))
                .plain()
                .placeholder("Select or create store...")
                .onReload((e, c) -> storeTypeahead.menu().reload());
        storeTypeahead = singleTypeahead(searchReloadInput)
                .applyToMenuToggle(FullWidth::fullWidth)
                .allowNewItems(value -> "Add \"" + value + "\"...", value -> newItem(value, capability))
                .addMenu(singleSelectMenu()
                        .addContent(menuContent()
                                .addList(menuList()
                                        .addItems(capabilityItems(template, capability)))));
        if (originalMode == Mode.STORE_REFERENCE && ra.value.hasDefined(STORE)) {
            failSafeSelectStore(ra.value.get(STORE).asString());
        }
        return storeTypeahead;
    }

    private void failSafeSelectStore(String value) {
        if (storeTypeahead.menu().hasAsyncItems()) {
            storeTypeahead.menuToggle().text(value);
            storeTypeahead.onLoaded((__, st) -> st.select(value));
        } else {
            storeTypeahead.select(value);
        }
    }

    // ------------------------------------------------------ mode switching

    private void switchMode(SelectedMode mode) {
        selectedMode = mode;
        setVisible(clearTextPanel, mode == SelectedMode.CLEAR_TEXT);
        setVisible(storePanel, mode == SelectedMode.CREDENTIAL_STORE);
        resetValidation();
    }

    // ------------------------------------------------------ validation

    @Override
    void resetValidation() {
        clearTextInput.resetValidation();
        aliasInput.resetValidation();
        if (storeTypeahead != null) {
            storeTypeahead.menuToggle().resetValidation();
        }
        if (formGroupControl != null) {
            formGroupControl.removeHelperText();
        }
    }

    @Override
    boolean validate() {
        switch (selectedMode) {
            case CLEAR_TEXT:
                if (clearTextValue().isEmpty()) {
                    clearTextInput.validated(error);
                    formGroupControl.addHelperText(required(ra));
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
                    formGroupControl.addHelperText(required(ra));
                }
                return valid;
            default:
                return true;
        }
    }

    // ------------------------------------------------------ data

    @Override
    boolean isNativeModifiedForNew() {
        return selectedMode != SelectedMode.NOT_CONFIGURED;
    }

    @Override
    boolean isNativeModifiedForExisting(boolean wasDefined) {
        Mode currentMode = selectedModeToMode();
        if (currentMode != originalMode) {
            return true;
        }
        switch (selectedMode) {
            case CLEAR_TEXT:
                String originalClearText = ra.value.hasDefined(CLEAR_TEXT) ? ra.value.get(CLEAR_TEXT).asString() : "";
                return !originalClearText.equals(clearTextValue());
            case CREDENTIAL_STORE:
                String originalStore = ra.value.hasDefined(STORE) ? ra.value.get(STORE).asString() : "";
                String originalAlias = ra.value.hasDefined(ALIAS) ? ra.value.get(ALIAS).asString() : "";
                String originalType = ra.value.hasDefined(TYPE) ? ra.value.get(TYPE).asString() : "";
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
    ModelNode modelNode() {
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

    // ------------------------------------------------------ internal

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

    private static HTMLElement fieldRow(String labelText, HTMLElement control) {
        return fieldRow(labelText, control, null);
    }

    private static HTMLElement fieldRow(String labelText, HTMLElement control, HTMLElement suffix) {
        HTMLElement row = div().css("cr-field-row")
                .add(span().css("cr-field-label").text(labelText))
                .add(div().css("cr-field-input").add(control))
                .element();
        if (suffix != null) {
            row.appendChild(suffix);
        }
        return row;
    }
}

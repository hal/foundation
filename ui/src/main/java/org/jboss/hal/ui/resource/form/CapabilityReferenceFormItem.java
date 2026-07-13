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

import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.pipeline.ResolvedAttribute;

import org.jboss.hal.dmr.ModelNode;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.menu.SingleTypeahead;
import org.patternfly.style.Modifiers.FullWidth;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITY_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.ui.resource.form.CapabilityReferenceSupport.capabilityItems;
import static org.jboss.hal.ui.resource.form.CapabilityReferenceSupport.newItem;
import static org.jboss.hal.ui.resource.form.InputMode.EXPRESSION;
import static org.jboss.hal.ui.resource.form.InputMode.NATIVE;
import static org.jboss.hal.ui.resource.form.SearchReloadInput.searchReloadInput;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.SingleSelectMenu.singleSelectMenu;
import static org.patternfly.component.menu.SingleTypeahead.singleTypeahead;

/** Form item for single STRING attributes with a capability reference, rendered as a typeahead select. */
public class CapabilityReferenceFormItem extends AbstractFormItem {

    private final String capability;
    private /*final*/ SingleTypeahead typeahead;

    public CapabilityReferenceFormItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        super(identifier, attribute, context);
        this.capability = attribute.description().get(CAPABILITY_REFERENCE).asString();
        defaultSetup();
    }

    @Override
    FormGroupControl readOnlyGroup() {
        return readOnlyGroupWithExpressionSwitch();
    }

    @Override
    FormGroupControl nativeGroup() {
        return formGroupControl().add(typeaheadControl());
    }

    @Override
    HTMLElement nativeContainer() {
        nativeContainer = inputGroup()
                .addItem(inputGroupItem().addButton(switchToExpressionModeButton()))
                .addItem(inputGroupItem().fill().add(typeaheadControl()))
                .element();
        return nativeContainer;
    }

    private SingleTypeahead typeaheadControl() {
        SearchReloadInput searchReloadInput = searchReloadInput(identifier)
                .plain()
                .placeholder("")
                .onReload((e, c) -> typeahead.menu().reload());
        typeahead = singleTypeahead(searchReloadInput)
                .applyToMenuToggle(FullWidth::fullWidth)
                .allowNewItems(value -> "Add \"" + value + "\"...", value -> newItem(value, capability))
                .addMenu(singleSelectMenu()
                        .addContent(menuContent()
                                .addList(menuList()
                                        .addItems(capabilityItems(context.template(), capability)))));

        if (attribute.value().isDefined()) {
            failSafeSelectValue(attribute.value().asString());
        } else if (attribute.description().hasDefault()) {
            typeahead.menuToggle().searchInput().placeholder(attribute.description().get(DEFAULT).asString());
        } else if (attribute.description().nillable()) {
            typeahead.menuToggle().searchInput().placeholder(UNDEFINED);
        }
        return typeahead;
    }

    // ------------------------------------------------------ validation

    @Override
    public void resetValidation() {
        super.resetValidation();
        if (typeahead != null) {
            typeahead.menuToggle().resetValidation();
        }
    }

    @Override
    public boolean validate() {
        if (inputMode == NATIVE) {
            if (requiredOnItsOwn() && value().isEmpty()) {
                typeahead.menuToggle().validated(error);
                formGroupControl.addHelperText(requiredHelperText());
                return false;
            }
        } else if (inputMode == EXPRESSION) {
            return validateExpressionMode();
        }
        return true;
    }

    // ------------------------------------------------------ data

    @Override
    boolean isNativeModifiedForNew() {
        String v = value();
        if (attribute.description().hasDefault()) {
            return !attribute.description().get(DEFAULT).asString().equals(v);
        } else {
            return v != null && !v.isEmpty();
        }
    }

    @Override
    boolean isNativeModifiedForExisting(boolean wasDefined) {
        String v = value();
        if (wasDefined) {
            return attribute.expression() || !attribute.value().asString().equals(v);
        } else {
            return v != null && !v.isEmpty();
        }
    }

    @Override
    public ModelNode modelNode() {
        if (inputMode == NATIVE) {
            String v = value();
            if (v == null || v.isEmpty()) {
                return new ModelNode();
            } else {
                return new ModelNode().set(v);
            }
        } else if (inputMode == EXPRESSION) {
            return expressionModelNode();
        }
        return new ModelNode();
    }

    // ------------------------------------------------------ events

    @Override
    void afterSwitchedToNativeMode() {
        if (attribute.value().isDefined() && !attribute.expression()) {
            failSafeSelectValue(attribute.value().asString());
        } else {
            if (attribute.description().hasDefault()) {
                failSafeSelectValue(attribute.description().get(DEFAULT).asString());
            } else if (attribute.description().nillable()) {
                typeahead.menuToggle().searchInput().placeholder(UNDEFINED);
            }
        }
    }

    private void failSafeSelectValue(String value) {
        if (typeahead.menu().hasAsyncItems()) {
            typeahead.menuToggle().text(value);
            typeahead.onLoaded((__, st) -> st.select(value));
        } else {
            typeahead.select(value);
        }
    }

    private String value() {
        return typeahead != null ? typeahead.menuToggle().searchInput().value() : "";
    }
}

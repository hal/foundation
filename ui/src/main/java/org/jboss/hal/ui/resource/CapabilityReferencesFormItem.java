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
import org.jboss.hal.meta.AddressTemplate;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MultiTypeahead;
import org.patternfly.style.Modifiers.FullWidth;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.ui.resource.CapabilityReferenceSupport.capabilityItems;
import static org.jboss.hal.ui.resource.CapabilityReferenceSupport.newItem;
import static org.jboss.hal.ui.resource.FilterReloadInput.filterReloadInput;
import static org.jboss.hal.ui.resource.FormItemFlags.Scope.EXISTING_RESOURCE;
import static org.jboss.hal.ui.resource.FormItemFlags.Scope.NEW_RESOURCE;
import static org.jboss.hal.ui.resource.FormItemInputMode.EXPRESSION;
import static org.jboss.hal.ui.resource.FormItemInputMode.NATIVE;
import static org.jboss.hal.ui.resource.HelperTexts.required;
import static org.jboss.hal.ui.resource.StringListSupport.defaultValues;
import static org.jboss.hal.ui.resource.StringListSupport.isExistingModified;
import static org.jboss.hal.ui.resource.StringListSupport.isNewModified;
import static org.jboss.hal.ui.resource.StringListSupport.modelValues;
import static org.jboss.hal.ui.resource.StringListSupport.valuesModelNode;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MultiSelectMenu.multiSelectMenu;
import static org.patternfly.component.menu.MultiTypeahead.multiTypeahead;

class CapabilityReferencesFormItem extends FormItem {

    private final AddressTemplate template;
    private final String capability;
    // The typeahead control is created in the constructor by
    // defaultSetup() -> nativeContainer() -> typeaheadControl().
    // It's, so to speak, final and never null!
    private /*final*/ MultiTypeahead typeahead;

    CapabilityReferencesFormItem(String identifier, ResourceAttribute ra, FormGroupLabel label, FormItemFlags flags,
            AddressTemplate template, String capability) {
        super(identifier, ra, label, flags);
        this.template = template;
        this.capability = capability;
        defaultSetup();
    }

    FormGroupControl readOnlyGroup() {
        return readOnlyGroupWithExpressionSwitch();
    }

    @Override
    FormGroupControl nativeGroup() {
        return formGroupControl().add(typeaheadControl());
    }

    @Override
    HTMLElement nativeContainer() {
        // Recreate every time the container is requested. Otherwise, the popper/menu won't work as expected.
        nativeContainer = inputGroup()
                .addItem(inputGroupItem().addButton(switchToExpressionModeButton()))
                .addItem(inputGroupItem().fill().add(typeaheadControl()))
                .element();
        return nativeContainer;
    }

    private MultiTypeahead typeaheadControl() {
        FilterReloadInput filterReloadInput = filterReloadInput(identifier)
                .plain()
                .placeholder("")
                .onReload((e, c) -> typeahead.menu().reload());
        typeahead = multiTypeahead(filterReloadInput)
                .applyTo(FullWidth::fullWidth)
                .allowNewItems(value -> "Add \"" + value + "\"...", value -> newItem(value, capability))
                .addMenu(multiSelectMenu()
                        .addContent(menuContent()
                                .addList(menuList()
                                        .addItems(capabilityItems(template, capability)))));

        if (ra.value.isDefined()) {
            values(modelValues(ra));
        } else if (ra.description.hasDefault()) {
            typeahead.menuToggle().filterInput().placeholder(ra.description.get(DEFAULT).asString());
        } else if (ra.description.nillable()) {
            typeahead.menuToggle().filterInput().placeholder(UNDEFINED);
        }
        return typeahead;
    }

    // ------------------------------------------------------ validation

    @Override
    void resetValidation() {
        super.resetValidation();
        typeahead.menuToggle().resetValidation();
    }

    @Override
    boolean validate() {
        if (inputMode == NATIVE) {
            if (requiredOnItsOwn()) {
                if (values().isEmpty()) {
                    typeahead.menuToggle().validated(error);
                    formGroupControl.addHelperText(required(ra));
                    return false;
                }
            }
        } else if (inputMode == EXPRESSION) {
            return validateExpressionMode();
        }
        return true;
    }

    // ------------------------------------------------------ data

    @Override
    boolean isModified() {
        if (ra.readable && !ra.description.readOnly()) {
            if (flags.scope == NEW_RESOURCE) {
                if (inputMode == NATIVE) {
                    return isNewModified(ra, values());
                } else if (inputMode == EXPRESSION) {
                    return isExpressionModified();
                }
            } else if (flags.scope == EXISTING_RESOURCE) {
                if (inputMode == NATIVE) {
                    return isExistingModified(ra, values(), ra.value.isDefined());
                } else if (inputMode == EXPRESSION) {
                    return isExpressionModified();
                }
            } else {
                unknownScope();
            }
        }
        return false;
    }

    @Override
    ModelNode modelNode() {
        if (inputMode == NATIVE) {
            return valuesModelNode(values());
        } else if (inputMode == EXPRESSION) {
            return expressionModelNode();
        }
        return new ModelNode();
    }

    // ------------------------------------------------------ events

    @Override
    void afterSwitchedToExpressionMode() {
        boolean wasDefined = ra.value.isDefined();
        if (wasDefined && !ra.expression) {
            values(modelValues(ra));
        } else {
            if (ra.description.hasDefault()) {
                List<String> defaultValues = defaultValues(ra);
                typeahead.menuToggle().searchInput().placeholder(String.join(" ", defaultValues));
                values(defaultValues);
            } else if (ra.description.nillable()) {
                typeahead.menuToggle().searchInput().placeholder(UNDEFINED);
            }
        }
    }

    // ------------------------------------------------------ internal

    private void values(List<String> values) {
        if (typeahead.menu().hasAsyncItems()) {
            typeahead.onLoaded((__, st) -> st.selectIdentifiers(values));
        } else {
            typeahead.selectIdentifiers(values);
        }
    }

    private List<String> values() {
        return typeahead.menu().items().stream().map(MenuItem::text).collect(toList());
    }
}

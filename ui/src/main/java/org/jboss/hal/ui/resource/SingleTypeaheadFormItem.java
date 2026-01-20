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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;
import org.patternfly.component.AsyncItems;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MenuList;
import org.patternfly.component.menu.MenuToggle;
import org.patternfly.component.menu.SingleTypeahead;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.resource.FormItemFlags.Scope.EXISTING_RESOURCE;
import static org.jboss.hal.ui.resource.FormItemFlags.Scope.NEW_RESOURCE;
import static org.jboss.hal.ui.resource.FormItemInputMode.EXPRESSION;
import static org.jboss.hal.ui.resource.FormItemInputMode.NATIVE;
import static org.jboss.hal.ui.resource.HelperTexts.required;
import static org.jboss.hal.ui.resource.SearchReloadInput.searchReloadInput;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuItem.menuItem;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuToggle.menuToggle;
import static org.patternfly.component.menu.SingleTypeahead.singleTypeahead;
import static org.patternfly.component.menu.SingleTypeaheadMenu.singleTypeaheadMenu;

class SingleTypeaheadFormItem extends FormItem {

    private final AddressTemplate template;
    private final String capability;
    // The single typeahead control is created in the constructor by
    // defaultSetup() -> nativeContainer() -> singleTypeaheadControl().
    // It's, so to speak, final and never null!
    private /*final*/ SingleTypeahead singleTypeahead;

    SingleTypeaheadFormItem(String identifier, ResourceAttribute ra, FormGroupLabel label, FormItemFlags flags,
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
        return formGroupControl().add(singleTypeaheadControl());
    }

    @Override
    HTMLElement nativeContainer() {
        // Recreate every time the container is requested. Otherwise, the popper/menu won't work as expected.
        nativeContainer = inputGroup()
                .addItem(inputGroupItem().addButton(switchToExpressionModeButton()))
                .addItem(inputGroupItem().fill().add(singleTypeaheadControl()))
                .element();
        return nativeContainer;
    }

    private SingleTypeahead singleTypeaheadControl() {
        AsyncItems<MenuList, MenuItem> asyncItems = ml -> uic().capabilityRegistry().suggestCapabilities(template, capability)
                .then(capabilities -> Promise.resolve(capabilities.stream()
                        .sorted()
                        .map(c -> menuItem(c, c))
                        .collect(toList())));
        SearchReloadInput searchReloadInput = searchReloadInput(identifier)
                .plain()
                .placeholder("")
                .onReload((e, c) -> singleTypeahead.menu().reload());
        MenuToggle menuToggle = menuToggle(searchReloadInput).fullWidth();
        singleTypeahead = singleTypeahead(menuToggle)
                .addMenu(singleTypeaheadMenu()
                        .addContent(menuContent()
                                .addList(menuList()
                                        .addItems(asyncItems))));

        if (ra.value.isDefined()) {
            failSafeSelectValue(ra.value.asString());
        } else if (ra.description.hasDefault()) {
            searchReloadInput.placeholder(ra.description.get(DEFAULT).asString());
        } else if (ra.description.nillable()) {
            searchReloadInput.placeholder(UNDEFINED);
        }

        return singleTypeahead;
    }

    // ------------------------------------------------------ validation

    @Override
    void resetValidation() {
        super.resetValidation();
        singleTypeahead.menuToggle().resetValidation();
    }

    @Override
    boolean validate() {
        if (inputMode == NATIVE) {
            if (requiredOnItsOwn() && singleTypeahead.value().isEmpty()) {
                singleTypeahead.menuToggle().validated(error);
                formGroupControl.addHelperText(required(ra));
                return false;
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
                    String value = singleTypeahead.value();
                    if (ra.description.hasDefault()) {
                        return !ra.description.get(DEFAULT).asString().equals(value);
                    } else {
                        return value != null && !value.isEmpty();
                    }
                } else if (inputMode == EXPRESSION) {
                    return isExpressionModified();
                }
            } else if (flags.scope == EXISTING_RESOURCE) {
                boolean wasDefined = ra.value.isDefined();
                if (inputMode == NATIVE) {
                    String value = singleTypeahead.value();
                    if (wasDefined) {
                        // modified if the original value was an expression or is different from the current user input
                        String originalValue = ra.value.asString();
                        return ra.expression || !originalValue.equals(value);
                    } else {
                        return value != null && !value.isEmpty();
                    }
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
            String value = singleTypeahead.value();
            if (value == null || value.isEmpty()) {
                return new ModelNode();
            } else {
                return new ModelNode().set(value);
            }
        } else if (inputMode == EXPRESSION) {
            return expressionModelNode();
        }
        return new ModelNode();
    }

    // ------------------------------------------------------ events

    @Override
    @SuppressWarnings("DuplicatedCode")
    void afterSwitchedToExpressionMode() {
        boolean wasDefined = ra.value.isDefined();
        if (wasDefined && !ra.expression) {
            String originalValue = ra.value.asString();
            failSafeSelectValue(originalValue);
        } else {
            if (ra.description.hasDefault()) {
                singleTypeahead.menuToggle().searchInput().placeholder(ra.description.get(DEFAULT).asString());
                failSafeSelectValue(ra.description.get(DEFAULT).asString());
            } else if (ra.description.nillable()) {
                singleTypeahead.menuToggle().searchInput().placeholder(UNDEFINED);
            }
        }
    }

    private void failSafeSelectValue(String value) {
        if (singleTypeahead.menu().hasAsyncItems()) {
            singleTypeahead.menuToggle().text(value);
            singleTypeahead.onLoaded((__, st) -> st.select(value));
        } else {
            singleTypeahead.select(value);
        }
    }
}

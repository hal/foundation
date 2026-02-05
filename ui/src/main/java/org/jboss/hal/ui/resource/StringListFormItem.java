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
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.component.label.Label;
import org.patternfly.component.textinputgroup.FilterInput;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
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
import static org.patternfly.component.textinputgroup.FilterInput.filterInput;

class StringListFormItem extends FormItem {

    // The select control is created in the constructor by defaultSetup() -> nativeContainer() -> stringListControl().
    // It's, so to speak, final and never null!
    private /*final*/ FilterInput filterInput;

    StringListFormItem(String identifier, ResourceAttribute ra, FormGroupLabel label, FormItemFlags flags) {
        super(identifier, ra, label, flags);
        defaultSetup();
    }

    @Override
    FormGroupControl readOnlyGroup() {
        return readOnlyGroupWithExpressionSwitch();
    }

    @Override
    FormGroupControl nativeGroup() {
        return formGroupControl().add(stringListControl());
    }

    @Override
    HTMLElement nativeContainer() {
        if (nativeContainer == null) {
            nativeContainer = inputGroup()
                    .addItem(inputGroupItem().addButton(switchToExpressionModeButton()))
                    .addItem(inputGroupItem().fill().add(stringListControl()))
                    .element();
        }
        return nativeContainer;
    }

    private FilterInput stringListControl() {
        filterInput = filterInput(identifier)
                .applyTo(inputElement -> inputElement.autocomplete("off"))
                .allowDuplicates(false);
        if (ra.value.isDefined()) {
            values(modelValues(ra));
        } else if (ra.description.hasDefault()) {
            filterInput.placeholder(ra.description.get(DEFAULT).asString());
        } else if (ra.description.nillable()) {
            filterInput.placeholder(UNDEFINED);
        }

        return filterInput;
    }

    // ------------------------------------------------------ validation

    @Override
    void resetValidation() {
        super.resetValidation();
        filterInput.resetValidation();
    }

    @Override
    boolean validate() {
        if (requiredOnItsOwn()) {
            if (inputMode == NATIVE) {
                if (values().isEmpty()) {
                    filterInput.validated(error);
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
        // TODO Refactor expression support. Expressions are on the list elements (strings), not on the list itself.
        //  See https://wildfly.zulipchat.com/#narrow/channel/174184-wildfly-developers/topic/Expression.20for.20attribute.20of.20type.20LIST/with/479335334
        boolean wasDefined = ra.value.isDefined();
        if (wasDefined && !ra.expression) {
            values(modelValues(ra));
        } else {
            if (ra.description.hasDefault()) {
                List<String> defaultValues = defaultValues(ra);
                filterInput.placeholder(String.join(" ", defaultValues));
                values(defaultValues);
            } else if (ra.description.nillable()) {
                filterInput.placeholder(UNDEFINED);
            }
        }
    }

    // ------------------------------------------------------ internal

    private void values(List<String> values) {
        filterInput.labelGroup().clear();
        filterInput.labelGroup().addItems(values, value -> filterInput.textToLabel().apply(value));
    }

    private List<String> values() {
        return filterInput.labelGroup().items().stream().map(Label::text).collect(toList());
    }
}

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

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.label.Label;
import org.patternfly.component.textinputgroup.FilterInput;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.ui.resource.form.InputMode.EXPRESSION;
import static org.jboss.hal.ui.resource.form.InputMode.NATIVE;
import static org.jboss.hal.ui.resource.form.StringListSupport.defaultValues;
import static org.jboss.hal.ui.resource.form.StringListSupport.isExistingModified;
import static org.jboss.hal.ui.resource.form.StringListSupport.isNewModified;
import static org.jboss.hal.ui.resource.form.StringListSupport.modelValues;
import static org.jboss.hal.ui.resource.form.StringListSupport.valuesModelNode;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.textinputgroup.FilterInput.filterInput;

/** Form item for LIST-of-STRING attributes, rendered as a label-based multi-value input. */
public class StringListFormItem extends AbstractFormItem {

    private /*final*/ FilterInput filterInput;

    public StringListFormItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        super(identifier, attribute, context);
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
        if (attribute.value().isDefined()) {
            values(modelValues(attribute));
        } else if (attribute.description().hasDefault()) {
            filterInput.placeholder(attribute.description().get(DEFAULT).asString());
        } else if (attribute.description().nillable()) {
            filterInput.placeholder(UNDEFINED);
        }
        return filterInput;
    }

    // ------------------------------------------------------ validation

    @Override
    public void resetValidation() {
        super.resetValidation();
        if (filterInput != null) {
            filterInput.resetValidation();
        }
    }

    @Override
    public boolean validate() {
        if (requiredOnItsOwn()) {
            if (inputMode == NATIVE) {
                if (values().isEmpty()) {
                    filterInput.validated(error);
                    formGroupControl.addHelperText(requiredHelperText());
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
    boolean isNativeModifiedForNew() {
        return isNewModified(attribute, values());
    }

    @Override
    boolean isNativeModifiedForExisting(boolean wasDefined) {
        return isExistingModified(attribute, values(), wasDefined);
    }

    @Override
    public ModelNode modelNode() {
        if (inputMode == NATIVE) {
            return valuesModelNode(values());
        } else if (inputMode == EXPRESSION) {
            return expressionModelNode();
        }
        return new ModelNode();
    }

    // ------------------------------------------------------ events

    @Override
    void afterSwitchedToNativeMode() {
        if (attribute.value().isDefined() && !attribute.expression()) {
            values(modelValues(attribute));
        } else {
            if (attribute.description().hasDefault()) {
                List<String> dv = defaultValues(attribute);
                filterInput.placeholder(String.join(" ", dv));
                values(dv);
            } else if (attribute.description().nillable()) {
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

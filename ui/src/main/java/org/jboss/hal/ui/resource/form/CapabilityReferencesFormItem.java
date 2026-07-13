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
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MultiTypeahead;
import org.patternfly.style.Modifiers.FullWidth;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITY_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.ui.resource.form.CapabilityReferenceSupport.capabilityItems;
import static org.jboss.hal.ui.resource.form.CapabilityReferenceSupport.newItem;
import static org.jboss.hal.ui.resource.form.FilterReloadInput.filterReloadInput;
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
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MultiSelectMenu.multiSelectMenu;
import static org.patternfly.component.menu.MultiTypeahead.multiTypeahead;

/** Form item for LIST-of-STRING attributes with a capability reference, rendered as a multi-select typeahead. */
public class CapabilityReferencesFormItem extends AbstractFormItem {

    private final String capability;
    private /*final*/ MultiTypeahead typeahead;

    public CapabilityReferencesFormItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
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

    private MultiTypeahead typeaheadControl() {
        FilterReloadInput fri = filterReloadInput(identifier)
                .plain()
                .placeholder("")
                .onReload((e, c) -> typeahead.menu().reload());
        typeahead = multiTypeahead(fri)
                .applyToMenuToggle(FullWidth::fullWidth)
                .allowNewItems(value -> "Add \"" + value + "\"...", value -> newItem(value, capability))
                .addMenu(multiSelectMenu()
                        .addContent(menuContent()
                                .addList(menuList()
                                        .addItems(capabilityItems(context.template(), capability)))));

        if (attribute.value().isDefined()) {
            values(modelValues(attribute));
        } else if (attribute.description().hasDefault()) {
            typeahead.menuToggle().filterInput().placeholder(attribute.description().get(DEFAULT).asString());
        } else if (attribute.description().nillable()) {
            typeahead.menuToggle().filterInput().placeholder(UNDEFINED);
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
            if (requiredOnItsOwn() && values().isEmpty()) {
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
                typeahead.menuToggle().searchInput().placeholder(String.join(" ", dv));
                values(dv);
            } else if (attribute.description().nillable()) {
                typeahead.menuToggle().searchInput().placeholder(UNDEFINED);
            }
        }
    }

    // ------------------------------------------------------ internal

    private void values(List<String> values) {
        if (typeahead.menu().hasAsyncItems()) {
            typeahead.menu().load().then(__ -> {
                typeahead.selectIdentifiers(values);
                return null;
            });
            typeahead.onLoaded((__, mt) -> mt.selectIdentifiers(values));
        } else {
            typeahead.selectIdentifiers(values);
        }
    }

    private List<String> values() {
        return typeahead.menu().items().stream().map(MenuItem::text).collect(toList());
    }
}

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
import org.patternfly.component.button.Button;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.switch_.Switch;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.resources.HalClasses.expression;
import static org.jboss.hal.resources.HalClasses.form;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.ui.BuildingBlocks.expressionModeIcon;
import static org.jboss.hal.ui.resource.FormItemFlags.Scope.EXISTING_RESOURCE;
import static org.jboss.hal.ui.resource.FormItemFlags.Scope.NEW_RESOURCE;
import static org.jboss.hal.ui.resource.FormItemInputMode.EXPRESSION;
import static org.jboss.hal.ui.resource.FormItemInputMode.NATIVE;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.switch_.Switch.switch_;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.SpaceItems.none;
import static org.patternfly.style.Classes.switch_;

// TODO Implement sensitive
//  Example: /subsystem=jmx, attribute "non-core-mbean-sensitivity"
public class BooleanFormItem extends FormItem {

    // The select control is created by selectControl() called during defaultSetup().
    // It's, so to speak, final and never null!
    private /*final*/ Switch switchControl;

    BooleanFormItem(String identifier, ResourceAttribute ra, FormGroupLabel label, FormItemFlags flags) {
        super(identifier, ra, label, flags);
        defaultSetup();
    }

    FormGroupControl readOnlyGroup() {
        TextInput textControl = readOnlyTextControl();
        if (ra.expression) {
            return formGroupControl()
                    .addInputGroup(inputGroup()
                            .addItem(inputGroupItem().fill().addControl(textControl))
                            .addItem(inputGroupItem().addButton(resolveExpressionButton())));
        } else if (!ra.value.isDefined()) {
            return formGroupControl().addControl(textControl);
        } else {
            return formGroupControl().add(switchControl());
        }
    }

    FormGroupControl nativeGroup() {
        return formGroupControl().add(switchControl());
    }

    HTMLElement nativeContainer() {
        if (nativeContainer == null) {
            nativeContainer = flex().alignItems(center).spaceItems(none)
                    .css(halComponent(resource, form, expression, switch_))
                    .addItem(flexItem().add(switchToExpressionModeButton()))
                    .addItem(flexItem().add(switchControl()))
                    .element();
        }
        return nativeContainer;
    }

    @Override
    Button switchToExpressionModeButton() {
        return button().id(switchToExpressionModeId).plain().icon(expressionModeIcon().get())
                .onClick((e, b) -> switchToExpressionMode());
    }

    private Switch switchControl() {
        boolean booleanValue = false;
        if (ra.value.isDefined()) {
            booleanValue = ra.value.asBoolean(false);
        } else {
            if (ra.description.hasDefined(DEFAULT)) {
                booleanValue = ra.description.get(DEFAULT).asBoolean(false);
            }
        }
        switchControl = switch_(identifier, identifier, booleanValue)
                .checkIcon()
                .ariaLabel(ra.name)
                .readonly(ra.description.readOnly());
        return switchControl;
    }

    // ------------------------------------------------------ validation

    @Override
    boolean validate() {
        if (inputMode == FormItemInputMode.EXPRESSION) {
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
                    if (ra.description.hasDefault()) {
                        return ra.description.get(DEFAULT).asBoolean() != switchControl.value();
                    } else {
                        return ra.value.asBoolean(false) != switchControl.value();
                    }
                } else if (inputMode == EXPRESSION) {
                    return isExpressionModified();
                }
            } else if (flags.scope == EXISTING_RESOURCE) {
                boolean wasDefined = ra.value.isDefined();
                if (inputMode == NATIVE) {
                    if (wasDefined) {
                        // modified if the original value was an expression or is different from the current user input
                        return ra.expression || ra.value.asBoolean() != switchControl.value();
                    } else {
                        return true;
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
            return new ModelNode().set(switchControl.value());
        } else if (inputMode == EXPRESSION) {
            return expressionModelNode();
        }
        return new ModelNode();
    }
}

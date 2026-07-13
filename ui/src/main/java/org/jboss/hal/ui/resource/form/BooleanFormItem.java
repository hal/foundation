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
import org.patternfly.component.button.Button;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.switch_.Switch;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.resources.HalClasses.expression;
import static org.jboss.hal.resources.HalClasses.form;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.ui.brick.ExpressionBricks.expressionModeIcon;
import static org.jboss.hal.ui.resource.form.InputMode.EXPRESSION;
import static org.jboss.hal.ui.resource.form.InputMode.NATIVE;
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

/** Form item for editing boolean attributes, rendered as a toggle switch with optional expression mode. */
public class BooleanFormItem extends AbstractFormItem {

    private /*final*/ Switch switchControl;

    public BooleanFormItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        super(identifier, attribute, context);
        defaultSetup();
    }

    @Override
    FormGroupControl readOnlyGroup() {
        TextInput tc = readOnlyTextControl();
        if (attribute.expression()) {
            return formGroupControl()
                    .addInputGroup(inputGroup()
                            .addItem(inputGroupItem().fill().addControl(tc))
                            .addItem(inputGroupItem().addButton(resolveExpressionButton())));
        } else if (!attribute.value().isDefined()) {
            return formGroupControl().addControl(tc);
        } else {
            return formGroupControl().add(switchControl());
        }
    }

    @Override
    FormGroupControl nativeGroup() {
        return formGroupControl().add(switchControl());
    }

    @Override
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
        return button().id(switchToExpressionModeId).control().icon(expressionModeIcon().get())
                .onClick((e, b) -> switchToExpressionMode());
    }

    private Switch switchControl() {
        boolean booleanValue = false;
        if (attribute.value().isDefined()) {
            booleanValue = attribute.value().asBoolean(false);
        } else {
            if (attribute.description().hasDefined(DEFAULT)) {
                booleanValue = attribute.description().get(DEFAULT).asBoolean(false);
            }
        }
        switchControl = switch_(identifier, identifier, booleanValue)
                .checkIcon()
                .ariaLabel(attribute.name())
                .readonly(attribute.description().readOnly());
        return switchControl;
    }

    // ------------------------------------------------------ validation

    @Override
    public boolean validate() {
        if (inputMode == EXPRESSION) {
            return validateExpressionMode();
        }
        return true;
    }

    // ------------------------------------------------------ data

    @Override
    boolean isNativeModifiedForNew() {
        if (attribute.description().hasDefault()) {
            return attribute.description().get(DEFAULT).asBoolean() != switchControl.value();
        } else {
            return attribute.value().asBoolean(false) != switchControl.value();
        }
    }

    @Override
    boolean isNativeModifiedForExisting(boolean wasDefined) {
        if (wasDefined) {
            return attribute.expression() || attribute.value().asBoolean() != switchControl.value();
        } else {
            return true;
        }
    }

    @Override
    public ModelNode modelNode() {
        if (inputMode == NATIVE) {
            return new ModelNode().set(switchControl.value());
        } else if (inputMode == EXPRESSION) {
            return expressionModelNode();
        }
        return new ModelNode();
    }
}

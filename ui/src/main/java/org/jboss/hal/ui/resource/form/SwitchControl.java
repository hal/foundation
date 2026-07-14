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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.switch_.Switch;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.resources.HalClasses.expression;
import static org.jboss.hal.resources.HalClasses.form;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.patternfly.component.switch_.Switch.switch_;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.SpaceItems.none;
import static org.patternfly.style.Classes.switch_;

/**
 * {@link NativeControl} for boolean attributes, rendered as a toggle switch.
 */
public final class SwitchControl implements NativeControl<Switch> {

    @Override
    public Switch create(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        boolean value = false;
        if (attribute.value().isDefined()) {
            value = attribute.value().asBoolean(false);
        } else if (attribute.description().hasDefined(DEFAULT)) {
            value = attribute.description().get(DEFAULT).asBoolean(false);
        }
        return switch_(identifier, identifier, value)
                .checkIcon()
                .ariaLabel(attribute.name())
                .readonly(attribute.description().readOnly());
    }

    @Override
    public HTMLElement element(Switch control) {
        return control.element();
    }

    @Override
    public ModelNode modelNode(Switch control, ResolvedAttribute attribute) {
        return new ModelNode().set(control.value());
    }

    @Override
    public boolean isModifiedForNew(Switch control, ResolvedAttribute attribute) {
        if (attribute.description().hasDefault()) {
            return attribute.description().get(DEFAULT).asBoolean() != control.value();
        }
        return attribute.value().asBoolean(false) != control.value();
    }

    @Override
    public boolean isModifiedForExisting(Switch control, ResolvedAttribute attribute, boolean wasDefined) {
        if (wasDefined) {
            return attribute.expression() || attribute.value().asBoolean() != control.value();
        }
        return true;
    }

    @Override
    public HTMLElement nativeContainer(Switch control, ExpressionToggle toggle) {
        return flex().alignItems(center).spaceItems(none)
                .css(halComponent(resource, form, expression, switch_))
                .addItem(flexItem().add(toggle.switchToExpressionButton()))
                .addItem(flexItem().add(control))
                .element();
    }
}

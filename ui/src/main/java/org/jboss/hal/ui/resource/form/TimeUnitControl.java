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
import org.jboss.hal.ui.resource.pipeline.Pipeline;
import org.jboss.hal.ui.resource.PipelineContext;
import org.patternfly.component.form.FormGroupControl;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNIT;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.Gap.sm;
import static org.patternfly.style.Breakpoint.default_;

/**
 * {@link NativeControl} for time-unit composite attributes. Combines a number input (time) with a unit dropdown.
 */
public final class TimeUnitControl implements NativeControl<HTMLElement> {

    private EditableControl<?> timeControl;
    private EditableControl<?> unitControl;

    @Override
    public HTMLElement create(PipelineContext context, String identifier, ResolvedAttribute attribute) {
        timeControl = Pipeline.instance().formItem(context, attribute.child(TIME)).editableControl();
        unitControl = Pipeline.instance().formItem(context, attribute.child(UNIT)).editableControl();
        return flex().gap(sm)
                .addItem(flexItem().grow(default_).add(timeControl))
                .addItem(flexItem().add(unitControl))
                .element();
    }

    @Override
    public HTMLElement element(HTMLElement control) {
        return control;
    }

    @Override
    public ModelNode modelNode(HTMLElement control, ResolvedAttribute attribute) {
        ModelNode timeNode = timeControl.modelNode();
        ModelNode unitNode = unitControl.modelNode();

        if (timeNode.isDefined() && unitNode.isDefined()) {
            ModelNode result = new ModelNode();
            result.get(TIME).set(timeNode);
            result.get(UNIT).set(unitNode);
            return result;
        } else {
            return new ModelNode();
        }
    }

    @Override
    public boolean isModifiedForNew(HTMLElement control, ResolvedAttribute attribute) {
        return timeControl.isModified() || unitControl.isModified();
    }

    @Override
    public boolean isModifiedForExisting(HTMLElement control, ResolvedAttribute attribute, boolean wasDefined) {
        return timeControl.isModified() || unitControl.isModified();
    }

    @Override
    public boolean validate(HTMLElement control, ResolvedAttribute attribute, FormGroupControl formGroupControl) {
        timeControl.setValidationTarget(formGroupControl);
        unitControl.setValidationTarget(formGroupControl);
        return timeControl.validate() && unitControl.validate();
    }

    @Override
    public void resetValidation(HTMLElement control) {
        timeControl.resetValidation();
        unitControl.resetValidation();
    }
}

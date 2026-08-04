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

import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;
import static org.jboss.hal.ui.brick.AttributeBricks.pathRelativeTo;

/**
 * {@link NativeControl} for file composite attributes ({@code {path, relative-to}}). Two text inputs side-by-side in an input
 * group.
 */
public final class FileControl implements NativeControl<HTMLElement> {

    private EditableControl<?> pathControl;
    private EditableControl<?> relativeToControl;

    @Override
    public HTMLElement create(PipelineContext context, String identifier, ResolvedAttribute attribute) {
        pathControl = Pipeline.instance().formItem(context, attribute.child(PATH)).editableControl();
        relativeToControl = Pipeline.instance().formItem(context, attribute.child(RELATIVE_TO)).editableControl();

        return pathRelativeTo(pathControl.element(), relativeToControl.element(), true).element();
    }

    @Override
    public HTMLElement element(HTMLElement control) {
        return control;
    }

    @Override
    public ModelNode modelNode(HTMLElement control, ResolvedAttribute attribute) {
        ModelNode pathNode = pathControl.modelNode();
        ModelNode relativeToNode = relativeToControl.modelNode();

        if (!pathNode.isDefined() && !relativeToNode.isDefined()) {
            return new ModelNode();
        } else {
            ModelNode result = new ModelNode();
            if (pathNode.isDefined()) {
                result.get(PATH).set(pathNode);
            }
            if (relativeToNode.isDefined()) {
                result.get(RELATIVE_TO).set(relativeToNode);
            }
            return result;
        }
    }

    @Override
    public boolean isModifiedForNew(HTMLElement control, ResolvedAttribute attribute) {
        return pathControl.isModified() || relativeToControl.isModified();
    }

    @Override
    public boolean isModifiedForExisting(HTMLElement control, ResolvedAttribute attribute, boolean wasDefined) {
        return pathControl.isModified() || relativeToControl.isModified();
    }

    @Override
    public boolean validate(HTMLElement control, ResolvedAttribute attribute, FormGroupControl formGroupControl) {
        pathControl.setValidationTarget(formGroupControl);
        relativeToControl.setValidationTarget(formGroupControl);
        return pathControl.validate() && relativeToControl.validate();
    }

    @Override
    public void resetValidation(HTMLElement control) {
        pathControl.resetValidation();
        relativeToControl.resetValidation();
    }
}

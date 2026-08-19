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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.elemento.Id;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.Pipeline;
import org.jboss.hal.ui.resource.PipelineContext;
import org.patternfly.component.form.FormGroupControl;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.ui.brick.AttributeBricks.pathRelativeTo;
import static org.jboss.hal.ui.resource.form.FormItemBricks.compositeLabel;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;

/**
 * Composite form item for a sibling path and relative-to attribute group. Uses the pipeline to create child {@link FormItem}s,
 * then extracts their {@link EditableControl}s for composition in a single {@link org.patternfly.component.form.FormGroup}.
 * <p>
 * All behavioral concerns (expression support, validation, modification tracking, operation generation) are delegated to the
 * child {@link EditableControl}s. The composite only provides visual assembly (flex layout, composite label) and coordinates
 * the children's results.
 *
 * @see EditableControl
 * @see FormItemBricks#compositeLabel(PipelineContext, String, AttributeDescription, AttributeDescription)
 */
public class PathRelativeToFormItem implements FormItem {

    private final String identifier;
    private final ResolvedAttribute primaryAttribute;
    private final EditableControl<?> pathControl;
    private final EditableControl<?> relativeToControl;
    private final HTMLElement root;

    public PathRelativeToFormItem(PipelineContext context, String identifier,
            ResolvedAttribute pathAttribute, ResolvedAttribute relativeToAttribute) {
        this.identifier = Id.build(pathAttribute.fqn(), relativeToAttribute.fqn());
        this.primaryAttribute = pathAttribute;
        this.pathControl = Pipeline.instance().formItem(context, pathAttribute).editableControl();
        this.relativeToControl = Pipeline.instance().formItem(context, relativeToAttribute).editableControl();

        FormGroupControl formGroupControl = formGroupControl();
        pathControl.setValidationTarget(formGroupControl);
        relativeToControl.setValidationTarget(formGroupControl);
        formGroupControl.add(pathRelativeTo(pathControl.element(), relativeToControl.element(), true));

        this.root = formGroup(identifier)
                .addLabel(compositeLabel(context, identifier, pathAttribute.description(), relativeToAttribute.description()))
                .addControl(formGroupControl)
                .element();
    }

    // ------------------------------------------------------ FormItem

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public ResolvedAttribute attribute() {
        return primaryAttribute;
    }

    @Override
    public void contributeToPayload(ModelNode payload) {
        ModelNode pathNode = pathControl.modelNode();
        ModelNode relativeToNode = relativeToControl.modelNode();
        if (pathNode.isDefined()) {
            payload.get(pathControl.attribute().fqn()).set(pathNode);
        }
        if (relativeToNode.isDefined()) {
            payload.get(relativeToControl.attribute().fqn()).set(relativeToNode);
        }
    }

    @Override
    public boolean isModified() {
        return pathControl.isModified() || relativeToControl.isModified();
    }

    @Override
    public boolean validate() {
        return pathControl.validate() && relativeToControl.validate();
    }

    @Override
    public void resetValidation() {
        pathControl.resetValidation();
        relativeToControl.resetValidation();
    }

    @Override
    public void disable() {
        pathControl.disable();
        relativeToControl.disable();
    }

    @Override
    public List<Operation> operations(ResourceAddress address) {
        List<Operation> ops = new ArrayList<>();
        if (pathControl.isModified()) {
            ops.add(OperationStrategy.writeOrUndefine(address, pathControl.attribute().fqn(),
                    pathControl.modelNode()));
        }
        if (relativeToControl.isModified()) {
            ops.add(OperationStrategy.writeOrUndefine(address, relativeToControl.attribute().fqn(),
                    relativeToControl.modelNode()));
        }
        return ops.isEmpty() ? Collections.emptyList() : ops;
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}

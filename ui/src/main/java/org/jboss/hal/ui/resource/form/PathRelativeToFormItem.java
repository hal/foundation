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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.Pipeline;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.patternfly.component.form.FormGroupControl;

import elemental2.dom.HTMLElement;

import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.Gap.sm;
import static org.patternfly.token.Token.globalTextColorPlaceholder;

/**
 * Composite form item for sibling path + relative-to attribute groups. Uses the pipeline to create child {@link FormItem}s,
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
    private final ResolvedAttribute pathAttr;
    private final EditableControl<?> pathControl;
    private final EditableControl<?> relativeToControl;
    private final HTMLElement root;

    public PathRelativeToFormItem(PipelineContext context, String identifier,
            ResolvedAttribute pathAttr, ResolvedAttribute relativeToAttr) {
        this.identifier = identifier;
        this.pathAttr = pathAttr;

        this.pathControl = Pipeline.instance().formItem(context, pathAttr).editableControl();
        this.relativeToControl = Pipeline.instance().formItem(context, relativeToAttr).editableControl();

        FormGroupControl formGroupControl = formGroupControl();
        pathControl.setValidationTarget(formGroupControl);
        relativeToControl.setValidationTarget(formGroupControl);

        formGroupControl.add(flex().alignItems(center).gap(sm)
                .addItem(flexItem().style("flex-grow", "1").add(pathControl.controlElement()))
                .addItem(flexItem().style("color", globalTextColorPlaceholder.var).text("relative to"))
                .addItem(flexItem().style("flex-grow", "1").add(relativeToControl.controlElement())));

        this.root = formGroup(identifier)
                .addLabel(FormItemBricks.compositeLabel(context, identifier,
                        pathAttr.description(), relativeToAttr.description()))
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
        return pathAttr;
    }

    @Override
    public ModelNode modelNode() {
        ModelNode result = new ModelNode();
        ModelNode pathNode = pathControl.modelNode();
        ModelNode relativeToNode = relativeToControl.modelNode();
        if (pathNode.isDefined()) {
            result.get(pathControl.attribute().name()).set(pathNode);
        }
        if (relativeToNode.isDefined()) {
            result.get(relativeToControl.attribute().name()).set(relativeToNode);
        }
        return result;
    }

    @Override
    public boolean isModified() {
        return pathControl.isModified() || relativeToControl.isModified();
    }

    @Override
    public boolean validate() {
        return pathControl.validate() & relativeToControl.validate();
    }

    @Override
    public void resetValidation() {
        pathControl.resetValidation();
        relativeToControl.resetValidation();
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

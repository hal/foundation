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
import org.jboss.hal.ui.resource.PipelineContext;

import elemental2.dom.HTMLElement;

import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.inputgroup.InputGroupText.inputGroupText;
import static org.patternfly.icon.IconSets.fas.lock;

/**
 * {@link NativeControl} if the attribute is not writable or the storage is memory.
 */
public final class ReadOnlyControl implements NativeControl<HTMLElement> {

    @Override
    public HTMLElement create(PipelineContext context, String identifier, ResolvedAttribute attribute) {
        return inputGroup()
                .addItem(inputGroupItem().fill()
                        .addControl(textInput(identifier)
                                .disabled()))
                .addText(inputGroupText().icon(lock()).plain())
                .element();
    }

    @Override
    public HTMLElement element(HTMLElement control) {
        return control;
    }

    @Override
    public void disable(HTMLElement control) {
        // noop
    }

    @Override
    public ModelNode modelNode(HTMLElement control, ResolvedAttribute attribute) {
        return new ModelNode();
    }

    @Override
    public boolean isModifiedForNew(HTMLElement control, ResolvedAttribute attribute) {
        return false;
    }

    @Override
    public boolean isModifiedForExisting(HTMLElement control, ResolvedAttribute attribute, boolean wasDefined) {
        return false;
    }
}

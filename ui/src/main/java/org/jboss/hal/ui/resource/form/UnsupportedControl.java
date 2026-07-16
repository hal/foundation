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
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.patternfly.component.help.HelperText;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.patternfly.component.ValidationStatus.warning;
import static org.patternfly.component.form.TextArea.textArea;
import static org.patternfly.component.form.TextInput.textInput;

/**
 * {@link NativeControl} for attribute types that are not yet supported (OBJECT, complex LIST, etc.). Displays the value
 * read-only with a warning.
 */
public final class UnsupportedControl implements NativeControl<HTMLElement> {

    private String formatType;

    @Override
    public HTMLElement create(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        formatType = attribute.description().formatType();
        ModelType type = attribute.description().get(TYPE).asType();
        if (type == ModelType.LIST || type == ModelType.OBJECT) {
            String value = attribute.value().toJSONString().replace("\\/", "/");
            return textArea(identifier)
                    .readonly()
                    .validated(warning)
                    .run(ta -> {
                        if (attribute.value().isDefined()) {
                            ta.value(value);
                        } else {
                            ta.placeholder("undefined");
                        }
                    })
                    .element();
        } else {
            return textInput(identifier)
                    .run(ti -> {
                        ti.input().autocomplete("off");
                        if (attribute.value().isDefined()) {
                            ti.value(attribute.value().asString());
                        }
                        FormItemBricks.applyPlaceholder(ti.input(), attribute, context.flags());
                    })
                    .readonly()
                    .validated(warning)
                    .element();
        }
    }

    @Override
    public HTMLElement element(HTMLElement control) {
        return control;
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

    @Override
    public HelperText helperText() {
        return HelperText.helperText("The type of this attribute is not yet supported: " + formatType, warning);
    }
}

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
import org.jboss.hal.ui.resource.ResolvedAttribute;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.patternfly.component.form.FormControl;

import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.patternfly.component.ValidationStatus.warning;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.TextArea.textArea;
import static org.patternfly.component.help.HelperText.helperText;

/** Read-only fallback form item displayed for attribute types that are not yet supported (OBJECT, complex LIST, etc.). */
public class UnsupportedFormItem extends AbstractFormItem {

    public UnsupportedFormItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        super(identifier, attribute, context);

        FormControl<?, ?> control;
        ModelType type = attribute.description().get(TYPE).asType();
        if (type == ModelType.LIST || type == ModelType.OBJECT) {
            String value = attribute.value().toJSONString().replace("\\/", "/");
            control = textArea(identifier)
                    .readonly()
                    .validated(warning)
                    .run(ta -> {
                        if (attribute.value().isDefined()) {
                            ta.value(value);
                        } else {
                            ta.placeholder("undefined");
                        }
                    });
        } else {
            control = textControl()
                    .readonly()
                    .validated(warning);
        }
        formGroup = formGroup(identifier)
                .required(attribute.description().required())
                .addLabel(label())
                .addControl(formGroupControl()
                        .addControl(control)
                        .addHelperText(helperText(
                                "The type of this attribute is not yet supported: " + attribute.description().formatType(),
                                warning)));
    }

    @Override
    boolean isNativeModifiedForNew() {
        return false;
    }

    @Override
    boolean isNativeModifiedForExisting(boolean wasDefined) {
        return false;
    }

    @Override
    public ModelNode modelNode() {
        return new ModelNode();
    }
}

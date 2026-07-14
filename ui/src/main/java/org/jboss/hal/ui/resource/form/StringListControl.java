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

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.label.Label;
import org.patternfly.component.textinputgroup.FilterInput;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.ui.resource.form.StringListSupport.defaultValues;
import static org.jboss.hal.ui.resource.form.StringListSupport.isExistingModified;
import static org.jboss.hal.ui.resource.form.StringListSupport.isNewModified;
import static org.jboss.hal.ui.resource.form.StringListSupport.modelValues;
import static org.jboss.hal.ui.resource.form.StringListSupport.valuesModelNode;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.textinputgroup.FilterInput.filterInput;

/**
 * {@link NativeControl} for LIST-of-STRING attributes, rendered as a label-based multi-value input.
 */
public final class StringListControl implements NativeControl<FilterInput> {

    @Override
    public FilterInput create(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        FilterInput fi = filterInput(identifier)
                .applyTo(inputElement -> inputElement.autocomplete("off"))
                .allowDuplicates(false);
        if (attribute.value().isDefined()) {
            setValues(fi, modelValues(attribute));
        } else if (attribute.description().hasDefault()) {
            fi.placeholder(attribute.description().get(DEFAULT).asString());
        } else if (attribute.description().nillable()) {
            fi.placeholder(UNDEFINED);
        }
        return fi;
    }

    @Override
    public HTMLElement element(FilterInput control) {
        return control.element();
    }

    @Override
    public ModelNode modelNode(FilterInput control, ResolvedAttribute attribute) {
        return valuesModelNode(getValues(control));
    }

    @Override
    public boolean isModifiedForNew(FilterInput control, ResolvedAttribute attribute) {
        return isNewModified(attribute, getValues(control));
    }

    @Override
    public boolean isModifiedForExisting(FilterInput control, ResolvedAttribute attribute, boolean wasDefined) {
        return isExistingModified(attribute, getValues(control), wasDefined);
    }

    @Override
    public boolean validate(FilterInput control, ResolvedAttribute attribute, FormGroupControl formGroupControl) {
        if (FormItemBricks.requiredOnItsOwn(attribute) && getValues(control).isEmpty()) {
            control.validated(error);
            formGroupControl.addHelperText(FormItemBricks.requiredHelperText(attribute));
            return false;
        }
        return true;
    }

    @Override
    public void resetValidation(FilterInput control) {
        control.resetValidation();
    }

    @Override
    public void afterSwitchedToNativeMode(FilterInput control, ResolvedAttribute attribute) {
        if (attribute.value().isDefined() && !attribute.expression()) {
            setValues(control, modelValues(attribute));
        } else {
            if (attribute.description().hasDefault()) {
                List<String> dv = defaultValues(attribute);
                control.placeholder(String.join(" ", dv));
                setValues(control, dv);
            } else if (attribute.description().nillable()) {
                control.placeholder(UNDEFINED);
            }
        }
    }

    private void setValues(FilterInput fi, List<String> values) {
        fi.labelGroup().clear();
        fi.labelGroup().addItems(values, value -> fi.textToLabel().apply(value));
    }

    private List<String> getValues(FilterInput fi) {
        return fi.labelGroup().items().stream().map(Label::text).collect(toList());
    }
}

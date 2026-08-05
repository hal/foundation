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
package org.jboss.hal.ui.resource.pipeline;

import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.ui.resource.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.form.CapabilitiesReferenceControl;
import org.jboss.hal.ui.resource.form.CapabilityReferenceControl;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.form.NumberInputControl;
import org.jboss.hal.ui.resource.form.ReadOnlyControl;
import org.jboss.hal.ui.resource.form.RestrictedControl;
import org.jboss.hal.ui.resource.form.SelectControl;
import org.jboss.hal.ui.resource.form.DefaultFormItem;
import org.jboss.hal.ui.resource.form.StringControl;
import org.jboss.hal.ui.resource.form.StringListControl;
import org.jboss.hal.ui.resource.form.SwitchControl;
import org.jboss.hal.ui.resource.form.UnsupportedControl;
import org.jboss.hal.ui.resource.view.DefaultViewItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITY_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;

/**
 * Catch-all provider that handles all attributes with type-based rendering. Must be registered last in the provider chain.
 */
public class DefaultProvider implements ItemProvider {

    @Override
    public boolean handles(ResolvedAttribute ra) {
        return true;
    }

    @Override
    public ViewItem viewItem(PipelineContext context, ResolvedAttribute ra) {
        return new DefaultViewItem(context, ra.fqn(), ra);
    }

    @Override
    public FormItem formItem(PipelineContext context, ResolvedAttribute ra) {
        String identifier = ra.fqn();
        if (!ra.readable()) {
            return new DefaultFormItem<>(context, identifier, ra, new RestrictedControl());
        }
        if (ra.readOnly() || !ra.writable()) {
            return new DefaultFormItem<>(context, identifier, ra, new ReadOnlyControl());
        }
        if (!ra.description().hasDefined(TYPE)) {
            return new DefaultFormItem<>(context, identifier, ra, new UnsupportedControl());
        }
        ModelType type = ra.description().get(TYPE).asType();
        switch (type) {
            case BOOLEAN:
                return new DefaultFormItem<>(context, identifier, ra, new SwitchControl());

            case INT:
            case LONG:
            case DOUBLE:
                return new DefaultFormItem<>(context, identifier, ra, new NumberInputControl());

            case STRING:
                if (ra.description().hasDefined(ALLOWED)) {
                    return new DefaultFormItem<>(context, identifier, ra, new SelectControl());
                } else if (ra.description().hasDefined(CAPABILITY_REFERENCE)) {
                    return new DefaultFormItem<>(context, identifier, ra, new CapabilityReferenceControl());
                } else {
                    return new DefaultFormItem<>(context, identifier, ra, new StringControl());
                }

            case LIST:
                ModelType valueType = ra.description().has(VALUE_TYPE) &&
                        ra.description().get(VALUE_TYPE).getType() != ModelType.OBJECT
                        ? ra.description().get(VALUE_TYPE).asType()
                        : null;
                if (valueType == ModelType.STRING) {
                    if (ra.description().hasDefined(CAPABILITY_REFERENCE)) {
                        return new DefaultFormItem<>(context, identifier, ra, new CapabilitiesReferenceControl());
                    } else {
                        return new DefaultFormItem<>(context, identifier, ra, new StringListControl());
                    }
                }
                return new DefaultFormItem<>(context, identifier, ra, new UnsupportedControl());

            case OBJECT:
            default:
                return new DefaultFormItem<>(context, identifier, ra, new UnsupportedControl());
        }
    }
}

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

import java.util.List;

import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.form.CapabilitiesReferenceControl;
import org.jboss.hal.ui.resource.form.NumberInputControl;
import org.jboss.hal.ui.resource.form.RestrictedControl;
import org.jboss.hal.ui.resource.form.SelectControl;
import org.jboss.hal.ui.resource.form.StandardFormItem;
import org.jboss.hal.ui.resource.form.StringControl;
import org.jboss.hal.ui.resource.form.StringListControl;
import org.jboss.hal.ui.resource.form.SwitchControl;
import org.jboss.hal.ui.resource.form.CapabilityReferenceControl;
import org.jboss.hal.ui.resource.form.UnsupportedControl;
import org.jboss.hal.ui.resource.view.DefaultViewItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import static java.util.Collections.singletonList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITY_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;

/**
 * Catch-all provider that handles all unmatched single-attribute groups with type-based rendering. Must be registered last in
 * the provider chain, after {@link FlatteningProvider} (which handles simpleRecord OBJECTs).
 */
class DefaultItemProvider implements ItemProvider {

    @Override
    public boolean matches(AttributeMatch match) {
        return true;
    }

    @Override
    public List<ViewItem> viewItems(PipelineContext context, AttributeMatch match) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(context, match.primary());
        return singletonList(new DefaultViewItem(context, ra.fqn(), ra));
    }

    @Override
    public List<FormItem> formItems(PipelineContext context, AttributeMatch match) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(context, match.primary());
        return singletonList(formItem(ra, context));
    }

    static FormItem formItem(ResolvedAttribute ra, PipelineContext context) {
        String identifier = ra.fqn();
        if (!ra.readable()) {
            return new StandardFormItem<>(context, identifier, ra, new RestrictedControl());
        }
        if (!ra.description().hasDefined(TYPE)) {
            return new StandardFormItem<>(context, identifier, ra, new UnsupportedControl());
        }
        ModelType type = ra.description().get(TYPE).asType();
        switch (type) {
            case BOOLEAN:
                return new StandardFormItem<>(context, identifier, ra, new SwitchControl());

            case INT:
            case LONG:
            case DOUBLE:
                return new StandardFormItem<>(context, identifier, ra, new NumberInputControl());

            case STRING:
                if (ra.description().hasDefined(ALLOWED)) {
                    return new StandardFormItem<>(context, identifier, ra, new SelectControl());
                } else if (ra.description().hasDefined(CAPABILITY_REFERENCE)) {
                    return new StandardFormItem<>(context, identifier, ra, new CapabilityReferenceControl());
                } else {
                    return new StandardFormItem<>(context, identifier, ra, new StringControl());
                }

            case LIST:
                ModelType valueType = ra.description().has(VALUE_TYPE) &&
                        ra.description().get(VALUE_TYPE).getType() != ModelType.OBJECT
                        ? ra.description().get(VALUE_TYPE).asType()
                        : null;
                if (valueType == ModelType.STRING) {
                    if (ra.description().hasDefined(CAPABILITY_REFERENCE)) {
                        return new StandardFormItem<>(context, identifier, ra, new CapabilitiesReferenceControl());
                    } else {
                        return new StandardFormItem<>(context, identifier, ra, new StringListControl());
                    }
                }
                return new StandardFormItem<>(context, identifier, ra, new UnsupportedControl());

            case OBJECT:
            default:
                return new StandardFormItem<>(context, identifier, ra, new UnsupportedControl());
        }
    }
}

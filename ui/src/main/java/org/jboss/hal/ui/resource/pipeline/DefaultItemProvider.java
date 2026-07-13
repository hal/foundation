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

import org.jboss.hal.ui.resource.form.BooleanFormItem;
import org.jboss.hal.ui.resource.form.CapabilityReferenceFormItem;
import org.jboss.hal.ui.resource.form.CapabilityReferencesFormItem;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.form.NumberFormItem;
import org.jboss.hal.ui.resource.form.RestrictedFormItem;
import org.jboss.hal.ui.resource.form.SelectFormItem;
import org.jboss.hal.ui.resource.form.StringFormItem;
import org.jboss.hal.ui.resource.form.StringListFormItem;
import org.jboss.hal.ui.resource.form.UnsupportedFormItem;
import org.jboss.hal.ui.resource.view.DefaultViewItem;
import org.jboss.hal.ui.resource.view.ViewItem;

import java.util.List;

import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelType;

import static java.util.Collections.singletonList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITY_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;

/**
 * Catch-all provider that handles all unmatched single-attribute groups with type-based rendering. Must be registered last in the
 * provider chain, after {@link FlatteningProvider} (which handles simpleRecord OBJECTs).
 */
class DefaultItemProvider implements ItemProvider {

    private static final Logger logger = Logger.getLogger(DefaultItemProvider.class.getName());

    @Override
    public boolean matches(AttributeGroup group) {
        return true;
    }

    @Override
    public List<ViewItem> viewItems(AttributeGroup group, PipelineContext context) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(group.primary(), context);
        return singletonList(new DefaultViewItem(ra.fqn(), ra, context));
    }

    @Override
    public List<FormItem> formItems(AttributeGroup group, PipelineContext context) {
        ResolvedAttribute ra = ResolvedAttribute.resolve(group.primary(), context);
        return singletonList(formItem(ra, context));
    }

    static FormItem formItem(ResolvedAttribute ra, PipelineContext context) {
        String identifier = ra.fqn();
        if (!ra.readable()) {
            return new RestrictedFormItem(identifier, ra, context);
        }
        if (!ra.description().hasDefined(TYPE)) {
            return new UnsupportedFormItem(identifier, ra, context);
        }
        ModelType type = ra.description().get(TYPE).asType();
        switch (type) {
            case BOOLEAN:
                return new BooleanFormItem(identifier, ra, context);

            case INT:
            case LONG:
            case DOUBLE:
                return new NumberFormItem(identifier, ra, context);

            case STRING:
                if (ra.description().hasDefined(ALLOWED)) {
                    return new SelectFormItem(identifier, ra, context);
                } else if (ra.description().hasDefined(CAPABILITY_REFERENCE)) {
                    return new CapabilityReferenceFormItem(identifier, ra, context);
                } else {
                    return new StringFormItem(identifier, ra, context);
                }

            case LIST:
                ModelType valueType = ra.description().has(VALUE_TYPE) &&
                        ra.description().get(VALUE_TYPE).getType() != ModelType.OBJECT
                        ? ra.description().get(VALUE_TYPE).asType()
                        : null;
                if (valueType == ModelType.STRING) {
                    if (ra.description().hasDefined(CAPABILITY_REFERENCE)) {
                        return new CapabilityReferencesFormItem(identifier, ra, context);
                    } else {
                        return new StringListFormItem(identifier, ra, context);
                    }
                }
                return new UnsupportedFormItem(identifier, ra, context);

            case OBJECT:
            default:
                return new UnsupportedFormItem(identifier, ra, context);
        }
    }
}

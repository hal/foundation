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
package org.jboss.hal.ui.resource.view;

import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.pipeline.ResolvedAttribute;

import java.util.List;

import org.jboss.elemento.Id;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.resources.HalClasses;
import org.patternfly.component.label.Label;

import elemental2.dom.HTMLElement;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITY_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;
import static org.jboss.hal.ui.resource.view.CapabilityReference.capabilityReference;
import static org.jboss.hal.dmr.ModelType.BOOLEAN;
import static org.jboss.hal.dmr.ModelType.LIST;
import static org.jboss.hal.dmr.ModelType.OBJECT;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.resources.HalClasses.view;
import static org.jboss.hal.ui.brick.CodeBricks.modelNodeCode;
import static org.patternfly.component.label.LabelGroup.labelGroup;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.component.switch_.Switch.switch_;
import static org.patternfly.style.Color.grey;

/**
 * Default view item for single attributes. Handles all standard types: BOOLEAN (switch), simple types (plain text, unit,
 * allowed values), LIST (inline list or JSON), and OBJECT (JSON). Used by {@link DefaultItemProvider} and
 * {@link FlatteningProvider}.
 */
public class DefaultViewItem extends AbstractViewItem {

    private static final Logger logger = Logger.getLogger(DefaultViewItem.class.getName());

    public DefaultViewItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        super(identifier, attribute, context);
    }

    @Override
    protected HTMLElement definedValue() {
        if (!attribute.description().hasDefined(TYPE)) {
            logger.warn("No type information for attribute %s. Fallback to plain text.", attribute.fqn());
            return plainText(attribute);
        }
        ModelType type = attribute.description().get(TYPE).asType();
        if (type == BOOLEAN) {
            return booleanValue();
        } else if (type.simple()) {
            return simpleValue();
        } else if (type == LIST) {
            return listValue();
        } else if (type == OBJECT) {
            return modelNodeCode(attribute.value()).element();
        } else {
            return plainText(attribute);
        }
    }

    private HTMLElement booleanValue() {
        String unique = Id.unique(attribute.name());
        return switch_(unique, unique)
                .value(attribute.value().asBoolean())
                .ariaLabel(attribute.name())
                .checkIcon()
                .readonly()
                .element();
    }

    private HTMLElement simpleValue() {
        String unit = attribute.description().unit();
        if (unit != null) {
            return span()
                    .add(span().text(attribute.value().asString()))
                    .add(span().css(halComponent(resource, view, HalClasses.unit)).text(unit))
                    .element();
        } else if (attribute.description().hasDefined(ALLOWED)) {
            List<String> allowed = attribute.description().get(ALLOWED)
                    .asList()
                    .stream()
                    .map(ModelNode::asString)
                    .sorted(naturalOrder())
                    .collect(toList());
            allowed.remove(attribute.value().asString());
            return labelGroup()
                    .numLabels(1)
                    .collapsedText("Allowed values")
                    .addItem(Label.label("", grey).text(attribute.value().asString()))
                    .addItems(allowed, a -> Label.label(a, grey).disabled())
                    .element();
        } else if (attribute.description().hasDefined(CAPABILITY_REFERENCE)) {
            String capability = attribute.description().get(CAPABILITY_REFERENCE).asString();
            return capabilityReference(context.template(), capability, attribute.value().asString()).element();
        } else {
            return plainText(attribute);
        }
    }

    private HTMLElement listValue() {
        ModelType valueType = attribute.description().has(VALUE_TYPE) &&
                attribute.description().get(VALUE_TYPE).getType() != OBJECT
                ? attribute.description().get(VALUE_TYPE).asType()
                : null;
        if (valueType != null && valueType.simple()) {
            return list().plain().inline()
                    .addItems(attribute.value().asList().stream().map(ModelNode::asString).collect(toList()),
                            v -> listItem(Id.build(v, "value")).text(v))
                    .element();
        } else {
            return modelNodeCode(attribute.value()).element();
        }
    }
}

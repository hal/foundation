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

import java.util.List;

import org.jboss.elemento.Id;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.patternfly.component.label.Label;

import elemental2.dom.HTMLElement;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITY_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;
import static org.jboss.hal.dmr.ModelType.BOOLEAN;
import static org.jboss.hal.dmr.ModelType.LIST;
import static org.jboss.hal.dmr.ModelType.OBJECT;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.resources.HalClasses.view;
import static org.jboss.hal.ui.brick.CodeBricks.modelNodeCode;
import static org.jboss.hal.ui.resource.view.CapabilityReference.capabilityReference;
import static org.jboss.hal.ui.resource.view.ViewItemDefaults.NUM_LABELS;
import static org.patternfly.component.label.LabelGroup.labelGroup;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.switch_.Switch.switch_;
import static org.patternfly.style.Color.blue;
import static org.patternfly.style.Color.grey;

/**
 * Default view item for single attributes. Handles all standard types: BOOLEAN (switch), simple types (plain text, unit,
 * allowed values), LIST (inline list or JSON), and OBJECT (JSON). Used by
 * {@code org.jboss.hal.ui.resource.pipeline.DefaultItemProvider} and
 * {@code org.jboss.hal.ui.resource.pipeline.FlatteningProvider}.
 */
public class DefaultViewItem extends AbstractViewItem {

    private final HTMLElement valueElement;
    private final HTMLElement root;

    public DefaultViewItem(PipelineContext context, String identifier, ResolvedAttribute attribute) {
        super(identifier, attribute);
        this.valueElement = ViewItemBricks.valueElement(context, attribute, this::definedValue);
        this.root = descriptionListGroup(identifier)
                .addTerm(ViewItemBricks.label(context, attribute.description()))
                .addDescription(descriptionListDescription().add(valueElement))
                .element();
    }

    private HTMLElement definedValue(PipelineContext context, ResolvedAttribute attribute) {
        if (!attribute.description().hasDefined(TYPE)) {
            return ViewItemBricks.plainText(attribute);
        }
        ModelType type = attribute.description().get(TYPE).asType();
        if (type == BOOLEAN) {
            return booleanValue(attribute);
        } else if (type.simple()) {
            return simpleValue(attribute, context.template());
        } else if (type == LIST) {
            return listValue(attribute);
        } else if (type == OBJECT) {
            return modelNodeCode(attribute.value()).element();
        } else {
            return ViewItemBricks.plainText(attribute);
        }
    }

    private HTMLElement booleanValue(ResolvedAttribute attribute) {
        String unique = Id.unique(attribute.name());
        return switch_(unique, unique)
                .value(attribute.value().asBoolean())
                .ariaLabel(attribute.name())
                .checkIcon()
                .readonly()
                .element();
    }

    private HTMLElement simpleValue(ResolvedAttribute attribute, AddressTemplate template) {
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
            return capabilityReference(template, capability, attribute.value().asString()).element();
        } else {
            return ViewItemBricks.plainText(attribute);
        }
    }

    private HTMLElement listValue(ResolvedAttribute attribute) {
        ModelType valueType = attribute.description().has(VALUE_TYPE) &&
                attribute.description().get(VALUE_TYPE).getType() != OBJECT
                ? attribute.description().get(VALUE_TYPE).asType()
                : null;
        if (valueType != null && valueType.simple()) {
            return labelGroup()
                    .numLabels(NUM_LABELS)
                    .addItems(attribute.value().asList().stream().map(ModelNode::asString).collect(toList()),
                            v -> Label.label(Id.build(v), v, blue))
                    .element();
        } else {
            return modelNodeCode(attribute.value()).element();
        }
    }

    // ------------------------------------------------------ api

    @Override
    public HTMLElement valueElement() {
        return valueElement;
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}

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
package org.jboss.hal.ui.resource;

import org.jboss.elemento.Elements;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.core.LabelBuilder;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.resources.Keys;
import org.jboss.hal.ui.resource.FormItemFlags.Placeholder;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.core.Aria;
import org.patternfly.core.Roles;
import org.patternfly.style.Classes;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.insertFirst;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ACCESS_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITY_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPRESSIONS_ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_WRITE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.resources.HalClasses.deprecated;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.resources.HalClasses.stabilityLevel;
import static org.jboss.hal.ui.BuildingBlocks.AttributeDescriptionContent.all;
import static org.jboss.hal.ui.BuildingBlocks.attributeDescriptionPopover;
import static org.jboss.hal.ui.BuildingBlocks.nestedElementSeparator;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.resource.FormItemProviders.specialFormItems;
import static org.jboss.hal.ui.resource.ItemIdentifier.identifier;
import static org.jboss.hal.ui.resource.ResourceManager.State.EDIT;
import static org.patternfly.component.form.FormGroupLabel.formGroupLabel;
import static org.patternfly.core.Attributes.role;
import static org.patternfly.core.Attributes.tabindex;
import static org.patternfly.core.Attributes.type;
import static org.patternfly.icon.IconSets.fas.questionCircle;
import static org.patternfly.style.Classes.button;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.form;
import static org.patternfly.style.Classes.group;
import static org.patternfly.style.Classes.help;
import static org.patternfly.style.Classes.icon;
import static org.patternfly.style.Classes.label;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.noPadding;
import static org.patternfly.style.Classes.plain;
import static org.patternfly.style.Classes.text;
import static org.patternfly.style.Classes.util;

class FormItemFactory {

    private static final Logger logger = Logger.getLogger(FormItemFactory.class.getName());

    static FormItem nameFormItem(Metadata metadata) {
        AttributeDescription nameDescription = metadata.resourceDescription().attributes().get(NAME);
        if (!nameDescription.isDefined()) {
            ModelNode modelNode = new ModelNode();
            modelNode.get(DESCRIPTION).set("The name of the resource");
            modelNode.get(TYPE).set(ModelType.STRING);
            nameDescription = new AttributeDescription(new Property(NAME, modelNode));
        }
        // Even if the name description already exists, make sure that these properties have the right value.
        nameDescription.get(REQUIRED).set(true);
        nameDescription.get(ACCESS_TYPE).set(READ_WRITE);
        nameDescription.get(EXPRESSIONS_ALLOWED).set(false);

        ResourceAttribute ra = new ResourceAttribute(new ModelNode(), nameDescription, SecurityContext.RWX);
        String identifier = identifier(ra, EDIT);
        FormGroupLabel formGroupLabel = label(identifier, metadata, ra);
        return new StringFormItem(identifier, ra, formGroupLabel,
                new FormItemFlags(FormItemFlags.Scope.NEW_RESOURCE, Placeholder.NONE));
    }

    static FormItem formItem(AddressTemplate template, Metadata metadata, ResourceAttribute ra, FormItemFlags flags) {
        FormItem formItem = null;
        for (FormItemProvider fip : specialFormItems) {
            if (fip.test(template, metadata, ra, flags)) {
                formItem = fip.formItem(template, metadata, ra, flags);
                break;
            }
        }
        if (formItem == null) {
            String identifier = identifier(ra, EDIT);
            FormGroupLabel formGroupLabel = label(identifier, metadata, ra);

            if (!ra.readable) {
                formItem = new RestrictedFormItem(identifier, ra, formGroupLabel, flags);
            } else {
                if (ra.description.hasDefined(TYPE)) {
                    ModelType type = ra.description.get(TYPE).asType();
                    switch (type) {
                        case BOOLEAN:
                            formItem = new BooleanFormItem(identifier, ra, formGroupLabel, flags);
                            break;

                        case INT:
                        case LONG:
                        case DOUBLE:
                            formItem = new NumberFormItem(identifier, ra, formGroupLabel, flags);
                            break;

                        case STRING:
                            if (ra.description.hasDefined(ALLOWED)) {
                                formItem = new SelectFormItem(identifier, ra, formGroupLabel, flags);
                            } else if (ra.description.hasDefined(CAPABILITY_REFERENCE)) {
                                String capability = ra.description.get(ModelDescriptionConstants.CAPABILITY_REFERENCE)
                                        .asString();
                                formItem = new SingleTypeaheadFormItem(identifier, ra, formGroupLabel, flags, template,
                                        capability);
                            } else {
                                formItem = new StringFormItem(identifier, ra, formGroupLabel, flags);
                            }
                            break;

                        case LIST:
                            // TODO Support simple list types depending on the VALUE_TYPE
                            formItem = new UnsupportedFormItem(identifier, ra, formGroupLabel, flags);
                            break;

                        case OBJECT:
                            // TODO Support simple object types like key=value pairs
                            formItem = new UnsupportedFormItem(identifier, ra, formGroupLabel, flags);
                            break;

                        // unsupported types
                        case BIG_DECIMAL:
                        case BIG_INTEGER:
                        case BYTES:
                        case EXPRESSION:
                        case PROPERTY:
                        case TYPE:
                        case UNDEFINED:
                            formItem = new UnsupportedFormItem(identifier, ra, formGroupLabel, flags);
                            logger.warn("Unsupported type %s for attribute %s in resource %s. " +
                                    "Unable to create a form item. Attribute will be skipped.", type.name(), ra.name, template);
                            break;

                        default:
                            formItem = new UnsupportedFormItem(identifier, ra, formGroupLabel, flags);
                            break;
                    }
                } else {
                    formItem = new UnsupportedFormItem(identifier, ra, formGroupLabel, flags);
                }
            }
        }
        return formItem.store(Keys.RESOURCE_ATTRIBUTE, ra);
    }

    private static FormGroupLabel label(String identifier, Metadata metadata, ResourceAttribute ra) {
        FormGroupLabel formGroupLabel;
        LabelBuilder labelBuilder = new LabelBuilder();
        if (ra.description != null) {
            if (ra.description.nested()) {
                // <unstable>
                // If the internal DOM of FormGroupLabel changes, this will no longer work
                AttributeDescription parentDescription = ra.description.parent();
                AttributeDescription nestedDescription = ra.description;
                String parentLabel = labelBuilder.label(parentDescription.name());
                String nestedLabel = labelBuilder.label(ra.name);
                formGroupLabel = formGroupLabel(nestedLabel)
                        .css(halComponent(resource, HalClasses.nestedLabel))
                        .help(nestedLabel + " description", attributeDescriptionPopover(nestedLabel, nestedDescription, all));
                HTMLElement parentLabelElement = Elements.label().css(component(form, label))
                        .apply(l -> l.htmlFor = identifier)
                        .add(span().css(component(form, label, text))
                                .text(parentLabel))
                        .element();
                HTMLElement parentHelpButton = span().css(component(form, group, Classes.label, help), util("ml-xs"))
                        .add(span().css(component(button), modifier(plain), modifier(noPadding))
                                .attr(type, "button")
                                .attr(role, Roles.button)
                                .attr(tabindex, 0)
                                .aria(Aria.label, parentLabel + " description")
                                .add(span().css(component(button, icon))
                                        .add(questionCircle())))
                        .element();
                // Use insert-first calls and add the elements in reverse order
                // to not mess with the required marker added in FormGroupLabel.attach()
                insertFirst(formGroupLabel.element(), nestedElementSeparator());
                insertFirst(formGroupLabel.element(), parentHelpButton);
                insertFirst(formGroupLabel.element(), parentLabelElement);
                attributeDescriptionPopover(parentLabel, parentDescription, all)
                        .trigger(parentHelpButton)
                        .appendToBody();
                // </unstable>
            } else {
                String label = labelBuilder.label(ra.name);
                formGroupLabel = formGroupLabel(label)
                        .help(label + " description", attributeDescriptionPopover(label, ra.description, all));

                // only the top level attribute is stability-labeled
                if (uic().environment()
                        .highlightStability(metadata.resourceDescription().stability(), ra.description.stability())) {
                    formGroupLabel.css(halComponent(resource, stabilityLevel))
                            .add(stabilityLabel(ra.description.stability()).compact()
                                    .style("align-self", "baseline")
                                    .css(util("ml-sm"), util("font-weight-normal"))
                                    .element());
                }
            }
            if (ra.description.deprecation().isDefined()) {
                formGroupLabel.classList().add(halModifier(deprecated));
            }
        } else {
            formGroupLabel = formGroupLabel(labelBuilder.label(ra.name));
        }
        return formGroupLabel;
    }
}

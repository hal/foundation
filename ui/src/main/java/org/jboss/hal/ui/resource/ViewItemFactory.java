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

import java.util.List;

import org.jboss.elemento.Id;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.core.LabelBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.resources.Keys;
import org.patternfly.component.label.Label;
import org.patternfly.component.list.DescriptionListTerm;
import org.patternfly.core.Roles;

import elemental2.dom.HTMLElement;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.wrapHtmlContainer;
import static org.jboss.hal.core.Notification.nyi;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITY_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;
import static org.jboss.hal.dmr.ModelType.BOOLEAN;
import static org.jboss.hal.dmr.ModelType.LIST;
import static org.jboss.hal.dmr.ModelType.OBJECT;
import static org.jboss.hal.resources.HalClasses.deprecated;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.resources.HalClasses.restricted;
import static org.jboss.hal.resources.HalClasses.stabilityLevel;
import static org.jboss.hal.resources.HalClasses.undefined;
import static org.jboss.hal.resources.HalClasses.view;
import static org.jboss.hal.ui.BuildingBlocks.AttributeDescriptionContent.all;
import static org.jboss.hal.ui.BuildingBlocks.attributeDescriptionPopover;
import static org.jboss.hal.ui.BuildingBlocks.modelNodeCode;
import static org.jboss.hal.ui.BuildingBlocks.nestedElementSeparator;
import static org.jboss.hal.ui.BuildingBlocks.renderExpression;
import static org.jboss.hal.ui.BuildingBlocks.resolveExpressionIcon;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.resource.CapabilityReference.capabilityReference;
import static org.jboss.hal.ui.resource.ItemIdentifier.identifier;
import static org.jboss.hal.ui.resource.ResourceManager.State.VIEW;
import static org.jboss.hal.ui.resource.ViewItemProviders.specialViewItems;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.icon.Icon.icon;
import static org.patternfly.component.label.LabelGroup.labelGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.component.switch_.Switch.switch_;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.core.Attributes.role;
import static org.patternfly.icon.IconSets.fas.lock;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.descriptionList;
import static org.patternfly.style.Classes.helpText;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.text;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Color.grey;

class ViewItemFactory {

    private static final Logger logger = Logger.getLogger(ViewItemFactory.class.getName());

    static ViewItem viewItem(AddressTemplate template, Metadata metadata, ResourceAttribute ra) {
        ViewItem viewItem = null;
        for (ViewItemProvider vip : specialViewItems) {
            if (vip.test(template, metadata, ra)) {
                viewItem = vip.viewItem(template, metadata, ra);
                break;
            }
        }
        if (viewItem == null) {
            DescriptionListTerm descriptionListTerm = label(metadata, ra);
            HTMLElement valueElement = value(template, ra);
            viewItem = new ViewItem(identifier(ra, VIEW), descriptionListTerm, valueElement)
                    .store(Keys.RESOURCE_ATTRIBUTE, ra);
        }
        return viewItem.store(Keys.RESOURCE_ATTRIBUTE, ra);
    }

    private static DescriptionListTerm label(Metadata metadata, ResourceAttribute ra) {
        DescriptionListTerm term;
        LabelBuilder labelBuilder = new LabelBuilder();
        if (ra.description.nested()) {
            // <unstable>
            // If the internal DOM of DescriptionListTerm changes, this will no longer work
            // By default, DescriptionListTerm supports only one text element. But in this case we
            // want to have one for the parent and one for the nested attribute description.
            // So we set up the internals of DescriptionListTerm manually.
            AttributeDescription parentDescription = ra.description.parent();
            AttributeDescription nestedDescription = ra.description;
            String parentLabel = labelBuilder.label(parentDescription.name());
            String nestedLabel = labelBuilder.label(ra.name);
            term = descriptionListTerm(parentLabel)
                    .css(halComponent(resource, HalClasses.nestedLabel))
                    .help(attributeDescriptionPopover(parentLabel, parentDescription, all));
            HTMLElement nestedTextElement = span()
                    .css(component(descriptionList, text), modifier(helpText))
                    .attr(role, Roles.button)
                    .attr("type", "button")
                    .apply(element -> element.tabIndex = 0)
                    .text(nestedLabel)
                    .element();
            attributeDescriptionPopover(nestedLabel, nestedDescription, all)
                    .trigger(nestedTextElement)
                    .appendToBody();
            wrapHtmlContainer(term.element())
                    .add(nestedElementSeparator())
                    .add(nestedTextElement);
            // </unstable>
        } else {
            String label = labelBuilder.label(ra.name);
            term = descriptionListTerm(label)
                    .help(attributeDescriptionPopover(label, ra.description, all));

            // only the top level attribute is stability-labeled
            if (uic().environment()
                    .highlightStability(metadata.resourceDescription().stability(), ra.description.stability())) {
                // <unstable>
                // If the internal DOM of DescriptionListTerm changes, this will no longer work
                // DescriptionListTerm implements ElementDelegate and delegates to the internal text element.
                // That's why we must use term.element.appendChild() instead of term.add() to add the
                // stability label after the text element instead of into the text element. Then we must
                // reset the font weight to normal (DescriptionListTerm uses bold)
                term.element().classList.add(halComponent(resource, stabilityLevel));
                term.element().appendChild(stabilityLabel(ra.description.stability()).compact()
                        .style("align-self", "baseline")
                        .css(util("ml-sm"), util("font-weight-normal"))
                        .element());
                // </unstable>
            }
        }
        if (ra.description.deprecation().isDefined()) {
            term.containerDelegate().classList.add(halModifier(deprecated));
        }
        return term;
    }

    private static HTMLElement value(AddressTemplate template, ResourceAttribute ra) {
        HTMLElement element;

        // TODO Implement sensitive constraints
        if (!ra.readable) {
            element = span().css(halModifier(restricted))
                    .text("restricted")
                    .add(icon(lock().css(util("ml-sm"))))
                    .element();

        } else {
            if (ra.expression) {
                HTMLElement resolveButton = button().plain().inline().icon(resolveExpressionIcon().get())
                        .onClick((e, b) -> {
                            // TODO Resolve expression
                            uic().notifications().send(nyi());
                            logger.info("Resolve expression: %s", ra.value.asString());
                        })
                        .element();
                element = span()
                        .add(renderExpression(ra.value.asString()))
                        .add(resolveButton)
                        .add(tooltip(resolveButton, "Resolve expression"))
                        .element();

            } else {
                if (ra.value.isDefined()) {
                    if (ra.description.hasDefined(TYPE)) {
                        ModelType type = ra.description.get(TYPE).asType();
                        if (type == BOOLEAN) {
                            String unique = Id.unique(ra.name);
                            element = switch_(unique, unique)
                                    .value(ra.value.asBoolean())
                                    .ariaLabel(ra.name)
                                    .checkIcon()
                                    .readonly()
                                    .element();
                        } else if (type.simple()) {
                            String unit = ra.description.unit();
                            if (unit != null) {
                                element = span()
                                        .add(span().text(ra.value.asString()))
                                        .add(span().css(halComponent(resource, view, HalClasses.unit))
                                                .text(unit))
                                        .element();
                            } else if (ra.description.hasDefined(ALLOWED)) {
                                List<String> allowed = ra.description.get(ALLOWED)
                                        .asList()
                                        .stream()
                                        .map(ModelNode::asString)
                                        .sorted(naturalOrder())
                                        .collect(toList());
                                allowed.remove(ra.value.asString());
                                element = labelGroup()
                                        .numLabels(1)
                                        .collapsedText("Allowed values")
                                        .addItem(Label.label("", grey).text(ra.value.asString()))
                                        .addItems(allowed, a -> Label.label(a, grey).disabled())
                                        .element();
                            } else {
                                if (ra.description.hasDefined(CAPABILITY_REFERENCE)) {
                                    String capability = ra.description.get(CAPABILITY_REFERENCE).asString();
                                    element = capabilityReference(template, capability, ra)
                                            .element();
                                } else {
                                    element = plainText(ra);
                                }
                            }
                        } else if (type == LIST) {
                            ModelType valueType = ra.description.has(VALUE_TYPE) &&
                                    ra.description.get(VALUE_TYPE).getType() != OBJECT
                                    ? ra.description.get(VALUE_TYPE).asType()
                                    : null;
                            if (valueType != null && valueType.simple()) {
                                element = list().plain().inline()
                                        .addItems(ra.value.asList().stream().map(ModelNode::asString).collect(toList()),
                                                v -> listItem(Id.build(v, "value")).text(v))
                                        .element();
                            } else {
                                element = modelNodeCode(ra.value).element();
                            }
                        } else if (type == OBJECT) {
                            element = modelNodeCode(ra.value).element();
                        } else {
                            element = plainText(ra);
                        }
                    } else {
                        logger.warn("No type information found in resource description for attribute %s in resource %s. " +
                                "Fallback to plain text representation.", ra.name, template);
                        element = plainText(ra);
                    }
                } else {
                    element = plainText(ra);
                    element.classList.add(halComponent(resource, view, undefined));
                }
            }
        }

        return element;
    }

    private static HTMLElement plainText(ResourceAttribute ra) {
        return span()
                .text(ra.value.asString())
                .element();
    }
}

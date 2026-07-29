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
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jboss.elemento.ButtonType;
import org.jboss.elemento.Elements;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.SelectInTree;
import org.patternfly.component.label.Label;
import org.patternfly.layout.flex.Flex;
import org.patternfly.overlay.Overlay;
import org.patternfly.overlay.TriggerMode;

import elemental2.dom.HTMLElement;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.br;
import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.strong;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;
import static org.jboss.hal.resources.HalClasses.capabilityReference;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.value;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.label.Label.label;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.Gap.sm;
import static org.patternfly.layout.stack.Stack.stack;
import static org.patternfly.layout.stack.StackItem.stackItem;
import static org.patternfly.overlay.Overlay.overlay;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.list;
import static org.patternfly.style.Classes.menu;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.overflow;
import static org.patternfly.style.Classes.overlay;
import static org.patternfly.style.Color.blue;
import static org.patternfly.style.Placement.bottom;

/**
 * Displays an attribute value that references a WildFly capability, with interactive navigation to the providing resource(s).
 * When a single provider exists, clicking the label navigates directly. When multiple providers exist, a popper menu lists all
 * providing resources ranked by proximity to the current resource.
 */
class CapabilityReferenceLabel implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    static CapabilityReferenceLabel capabilityReferenceLabel(AddressTemplate origin, String capability,
            String attributeValue) {
        return new CapabilityReferenceLabel(origin, capability, attributeValue);
    }

    // ------------------------------------------------------ instance

    private static final Logger logger = Logger.getLogger(CapabilityReferenceLabel.class.getName());
    private final AddressTemplate origin;
    private final Flex root;

    CapabilityReferenceLabel(AddressTemplate origin, String capability, String attributeValue) {
        this.origin = origin;
        this.root = flex().css(halComponent(capabilityReference))
                .alignItems(center).columnGap(sm)
                .add(span().css(halComponent(capabilityReference, value))
                        .text(attributeValue));

        uic().capabilityRegistry().findResources(capability, attributeValue).then(templates -> {
            if (templates.isEmpty()) {
                logger.warn("No resources found for capability %s and attribute %s", capability, attributeValue);

            } else {
                Label providedBy = label(Elements.button(ButtonType.button), Id.build("provided-by", capability), "", blue)
                        .css(modifier(overflow));
                root.add(providedBy);

                if (templates.size() == 1) {
                    providedBy.text("provided by 1 resource");
                    providedBy.on(click, e -> {
                        SelectInTree.dispatch(element(), templates.get(0));
                    });
                    root.add(tooltip(providedBy.element(), templates.get(0).toString()).element());

                } else {
                    providedBy.text("provided by " + templates.size() + " resources");
                    List<AddressTemplate> ranked = rank(templates).values().stream()
                            .flatMap(List::stream)
                            .collect(toList());
                    HTMLElement overlayElement = div().css(component(overlay))
                            .add(stack().css(halComponent(capabilityReference, menu))
                                    .gutter()
                                    .addItem(stackItem()
                                            .add("Attribute ")
                                            .add(strong().text(attributeValue).element())
                                            .add(" references the capability")
                                            .add(br())
                                            .add(strong().add(code().text(capability)))
                                            .add(br())
                                            .add("provided by ")
                                            .add(strong().element())
                                            .add(" resources:"))
                                    .addItem(stackItem().fill()
                                            .add(list().css(halComponent(capabilityReference, menu, list)).plain()
                                                    .addItems(templates, tpl -> listItem()
                                                            .add(button(tpl.toString()).link().inline()
                                                                    .onClick((e, btn) ->
                                                                            SelectInTree.dispatch(element(), tpl)))))))
                            .element();
                    Overlay overlay = overlay(overlayElement, bottom)
                            .distance(10)
                            .trigger(providedBy.element())
                            .triggerMode(TriggerMode.click);
                    overlay.attach();
                    root.add(overlayElement);
                }
            }
            return null;
        });
    }

    @Override
    public HTMLElement element() {
        return root.element();
    }

    // ------------------------------------------------------ internal

    private SortedMap<Integer, List<AddressTemplate>> rank(List<AddressTemplate> templates) {
        // rank -> list of templates
        //   1: template starts with origin (current resource)
        //   2: same profile or server group
        //   3: anything else
        return templates.stream()
                .sorted(comparing(AddressTemplate::toString))
                .collect(groupingBy(template -> {
                    if (template.template.startsWith(origin.template)) {
                        return 1;
                    } else if (uic().environment().domain()) {
                        if (PROFILE.equals(origin.first().key) && PROFILE.equals(template.first().key)) {
                            return Objects.equals(origin.first().value, template.first().value) ? 2 : 3;
                        } else if (SERVER_GROUP.equals(origin.first().key) && SERVER_GROUP.equals(template.first().key)) {
                            return Objects.equals(origin.first().value, template.first().value) ? 2 : 3;
                        } else {
                            return 3;
                        }
                    } else {
                        return 3;
                    }
                }, TreeMap::new, toList()));
    }
}

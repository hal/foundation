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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import org.gwtproject.event.shared.HandlerRegistration;
import org.jboss.elemento.Attachable;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.Key;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.SelectInTree;
import org.patternfly.component.button.Button;
import org.patternfly.popper.Modifiers;
import org.patternfly.popper.Popper;
import org.patternfly.popper.PopperBuilder;
import org.patternfly.popper.TriggerAction;

import elemental2.dom.HTMLElement;
import elemental2.dom.MutationRecord;
import elemental2.dom.Node;
import elemental2.promise.Promise;

import static elemental2.dom.DomGlobal.document;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.br;
import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.isVisible;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.small;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.strong;
import static org.jboss.elemento.EventType.bind;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;
import static org.jboss.hal.resources.HalClasses.capabilityReference;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.providedBy;
import static org.jboss.hal.resources.HalClasses.value;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.Gap.sm;
import static org.patternfly.layout.stack.Stack.stack;
import static org.patternfly.layout.stack.StackItem.stackItem;
import static org.patternfly.popper.Placement.bottom;
import static org.patternfly.style.Classes.list;
import static org.patternfly.style.Classes.menu;

class CapabilityReference implements IsElement<HTMLElement>, Attachable {

    // ------------------------------------------------------ factory

    static CapabilityReference capabilityReference(AddressTemplate origin, String capability,
            ResourceAttribute ra) {
        return new CapabilityReference(origin, capability, ra);
    }

    // ------------------------------------------------------ instance

    private enum State {
        NO_RESOURCES, ONE_RESOURCE, MULTIPLE_RESOURCES, FAILED
    }

    private static final int DISTANCE = 10;
    private static final int Z_INDEX = 9999;
    private static final Logger logger = Logger.getLogger(CapabilityReference.class.getName());

    private final AddressTemplate origin;
    private final String capability;
    private final ResourceAttribute ra;
    private final List<HandlerRegistration> handlerRegistrations;
    private final Button providedByButton;
    private final HTMLElement menuElement;
    private final HTMLElement menuCountElement;
    private final org.patternfly.component.list.List menuList;
    private final HTMLElement root;
    private State state;
    private AddressTemplate singleTemplate;
    private Popper popper;

    CapabilityReference(AddressTemplate origin, String capability, ResourceAttribute ra) {
        this.origin = origin;
        this.capability = capability;
        this.ra = ra;
        this.handlerRegistrations = new ArrayList<>();
        this.state = null;
        this.singleTemplate = null;

        this.root = flex().css(halComponent(capabilityReference))
                .alignItems(center).columnGap(sm)
                .add(span().css(halComponent(capabilityReference, value))
                        .text(ra.value.asString())
                        .element())
                .add(small().css(halComponent(capabilityReference, providedBy))
                        .add(providedByButton = button("").link().inline().onClick((e, btn) -> onClick())))
                .element();

        this.menuElement = div()
                .add(stack().css(halComponent(capabilityReference, menu))
                        .gutter()
                        .addItem(stackItem()
                                .add("Attribute ")
                                .add(strong().text(ra.value.asString()).element())
                                .add(" references the capability")
                                .add(br())
                                .add(strong().add(code().text(capability)))
                                .add(br())
                                .add("provided by ")
                                .add(menuCountElement = strong().element())
                                .add(" resources:"))
                        .addItem(stackItem().fill()
                                .add(menuList = list().css(halComponent(capabilityReference, menu, list))
                                        .plain())))
                .element();

        setVisible(menuElement, false);
        setVisible(providedByButton, false);
        body().add(menuElement);
        Attachable.register(this, this);
    }

    @Override
    public void attach(MutationRecord mutationRecord) {
        popper = new PopperBuilder("CapabilityReference", providedByButton.element(), menuElement)
                .zIndex(Z_INDEX)
                .placement(bottom)
                .addModifier(Modifiers.offset(DISTANCE),
                        Modifiers.noOverflow(),
                        Modifiers.hide(),
                        Modifiers.flip(true),
                        Modifiers.widths(),
                        Modifiers.placement(),
                        Modifiers.eventListeners(false))
                .registerHandler(EnumSet.of(TriggerAction.manual), null, null)
                .removePopperOnTriggerDetach()
                .build();

        handlerRegistrations.add(bind(document, EventType.click, true, e -> {
            if (isVisible(menuElement)) {
                Node target = (Node) e.target;
                if (target != providedByButton.element() && !menuElement.contains(target)) {
                    popper.hide(null);
                }
            }
        }));
        handlerRegistrations.add(bind(document, EventType.keydown, true, e -> {
            if (isVisible(menuElement) && Key.Escape.match(e)) {
                popper.hide(null);
            }
        }));

        findResources().then(__ -> {
            setVisible(providedByButton, state == State.ONE_RESOURCE || state == State.MULTIPLE_RESOURCES);
            if (state == State.ONE_RESOURCE && singleTemplate != null) {
                tooltip(providedByButton.element(), singleTemplate.toString()).appendToBody();
            }
            return null;
        });
    }

    @Override
    public void detach(MutationRecord mutationRecord) {
        for (HandlerRegistration handlerRegistration : handlerRegistrations) {
            handlerRegistration.removeHandler();
        }
        if (popper != null) {
            popper.cleanup();
        }
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ event

    private void onClick() {
        if (state == State.ONE_RESOURCE && singleTemplate != null) {
            SelectInTree.dispatch(element(), singleTemplate);
        } else if (state == State.MULTIPLE_RESOURCES) {
            popper.show(null);
        } else {
            logger.error("Invalid state %s in capability reference for capability %s and attribute %s",
                    state.name(), capability, ra);
        }
    }

    // ------------------------------------------------------ internal

    // Only this method may change the state!
    private Promise<Void> findResources() {
        return uic().capabilityRegistry().findResources(capability, ra.value.asString())
                .then(templates -> {

                    if (templates.isEmpty()) {
                        state = State.NO_RESOURCES;
                        setVisible(providedByButton.element(), false);
                        logger.warn("No resources found for capability %s and attribute %s", capability, ra);

                    } else if (templates.size() == 1) {
                        state = State.ONE_RESOURCE;
                        singleTemplate = templates.get(0);
                        providedByButton.text("provided by 1 resource");

                    } else {
                        state = State.MULTIPLE_RESOURCES;
                        String size = String.valueOf(templates.size());
                        SortedMap<Integer, List<AddressTemplate>> ranked = rank(templates);
                        for (List<AddressTemplate> rank : ranked.values()) {
                            menuList.addItems(rank, tpl -> listItem()
                                    .add(button(tpl.toString()).link().inline()
                                            .onClick((e, btn) -> SelectInTree.dispatch(element(), tpl))));
                        }
                        menuCountElement.textContent = size;
                        providedByButton.text("provided by " + size + " resources");
                    }
                    return Promise.resolve((Void) null);
                })
                .catch_(error -> {
                    state = State.FAILED;
                    logger.error("Unable to find resources for capability %s and attribute %s: %s",
                            capability, ra, String.valueOf(error));
                    return Promise.resolve((Void) null);
                });
    }

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

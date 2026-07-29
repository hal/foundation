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
package org.jboss.hal.ui.modelbrowser;

import org.jboss.elemento.IsElement;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.SelectInTree;
import org.patternfly.component.form.TextInput;
import org.patternfly.overlay.Overlay;
import org.patternfly.style.Classes;

import elemental2.dom.Event;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.elemento.Key.Enter;
import static org.jboss.hal.resources.HalClasses.goto_;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.modelBrowser;
import static org.jboss.hal.resources.HalClasses.results;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.icon.IconSets.far.compass;
import static org.patternfly.layout.flex.Direction.column;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.FlexShorthand._1;
import static org.patternfly.overlay.Overlay.overlay;
import static org.patternfly.overlay.TriggerMode.click;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.search;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Placement.bottom;

/**
 * Popper-based input that allows direct navigation to a resource by entering its address template.
 * <p>
 * When the entered address contains wildcards, the component resolves them and presents a list of matching fully qualified
 * addresses for the user to choose from. Triggered by a compass icon button in the tree toolbar.
 */
class GotoResource implements IsElement<HTMLElement> {

    private final HTMLElement button;
    private final HTMLElement menu;
    private final TextInput input;
    private final Overlay overlay;
    private final HTMLElement root;
    private HTMLElement multiple;

    GotoResource() {
        this.button = button().plain().icon(compass()).element();
        this.input = textInput("goto").placeholder("Goto resource")
                .onKeyup((event, component, value) -> gotoResource(event));
        this.menu = div().css(halComponent(modelBrowser, goto_))
                .add(input)
                .element();
        HTMLDivElement overlayElement = div().css(component(Classes.overlay)).add(menu).element();
        this.overlay = overlay(overlayElement, bottom)
                .trigger(button)
                .triggerMode(click)
                .onToggle((event, open) -> {
                    if (open) {
                        input.value("");
                        input.input().element().focus();
                    }
                });
        overlay.attach();
        this.root = div()
                .add(button)
                .add(overlayElement)
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    private void gotoResource(Event event) {
        if (Enter.match(event)) {
            HTMLInputElement inputElement = (HTMLInputElement) event.target;
            AddressTemplate template = AddressTemplate.ofTrusted(inputElement.value);
            if (!template.fullyQualified()) {
                failSafeRemoveFromParent(multiple);
                uic().modelTree().resolveWildcards(template).then(templates -> {
                    multiple = flex().css(util("mt-md")).direction(column)
                            .addItem(flexItem()
                                    .add(content(p)
                                            .add("The address you entered contains wildcards. Please select a resource from the list below.")))
                            .addItem(flexItem()
                                    .flex(_1)
                                    .add(list().css(halComponent(modelBrowser, search, results))
                                            .plain()
                                            .addItems(templates, fullyQualified -> listItem()
                                                    .add(button().link().inline().text(fullyQualified.toString())
                                                            .onClick((e, b) -> {
                                                                SelectInTree.dispatch(button, fullyQualified);
                                                                close(event);
                                                            })))))
                            .element();
                    menu.appendChild(multiple);
                    return null;
                });
            } else {
                SelectInTree.dispatch(button, template);
                close(event);
            }
        }
    }

    private void close(Event event) {
        event.stopPropagation();
        event.preventDefault();
        failSafeRemoveFromParent(multiple);
        overlay.hide();
    }
}

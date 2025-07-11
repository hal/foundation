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

import java.util.EnumSet;

import org.jboss.elemento.Attachable;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.Key;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.SelectInTree;
import org.patternfly.component.form.TextInput;
import org.patternfly.popper.Modifiers;
import org.patternfly.popper.Popper;
import org.patternfly.popper.PopperBuilder;
import org.patternfly.popper.TriggerAction;

import elemental2.dom.Event;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.MutationRecord;

import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.resources.HalClasses.goto_;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.modelBrowser;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.icon.IconSets.far.compass;
import static org.patternfly.popper.Placement.bottomStart;

class GotoResource implements IsElement<HTMLElement>, Attachable {

    static final int DISTANCE = 10;
    static final int Z_INDEX = 9999;

    private final HTMLElement button;
    private final HTMLElement menu;
    private final TextInput input;
    private Popper popper;

    GotoResource() {
        this.button = button().plain().icon(compass()).element();
        this.input = textInput("goto").placeholder("Goto resource");
        this.menu = div().css(halComponent(modelBrowser, goto_))
                .style("display", "none")
                .add(input)
                .element();
        body().add(menu);
        Attachable.register(this, this);
    }

    @Override
    public void attach(MutationRecord mutationRecord) {
        popper = new PopperBuilder("GotoResource", button, menu)
                .zIndex(Z_INDEX)
                .placement(bottomStart)
                .addModifier(Modifiers.offset(DISTANCE),
                        Modifiers.noOverflow(),
                        Modifiers.hide(),
                        Modifiers.placement(),
                        Modifiers.eventListeners(false))
                .registerHandler(EnumSet.of(TriggerAction.stayOpen), this::show, this::close)
                .removePopperOnTriggerDetach()
                .build();
    }

    @Override
    public void detach(MutationRecord mutationRecord) {
        if (popper != null) {
            popper.cleanup();
        }
    }

    @Override
    public HTMLElement element() {
        return button;
    }

    private void show(Event event) {
        popper.show(null);
        input.value("");
        input.input().element().focus();
    }

    private void close(Event event) {
        if (Key.Enter.match(event)) {
            HTMLInputElement inputElement = (HTMLInputElement) event.target;
            AddressTemplate template = AddressTemplate.of(inputElement.value);
            SelectInTree.dispatch(button, template);
            inputElement.value = "";
            event.stopPropagation();
            event.preventDefault();
        }
        popper.hide(null);
    }
}

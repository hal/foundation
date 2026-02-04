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
import java.util.List;

import org.patternfly.component.ComponentType;
import org.patternfly.component.button.Button;
import org.patternfly.component.textinputgroup.BaseFilterInput;
import org.patternfly.handler.ComponentHandler;

import static org.jboss.elemento.Elements.setVisible;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.textinputgroup.TextInputGroupUtilities.textInputGroupUtilities;
import static org.patternfly.icon.IconSets.fas.redo;
import static org.patternfly.icon.IconSets.fas.times;

class FilterReloadInput extends BaseFilterInput<FilterReloadInput> {

    // ------------------------------------------------------ factory

    static FilterReloadInput filterReloadInput(String id) {
        return new FilterReloadInput(id);
    }

    // ------------------------------------------------------ instance

    private final Button clearButton;
    private final List<ComponentHandler<FilterReloadInput>> onReload;

    FilterReloadInput(String id) {
        super(ComponentType.FilterInput, id);
        this.onReload = new ArrayList<>();

        addUtilities(textInputGroupUtilities()
                .add(clearButton = button().icon(times()).plain().onClick((e, b) -> {
                    if (defaultOnClear != null) {
                        defaultOnClear.handle(e, that());
                    }
                    onClear.forEach(handler ->
                            handler.handle(e, that()));
                }))
                .add(button().icon(redo()).plain().onClick((e, b) ->
                        onReload.forEach(handler -> handler.handle(e, that())))));
        toggleUtilities(value());
    }

    // ------------------------------------------------------ builder

    @Override
    public FilterReloadInput that() {
        return this;
    }

    // ------------------------------------------------------ events


    // ------------------------------------------------------ events

    public FilterReloadInput onReload(ComponentHandler<FilterReloadInput> handler) {
        onReload.add(handler);
        return this;
    }

    // ------------------------------------------------------ internal

    @Override
    protected void toggleUtilities(String value) {
        // Change the default behavior of super class: Don't completely add/remove the utilities.
        // The reload button always remains in the utilities container, only show/hide the clear button.
        setVisible(clearButton, utilitiesVisibility.apply(that(), value));
    }
}

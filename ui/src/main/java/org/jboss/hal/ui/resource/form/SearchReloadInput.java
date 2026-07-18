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
package org.jboss.hal.ui.resource.form;

import java.util.ArrayList;
import java.util.List;

import org.patternfly.component.ComponentType;
import org.patternfly.component.button.Button;
import org.patternfly.component.textinputgroup.BaseSearchInput;
import org.patternfly.handler.ComponentHandler;

import static org.jboss.elemento.Elements.setVisible;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.textinputgroup.TextInputGroupUtilities.textInputGroupUtilities;
import static org.patternfly.icon.IconSets.fas.rotateRight;
import static org.patternfly.icon.IconSets.fas.xmark;

/**
 * Search input with an additional reload button, used by {@link CapabilityReferenceControl} to refresh capability data.
 * <p>
 * This class mirrors the structure of {@link FilterReloadInput}. The duplication exists because the PatternFly type hierarchy
 * requires separate subclasses: this class extends {@link org.patternfly.component.textinputgroup.BaseSearchInput} (for
 * single-select typeaheads), while {@link FilterReloadInput} extends
 * {@link org.patternfly.component.textinputgroup.BaseFilterInput} (for multi-select typeaheads). A shared abstract class is not
 * possible since Java does not support multiple inheritance.
 */
class SearchReloadInput extends BaseSearchInput<SearchReloadInput> {

    // ------------------------------------------------------ factory

    /** Creates a new search reload input with the given identifier. */
    static SearchReloadInput searchReloadInput(String id) {
        return new SearchReloadInput(id);
    }

    // ------------------------------------------------------ instance

    private final Button clearButton;
    private final List<ComponentHandler<SearchReloadInput>> onReload;

    SearchReloadInput(String id) {
        super(ComponentType.SearchInput, id);
        this.onReload = new ArrayList<>();

        inputElement.autocomplete = "off";
        addUtilities(textInputGroupUtilities()
                .add(clearButton = button().icon(xmark()).plain().onClick((e, b) -> {
                    if (defaultOnClear != null) {
                        defaultOnClear.handle(e, that());
                    }
                    onClear.forEach(handler ->
                            handler.handle(e, that()));
                }))
                .add(button().icon(rotateRight()).plain().onClick((e, b) ->
                        onReload.forEach(handler -> handler.handle(e, that())))));
        toggleUtilities(value());
    }

    // ------------------------------------------------------ builder

    @Override
    public SearchReloadInput that() {
        return this;
    }

    // ------------------------------------------------------ events

    /** Registers a handler to be called when the reload button is clicked. */
    public SearchReloadInput onReload(ComponentHandler<SearchReloadInput> handler) {
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

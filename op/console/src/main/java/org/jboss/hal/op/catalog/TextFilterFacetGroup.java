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
package org.jboss.hal.op.catalog;

import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.mgt.BooleanCounts;
import org.jboss.hal.mgt.CatalogFacet;
import org.jboss.hal.mgt.FacetValue;
import org.jboss.hal.ui.Format;
import org.patternfly.component.form.TextInput;

import elemental2.dom.Event;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.fieldset;
import static org.jboss.elemento.Elements.form;
import static org.jboss.elemento.Elements.legend;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.EventType.submit;
import static org.jboss.elemento.Key.Enter;
import static org.patternfly.component.form.Checkbox.checkbox;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.form;

/**
 * Facet group for capability boolean facets ({@link CatalogFacet#DECLARES_CAP}, {@link CatalogFacet#REFS_CAP}) that shows an
 * additional text input when "true" is checked, allowing users to filter by capability name substring.
 */
class TextFilterFacetGroup implements IsElement<HTMLElement> {

    private final HTMLElement root;

    @SuppressWarnings("DeconstructionCanBeUsed")
    TextFilterFacetGroup(CatalogController controller, String text, CatalogFacet facet, BooleanCounts counts) {
        TextInput textInput = textInput(Id.build("sidebar", "capability", facet.key, "text"))
                .placeholder("Filter by name...")
                .applyTo(input -> input.element().autocomplete = "off")
                .onKeyup((event, ti, value) -> {
                    if (Enter.match(event)) {
                        controller.applyFacetValue(facet, new FacetValue.BoolText(true, value));
                    }
                });

        // Restore state on rebuild
        FacetValue currentValue = controller.state().facetValue(facet);
        boolean trueActive = controller.state().isActive(facet, "true");
        boolean falseActive = controller.state().isActive(facet, "false");
        boolean showTextInput = trueActive;
        String restoredText = "";
        if (currentValue instanceof FacetValue.BoolText bt && bt.value()) {
            showTextInput = true;
            restoredText = bt.text() != null ? bt.text() : "";
        }
        setVisible(textInput, showTextInput);
        if (!restoredText.isEmpty()) {
            textInput.value(restoredText);
        }

        this.root = form().css("filter-panel-pf-category")
                .on(submit, Event::preventDefault)
                .add(fieldset()
                        .css(component(form, "fieldset"), "filter-panel-pf-category-items")
                        .add(legend()
                                .css("filter-panel-pf-category-title")
                                .text(text))
                        .run(fieldset -> {
                            // "true" checkbox
                            if (counts.true_ > 0) {
                                String trueId = Id.build("sidebar", "category", facet.key, "true");
                                fieldset.add(div().css("filter-panel-pf-category-item")
                                        .add(checkbox(trueId, trueId, "true")
                                                .onChange((e, cb, checked) -> {
                                                    if (checked) {
                                                        setVisible(textInput, true);
                                                        controller.applyFacetValue(facet,
                                                                new FacetValue.BoolText(true, null));
                                                    } else {
                                                        setVisible(textInput, false);
                                                        controller.removeFacet(facet);
                                                    }
                                                })
                                                .run(cb -> {
                                                    if (trueActive) {
                                                        cb.value(true);
                                                    }
                                                    cb.textDelegate()
                                                            .appendChild(span().css("item-count")
                                                                    .text("(" + Format.number(counts.true_) + ")")
                                                                    .element());
                                                })));
                            }

                            // Text input (below "true" checkbox)
                            fieldset.add(div().css("filter-panel-pf-category-item")
                                    .add(textInput));

                            // "false" checkbox
                            if (counts.false_ > 0) {
                                String falseId = Id.build("sidebar", "category", facet.key, "false");
                                fieldset.add(div().css("filter-panel-pf-category-item")
                                        .add(checkbox(falseId, falseId, "false")
                                                .onChange((e, cb, checked) -> {
                                                    if (checked) {
                                                        setVisible(textInput, false);
                                                        controller.applyFacetValue(facet, new FacetValue.Bool(false));
                                                    } else {
                                                        controller.removeFacet(facet);
                                                    }
                                                })
                                                .run(cb -> {
                                                    if (falseActive) {
                                                        cb.value(true);
                                                    }
                                                    cb.textDelegate().appendChild(span().css("item-count")
                                                            .text("(" + Format.number(counts.false_) + ")")
                                                            .element());
                                                })));
                            }
                        }))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}

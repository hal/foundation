package org.jboss.hal.op.catalog;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.mgt.CatalogFacet;
import org.jboss.hal.mgt.FacetValue;
import org.jboss.hal.ui.Format;

import elemental2.dom.Event;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.fieldset;
import static org.jboss.elemento.Elements.form;
import static org.jboss.elemento.Elements.legend;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.EventType.submit;
import static org.patternfly.component.form.Checkbox.checkbox;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.form;

/**
 * UI component representing a single facet category in the catalog filter panel.
 * <p>
 * Renders a field set with a category title and a list of checkbox values for the associated {@link CatalogFacet}. Each
 * checkbox displays the facet value and its result count, reflects the active state from {@link CatalogState}, and notifies the
 * supplied toggle handler when its selection changes.
 * <p>
 * The generated element can be inserted into the DOM through the {@link #element()} method.
 */
class CheckboxFacetGroup implements IsElement<HTMLElement> {

    private final HTMLElement root;

    CheckboxFacetGroup(CatalogController controller, String text, CatalogFacet facet, Map<String, Integer> values) {
        this.root = form().css("filter-panel-pf-category")
                .on(submit, Event::preventDefault)
                .add(fieldset()
                        .css(component(form, "fieldset"), "filter-panel-pf-category-items")
                        .add(legend()
                                .css("filter-panel-pf-category-title")
                                .text(text))
                        .run(fieldset -> {
                            for (Map.Entry<String, Integer> entry : values.entrySet()) {
                                String value = entry.getKey();
                                int count = entry.getValue();

                                String cbId = Id.build("sidebar", "category", facet.key, value);
                                fieldset.add(div().css("filter-panel-pf-category-item")
                                        .add(checkbox(cbId, cbId, value)
                                                .onChange((e, cb, checked) ->
                                                        onToggle(controller, facet, value))
                                                .run(cb -> {
                                                    if (controller.state().isActive(facet, value)) {
                                                        cb.value(true);
                                                    }
                                                    cb.textDelegate().appendChild(span().css("item-count")
                                                            .text("(" + Format.number(count) + ")")
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

    @SuppressWarnings("DeconstructionCanBeUsed")
    private void onToggle(CatalogController controller, CatalogFacet facet, String value) {
        if (facet.exclusive) {
            if (controller.state().isActive(facet, value)) {
                controller.removeFacet(facet);
            } else {
                controller.applyFacetValue(facet, new FacetValue.Bool("true".equals(value)));
            }
        } else {
            FacetValue current = controller.state().facetValue(facet);
            if (current instanceof FacetValue.Multi multi && multi.values().contains(value)) {
                Set<String> remaining = new LinkedHashSet<>(multi.values());
                remaining.remove(value);
                if (remaining.isEmpty()) {
                    controller.removeFacet(facet);
                } else {
                    controller.applyFacetValue(facet, new FacetValue.Multi(remaining));
                }
            } else {
                Set<String> values = current instanceof FacetValue.Multi multi
                        ? new LinkedHashSet<>(multi.values())
                        : new LinkedHashSet<>();
                values.add(value);
                controller.applyFacetValue(facet, new FacetValue.Multi(values));
            }
        }
    }
}

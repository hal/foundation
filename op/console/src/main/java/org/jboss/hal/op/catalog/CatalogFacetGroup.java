package org.jboss.hal.op.catalog;

import java.util.Map;

import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.mgt.CatalogFacet;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.fieldset;
import static org.jboss.elemento.Elements.form;
import static org.jboss.elemento.Elements.legend;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.op.catalog.CatalogBricks.formattedCount;
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
class CatalogFacetGroup implements IsElement<HTMLElement> {

    private final HTMLElement root;

    CatalogFacetGroup(CatalogController controller, String text, CatalogFacet facet, Map<String, Integer> values) {
        this.root = form().css("filter-panel-pf-category")
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
                                                        controller.applyFacetValue(facet, value))
                                                .run(cb -> {
                                                    if (controller.state().isActive(facet, value)) {
                                                        cb.value(true);
                                                    }
                                                    cb.textDelegate()
                                                            .appendChild(span().css("item-count")
                                                                    .text("(" + formattedCount(count) + ")")
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

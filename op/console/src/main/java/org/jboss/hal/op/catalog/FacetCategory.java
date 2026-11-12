package org.jboss.hal.op.catalog;

import java.util.Map;
import java.util.function.BiConsumer;

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.mgt.CatalogFacet;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLFieldSetElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.fieldset;
import static org.jboss.elemento.Elements.form;
import static org.jboss.elemento.Elements.legend;
import static org.jboss.elemento.Elements.span;
import static org.patternfly.component.form.Checkbox.checkbox;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.form;

class FacetCategory implements IsElement<HTMLElement> {

    private final HTMLElement root;
    private final CatalogFacet facet;
    private final BiConsumer<CatalogFacet, String> onToggle;
    private final HTMLContainerBuilder<HTMLFieldSetElement> fieldset;

    FacetCategory(String text, CatalogFacet facet, BiConsumer<CatalogFacet, String> onToggle) {
        this.facet = facet;
        this.onToggle = onToggle;
        this.root = form().css("filter-panel-pf-category")
                .add(fieldset = fieldset()
                        .css(component(form, "fieldset"), "filter-panel-pf-category-items")
                        .add(legend()
                                .css("filter-panel-pf-category-title")
                                .text(text)))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    void addValues(CatalogState state, Map<String, Integer> values) {
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            String value = entry.getKey();
            int count = entry.getValue();

            String cbId = Id.build("sidebar", "category", facet.key, value);
            fieldset.add(div().css("filter-panel-pf-category-item")
                    .add(checkbox(cbId, cbId, value)
                            .onChange((e, cb, checked) -> onToggle.accept(facet, value))
                            .run(cb -> {
                                if (state.isActive(facet, value)) {
                                    cb.value(true);
                                }
                                cb.textDelegate().appendChild(span().css("item-count").text("(" + count + ")").element());
                            })));
        }
    }
}

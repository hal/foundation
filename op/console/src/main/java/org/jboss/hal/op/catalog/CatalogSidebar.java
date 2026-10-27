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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jboss.elemento.IsElement;
import org.jboss.hal.mgt.AccessTypeCounts;
import org.jboss.hal.mgt.BooleanCounts;
import org.jboss.hal.mgt.CatalogFacet;
import org.jboss.hal.mgt.CatalogFacets;
import org.jboss.hal.mgt.DataTypeCounts;
import org.jboss.hal.mgt.ModelGraphType;
import org.jboss.hal.mgt.RestartCounts;
import org.jboss.hal.mgt.StabilityCounts;
import org.jboss.hal.mgt.StorageCounts;
import org.jboss.hal.resources.OuiaIds;
import org.patternfly.component.form.Checkbox;
import org.patternfly.core.OuiaSupport;

import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.fieldset;
import static org.jboss.elemento.Elements.legend;
import static org.jboss.elemento.Elements.li;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.ul;
import static org.patternfly.component.form.Checkbox.checkbox;
import static org.patternfly.layout.stack.Stack.stack;
import static org.patternfly.layout.stack.StackItem.stackItem;

/**
 * Sidebar with type tabs and facet filter checkboxes. Type tabs filter by {@link ModelGraphType}. Facet filters are grouped
 * by facet category (general, resource, attribute, operation) with checkboxes showing counts from the API response.
 */
class CatalogSidebar implements IsElement<HTMLElement>, OuiaSupport<HTMLElement, CatalogSidebar> {

    private final Consumer<ModelGraphType> onTypeSelect;
    private final BiConsumer<CatalogFacet, String> onFacetToggle;
    private final HTMLElement container;
    private final HTMLElement tabList;
    private final HTMLElement filterContainer;
    private final Map<String, HTMLAnchorElement> tabLinks;
    private final Map<CatalogFacet, List<Checkbox>> facetCheckboxes;

    CatalogSidebar(Consumer<ModelGraphType> onTypeSelect, BiConsumer<CatalogFacet, String> onFacetToggle) {
        this.onTypeSelect = onTypeSelect;
        this.onFacetToggle = onFacetToggle;
        this.tabLinks = new LinkedHashMap<>();
        this.facetCheckboxes = new EnumMap<>(CatalogFacet.class);

        tabList = ul().style("list-style", "none")
                .style("padding", "0")
                .style("margin", "0 0 var(--pf-t--global--spacer--md) 0")
                .element();
        addTypeTab("All", null);
        for (ModelGraphType type : ModelGraphType.values()) {
            addTypeTab(capitalize(type.type), type);
        }
        markActive(null);

        filterContainer = div().element();

        container = div().css("pf-v6-c-catalog-sidebar")
                .add(tabList)
                .add(filterContainer)
                .element();
        initOuia();
    }

    @Override
    public String ouiaComponentType() {
        return "halOP/CatalogSidebar";
    }

    @Override
    public CatalogSidebar that() {
        return this;
    }

    @Override
    public HTMLElement element() {
        return container;
    }

    // ------------------------------------------------------ type tabs

    private void addTypeTab(String label, ModelGraphType type) {
        String key = type != null ? type.type : "all";
        HTMLAnchorElement link = a("#").text(label)
                .style("display", "block")
                .style("padding", "var(--pf-t--global--spacer--xs) var(--pf-t--global--spacer--sm)")
                .style("text-decoration", "none")
                .style("border-left", "3px solid transparent")
                .style("color", "var(--pf-t--global--text--color--regular)")
                .element();
        link.addEventListener("click", e -> {
            e.preventDefault();
            markActive(type);
            onTypeSelect.accept(type);
        });
        tabLinks.put(key, link);
        tabList.appendChild(li().add(link).element());
    }

    private void markActive(ModelGraphType type) {
        String activeKey = type != null ? type.type : "all";
        tabLinks.forEach((key, link) -> {
            if (key.equals(activeKey)) {
                link.style.setProperty("border-left-color", "var(--pf-t--global--border--color--brand--default)");
                link.style.setProperty("font-weight", "bold");
            } else {
                link.style.setProperty("border-left-color", "transparent");
                link.style.setProperty("font-weight", "normal");
            }
        });
    }

    // ------------------------------------------------------ facet filters

    void update(CatalogFacets facets, CatalogState state) {
        facetCheckboxes.clear();
        removeChildrenFrom(filterContainer);
        if (facets == null) {
            return;
        }

        HTMLElement stackElement = stack().gutter().element();

        // General facets (always visible)
        addStabilityGroup(stackElement, facets.stability, state);
        addBooleanGroup(stackElement, "Deprecated", CatalogFacet.DEPRECATED, facets.deprecated, state);

        // Type-specific facets based on selected type or facets presence
        addResourceFacets(stackElement, facets, state);
        addAttributeFacets(stackElement, facets, state);
        addOperationFacets(stackElement, facets, state);

        filterContainer.appendChild(stackElement);
    }

    private void addResourceFacets(HTMLElement stackEl, CatalogFacets facets, CatalogState state) {
        if (facets.singleton != null) {
            addBooleanGroup(stackEl, "Singleton", CatalogFacet.SINGLETON, facets.singleton, state);
        }
        if (facets.declaresCap != null) {
            addBooleanGroup(stackEl, "Declares Capability", CatalogFacet.DECLARES_CAP, facets.declaresCap, state);
        }
    }

    private void addAttributeFacets(HTMLElement stackEl, CatalogFacets facets, CatalogState state) {
        if (facets.dataType != null) {
            addDataTypeGroup(stackEl, facets.dataType, state);
        }
        if (facets.accessType != null) {
            addAccessTypeGroup(stackEl, facets.accessType, state);
        }
        if (facets.storage != null) {
            addStorageGroup(stackEl, facets.storage, state);
        }
        if (facets.restart != null) {
            addRestartGroup(stackEl, facets.restart, state);
        }
        if (facets.required != null) {
            addBooleanGroup(stackEl, "Required", CatalogFacet.REQUIRED, facets.required, state);
        }
        if (facets.nillable != null) {
            addBooleanGroup(stackEl, "Nillable", CatalogFacet.NILLABLE, facets.nillable, state);
        }
        if (facets.expressions != null) {
            addBooleanGroup(stackEl, "Expressions", CatalogFacet.EXPRESSIONS, facets.expressions, state);
        }
        if (facets.sensitive != null) {
            addBooleanGroup(stackEl, "Sensitive", CatalogFacet.SENSITIVE, facets.sensitive, state);
        }
    }

    private void addOperationFacets(HTMLElement stackEl, CatalogFacets facets, CatalogState state) {
        if (facets.readOnly != null) {
            addBooleanGroup(stackEl, "Read Only", CatalogFacet.READ_ONLY, facets.readOnly, state);
        }
        if (facets.runtimeOnly != null) {
            addBooleanGroup(stackEl, "Runtime Only", CatalogFacet.RUNTIME_ONLY, facets.runtimeOnly, state);
        }
        if (facets.global != null) {
            addBooleanGroup(stackEl, "Global", CatalogFacet.GLOBAL, facets.global, state);
        }
        if (facets.hasParams != null) {
            addBooleanGroup(stackEl, "Has Parameters", CatalogFacet.HAS_PARAMS, facets.hasParams, state);
        }
        if (facets.hasReturnValue != null) {
            addBooleanGroup(stackEl, "Has Return Value", CatalogFacet.HAS_RETURN_VALUE, facets.hasReturnValue, state);
        }
    }

    // ------------------------------------------------------ specific facet groups

    private void addStabilityGroup(HTMLElement stackEl, StabilityCounts counts, CatalogState state) {
        if (counts == null) {
            return;
        }
        Map<String, Integer> values = new LinkedHashMap<>();
        putIfPositive(values, "default", counts.default_);
        putIfPositive(values, "community", counts.community);
        putIfPositive(values, "preview", counts.preview);
        putIfPositive(values, "experimental", counts.experimental);
        addFacetGroup(stackEl, "Stability", CatalogFacet.STABILITY, values, state);
    }

    private void addDataTypeGroup(HTMLElement stackEl, DataTypeCounts counts, CatalogState state) {
        if (counts == null) {
            return;
        }
        Map<String, Integer> values = new LinkedHashMap<>();
        putIfPositive(values, "string", counts.string);
        putIfPositive(values, "int", counts.int_);
        putIfPositive(values, "long", counts.long_);
        putIfPositive(values, "boolean", counts.boolean_);
        putIfPositive(values, "double", counts.double_);
        putIfPositive(values, "big-decimal", counts.bigDecimal);
        putIfPositive(values, "object", counts.object);
        putIfPositive(values, "list", counts.list);
        putIfPositive(values, "bytes", counts.bytes);
        putIfPositive(values, "property", counts.property);
        addFacetGroup(stackEl, "Data Type", CatalogFacet.DATA_TYPE, values, state);
    }

    private void addAccessTypeGroup(HTMLElement stackEl, AccessTypeCounts counts, CatalogState state) {
        if (counts == null) {
            return;
        }
        Map<String, Integer> values = new LinkedHashMap<>();
        putIfPositive(values, "read-write", counts.readWrite);
        putIfPositive(values, "read-only", counts.readOnly);
        putIfPositive(values, "metric", counts.metric);
        addFacetGroup(stackEl, "Access Type", CatalogFacet.ACCESS_TYPE, values, state);
    }

    private void addStorageGroup(HTMLElement stackEl, StorageCounts counts, CatalogState state) {
        if (counts == null) {
            return;
        }
        Map<String, Integer> values = new LinkedHashMap<>();
        putIfPositive(values, "configuration", counts.configuration);
        putIfPositive(values, "runtime", counts.runtime);
        addFacetGroup(stackEl, "Storage", CatalogFacet.STORAGE, values, state);
    }

    private void addRestartGroup(HTMLElement stackEl, RestartCounts counts, CatalogState state) {
        if (counts == null) {
            return;
        }
        Map<String, Integer> values = new LinkedHashMap<>();
        putIfPositive(values, "no-services", counts.noServices);
        putIfPositive(values, "all-services", counts.allServices);
        putIfPositive(values, "resource-services", counts.resourceServices);
        putIfPositive(values, "jvm", counts.jvm);
        addFacetGroup(stackEl, "Restart", CatalogFacet.RESTART, values, state);
    }

    private void addBooleanGroup(HTMLElement stackEl, String label, CatalogFacet facet, BooleanCounts counts,
            CatalogState state) {
        if (counts == null) {
            return;
        }
        Map<String, Integer> values = new LinkedHashMap<>();
        if (counts.yes > 0) values.put("yes", counts.yes);
        if (counts.no > 0) values.put("no", counts.no);
        addFacetGroup(stackEl, label, facet, values, state);
    }

    // ------------------------------------------------------ generic facet group

    private void addFacetGroup(HTMLElement stackEl, String label, CatalogFacet facet, Map<String, Integer> values,
            CatalogState state) {
        if (values.isEmpty()) {
            return;
        }

        HTMLElement fs = fieldset().css("pf-v6-c-form__fieldset")
                .add(legend().text(label)
                        .style("font-weight", "bold")
                        .style("margin-bottom", "var(--pf-t--global--spacer--xs)"))
                .element();

        List<Checkbox> checkboxes = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            String value = entry.getKey();
            int count = entry.getValue();
            String cbId = facet.key + "-" + value;
            String cbLabel = value + " (" + count + ")";
            Checkbox cb = checkbox(cbId, cbId, cbLabel);
            if (state.isActive(facet, value)) {
                cb.value(true);
            }
            cb.onChange((event, checkbox, checked) -> onFacetToggle.accept(facet, value));
            checkboxes.add(cb);
            fs.appendChild(div().add(cb).element());
        }
        facetCheckboxes.put(facet, checkboxes);

        stackEl.appendChild(stackItem().add(fs).element());
    }

    // ------------------------------------------------------ helpers

    private static void putIfPositive(Map<String, Integer> map, String key, int value) {
        if (value > 0) {
            map.put(key, value);
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}

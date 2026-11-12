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
import org.jboss.hal.ui.component.VerticalTab;
import org.jboss.hal.ui.component.VerticalTabs;
import org.patternfly.component.form.Checkbox;
import org.patternfly.core.OuiaSupport;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.hal.ui.component.VerticalTab.verticalTab;
import static org.jboss.hal.ui.component.VerticalTabs.verticalTabs;
import static org.patternfly.component.form.Checkbox.checkbox;
import static org.patternfly.layout.stack.Stack.stack;
import static org.patternfly.layout.stack.StackItem.stackItem;

/**
 * Sidebar with type tabs and facet filter checkboxes. Type tabs filter by {@link ModelGraphType}. Facet filters are grouped by
 * facet category (general, resource, attribute, operation) with checkboxes showing counts from the API response.
 */
class CatalogSidebar implements IsElement<HTMLElement>, OuiaSupport<HTMLElement, CatalogSidebar> {

    private static final String MODEL_GRAPH_TYPE_KEY = "model-graph-type";

    private final HTMLElement container;
    private final VerticalTabs verticalTabs;
    private final VerticalTab allVerticalTabs;
    private final HTMLElement filterContainer;
    private final BiConsumer<CatalogFacet, String> onFacetToggle;
    private final Map<CatalogFacet, List<Checkbox>> facetCheckboxes;

    CatalogSidebar(Consumer<ModelGraphType> onTypeSelect, BiConsumer<CatalogFacet, String> onFacetToggle) {
        this.onFacetToggle = onFacetToggle;
        this.verticalTabs = verticalTabs();
        this.allVerticalTabs = verticalTab("all", "All");
        this.facetCheckboxes = new EnumMap<>(CatalogFacet.class);

        verticalTabs.addItem(allVerticalTabs);
        verticalTabs.addItems(asList(ModelGraphType.values()), type -> verticalTab(type.type, type.plural)
                .store(MODEL_GRAPH_TYPE_KEY, type));
        verticalTabs.onSelect((e, vt, s) -> {
            ModelGraphType type = vt.get(MODEL_GRAPH_TYPE_KEY);
            onTypeSelect.accept(type);
        });

        filterContainer = div().css("filter-panel-pf").element();
        container = div()
                .add(verticalTabs)
                .add(filterContainer)
                .element();
        initOuia();
    }

    @Override
    public HTMLElement element() {
        return container;
    }

    @Override
    public String ouiaComponentType() {
        return "halOP/CatalogSidebar";
    }

    @Override
    public CatalogSidebar that() {
        return this;
    }

    void resetType() {
        for (VerticalTab verticalTab : verticalTabs.items()) {
            verticalTabs.select(verticalTab, false, false);
        }
    }

    void selectType(ModelGraphType type) {
        if (type != null) {
            VerticalTab vt = verticalTabs.item(type.type);
            if (vt != null) {
                verticalTabs.select(vt, true, false);
            }
        } else {
            verticalTabs.select(allVerticalTabs, true, false);
        }
    }

    // ------------------------------------------------------ facet filters

    void update(CatalogFacets facets, CatalogState state) {
        facetCheckboxes.clear();
        removeChildrenFrom(filterContainer);
        if (facets == null) {
            return;
        }

        HTMLElement stackElement = stack().gutter().element();

        // Global type counts
        int all = facets.type.resource + facets.type.attribute + facets.type.operation + facets.type.capability;
        allVerticalTabs.text("All (" + all + ")");
        for (ModelGraphType type : ModelGraphType.values()) {
            VerticalTab vt = verticalTabs.item(type.type);
            if (vt != null) {
                String count = " (" + switch (type) {
                    case RESOURCE -> String.valueOf(facets.type.resource);
                    case ATTRIBUTE -> String.valueOf(facets.type.attribute);
                    case OPERATION -> String.valueOf(facets.type.operation);
                    case CAPABILITY -> String.valueOf(facets.type.capability);
                    default -> "";
                } + ")";
                vt.text(type.plural + count);
            }
        }

        // General facets
        if (facets.stability != null || facets.deprecated != null) {
            addSectionHeader(stackElement, "General");
            addStabilityGroup(stackElement, facets.stability, state);
            addBooleanGroup(stackElement, "Deprecated", CatalogFacet.DEPRECATED, facets.deprecated, state);
        }

        // Type-specific facets
        if (hasResourceFacets(facets)) {
            addSectionHeader(stackElement, "Resource");
            addResourceFacets(stackElement, facets, state);
        }
        if (hasAttributeFacets(facets)) {
            addSectionHeader(stackElement, "Attribute");
            addAttributeFacets(stackElement, facets, state);
        }
        if (hasOperationFacets(facets)) {
            addSectionHeader(stackElement, "Operation");
            addOperationFacets(stackElement, facets, state);
        }

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
        putIfPositive(values, "big-decimal", counts.bigDecimal());
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
        putIfPositive(values, "read-write", counts.readWrite());
        putIfPositive(values, "read-only", counts.readOnly());
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
        putIfPositive(values, "no-services", counts.noServices());
        putIfPositive(values, "all-services", counts.allServices());
        putIfPositive(values, "resource-services", counts.resourceServices());
        putIfPositive(values, "jvm", counts.jvm);
        addFacetGroup(stackEl, "Restart", CatalogFacet.RESTART, values, state);
    }

    private void addBooleanGroup(HTMLElement stackEl, String label, CatalogFacet facet, BooleanCounts counts,
            CatalogState state) {
        if (counts == null) {
            return;
        }
        Map<String, Integer> values = new LinkedHashMap<>();
        if (counts.yes > 0) {values.put("yes", counts.yes);}
        if (counts.no > 0) {values.put("no", counts.no);}
        addFacetGroup(stackEl, label, facet, values, state);
    }

    // ------------------------------------------------------ generic facet group

    private void addFacetGroup(HTMLElement stackEl, String label, CatalogFacet facet, Map<String, Integer> values,
            CatalogState state) {
        if (values.isEmpty()) {
            return;
        }

        HTMLElement category = div().css("filter-panel-pf-category")
                .add(div().css("filter-panel-pf-category-title").text(label))
                .element();

        HTMLElement items = div().css("filter-panel-pf-category-items").element();
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
            items.appendChild(div().css("filter-panel-pf-category-item").add(cb).element());
        }
        facetCheckboxes.put(facet, checkboxes);
        category.appendChild(items);

        stackEl.appendChild(stackItem().add(category).element());
    }

    // ------------------------------------------------------ helpers

    private static void putIfPositive(Map<String, Integer> map, String key, int value) {
        if (value > 0) {
            map.put(key, value);
        }
    }

    // ------------------------------------------------------ section headers

    private void addSectionHeader(HTMLElement stackEl, String title) {
        stackEl.appendChild(stackItem()
                .add(div().css("filter-panel-pf-section-title").text(title))
                .element());
    }

    private boolean hasResourceFacets(CatalogFacets facets) {
        return facets.singleton != null || facets.declaresCap != null;
    }

    private boolean hasAttributeFacets(CatalogFacets facets) {
        return facets.dataType != null || facets.accessType != null || facets.storage != null
                || facets.restart != null || facets.required != null || facets.nillable != null
                || facets.expressions != null || facets.sensitive != null;
    }

    private boolean hasOperationFacets(CatalogFacets facets) {
        return facets.readOnly != null || facets.runtimeOnly != null || facets.global != null
                || facets.hasParams != null || facets.hasReturnValue != null;
    }
}

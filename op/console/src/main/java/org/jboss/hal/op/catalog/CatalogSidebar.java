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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jboss.elemento.HTMLContainerBuilder;
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
import org.patternfly.core.OuiaSupport;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.nav;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.hal.ui.component.VerticalTab.verticalTab;
import static org.jboss.hal.ui.component.VerticalTabs.verticalTabs;
import static org.patternfly.component.title.Title.title;

/**
 * Sidebar with type tabs and facet filter checkboxes. Type tabs filter by {@link ModelGraphType}. Facet filters are grouped by
 * facet category (general, resource, attribute, operation) with checkboxes showing counts from the API response.
 */
class CatalogSidebar implements IsElement<HTMLElement>, OuiaSupport<HTMLElement, CatalogSidebar> {

    private static final String MODEL_GRAPH_TYPE_KEY = "model-graph-type";

    private final BiConsumer<CatalogFacet, String> onFacetToggle;
    private final VerticalTabs verticalTabs;
    private final VerticalTab allVerticalTabs;
    private final HTMLElement root;
    private final HTMLContainerBuilder<HTMLDivElement> filterPanel;

    CatalogSidebar(Consumer<ModelGraphType> onTypeSelect, BiConsumer<CatalogFacet, String> onFacetToggle) {
        this.onFacetToggle = onFacetToggle;
        this.verticalTabs = verticalTabs();
        this.allVerticalTabs = verticalTab("all", "All");

        verticalTabs.addItem(allVerticalTabs);
        verticalTabs.addItems(asList(ModelGraphType.values()), type -> verticalTab(type.type, type.plural)
                .store(MODEL_GRAPH_TYPE_KEY, type));
        verticalTabs.onSelect((e, vt, s) -> {
            ModelGraphType type = vt.get(MODEL_GRAPH_TYPE_KEY);
            onTypeSelect.accept(type);
        });
        root = nav()
                .add(verticalTabs)
                .add(filterPanel = div().css("filter-panel-pf"))
                .element();
        initOuia();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public String ouiaComponentType() {
        return "halOP/CatalogSidebar";
    }

    @Override
    public CatalogSidebar that() {
        return this;
    }

    // ------------------------------------------------------ update

    void update(CatalogState state, CatalogFacets facets) {
        removeChildrenFrom(filterPanel);
        if (facets == null) {
            return;
        }

        updateTypeCounts(facets);

        // General facets
        if (facets.stability != null || facets.deprecated != null) {
            sectionHeader("General");
            addStabilityGroup(state, facets.stability);
            addBooleanGroup("Deprecated", state, CatalogFacet.DEPRECATED, facets.deprecated);
        }

        // Type-specific facets
        if (hasResourceFacets(facets)) {
            sectionHeader("Resources");
            addResourceFacets(state, facets);
        }
        if (hasAttributeFacets(facets)) {
            sectionHeader("Attributes");
            addAttributeFacets(state, facets);
        }
        if (hasOperationFacets(facets)) {
            sectionHeader("Operations");
            addOperationFacets(state, facets);
        }
    }

    // ------------------------------------------------------ type

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

    void resetType() {
        for (VerticalTab verticalTab : verticalTabs.items()) {
            verticalTabs.select(verticalTab, false, false);
        }
    }

    private void updateTypeCounts(CatalogFacets facets) {
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
                } + ")";
                vt.text(type.plural + count);
            }
        }
    }

    // ------------------------------------------------------ facets

    private void addResourceFacets(CatalogState state, CatalogFacets facets) {
        // @formatter:off
        if (facets.singleton != null) {addBooleanGroup("Singleton", state, CatalogFacet.SINGLETON, facets.singleton);}
        if (facets.declaresCap != null) {addBooleanGroup("Declares Capability", state, CatalogFacet.DECLARES_CAP, facets.declaresCap);}
        // @formatter:on
    }

    private void addAttributeFacets(CatalogState state, CatalogFacets facets) {
        // @formatter:off
        if (facets.dataType != null) {addDataTypeGroup(state, facets.dataType);}
        if (facets.accessType != null) {addAccessTypeGroup(state, facets.accessType);}
        if (facets.storage != null) {addStorageGroup(state, facets.storage);}
        if (facets.restart != null) {addRestartGroup(state, facets.restart);}
        if (facets.required != null) {addBooleanGroup("Required", state, CatalogFacet.REQUIRED, facets.required);}
        if (facets.nillable != null) {addBooleanGroup("Nillable", state, CatalogFacet.NILLABLE, facets.nillable);}
        if (facets.expressions != null) {addBooleanGroup("Expressions", state, CatalogFacet.EXPRESSIONS, facets.expressions);}
        if (facets.sensitive != null) {addBooleanGroup("Sensitive", state, CatalogFacet.SENSITIVE, facets.sensitive);}
        // @formatter:on
    }

    private void addOperationFacets(CatalogState state, CatalogFacets facets) {
        // @formatter:off
        if (facets.readOnly != null) {addBooleanGroup("Read Only", state, CatalogFacet.READ_ONLY, facets.readOnly);}
        if (facets.runtimeOnly != null) {addBooleanGroup("Runtime Only", state, CatalogFacet.RUNTIME_ONLY, facets.runtimeOnly);}
        if (facets.global != null) {addBooleanGroup("Global", state, CatalogFacet.GLOBAL, facets.global);}
        if (facets.hasParams != null) {addBooleanGroup("Has Parameters", state, CatalogFacet.HAS_PARAMS, facets.hasParams);}
        if (facets.hasReturnValue != null) {addBooleanGroup("Has Return Value", state, CatalogFacet.HAS_RETURN_VALUE, facets.hasReturnValue);}
        // @formatter:on
    }

    // ------------------------------------------------------ specific facets

    private void addStabilityGroup(CatalogState state, StabilityCounts counts) {
        if (counts == null) {
            return;
        }
        Map<String, Integer> values = new LinkedHashMap<>();
        putIfPositive(values, "default", counts.default_);
        putIfPositive(values, "community", counts.community);
        putIfPositive(values, "preview", counts.preview);
        putIfPositive(values, "experimental", counts.experimental);
        addFacetGroup("Stability", state, CatalogFacet.STABILITY, values);
    }

    private void addDataTypeGroup(CatalogState state, DataTypeCounts counts) {
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
        addFacetGroup("Data Type", state, CatalogFacet.DATA_TYPE, values);
    }

    private void addAccessTypeGroup(CatalogState state, AccessTypeCounts counts) {
        if (counts == null) {
            return;
        }
        Map<String, Integer> values = new LinkedHashMap<>();
        putIfPositive(values, "read-write", counts.readWrite());
        putIfPositive(values, "read-only", counts.readOnly());
        putIfPositive(values, "metric", counts.metric);
        addFacetGroup("Access Type", state, CatalogFacet.ACCESS_TYPE, values);
    }

    private void addStorageGroup(CatalogState state, StorageCounts counts) {
        if (counts == null) {
            return;
        }
        Map<String, Integer> values = new LinkedHashMap<>();
        putIfPositive(values, "configuration", counts.configuration);
        putIfPositive(values, "runtime", counts.runtime);
        addFacetGroup("Storage", state, CatalogFacet.STORAGE, values);
    }

    private void addRestartGroup(CatalogState state, RestartCounts counts) {
        if (counts == null) {
            return;
        }
        Map<String, Integer> values = new LinkedHashMap<>();
        putIfPositive(values, "no-services", counts.noServices());
        putIfPositive(values, "all-services", counts.allServices());
        putIfPositive(values, "resource-services", counts.resourceServices());
        putIfPositive(values, "jvm", counts.jvm);
        addFacetGroup("Restart", state, CatalogFacet.RESTART, values);
    }

    private void addBooleanGroup(String label, CatalogState state, CatalogFacet facet, BooleanCounts counts) {
        if (counts == null) {
            return;
        }
        Map<String, Integer> values = new LinkedHashMap<>();
        if (counts.yes > 0) {values.put("yes", counts.yes);}
        if (counts.no > 0) {values.put("no", counts.no);}
        addFacetGroup(label, state, facet, values);
    }

    // ------------------------------------------------------ generic facet

    private void addFacetGroup(String label, CatalogState state, CatalogFacet facet, Map<String, Integer> values) {
        if (values.isEmpty()) {
            return;
        }
        FacetCategory facetCategory = new FacetCategory(label, facet, onFacetToggle);
        facetCategory.addValues(state, values);
        filterPanel.add(facetCategory);
    }

    // ------------------------------------------------------ helpers

    private void sectionHeader(String title) {
        filterPanel.add(title(4, title).css("filter-panel-pf-section-title"));
    }

    private void putIfPositive(Map<String, Integer> map, String key, int value) {
        if (value > 0) {
            map.put(key, value);
        }
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

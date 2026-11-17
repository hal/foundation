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

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;
import org.jboss.hal.mgt.AccessTypeCounts;
import org.jboss.hal.mgt.BooleanCounts;
import org.jboss.hal.mgt.CatalogFacet;
import org.jboss.hal.mgt.CatalogFacets;
import org.jboss.hal.mgt.CatalogResponse;
import org.jboss.hal.mgt.DataTypeCounts;
import org.jboss.hal.mgt.ModelGraphType;
import org.jboss.hal.mgt.RestartCounts;
import org.jboss.hal.mgt.StabilityCounts;
import org.jboss.hal.mgt.StorageCounts;
import org.jboss.hal.ui.Format;
import org.jboss.hal.ui.component.VerticalTab;
import org.jboss.hal.ui.component.VerticalTabs;
import org.patternfly.core.OuiaSupport;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.nav;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.modelCatalog;
import static org.jboss.hal.resources.HalClasses.sidebar;
import static org.jboss.hal.ui.component.VerticalTab.verticalTab;
import static org.jboss.hal.ui.component.VerticalTabs.verticalTabs;
import static org.patternfly.component.divider.Divider.divider;
import static org.patternfly.component.divider.DividerType.hr;
import static org.patternfly.component.title.Title.title;

/**
 * Sidebar with type tabs and facet filter checkboxes. Type tabs filter by {@link ModelGraphType}. Facet filters are grouped by
 * facet category (general, resource, attribute, operation) with checkboxes showing counts from the API response.
 */
class CatalogSidebar implements IsElement<HTMLElement>, OuiaSupport<HTMLElement, CatalogSidebar> {

    private static final String MODEL_GRAPH_TYPE_KEY = "model-graph-type";

    private final CatalogController controller;
    private final VerticalTabs verticalTabs;
    private final HTMLElement root;
    private final HTMLContainerBuilder<HTMLDivElement> filterPanel;

    CatalogSidebar(CatalogController controller) {
        this.controller = controller;
        this.verticalTabs = verticalTabs();

        for (ModelGraphType type : ModelGraphType.values()) {
            VerticalTab vt = verticalTab(type.type, type.plural)
                    .store(MODEL_GRAPH_TYPE_KEY, type);
            if (type != ModelGraphType.ALL) {
                vt.style("margin-block-start", 0);
            }
            verticalTabs.addItem(vt);
        }
        verticalTabs.onSelect((e, vt, s) -> {
            ModelGraphType type = vt.get(MODEL_GRAPH_TYPE_KEY);
            controller.selectType(type);
        });
        root = nav().css(halComponent(modelCatalog, sidebar))
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

    // ------------------------------------------------------ reset / update

    void reset(CatalogResponse response) {
        removeChildrenFrom(filterPanel);
        clearTypeSelection();
        updateCounts(response.facets);
    }

    void update(CatalogResponse response) {
        removeChildrenFrom(filterPanel);
        markTypeSelected(controller.state().effectiveType());
        updateCounts(response.facets);
    }

    private void updateCounts(CatalogFacets facets) {
        updateTypeCounts(facets);

        // General facets (always present in the API response)
        addStabilityGroup(facets.stability);
        addBooleanGroup("Deprecated", CatalogFacet.DEPRECATED, facets.deprecated);

        // Type-specific facets (null when the API omits them for the current type filter)
        addFacetSection("Resources", facets, this::addResourceFacets);
        addFacetSection("Attributes", facets, this::addAttributeFacets);
        addFacetSection("Operations", facets, this::addOperationFacets);
    }

    private void addFacetSection(String heading, CatalogFacets facets, FacetAdder adder) {
        int childCountBefore = filterPanel.element().childElementCount;
        adder.addFacets(facets);
        if (filterPanel.element().childElementCount > childCountBefore) {
            HTMLElement dividerElement = divider(hr).css("filter-panel-divider").element();
            HTMLElement headerElement = title(4, heading).css("filter-panel-header").element();
            filterPanel.element().insertBefore(headerElement,
                    filterPanel.element().childNodes.item(childCountBefore));
            filterPanel.element().insertBefore(dividerElement,
                    filterPanel.element().childNodes.item(childCountBefore));
        }
    }

    // ------------------------------------------------------ type

    private void clearTypeSelection() {
        for (VerticalTab item : verticalTabs.items()) {
            verticalTabs.select(item, false, false);
        }
    }

    private void markTypeSelected(ModelGraphType type) {
        VerticalTab vt = verticalTabs.item(type.type);
        if (vt != null) {
            verticalTabs.select(vt, true, false);
        }
    }

    private void updateTypeCounts(CatalogFacets facets) {
        for (ModelGraphType type : ModelGraphType.values()) {
            VerticalTab vt = verticalTabs.item(type.type);
            if (vt != null) {
                int count = switch (type) {
                    case ALL -> facets.type.resource + facets.type.attribute
                            + facets.type.operation + facets.type.capability;
                    case RESOURCE -> facets.type.resource;
                    case ATTRIBUTE -> facets.type.attribute;
                    case OPERATION -> facets.type.operation;
                    case CAPABILITY -> facets.type.capability;
                };
                vt.disabled(count == 0).text(type.plural + " (" + Format.number(count) + ")");
            }
        }
    }

    // ------------------------------------------------------ facets

    private void addResourceFacets(CatalogFacets facets) {
        // @formatter:off
        if (facets.singleton != null) {addBooleanGroup("Singleton", CatalogFacet.SINGLETON, facets.singleton);}
        if (facets.declaresCap != null) {addCapabilityGroup("Declares capability", CatalogFacet.DECLARES_CAP, facets.declaresCap);}
        if (facets.definesAttrGroups != null) {addBooleanGroup("Has attribute groups", CatalogFacet.DEFINES_ATTR_GROUPS, facets.definesAttrGroups);}
        // @formatter:on
    }

    private void addAttributeFacets(CatalogFacets facets) {
        // @formatter:off
        if (facets.dataType != null) {addDataTypeGroup(facets.dataType);}
        Map<String, Integer> units = facets.unitCounts();
        if (!units.isEmpty()) { addFacetGroup("Unit", CatalogFacet.UNIT, units); }
        if (facets.hasAllowed != null) {addBooleanGroup("Allowed values", CatalogFacet.HAS_ALLOWED, facets.hasAllowed);}
        if (facets.refsCap != null) {addCapabilityGroup("References capability", CatalogFacet.REFS_CAP, facets.refsCap);}
        if (facets.expressions != null) {addBooleanGroup("Supports expressions", CatalogFacet.EXPRESSIONS, facets.expressions);}
        if (facets.required != null) {addBooleanGroup("Required", CatalogFacet.REQUIRED, facets.required);}
        if (facets.nillable != null) {addBooleanGroup("Nillable", CatalogFacet.NILLABLE, facets.nillable);}
        if (facets.hasAlternatives != null) {addBooleanGroup("Alternatives", CatalogFacet.HAS_ALTERNATIVES, facets.hasAlternatives);}
        if (facets.hasRequires != null) {addBooleanGroup("Requires", CatalogFacet.HAS_REQUIRES, facets.hasRequires);}
        if (facets.accessType != null) {addAccessTypeGroup(facets.accessType);}
        if (facets.storage != null) {addStorageGroup(facets.storage);}
        if (facets.restart != null) {addRestartGroup(facets.restart);}
        if (facets.sensitive != null) {addBooleanGroup("Sensitive", CatalogFacet.SENSITIVE, facets.sensitive);}
        // @formatter:on
    }

    private void addOperationFacets(CatalogFacets facets) {
        // @formatter:off
        if (facets.hasParams != null) {addBooleanGroup("Has parameters", CatalogFacet.HAS_PARAMS, facets.hasParams);}
        if (facets.hasReturnValue != null) {addBooleanGroup("Has return value", CatalogFacet.HAS_RETURN_VALUE, facets.hasReturnValue);}
        if (facets.global != null) {addBooleanGroup("Global", CatalogFacet.GLOBAL, facets.global);}
        if (facets.readOnly != null) {addBooleanGroup("Read only", CatalogFacet.READ_ONLY, facets.readOnly);}
        if (facets.runtimeOnly != null) {addBooleanGroup("Runtime only", CatalogFacet.RUNTIME_ONLY, facets.runtimeOnly);}
        // @formatter:on
    }

    // ------------------------------------------------------ specific facets

    private void addStabilityGroup(StabilityCounts counts) {
        Map<String, Integer> values = new LinkedHashMap<>();
        putIfPositive(values, "default", counts.default_);
        putIfPositive(values, "community", counts.community);
        putIfPositive(values, "preview", counts.preview);
        putIfPositive(values, "experimental", counts.experimental);
        addFacetGroup("Stability", CatalogFacet.STABILITY, values);
    }

    private void addDataTypeGroup(DataTypeCounts counts) {
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
        putIfPositive(values, "list-simple", counts.listSimple());
        putIfPositive(values, "list-object", counts.listObject());
        putIfPositive(values, "map", counts.map);
        putIfPositive(values, "object", counts.object);
        putIfPositive(values, "bytes", counts.bytes);
        putIfPositive(values, "property", counts.property);
        addFacetGroup("Data Type", CatalogFacet.DATA_TYPE, values);
    }

    private void addAccessTypeGroup(AccessTypeCounts counts) {
        if (counts == null) {
            return;
        }
        Map<String, Integer> values = new LinkedHashMap<>();
        putIfPositive(values, "read-write", counts.readWrite());
        putIfPositive(values, "read-only", counts.readOnly());
        putIfPositive(values, "metric", counts.metric);
        addFacetGroup("Access Type", CatalogFacet.ACCESS_TYPE, values);
    }

    private void addStorageGroup(StorageCounts counts) {
        if (counts == null) {
            return;
        }
        Map<String, Integer> values = new LinkedHashMap<>();
        putIfPositive(values, "configuration", counts.configuration);
        putIfPositive(values, "runtime", counts.runtime);
        addFacetGroup("Storage", CatalogFacet.STORAGE, values);
    }

    private void addRestartGroup(RestartCounts counts) {
        if (counts == null) {
            return;
        }
        Map<String, Integer> values = new LinkedHashMap<>();
        putIfPositive(values, "no-services", counts.noServices());
        putIfPositive(values, "all-services", counts.allServices());
        putIfPositive(values, "resource-services", counts.resourceServices());
        putIfPositive(values, "jvm", counts.jvm);
        addFacetGroup("Restart", CatalogFacet.RESTART, values);
    }

    private void addBooleanGroup(String label, CatalogFacet facet, BooleanCounts counts) {
        if (counts == null) {
            return;
        }
        Map<String, Integer> values = new LinkedHashMap<>();
        if (counts.true_ > 0) {values.put("true", counts.true_);}
        if (counts.false_ > 0) {values.put("false", counts.false_);}
        addFacetGroup(label, facet, values);
    }

    // ------------------------------------------------------ capability facet

    private void addCapabilityGroup(String label, CatalogFacet facet, BooleanCounts counts) {
        if (counts == null) {
            return;
        }
        if (counts.true_ > 0 || counts.false_ > 0) {
            filterPanel.add(new CatalogCapabilityFacetGroup(controller, label, facet, counts));
        }
    }

    // ------------------------------------------------------ generic facet

    private void addFacetGroup(String label, CatalogFacet facet, Map<String, Integer> values) {
        if (values.size() <= 1) {
            return;
        }
        filterPanel.add(new CatalogFacetGroup(controller, label, facet, values));
    }

    // ------------------------------------------------------ helpers

    @FunctionalInterface
    private interface FacetAdder {

        void addFacets(CatalogFacets facets);
    }

    private void putIfPositive(Map<String, Integer> map, String key, int value) {
        if (value > 0) {
            map.put(key, value);
        }
    }
}

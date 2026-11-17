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

import java.util.LinkedHashSet;
import java.util.List;

import org.jboss.hal.mgt.CatalogFacet;
import org.jboss.hal.mgt.FacetValue;
import org.jboss.hal.mgt.ModelGraphType;
import org.patternfly.component.pagination.PageInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogStateTest {

    private CatalogState state;

    @BeforeEach
    void setUp() {
        state = new CatalogState();
    }

    // ------------------------------------------------------ initial state

    @Test
    void initialTypeIsAll() {
        assertEquals(ModelGraphType.ALL, state.effectiveType());
        assertFalse(state.typeSelection().implicit());
    }

    @Test
    void emptyStateHasNoFilters() {
        assertFalse(state.hasActiveFilters());
        assertEquals("", state.buildQuery().toQueryString());
    }

    // ------------------------------------------------------ explicit type

    @Test
    void selectExplicitType() {
        state.selectType(ModelGraphType.ATTRIBUTE);
        assertEquals(ModelGraphType.ATTRIBUTE, state.effectiveType());
        assertFalse(state.typeSelection().implicit());
    }

    @Test
    void selectAllType() {
        state.selectType(ModelGraphType.ATTRIBUTE);
        state.selectType(ModelGraphType.ALL);
        assertEquals(ModelGraphType.ALL, state.effectiveType());
    }

    @Test
    void selectTypeClearsTypeSpecificFacets() {
        state.selectType(ModelGraphType.ATTRIBUTE);
        state.applyFacetValue(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        state.selectType(ModelGraphType.RESOURCE);
        assertFalse(state.isActive(CatalogFacet.REQUIRED));
    }

    // ------------------------------------------------------ facet apply/remove

    @Test
    void applyBoolFacet() {
        state.applyFacetValue(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        assertTrue(state.isActive(CatalogFacet.REQUIRED));
        assertInstanceOf(FacetValue.Bool.class, state.facetValue(CatalogFacet.REQUIRED));
    }

    @Test
    void applyMultiFacet() {
        state.applyFacetValue(CatalogFacet.STABILITY,
                new FacetValue.Multi(new LinkedHashSet<>(List.of("default", "community"))));
        assertTrue(state.isActive(CatalogFacet.STABILITY));
        assertInstanceOf(FacetValue.Multi.class, state.facetValue(CatalogFacet.STABILITY));
    }

    @Test
    void removeFacet() {
        state.applyFacetValue(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        state.removeFacet(CatalogFacet.REQUIRED);
        assertFalse(state.isActive(CatalogFacet.REQUIRED));
        assertNull(state.facetValue(CatalogFacet.REQUIRED));
    }

    // ------------------------------------------------------ implicit type

    @Test
    void typeSpecificFacetImpliesType() {
        state.applyFacetValue(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        assertEquals(ModelGraphType.ATTRIBUTE, state.effectiveType());
        assertTrue(state.typeSelection().implicit());
    }

    @Test
    void removeLastTypeSpecificFacetRemovesImplicitType() {
        state.applyFacetValue(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        state.removeFacet(CatalogFacet.REQUIRED);
        assertEquals(ModelGraphType.ALL, state.effectiveType());
        assertFalse(state.typeSelection().implicit());
    }

    @Test
    void multipleTypeSpecificFacetsKeepImplicitType() {
        state.applyFacetValue(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        state.applyFacetValue(CatalogFacet.STORAGE,
                new FacetValue.Multi(new LinkedHashSet<>(List.of("configuration"))));
        state.removeFacet(CatalogFacet.REQUIRED);
        assertEquals(ModelGraphType.ATTRIBUTE, state.effectiveType());
        assertTrue(state.typeSelection().implicit());
    }

    @Test
    void explicitTypeOverridesImplicit() {
        state.applyFacetValue(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        assertTrue(state.typeSelection().implicit());
        state.selectType(ModelGraphType.ATTRIBUTE);
        assertFalse(state.typeSelection().implicit());
    }

    @Test
    void explicitTypeThenRemoveTypeSpecificKeepsType() {
        state.selectType(ModelGraphType.ATTRIBUTE);
        state.applyFacetValue(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        state.removeFacet(CatalogFacet.REQUIRED);
        assertFalse(state.typeSelection().implicit());
        assertEquals(ModelGraphType.ATTRIBUTE, state.effectiveType());
    }

    @Test
    void secondGroupFacetDoesNotOverwriteImplicitType() {
        state.applyFacetValue(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        ModelGraphType impliedType = state.effectiveType();
        state.applyFacetValue(CatalogFacet.SINGLETON, new FacetValue.Bool(true));
        assertEquals(impliedType, state.effectiveType());
    }

    // ------------------------------------------------------ general facets survive type change

    @Test
    void generalFacetsSurviveTypeChange() {
        state.applyFacetValue(CatalogFacet.STABILITY,
                new FacetValue.Multi(new LinkedHashSet<>(List.of("default"))));
        state.selectType(ModelGraphType.RESOURCE);
        assertTrue(state.isActive(CatalogFacet.STABILITY));
    }

    @Test
    void generalFacetsSurviveTypeChangeToAll() {
        state.selectType(ModelGraphType.ATTRIBUTE);
        state.applyFacetValue(CatalogFacet.DEPRECATED, new FacetValue.Bool(true));
        state.selectType(ModelGraphType.ALL);
        assertTrue(state.isActive(CatalogFacet.DEPRECATED));
    }

    // ------------------------------------------------------ reset

    @Test
    void resetClearsEverything() {
        state.selectType(ModelGraphType.ATTRIBUTE);
        state.query("test");
        state.applyFacetValue(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        state.applyFacetValue(CatalogFacet.STABILITY,
                new FacetValue.Multi(new LinkedHashSet<>(List.of("default"))));

        state.reset();

        assertEquals(ModelGraphType.ALL, state.effectiveType());
        assertFalse(state.typeSelection().implicit());
        assertFalse(state.hasActiveFilters());
        assertEquals("", state.buildQuery().toQueryString());
    }

    @Test
    void resetPreservesPageSize() {
        state.navigate(new PageInfo(2, 100, 500));
        state.reset();
        assertEquals(100, state.pageInfo().pageSize());
    }

    // ------------------------------------------------------ pagination

    @Test
    void navigateUpdatesPageInfo() {
        PageInfo newPage = new PageInfo(3, 50, 200);
        state.navigate(newPage);
        assertEquals(3, state.pageInfo().page());
        assertEquals(50, state.pageInfo().pageSize());
        assertEquals(200, state.pageInfo().total());
    }

    @Test
    void updateTotalPreservesPageAndPageSize() {
        state.navigate(new PageInfo(2, 50, 100));
        state.updateTotal(999);
        assertEquals(2, state.pageInfo().page());
        assertEquals(50, state.pageInfo().pageSize());
        assertEquals(999, state.pageInfo().total());
    }

    // ------------------------------------------------------ isActive with BoolText

    @Test
    void isActiveWithBoolTextTrue() {
        state.applyFacetValue(CatalogFacet.DECLARES_CAP, new FacetValue.BoolText(true, "Yes"));
        assertTrue(state.isActive(CatalogFacet.DECLARES_CAP, "true"));
        assertFalse(state.isActive(CatalogFacet.DECLARES_CAP, "false"));
    }

    @Test
    void isActiveWithBoolTextFalse() {
        state.applyFacetValue(CatalogFacet.DECLARES_CAP, new FacetValue.BoolText(false, "No"));
        assertTrue(state.isActive(CatalogFacet.DECLARES_CAP, "false"));
        assertFalse(state.isActive(CatalogFacet.DECLARES_CAP, "true"));
    }

    // ------------------------------------------------------ isActive edge cases

    @Test
    void isActiveReturnsFalseForInactiveFacet() {
        assertFalse(state.isActive(CatalogFacet.REQUIRED, "true"));
    }

    @Test
    void isActiveMultiMatchesContainedValue() {
        state.applyFacetValue(CatalogFacet.DATA_TYPE,
                new FacetValue.Multi(new LinkedHashSet<>(List.of("string", "int"))));
        assertTrue(state.isActive(CatalogFacet.DATA_TYPE, "string"));
        assertTrue(state.isActive(CatalogFacet.DATA_TYPE, "int"));
        assertFalse(state.isActive(CatalogFacet.DATA_TYPE, "boolean"));
    }

    // ------------------------------------------------------ query

    @Test
    void clearQuery() {
        state.query("connection");
        state.clearQuery();
        assertEquals("", state.buildQuery().toQueryString());
    }

    // ------------------------------------------------------ buildFilter

    @Test
    void buildFilterProducesCorrectQueryString() {
        state.selectType(ModelGraphType.ATTRIBUTE);
        state.applyFacetValue(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        state.query("test");
        String qs = state.buildQuery().toQueryString();
        assertTrue(qs.contains("type=attribute"));
        assertTrue(qs.contains("required=true"));
        assertTrue(qs.contains("q=test"));
    }

    @Test
    void buildFilterBooleanTranslation() {
        state.selectType(ModelGraphType.ATTRIBUTE);
        state.applyFacetValue(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        state.applyFacetValue(CatalogFacet.NILLABLE, new FacetValue.Bool(false));
        String qs = state.buildQuery().toQueryString();
        assertTrue(qs.contains("required=true"));
        assertTrue(qs.contains("nillable=false"));
    }

    @Test
    void buildFilterMultiValueTranslation() {
        state.selectType(ModelGraphType.ATTRIBUTE);
        state.applyFacetValue(CatalogFacet.ACCESS_TYPE,
                new FacetValue.Multi(new LinkedHashSet<>(List.of("read-write", "metric"))));
        String qs = state.buildQuery().toQueryString();
        assertTrue(qs.contains("accessType="));
        assertTrue(qs.contains("read-write"));
        assertTrue(qs.contains("metric"));
    }
}

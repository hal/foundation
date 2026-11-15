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
        assertEquals("", state.buildFilter().toQueryString());
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
        state.apply(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        state.selectType(ModelGraphType.RESOURCE);
        assertFalse(state.isActive(CatalogFacet.REQUIRED));
    }

    // ------------------------------------------------------ facet apply/remove

    @Test
    void applyBoolFacet() {
        state.apply(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        assertTrue(state.isActive(CatalogFacet.REQUIRED));
        assertInstanceOf(FacetValue.Bool.class, state.getValue(CatalogFacet.REQUIRED));
    }

    @Test
    void applyMultiFacet() {
        state.apply(CatalogFacet.STABILITY,
                new FacetValue.Multi(new LinkedHashSet<>(List.of("default", "community"))));
        assertTrue(state.isActive(CatalogFacet.STABILITY));
        assertInstanceOf(FacetValue.Multi.class, state.getValue(CatalogFacet.STABILITY));
    }

    @Test
    void removeFacet() {
        state.apply(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        state.remove(CatalogFacet.REQUIRED);
        assertFalse(state.isActive(CatalogFacet.REQUIRED));
        assertNull(state.getValue(CatalogFacet.REQUIRED));
    }

    // ------------------------------------------------------ implicit type

    @Test
    void typeSpecificFacetImpliesType() {
        state.apply(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        assertEquals(ModelGraphType.ATTRIBUTE, state.effectiveType());
        assertTrue(state.typeSelection().implicit());
    }

    @Test
    void removeLastTypeSpecificFacetRemovesImplicitType() {
        state.apply(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        state.remove(CatalogFacet.REQUIRED);
        assertEquals(ModelGraphType.ALL, state.effectiveType());
        assertFalse(state.typeSelection().implicit());
    }

    @Test
    void multipleTypeSpecificFacetsKeepImplicitType() {
        state.apply(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        state.apply(CatalogFacet.STORAGE,
                new FacetValue.Multi(new LinkedHashSet<>(List.of("configuration"))));
        state.remove(CatalogFacet.REQUIRED);
        assertEquals(ModelGraphType.ATTRIBUTE, state.effectiveType());
        assertTrue(state.typeSelection().implicit());
    }

    @Test
    void explicitTypeOverridesImplicit() {
        state.apply(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        assertTrue(state.typeSelection().implicit());
        state.selectType(ModelGraphType.ATTRIBUTE);
        assertFalse(state.typeSelection().implicit());
    }

    @Test
    void explicitTypeThenRemoveTypeSpecificKeepsType() {
        state.selectType(ModelGraphType.ATTRIBUTE);
        state.apply(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        state.remove(CatalogFacet.REQUIRED);
        assertFalse(state.typeSelection().implicit());
        assertEquals(ModelGraphType.ATTRIBUTE, state.effectiveType());
    }

    @Test
    void secondGroupFacetDoesNotOverwriteImplicitType() {
        state.apply(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        ModelGraphType impliedType = state.effectiveType();
        state.apply(CatalogFacet.SINGLETON, new FacetValue.Bool(true));
        assertEquals(impliedType, state.effectiveType());
    }

    // ------------------------------------------------------ query

    @Test
    void clearAllPreservesQuery() {
        state.query("connection");
        state.apply(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        state.clearAll();
        assertFalse(state.hasActiveFilters());
        assertTrue(state.buildFilter().toQueryString().contains("q=connection"));
    }

    @Test
    void clearQuery() {
        state.query("connection");
        state.clearQuery();
        assertEquals("", state.buildFilter().toQueryString());
    }

    // ------------------------------------------------------ buildFilter

    @Test
    void buildFilterProducesCorrectQueryString() {
        state.selectType(ModelGraphType.ATTRIBUTE);
        state.apply(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        state.query("test");
        String qs = state.buildFilter().toQueryString();
        assertTrue(qs.contains("type=attribute"));
        assertTrue(qs.contains("required=true"));
        assertTrue(qs.contains("q=test"));
    }

    @Test
    void buildFilterBooleanTranslation() {
        state.selectType(ModelGraphType.ATTRIBUTE);
        state.apply(CatalogFacet.REQUIRED, new FacetValue.Bool(true));
        state.apply(CatalogFacet.NILLABLE, new FacetValue.Bool(false));
        String qs = state.buildFilter().toQueryString();
        assertTrue(qs.contains("required=true"));
        assertTrue(qs.contains("nillable=false"));
    }

    @Test
    void buildFilterMultiValueTranslation() {
        state.selectType(ModelGraphType.ATTRIBUTE);
        state.apply(CatalogFacet.ACCESS_TYPE,
                new FacetValue.Multi(new LinkedHashSet<>(List.of("read-write", "metric"))));
        String qs = state.buildFilter().toQueryString();
        assertTrue(qs.contains("accessType="));
        assertTrue(qs.contains("read-write"));
        assertTrue(qs.contains("metric"));
    }
}

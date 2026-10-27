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

import org.jboss.hal.mgt.CatalogFacet;
import org.jboss.hal.mgt.CatalogFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.jboss.hal.mgt.CatalogFacet.*;
import static org.junit.jupiter.api.Assertions.*;

class CatalogStateTest {

    private CatalogState state;

    @BeforeEach
    void setUp() {
        state = new CatalogState();
    }

    // ------------------------------------------------------ basic apply/remove

    @Test
    void emptyStateHasNoFilters() {
        assertFalse(state.hasActiveFilters());
        assertTrue(state.activeTypes().isEmpty());
        assertEquals("", state.currentFilter().toQueryString());
    }

    @Test
    void applyAndRemoveFacet() {
        state.apply(REQUIRED, "yes");
        assertTrue(state.isActive(REQUIRED, "yes"));

        state.remove(REQUIRED, "yes");
        assertFalse(state.isActive(REQUIRED, "yes"));
        assertFalse(state.hasActiveFilters());
    }

    @Test
    void applyMultiValueFacet() {
        state.apply(DATA_TYPE, "string");
        state.apply(DATA_TYPE, "int");
        assertTrue(state.isActive(DATA_TYPE, "string"));
        assertTrue(state.isActive(DATA_TYPE, "int"));

        String qs = state.currentFilter().toQueryString();
        assertTrue(qs.contains("dataType="));
    }

    @Test
    void clearAllPreservesQuery() {
        state.query("connection");
        state.apply(REQUIRED, "yes");
        state.clearAll();

        assertFalse(state.hasActiveFilters());
        String qs = state.currentFilter().toQueryString();
        assertTrue(qs.contains("q=connection"));
    }

    @Test
    void clearQuery() {
        state.query("connection");
        state.clearQuery();
        assertEquals("", state.currentFilter().toQueryString());
    }

    // ------------------------------------------------------ explicit type

    @Test
    void explicitTypeSelection() {
        state.apply(TYPE, "attribute");
        assertFalse(state.isImplicitType());
        assertTrue(state.activeTypes().contains("attribute"));
    }

    @Test
    void removeExplicitTypeRemovesTypeSpecificFacets() {
        state.apply(TYPE, "attribute");
        state.apply(REQUIRED, "yes");
        state.apply(STORAGE, "configuration");

        state.remove(TYPE, "attribute");

        assertFalse(state.isActive(REQUIRED, "yes"));
        assertFalse(state.isActive(STORAGE, "configuration"));
    }

    @Test
    void removeTypeDoesNotAffectOtherGroups() {
        state.apply(TYPE, "attribute");
        state.apply(TYPE, "resource");
        state.apply(REQUIRED, "yes");
        state.apply(SINGLETON, "yes");

        state.remove(TYPE, "attribute");

        assertFalse(state.isActive(REQUIRED, "yes"));
        assertTrue(state.isActive(SINGLETON, "yes"));
    }

    // ------------------------------------------------------ implicit type

    @Test
    void typeSpecificFacetImpliesType() {
        state.apply(REQUIRED, "yes");

        assertTrue(state.isImplicitType());
        assertTrue(state.activeTypes().contains("attribute"));
    }

    @Test
    void removeLastTypeSpecificFacetRemovesImplicitType() {
        state.apply(REQUIRED, "yes");
        state.remove(REQUIRED, "yes");

        assertFalse(state.isImplicitType());
        assertTrue(state.activeTypes().isEmpty());
    }

    @Test
    void multipleTypeSpecificFacetsKeepImplicitType() {
        state.apply(REQUIRED, "yes");
        state.apply(STORAGE, "configuration");

        state.remove(REQUIRED, "yes");

        assertTrue(state.isImplicitType());
        assertTrue(state.activeTypes().contains("attribute"));
    }

    @Test
    void explicitTypeOverridesImplicit() {
        state.apply(REQUIRED, "yes");
        assertTrue(state.isImplicitType());

        state.apply(TYPE, "attribute");
        assertFalse(state.isImplicitType());
    }

    @Test
    void explicitTypeThenRemoveTypeSpecificKeepsType() {
        state.apply(TYPE, "attribute");
        state.apply(REQUIRED, "yes");
        state.remove(REQUIRED, "yes");

        assertFalse(state.isImplicitType());
        assertTrue(state.activeTypes().contains("attribute"));
    }

    // ------------------------------------------------------ query string translation

    @Test
    void booleanFacetTranslation() {
        state.apply(TYPE, "attribute");
        state.apply(REQUIRED, "yes");
        state.apply(NILLABLE, "no");

        String qs = state.currentFilter().toQueryString();
        assertTrue(qs.contains("required=true"));
        assertTrue(qs.contains("nillable=false"));
    }

    @Test
    void multiValueFacetTranslation() {
        state.apply(TYPE, "attribute");
        state.apply(ACCESS_TYPE, "read-write");
        state.apply(ACCESS_TYPE, "metric");

        String qs = state.currentFilter().toQueryString();
        assertTrue(qs.contains("accessType="));
        assertTrue(qs.contains("read-write"));
        assertTrue(qs.contains("metric"));
    }

    @Test
    void operationFacetTranslation() {
        state.apply(TYPE, "operation");
        state.apply(HAS_PARAMS, "yes");
        state.apply(READ_ONLY, "no");

        String qs = state.currentFilter().toQueryString();
        assertTrue(qs.contains("hasParams=true"));
        assertTrue(qs.contains("readOnly=false"));
    }

    @Test
    void resourceFacetTranslation() {
        state.apply(TYPE, "resource");
        state.apply(SINGLETON, "yes");

        String qs = state.currentFilter().toQueryString();
        assertTrue(qs.contains("singleton=true"));
    }
}

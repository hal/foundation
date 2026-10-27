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

import org.jboss.elemento.IsElement;
import org.jboss.hal.mgt.CatalogProperties;
import org.jboss.hal.mgt.CatalogResult;
import org.jboss.hal.mgt.ModelGraphType;
import org.jboss.hal.resources.Identifiers;
import org.jboss.hal.resources.OuiaIds;
import org.patternfly.component.table.Table;
import org.patternfly.component.table.Tr;
import org.patternfly.core.OuiaSupport;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.table.Tbody.tbody;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.Th.th;
import static org.patternfly.component.table.Thead.thead;
import static org.patternfly.component.table.Tr.tr;

/**
 * Dynamic table for catalog search results. Columns adapt based on the effective type: mixed results show generic columns,
 * while single-type results show type-specific properties. The table is rebuilt when the effective type changes; only rows
 * are replaced when the type stays the same.
 */
class CatalogTable implements IsElement<HTMLElement>, OuiaSupport<HTMLElement, CatalogTable> {

    private final HTMLElement container;
    private Table table;
    private ModelGraphType currentType;

    CatalogTable() {
        container = div().element();
        initOuia();
    }

    @Override
    public String ouiaComponentType() {
        return "halOP/CatalogTable";
    }

    @Override
    public CatalogTable that() {
        return this;
    }

    @Override
    public HTMLElement element() {
        return container;
    }

    // ------------------------------------------------------ update

    void update(ModelGraphType effectiveType, CatalogResult[] results) {
        if (effectiveType != currentType) {
            failSafeRemoveFromParent(table);
            table = buildTable(effectiveType);
            container.appendChild(table.element());
            currentType = effectiveType;
        }
        populateRows(effectiveType, results);
    }

    // ------------------------------------------------------ table building

    private Table buildTable(ModelGraphType effectiveType) {
        Table t = table().compact().ouiaId(OuiaIds.PAGE_CATALOG_TABLE);
        if (effectiveType == null) {
            t.addHead(thead().addRow(mixedHeader()));
        } else {
            switch (effectiveType) {
                case RESOURCE -> t.addHead(thead().addRow(resourceHeader()));
                case ATTRIBUTE -> t.addHead(thead().addRow(attributeHeader()));
                case OPERATION -> t.addHead(thead().addRow(operationHeader()));
                case CAPABILITY -> t.addHead(thead().addRow(capabilityHeader()));
            }
        }
        t.addBody(tbody());
        return t;
    }

    private Tr mixedHeader() {
        return tr(Identifiers.CATALOG_THEAD)
                .addItem(th().text("Type"))
                .addItem(th().text("Name"))
                .addItem(th().text("Address"))
                .addItem(th().text("Stability"))
                .addItem(th().text("Deprecated"));
    }

    private Tr resourceHeader() {
        return tr(Identifiers.CATALOG_THEAD)
                .addItem(th().text("Name"))
                .addItem(th().text("Address"))
                .addItem(th().text("Singleton"))
                .addItem(th().text("Declares Capability"))
                .addItem(th().text("Stability"))
                .addItem(th().text("Deprecated"));
    }

    private Tr attributeHeader() {
        return tr(Identifiers.CATALOG_THEAD)
                .addItem(th().text("Name"))
                .addItem(th().text("Resource"))
                .addItem(th().text("Data Type"))
                .addItem(th().text("Access Type"))
                .addItem(th().text("Storage"))
                .addItem(th().text("Required"))
                .addItem(th().text("Nillable"))
                .addItem(th().text("Stability"));
    }

    private Tr operationHeader() {
        return tr(Identifiers.CATALOG_THEAD)
                .addItem(th().text("Name"))
                .addItem(th().text("Resource"))
                .addItem(th().text("Read Only"))
                .addItem(th().text("Runtime Only"))
                .addItem(th().text("Global"))
                .addItem(th().text("Has Params"))
                .addItem(th().text("Stability"));
    }

    private Tr capabilityHeader() {
        return tr(Identifiers.CATALOG_THEAD)
                .addItem(th().text("Name"))
                .addItem(th().text("Address"))
                .addItem(th().text("Stability"))
                .addItem(th().text("Deprecated"));
    }

    // ------------------------------------------------------ row population

    private void populateRows(ModelGraphType effectiveType, CatalogResult[] results) {
        if (table == null || table.tbody() == null) {
            return;
        }
        table.tbody().clear();
        if (results == null || results.length == 0) {
            return;
        }
        for (CatalogResult result : results) {
            Tr row;
            if (effectiveType == null) {
                row = mixedRow(result);
            } else {
                row = switch (effectiveType) {
                    case RESOURCE -> resourceRow(result);
                    case ATTRIBUTE -> attributeRow(result);
                    case OPERATION -> operationRow(result);
                    case CAPABILITY -> capabilityRow(result);
                };
            }
            table.tbody().addRow(row);
        }
    }

    private Tr mixedRow(CatalogResult result) {
        return tr(rowId(result))
                .addItem(td().text(safe(result.type)))
                .addItem(td().text(safe(result.name)))
                .addItem(td().text(safe(result.address)))
                .addItem(td().text(safe(result.stability)))
                .addItem(td().text(yesNo(result.deprecated)));
    }

    private Tr resourceRow(CatalogResult result) {
        CatalogProperties p = result.properties;
        return tr(rowId(result))
                .addItem(td().text(safe(result.name)))
                .addItem(td().text(safe(result.address)))
                .addItem(td().text(p != null ? yesNo(p.isSingleton()) : ""))
                .addItem(td().text(p != null ? yesNo(p.isDeclaresCap()) : ""))
                .addItem(td().text(safe(result.stability)))
                .addItem(td().text(yesNo(result.deprecated)));
    }

    private Tr attributeRow(CatalogResult result) {
        CatalogProperties p = result.properties;
        return tr(rowId(result))
                .addItem(td().text(safe(result.name)))
                .addItem(td().text(safe(result.resourceAddress)))
                .addItem(td().text(p != null ? safe(p.dataType) : ""))
                .addItem(td().text(p != null ? safe(p.accessType) : ""))
                .addItem(td().text(p != null ? safe(p.storage) : ""))
                .addItem(td().text(p != null ? yesNo(p.isRequired()) : ""))
                .addItem(td().text(p != null ? yesNo(p.isNillable()) : ""))
                .addItem(td().text(safe(result.stability)));
    }

    private Tr operationRow(CatalogResult result) {
        CatalogProperties p = result.properties;
        return tr(rowId(result))
                .addItem(td().text(safe(result.name)))
                .addItem(td().text(safe(result.resourceAddress)))
                .addItem(td().text(p != null ? yesNo(p.isReadOnly()) : ""))
                .addItem(td().text(p != null ? yesNo(p.isRuntimeOnly()) : ""))
                .addItem(td().text(p != null ? yesNo(p.isGlobal()) : ""))
                .addItem(td().text(p != null ? yesNo(p.isHasParams()) : ""))
                .addItem(td().text(safe(result.stability)));
    }

    private Tr capabilityRow(CatalogResult result) {
        return tr(rowId(result))
                .addItem(td().text(safe(result.name)))
                .addItem(td().text(safe(result.address)))
                .addItem(td().text(safe(result.stability)))
                .addItem(td().text(yesNo(result.deprecated)));
    }

    // ------------------------------------------------------ helpers

    private static String rowId(CatalogResult result) {
        String id = safe(result.type) + "-" + safe(result.name);
        if (result.address != null) {
            id = id + "-" + Identifiers.fromTemplate(result.address);
        }
        return id;
    }

    private static String safe(String value) {
        return value != null ? value : "";
    }

    private static String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }
}

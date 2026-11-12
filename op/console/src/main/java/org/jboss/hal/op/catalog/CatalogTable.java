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
import org.jboss.hal.env.Stability;
import org.jboss.hal.mgt.CatalogProperties;
import org.jboss.hal.mgt.CatalogResult;
import org.jboss.hal.mgt.ModelGraphType;
import org.jboss.hal.resources.Identifiers;
import org.jboss.hal.resources.OuiaIds;
import org.jboss.hal.ui.UIContext;
import org.patternfly.component.table.Table;
import org.patternfly.component.table.Tr;
import org.patternfly.core.OuiaSupport;
import org.patternfly.style.Classes;
import org.patternfly.style.Variable;
import org.patternfly.style.Variables;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.hal.op.catalog.CatalogBricks.typeLabel;
import static org.jboss.hal.resources.HalClasses.deprecated;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.ui.brick.AttributeBricks.accessTypeTd;
import static org.jboss.hal.ui.brick.AttributeBricks.storageTd;
import static org.jboss.hal.ui.component.StabilityLabel.stabilityLabel;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.table.Tbody.tbody;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.Th.th;
import static org.patternfly.component.table.Thead.thead;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.style.Classes.cell;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.table;
import static org.patternfly.style.Variable.componentVar;
import static org.patternfly.style.Variables.Width;
import static org.patternfly.style.Width.width10;
import static org.patternfly.style.Width.width20;
import static org.patternfly.style.Width.width40;
import static org.patternfly.style.Width.width50;
import static org.patternfly.style.Width.width60;
import static org.patternfly.style.Width.width70;
import static org.patternfly.style.Width.width80;
import static org.patternfly.style.Width.width90;

/**
 * Dynamic table for catalog search results. Columns adapt based on the effective type: mixed results show generic columns,
 * while single-type results show type-specific properties. The table is rebuilt when the effective type changes; only rows are
 * replaced when the type stays the same.
 */
class CatalogTable implements IsElement<HTMLElement>, OuiaSupport<HTMLElement, CatalogTable> {

    private final Stability builtInStability;
    private final HTMLElement container;
    private Table table;
    private ModelGraphType currentType;

    CatalogTable() {
        builtInStability = UIContext.uic().environment().builtInStability();
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
        if (table == null || effectiveType != currentType) {
            failSafeRemoveFromParent(table);
            table = buildTable(effectiveType);
            container.appendChild(table.element());
            currentType = effectiveType;
        }
        populateRows(effectiveType, results);
    }

    // ------------------------------------------------------ table

    private Table buildTable(ModelGraphType effectiveType) {
        Table t = table().ouiaId(OuiaIds.PAGE_CATALOG_TABLE);
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

    private void populateRows(ModelGraphType effectiveType, CatalogResult[] results) {
        if (table == null || table.tbody() == null) {
            return;
        }
        table.tbody().clear();
        if (results == null) {
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

    // ------------------------------------------------------ mixed

    private Tr mixedHeader() {
        return tr(Identifiers.CATALOG_THEAD)
                .addItem(th("type").width(width10).text("Type"))
                .addItem(th("name").width(width20).text("Name"))
                .addItem(th("address").width(width60).text("Address"))
                .addItem(th("stability").width(width10).text("Stability"));
    }

    private Tr mixedRow(CatalogResult result) {
        return tr(rowId(result))
                .addItem(td("Type").add(typeLabel(result.type())))
                .addItem(td("Name").text(safe(result.name))
                        .run(td -> {
                            if (result.deprecated) {
                                td.css(halModifier(deprecated));
                            }
                        }))
                .addItem(td("Address").text(safe(result.address)))
                .addItem(td("Stability").add(stabilityLabel(result.stability(builtInStability)).outline()));
    }

    // ------------------------------------------------------ resource

    private Tr resourceHeader() {
        return tr(Identifiers.CATALOG_THEAD)
                .addItem(th("address").width(width80).text("Address"))
                .addItem(th("singleton")
                        .style(componentVar(component(Classes.table), cell, Width).name, "5%")
                        .text("Singleton"))
                .addItem(th("declares-cap")
                        .style(componentVar(component(Classes.table), cell, Width).name, "5%")
                        .text("Declares capability"))
                .addItem(th("stability").width(width10).text("Stability"));
    }

    private Tr resourceRow(CatalogResult result) {
        CatalogProperties p = result.properties;
        return tr(rowId(result))
                .addItem(td("Address").text(safe(result.address))
                        .run(td -> {
                            if (result.deprecated) {
                                td.css(halModifier(deprecated));
                            }
                        }))
                .addItem(td("Singleton").text(p != null ? yesNo(p.isSingleton()) : ""))
                .addItem(td("Declares capability").text(p != null ? yesNo(p.isDeclaresCap()) : ""))
                .addItem(td("Stability").add(stabilityLabel(result.stability(builtInStability)).outline()));
    }

    // ------------------------------------------------------ attribute

    private Tr attributeHeader() {
        // --pf-v6-c-table--cell--Width
        return tr(Identifiers.CATALOG_THEAD)
                .addItem(th("name").width(width20).text("Name"))
                .addItem(th("res").width(width50).text("Resource"))
                .addItem(th("type").width(width10).text("Data type"))
                .addItem(th("storage")
                        .style(componentVar(component(Classes.table), cell, Width).name, "5%")
                        .text("Storage"))
                .addItem(th("access-type")
                        .style(componentVar(component(Classes.table), cell, Width).name, "5%")
                        .text("Access"))
                .addItem(th("stability").width(width10).text("Stability"));
    }

    private Tr attributeRow(CatalogResult result) {
        CatalogProperties p = result.properties;
        Tr tr = tr(rowId(result))
                .addItem(td("Name").text(safe(result.name))
                        .run(td -> {
                            if (result.deprecated) {
                                td.css(halModifier(deprecated));
                            }
                        }))
                .addItem(td("Resource").text(safe(result.resourceAddress)));
        if (p != null) {
            tr.addItem(td("Data type").text(safe(p.dataType)))
                    .addItem(storageTd("Storage", p.storage))
                    .addItem(accessTypeTd("Access", p.accessType));

        } else {
            tr.addItem(td("Data type").text(" "))
                    .addItem(td("Storage").text(" "))
                    .addItem(td("Access").text(" "));
        }
        tr.addItem(td("Stability").add(stabilityLabel(result.stability(builtInStability)).outline()));
        return tr;
    }

    // ------------------------------------------------------ operation

    private Tr operationHeader() {
        return tr(Identifiers.CATALOG_THEAD)
                .addItem(th("name").width(width20).text("Name"))
                .addItem(th("res").width(width60).text("Resource"))
                .addItem(th("has-params")
                        .style(componentVar(component(Classes.table), cell, Width).name, "5%")
                        .text("Parameter"))
                .addItem(th("has-return-value")
                        .style(componentVar(component(Classes.table), cell, Width).name, "5%")
                        .text("Return value"))
                .addItem(th("stability").width(width10).text("Stability"));
    }

    private Tr operationRow(CatalogResult result) {
        CatalogProperties p = result.properties;
        return tr(rowId(result))
                .addItem(td("Name").text(safe(result.name))
                        .run(td -> {
                            if (result.deprecated) {
                                td.css(halModifier(deprecated));
                            }
                        }))
                .addItem(td("Resource").text(safe(result.resourceAddress)))
                .addItem(td("Parameter").text(p != null ? yesNo(p.isHasParams()) : ""))
                .addItem(td("Return value").text(p != null ? yesNo(p.isHasReturnValue()) : ""))
                .addItem(td("Stability").add(stabilityLabel(result.stability(builtInStability)).outline()));
    }

    // ------------------------------------------------------ capability

    private Tr capabilityHeader() {
        return tr(Identifiers.CATALOG_THEAD)
                .addItem(th("name").width(width90).text("Name"))
                .addItem(th("stability").width(width10).text("Stability"));
    }

    private Tr capabilityRow(CatalogResult result) {
        return tr(rowId(result))
                .addItem(td("Name").text(safe(result.name))
                        .run(td -> {
                            if (result.deprecated) {
                                td.css(halModifier(deprecated));
                            }
                        }))
                .addItem(td("Stability").add(stabilityLabel(result.stability(builtInStability)).outline()));
    }

    // ------------------------------------------------------ helpers

    private String rowId(CatalogResult result) {
        String id = safe(result.type) + "-" + safe(result.name);
        if (result.address != null) {
            id = id + "-" + Identifiers.fromTemplate(result.address);
        }
        return id;
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }
}

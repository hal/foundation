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

import java.util.List;

import org.jboss.elemento.By;
import org.jboss.elemento.IsElement;
import org.jboss.hal.env.Stability;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.mgt.CatalogProperties;
import org.jboss.hal.mgt.CatalogResult;
import org.jboss.hal.mgt.ModelGraphType;
import org.jboss.hal.resources.Identifiers;
import org.jboss.hal.resources.OuiaIds;
import org.jboss.hal.ui.UIContext;
import org.patternfly.component.button.Button;
import org.patternfly.component.table.Table;
import org.patternfly.component.table.Tr;
import org.patternfly.core.OuiaSupport;
import org.patternfly.overlay.Overlay;
import org.patternfly.style.Classes;
import org.patternfly.style.Placement;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.br;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.elemento.Elements.insertAfter;
import static org.jboss.elemento.Elements.li;
import static org.jboss.elemento.Elements.querySelector;
import static org.jboss.elemento.Elements.small;
import static org.jboss.elemento.Elements.ul;
import static org.jboss.hal.mgt.CatalogFacet.DECLARES_CAP;
import static org.jboss.hal.mgt.CatalogFacet.REFS_CAP;
import static org.jboss.hal.resources.HalClasses.deprecated;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.ui.brick.AttributeBricks.accessTypeTd;
import static org.jboss.hal.ui.brick.AttributeBricks.storageTd;
import static org.jboss.hal.ui.brick.ModelBricks.typeLabel;
import static org.jboss.hal.ui.component.StabilityLabel.stabilityLabel;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.table.Tbody.tbody;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.Th.th;
import static org.patternfly.component.table.Thead.thead;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.overlay.Overlay.overlay;
import static org.patternfly.overlay.TriggerMode.click;
import static org.patternfly.style.Classes.cell;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Placement.bottom;
import static org.patternfly.style.Variable.componentVar;
import static org.patternfly.style.Variables.Width;
import static org.patternfly.style.Width.width10;
import static org.patternfly.style.Width.width20;
import static org.patternfly.style.Width.width50;
import static org.patternfly.style.Width.width60;
import static org.patternfly.style.Width.width80;
import static org.patternfly.style.Width.width90;

/**
 * Dynamic table for catalog search results. Columns adapt based on the effective type: mixed results show generic columns,
 * while single-type results show type-specific properties. The table is rebuilt when the effective type changes; only rows are
 * replaced when the type stays the same.
 */
class CatalogTable implements IsElement<HTMLElement>, OuiaSupport<HTMLElement, CatalogTable> {

    private final CatalogController controller;
    private final Stability builtInStability;
    private final HTMLElement container;
    private Table table;
    private ModelGraphType currentType;
    private Overlay currentOverlay;
    private HTMLElement currentOverlayElement;

    CatalogTable(CatalogController controller) {
        this.controller = controller;
        this.builtInStability = UIContext.uic().environment().builtInStability();
        this.container = div().element();
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

    // ------------------------------------------------------ api

    void update(ModelGraphType effectiveType, CatalogResult[] results) {
        if (table == null || effectiveType != currentType) {
            failSafeRemoveFromParent(table);
            table = buildTable(effectiveType);
            container.appendChild(table.element());
            currentType = effectiveType;
        }
        populateRows(effectiveType, results);
    }

    void showPickList(CatalogResult result, List<AddressTemplate> templates) {
        // TODO Find the button element, create and open an overlay to pick a template from the list
        currentOverlayElement = div().css(component(Classes.overlay)).text("Picklist").element();
        HTMLElement linkElement = querySelector(this, By.id(result.id()));
        if (linkElement != null) {
            currentOverlay = overlay(currentOverlayElement, bottom)
                    .distance(10)
                    .trigger(linkElement)
                    .triggerMode(click)
                    .onToggle((event, open) -> {
                        if (!open) {
                            failSafeRemoveFromParent(currentOverlayElement);
                            currentOverlay.detach();
                            currentOverlay = null;
                            currentOverlayElement = null;
                        }
                    });
            insertAfter(currentOverlayElement, linkElement);
            currentOverlay.attach();
            currentOverlay.show();
        }
    }

    // ------------------------------------------------------ table

    private Table buildTable(ModelGraphType effectiveType) {
        Table t = table().css(modifier("sticky-header"))
                .ouiaId(OuiaIds.PAGE_CATALOG_TABLE);
        switch (effectiveType) {
            case ALL -> t.addHead(thead().addRow(mixedHeader()));
            case RESOURCE -> t.addHead(thead().addRow(resourceHeader()));
            case ATTRIBUTE -> t.addHead(thead().addRow(attributeHeader()));
            case OPERATION -> t.addHead(thead().addRow(operationHeader()));
            case CAPABILITY -> t.addHead(thead().addRow(capabilityHeader()));
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
            Tr row = switch (effectiveType) {
                case ALL -> mixedRow(result);
                case RESOURCE -> resourceRow(result);
                case ATTRIBUTE -> attributeRow(result);
                case OPERATION -> operationRow(result);
                case CAPABILITY -> capabilityRow(result);
            };
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
                .addItem(td("Type").add(typeLabel(result.type().type)))
                .addItem(td("Name")
                        .add(link(result))
                        .run(td -> {
                            if (result.deprecated) {
                                td.css(halModifier(deprecated));
                            }
                        }))
                .addItem(td("Address")
                        .run(td -> {
                            if (result.type() == ModelGraphType.RESOURCE) {
                                td.text(safe(result.address));
                            } else {
                                td.text(safe(result.resourceAddress));
                            }
                        }))
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
                .addItem(td("Address")
                        .add(link(result))
                        .run(td -> {
                            if (controller.state().isActive(DECLARES_CAP, "true")
                                    && p.capabilities != null && p.capabilities.length > 0) {
                                td.add(br()).add(small()
                                        .add(ul().css("capabilities").run(ul -> {
                                            for (String capability : p.capabilities) {
                                                ul.add(li().text(capability));
                                            }
                                        })));
                            }
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
                .addItem(td("Name")
                        .add(link(result))
                        .run(td -> {
                            if (controller.state().isActive(REFS_CAP, "true") && p.capabilityReference != null) {
                                td.add(br()).add(small()
                                        .add(ul().css("capabilities")
                                                .add(li().text(p.capabilityReference))));
                            }
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
                .addItem(td("Name")
                        .add(link(result))
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

    private Button link(CatalogResult result) {
        String text = result.type() == ModelGraphType.RESOURCE ? result.resourceAddress : result.name;
        return button(safe(text)).inline().link()
                .id(result.id())
                .onClick((e, b) -> controller.verifyAndGo(result));
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }
}

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jboss.elemento.By;
import org.jboss.elemento.ElementTextMethods;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.env.Stability;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.mgt.CatalogProperties;
import org.jboss.hal.mgt.CatalogResult;
import org.jboss.hal.mgt.DeclaringResource;
import org.jboss.hal.mgt.ModelGraphType;
import org.jboss.hal.resources.Dataset;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.resources.Identifiers;
import org.jboss.hal.resources.Keys;
import org.jboss.hal.resources.OuiaIds;
import org.patternfly.component.button.Button;
import org.patternfly.component.table.Table;
import org.patternfly.component.table.Tbody;
import org.patternfly.component.table.Tr;
import org.patternfly.core.OuiaSupport;
import org.patternfly.overlay.Overlay;
import org.patternfly.style.Classes;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.br;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.elemento.Elements.insertAfter;
import static org.jboss.elemento.Elements.li;
import static org.jboss.elemento.Elements.small;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.strong;
import static org.jboss.elemento.Elements.ul;
import static org.jboss.hal.mgt.CatalogFacet.DECLARES_CAP;
import static org.jboss.hal.mgt.CatalogFacet.REFS_CAP;
import static org.jboss.hal.mgt.ModelGraphType.CAPABILITY;
import static org.jboss.hal.mgt.ModelGraphType.RESOURCE;
import static org.jboss.hal.resources.HalClasses.deprecated;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.resources.HalClasses.items;
import static org.jboss.hal.resources.HalClasses.modelCatalog;
import static org.jboss.hal.resources.HalClasses.noInset;
import static org.jboss.hal.resources.HalClasses.pickList;
import static org.jboss.hal.resources.HalClasses.results;
import static org.jboss.hal.ui.brick.AttributeBricks.accessTypeTd;
import static org.jboss.hal.ui.brick.AttributeBricks.storageTd;
import static org.jboss.hal.ui.brick.ModelBricks.typeLabel;
import static org.jboss.hal.ui.component.StabilityLabel.stabilityLabel;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.table.Tbody.tbody;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.Th.th;
import static org.patternfly.component.table.Thead.thead;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.layout.stack.Stack.stack;
import static org.patternfly.layout.stack.StackItem.stackItem;
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
import static org.patternfly.style.Width.width30;
import static org.patternfly.style.Width.width50;
import static org.patternfly.style.Width.width60;
import static org.patternfly.style.Width.width80;

/**
 * Dynamic table for catalog search results. Columns adapt based on the effective type: mixed results show generic columns,
 * while single-type results show type-specific properties. The table is rebuilt when the effective type changes; only rows are
 * replaced when the type stays the same.
 */
class CatalogTable implements IsElement<HTMLElement>, OuiaSupport<HTMLElement, CatalogTable> {

    private final CatalogController controller;
    private final Stability builtInStability;
    private final Map<String, Button> links;
    private final Map<String, Button> declaringLinks;
    private final Map<String, ElementTextMethods<?, ?>> addressTexts;
    private final HTMLElement container;

    private Table table;
    private Tbody tbody;
    private ModelGraphType currentType;
    private Overlay overlay;
    private HTMLElement overlayElement;

    CatalogTable(CatalogController controller, Stability builtInStability) {
        this.controller = controller;
        this.builtInStability = builtInStability;
        this.links = new HashMap<>();
        this.declaringLinks = new HashMap<>();
        this.addressTexts = new HashMap<>();
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
        links.clear();
        declaringLinks.clear();
        addressTexts.clear();
        if (table == null || effectiveType != currentType) {
            failSafeRemoveFromParent(table);
            table = buildTable(effectiveType);
            container.appendChild(table.element());
            currentType = effectiveType;
        }
        populateRows(effectiveType, results);
    }

    void markVerified(CatalogResult result, VerificationStatus status, AddressTemplate verifiedTemplate,
            String originalAddress) {
        if (tbody != null) {
            if (result.type() == CAPABILITY) {
                Button link = declaringLinks.get(declaringLinkId(result, originalAddress));
                if (link != null) {
                    HTMLElement tr = link.closest(By.element("tr"));
                    adjustLinkAndText(status, verifiedTemplate, link, link);
                    if (tr != null) {
                        tr.dataset.set(Dataset.verified, "");
                    }
                }
            } else {
                Button link = links.get(result.id());
                ElementTextMethods<?, ?> addressText = addressTexts.get(result.id());
                if (link != null) {
                    HTMLElement tr = link.closest(By.element("tr"));
                    adjustLinkAndText(status, verifiedTemplate, link, addressText);
                    if (tr != null) {
                        tr.dataset.set(Dataset.verified, "");
                    }
                }
            }
        }
    }

    private void adjustLinkAndText(VerificationStatus status, AddressTemplate template,
            Button link, ElementTextMethods<?, ?> text) {
        switch (status) {
            case DOES_NOT_EXIST, INVALID, ERROR, UNKNOWN -> link.element().replaceWith(
                    link.element().ownerDocument.createTextNode(link.text()));
            case EXISTS_ONCE -> {
                if (text != null && template != null) {
                    text.text(template.template);
                }
            }
            case EXISTS_MULTIPLE -> { /*nothing to do*/ }
        }
    }

    void showPickList(CatalogResult result, List<AddressTemplate> templates, String originalAddress) {
        Button link = result.type() == CAPABILITY
                ? declaringLinks.get(declaringLinkId(result, originalAddress))
                : links.get(result.id());
        String address = originalAddress != null ? originalAddress : result.resourceAddress;
        if (link != null) {
            overlayElement = div().css(component(Classes.overlay))
                    .add(stack().css(halComponent(pickList))
                            .gutter()
                            .addItem(stackItem()
                                    .add("The address ")
                                    .add(strong(address))
                                    .add(" resolves to multiple resources. Please pick a resource from the list."))
                            .addItem(stackItem().fill()
                                    .add(list().css(halComponent(pickList, items)).plain()
                                            .addItems(templates, template -> listItem()
                                                    .add(button(template.toString()).link().inline()
                                                            .onClick((e, btn) ->
                                                                    controller.goTo(result, template)))))
                            ))
                    .element();
            overlay = overlay(overlayElement, bottom)
                    .distance(10)
                    .trigger(link.element())
                    .triggerMode(click)
                    .onToggle((event, open) -> {
                        if (!open) {
                            failSafeRemoveFromParent(overlayElement);
                            overlay.detach();
                            overlay = null;
                            overlayElement = null;
                        }
                    });
            insertAfter(overlayElement, link.element());
            overlay.attach();
            overlay.show();
        }
    }

    public List<CatalogResult> unverifiedResults() {
        if (tbody != null) {
            return tbody.items().stream()
                    .filter(tr -> !tr.element().dataset.has(Dataset.verified))
                    .map(tr -> tr.<CatalogResult>get(Keys.CATALOG_RESULT))
                    .filter(Objects::nonNull)
                    .collect(toList());
        }
        return emptyList();
    }

    // ------------------------------------------------------ table

    private Table buildTable(ModelGraphType effectiveType) {
        Table table = table().css(modifier("sticky-header"))
                .ouiaId(OuiaIds.PAGE_CATALOG_TABLE);
        switch (effectiveType) {
            case ALL -> table.addHead(thead().addRow(mixedHeader()));
            case RESOURCE -> table.addHead(thead().addRow(resourceHeader()));
            case ATTRIBUTE -> table.addHead(thead().addRow(attributeHeader()));
            case OPERATION -> table.addHead(thead().addRow(operationHeader()));
            case CAPABILITY -> table.addHead(thead().addRow(capabilityHeader()));
        }
        table.addBody(tbody = tbody());
        return table;
    }

    private void populateRows(ModelGraphType effectiveType, CatalogResult[] results) {
        if (table == null || tbody == null) {
            return;
        }
        tbody.clear();
        if (results == null) {
            return;
        }
        for (CatalogResult result : results) {
            if (effectiveType == CAPABILITY) {
                for (Tr row : capabilityRows(result)) {
                    tbody.addRow(row.store(Keys.CATALOG_RESULT, result));
                }
            } else {
                Tr row = switch (effectiveType) {
                    case ALL -> mixedRow(result);
                    case RESOURCE -> resourceRow(result);
                    case ATTRIBUTE -> attributeRow(result);
                    case OPERATION -> operationRow(result);
                    case CAPABILITY -> null;
                };
                if (row != null) {
                    tbody.addRow(row.store(Keys.CATALOG_RESULT, result));
                }
            }
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
                        .run(td -> {
                            if (result.type() == CAPABILITY) {
                                td.text(safe(result.name));
                            } else {
                                td.add(link(result, false));
                            }
                            if (result.deprecated) {
                                td.css(halModifier(deprecated));
                            }
                        }))
                .addItem(td("Address")
                        .run(td -> {
                            if (result.type() == CAPABILITY && result.properties.declaringResources != null) {
                                td.add(list().plain()
                                        .addItems(asList(result.properties.declaringResources), dr ->
                                                listItem().add(declaringResourceLink(result, dr.address))));
                            } else {
                                td.add(addressText(result));
                            }
                        }))
                .addItem(td("Stability")
                        .run(td -> {
                            if (result.type() == CAPABILITY) {
                                td.text(" ");
                            } else {
                                td.add(stabilityLabel(result.stability(builtInStability)).outline());
                            }
                        }));
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
                        .add(link(result, true))
                        .run(td -> {
                            if (controller.state().isActive(DECLARES_CAP, "true")
                                    && p.capabilities != null && p.capabilities.length > 0) {
                                td.add(br()).add(capabilities(p.capabilities));
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
                        .add(link(result, false))
                        .run(td -> {
                            if (controller.state().isActive(REFS_CAP, "true") && p.capabilityReference != null) {
                                td.add(br()).add(capabilities(new String[]{p.capabilityReference}));
                            }
                            if (result.deprecated) {
                                td.css(halModifier(deprecated));
                            }
                        }))
                .addItem(td("Resource").add(addressText(result)));
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
                        .add(link(result, false))
                        .run(td -> {
                            if (result.deprecated) {
                                td.css(halModifier(deprecated));
                            }
                        }))
                .addItem(td("Resource").add(addressText(result)))
                .addItem(td("Parameter").text(p != null ? yesNo(p.isHasParams()) : ""))
                .addItem(td("Return value").text(p != null ? yesNo(p.isHasReturnValue()) : ""))
                .addItem(td("Stability").add(stabilityLabel(result.stability(builtInStability)).outline()));
    }

    // ------------------------------------------------------ capability

    private Tr capabilityHeader() {
        return tr(Identifiers.CATALOG_THEAD)
                .addItem(th("name").width(width30).text("Name"))
                .addItem(th("res").width(width60).text("Declaring resources"))
                .addItem(th("stability").width(width10).text("Stability"));
    }

    private List<Tr> capabilityRows(CatalogResult result) {
        CatalogProperties p = result.properties;
        List<Tr> rows = new ArrayList<>();
        if (p.declaringResources != null) {
            int index = 0;
            for (DeclaringResource declaringResource : p.declaringResources) {
                rows.add(capabilityRow(index, result, declaringResource));
                index++;
            }
        } else {
            rows.add(capabilityRow(0, result, null));
        }
        return rows;
    }

    private Tr capabilityRow(int index, CatalogResult result, DeclaringResource declaringResource) {
        Tr tr = tr(rowId(result, declaringResource));
        if (index == 0) {
            tr.addItem(td("Name").text(safe(result.name))
                    .run(td -> {
                        td.element().rowSpan = result.properties.declaringResources.length;
                        if (result.deprecated) {
                            td.css(halModifier(deprecated));
                        }
                    }));
        }
        tr.addItem(td("Declaring resources")
                .run(td -> {
                    if (index > 0) {
                        td.css(modifier(noInset));
                    }
                })
                .add(declaringResourceLink(result, declaringResource.address)));
        tr.addItem(td("Stability").add(stabilityLabel(declaringResource.stability(builtInStability)).outline()));

        return tr;
    }

    // ------------------------------------------------------ helpers

    private String rowId(CatalogResult result) {
        return Id.build("row", result.id());
    }

    private String rowId(CatalogResult result, DeclaringResource dr) {
        return Id.build("row", result.id(), Identifiers.fromTemplate(dr.address));
    }

    private Button link(CatalogResult result, boolean addressText) {
        String text = result.type() == RESOURCE && controller.state().effectiveType() == RESOURCE
                ? result.resourceAddress
                : result.name;
        Button button = button(safe(text)).inline().link()
                .onClick((e, b) -> controller.verifyAndGo(result));
        links.put(result.id(), button);
        if (addressText) {
            addressTexts.put(result.id(), button);
        }
        return button;
    }

    private ElementTextMethods<?, ?> addressText(CatalogResult result) {
        HTMLContainerBuilder<HTMLElement> span = span().text(safe(result.resourceAddress));
        addressTexts.put(result.id(), span);
        return span;
    }

    private Button declaringResourceLink(CatalogResult capability, String resource) {
        Button button = button(safe(resource)).inline().link()
                .onClick((e, b) -> controller.verifyAndGo(capability, resource));
        declaringLinks.put(declaringLinkId(capability, resource), button);
        return button;
    }

    private String declaringLinkId(CatalogResult capability, String resource) {
        return capability.id() + ":" + resource;
    }

    private HTMLContainerBuilder<HTMLElement> capabilities(String[] capabilities) {
        return small().add(ul().css(halComponent(modelCatalog, results, HalClasses.capabilities))
                .run(ul -> {
                    for (String capability : capabilities) {
                        ul.add(li().text(capability));
                    }
                }));
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }
}

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
package org.jboss.hal.ui.resource;

import java.util.function.BiConsumer;

import org.jboss.elemento.IsElement;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.ui.resource.table.AttributesTable;
import org.jboss.hal.ui.resource.table.CapabilitiesTable;
import org.jboss.hal.ui.resource.table.OperationsTable;
import org.patternfly.component.tabs.Tabs;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.ui.resource.data.ResourceData.resourceData;
import static org.patternfly.component.tabs.Tab.tab;
import static org.patternfly.component.tabs.TabContent.tabContent;
import static org.patternfly.component.tabs.Tabs.tabs;
import static org.patternfly.style.Classes.util;

/**
 * Tab container presenting multiple perspectives on a WildFly management resource.
 * <p>
 * Assembles four tabs:
 * <dl>
 * <dt>Data</dt>
 * <dd>View and edit resource attribute values via {@link org.jboss.hal.ui.resource.data.ResourceData}.</dd>
 * <dt>Attributes</dt>
 * <dd>Read-only metadata table of attribute descriptions (shown only when the resource has attributes).</dd>
 * <dt>Operations</dt>
 * <dd>Filterable operations table with execute buttons.</dd>
 * <dt>Capabilities</dt>
 * <dd>Table of capabilities declared by the resource.</dd>
 * </dl>
 * Supports optional initial tab selection and tab-change callbacks via builder methods.
 * The element is constructed lazily on the first call to {@link #element()}.
 */
public class ResourceTabs implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    /** Creates a new tabbed view for the given resource. */
    public static ResourceTabs resourceTabs(AddressTemplate template, Metadata metadata) {
        return new ResourceTabs(template, metadata);
    }

    // ------------------------------------------------------ instance

    private final AddressTemplate template;
    private final Metadata metadata;
    private String initialSelection;
    private BiConsumer<String, Boolean> onSelect;
    private HTMLElement root;

    ResourceTabs(AddressTemplate template, Metadata metadata) {
        this.template = template;
        this.metadata = metadata;
    }

    @Override
    public HTMLElement element() {
        if (root == null) {
            root = build();
        }
        return root;
    }

    // ------------------------------------------------------ builder

    /** Sets the initially selected tab by its identifier. */
    public ResourceTabs initialSelection(String tabId) {
        this.initialSelection = tabId;
        return this;
    }

    /** Registers a callback invoked when a tab is selected. Receives the tab identifier and selection state. */
    public ResourceTabs onSelect(BiConsumer<String, Boolean> onSelect) {
        this.onSelect = onSelect;
        return this;
    }

    // ------------------------------------------------------ internal

    private HTMLElement build() {
        Tabs tbs = tabs()
                .addItem(tab("data", "Data")
                        .addContent(tabContent().css(util("pt-md"))
                                .add(resourceData(template, metadata))))
                .run(t -> {
                    if (!metadata.resourceDescription().attributes().isEmpty()) {
                        t.addItem(tab("attributes", "Attributes")
                                .addContent(tabContent().css(util("pt-md"))
                                        .add(new AttributesTable(metadata))));
                    }
                })
                .addItem(tab("operations", "Operations")
                        .addContent(tabContent().css(util("pt-md"))
                                .add(new OperationsTable(template, metadata))))
                .addItem(tab("capabilities", "Capabilities")
                        .addContent(tabContent()
                                .add(new CapabilitiesTable(metadata))));

        if (initialSelection != null) {
            tbs.initialSelection(initialSelection);
        }
        if (onSelect != null) {
            tbs.onSelect((e, tab, selected) -> onSelect.accept(tab.identifier(), selected));
        }
        return tbs.element();
    }
}

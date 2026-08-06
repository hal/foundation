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
package org.jboss.hal.ui.resource.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.jboss.elemento.IsElement;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.OuiaIds;
import org.jboss.hal.ui.resource.table.AttributesTable;
import org.jboss.hal.ui.resource.table.CapabilitiesTable;
import org.jboss.hal.ui.resource.table.OperationsTable;
import org.patternfly.component.tabs.Tabs;
import org.patternfly.core.OuiaSupport;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.ui.resource.shell.ResourceData.resourceData;
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
 * <dd>View and edit resource attribute values via {@link org.jboss.hal.ui.resource.shell.ResourceData}.</dd>
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
public class ResourceTabs implements IsElement<HTMLElement>, OuiaSupport<HTMLElement, ResourceTabs> {

    // ------------------------------------------------------ factory

    /** Creates a new tabbed view for the given resource. */
    public static ResourceTabs resourceTabs(AddressTemplate template, Metadata metadata) {
        return new ResourceTabs(template, metadata);
    }

    // ------------------------------------------------------ instance

    public static final String DATA_TAB = "resource-tabs-data";
    public static final String ATTRIBUTES_TAB = "resource-tabs-attributes";
    public static final String OPERATIONS_TAB = "resource-tabs-operations";
    public static final String CAPABILITIES_TAB = "resource-tabs-capabilities";

    record TabDescriptor(String id, String title, Supplier<HTMLElement> content, String css) {}

    private final List<TabDescriptor> descriptors;
    private String initialSelection;
    private BiConsumer<String, Boolean> onSelect;
    private HTMLElement root;

    ResourceTabs(AddressTemplate template, Metadata metadata) {
        this.descriptors = new ArrayList<>();
        this.descriptors.add(new TabDescriptor(DATA_TAB, "Data",
                () -> resourceData(template, metadata).element(), util("pt-md")));
        if (!metadata.resourceDescription().attributes().isEmpty()) {
            this.descriptors.add(new TabDescriptor(ATTRIBUTES_TAB, "Attributes",
                    () -> new AttributesTable(metadata).element(), util("pt-md")));
        }
        this.descriptors.add(new TabDescriptor(OPERATIONS_TAB, "Operations",
                () -> new OperationsTable(template, metadata).element(), util("pt-md")));
        this.descriptors.add(new TabDescriptor(CAPABILITIES_TAB, "Capabilities",
                () -> new CapabilitiesTable(metadata).element(), null));
    }

    @Override
    public String ouiaComponentType() {
        return "halOP/ResourceTabs";
    }

    @Override
    public ResourceTabs that() {
        return this;
    }

    @Override
    public HTMLElement element() {
        if (root == null) {
            root = build();
            initOuia(OuiaIds.RESOURCE_TABS);
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

    /** Adds a tab at the end. Must be called before {@link #element()}. */
    public ResourceTabs addTab(String id, String title, HTMLElement element) {
        descriptors.add(new TabDescriptor(id, title, () -> element, util("pt-md")));
        return this;
    }

    /** Adds a tab at the given position. The index is clamped to the valid range. Must be called before {@link #element()}. */
    public ResourceTabs addTab(int index, String id, String title, HTMLElement element) {
        int clamped = Math.max(0, Math.min(index, descriptors.size()));
        descriptors.add(clamped, new TabDescriptor(id, title, () -> element, util("pt-md")));
        return this;
    }

    /** Removes a tab by its identifier. No-op if the identifier is not found. Must be called before {@link #element()}. */
    public ResourceTabs removeTab(String id) {
        descriptors.removeIf(d -> d.id.equals(id));
        return this;
    }

    /** Replaces the content of an existing tab. No-op if the identifier is not found. Must be called before {@link #element()}. */
    public ResourceTabs replaceTab(String id, HTMLElement element) {
        for (int i = 0; i < descriptors.size(); i++) {
            TabDescriptor d = descriptors.get(i);
            if (d.id.equals(id)) {
                descriptors.set(i, new TabDescriptor(d.id, d.title, () -> element, d.css));
                break;
            }
        }
        return this;
    }

    // ------------------------------------------------------ internal

    private HTMLElement build() {
        Tabs tbs = tabs();
        for (TabDescriptor descriptor : descriptors) {
            var tc = tabContent();
            if (descriptor.css != null) {
                tc.css(descriptor.css);
            }
            tbs.addItem(tab(descriptor.id, descriptor.title)
                    .addContent(tc.add(descriptor.content.get())));
        }

        if (initialSelection != null) {
            tbs.initialSelection(initialSelection);
        }
        if (onSelect != null) {
            tbs.onSelect((e, tab, selected) -> onSelect.accept(tab.identifier(), selected));
        }
        return tbs.element();
    }
}

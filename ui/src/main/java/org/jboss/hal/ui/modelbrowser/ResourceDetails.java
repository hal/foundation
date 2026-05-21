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
package org.jboss.hal.ui.modelbrowser;

import org.jboss.elemento.IsElement;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.ui.resource.ResourceManager.resourceManager;
import static org.patternfly.component.tabs.Tab.tab;
import static org.patternfly.component.tabs.TabContent.tabContent;
import static org.patternfly.component.tabs.Tabs.tabs;
import static org.patternfly.style.Classes.util;

class ResourceDetails implements IsElement<HTMLElement> {

    private final HTMLElement root;

    ResourceDetails(ModelBrowserNode mbn, Metadata metadata) {
        this.root = tabs()
                .ouiaId(Ids.MODEL_BROWSER_TABS)
                .initialSelection(ModelBrowserDetail.lastTab)
                .addItem(tab("data", "Data")
                        .ouiaId(Ids.MODEL_BROWSER_TAB_DATA)
                        .addContent(tabContent().css(util("pt-md"))
                                .add(resourceManager(mbn.template, metadata))))
                .run(tbs -> {
                    if (!metadata.resourceDescription().attributes().isEmpty()) {
                        tbs.addItem(tab("attributes", "Attributes")
                                .ouiaId(Ids.MODEL_BROWSER_TAB_ATTRIBUTES)
                                .addContent(tabContent().css(util("pt-md"))
                                        .add(new AttributesTable(metadata))));
                    }
                })
                .addItem(tab("operations", "Operations")
                        .ouiaId(Ids.MODEL_BROWSER_TAB_OPERATIONS)
                        .addContent(tabContent().css(util("pt-md"))
                                .add(new OperationsTable(mbn.template, metadata))))
                .addItem(tab("capabilities", "Capabilities")
                        .ouiaId(Ids.MODEL_BROWSER_TAB_CAPABILITIES)
                        .addContent(tabContent()
                                .add(new CapabilitiesTable(metadata))))
                .onSelect((e, tab, selected) -> ModelBrowserDetail.lastTab = tab.identifier())
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}

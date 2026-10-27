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

import java.util.function.Consumer;

import org.jboss.elemento.IsElement;
import org.jboss.hal.resources.OuiaIds;
import org.patternfly.component.textinputgroup.TextInputGroup;
import org.patternfly.component.toolbar.Toolbar;

import elemental2.dom.HTMLElement;
import elemental2.dom.KeyboardEvent;

import static org.jboss.elemento.Elements.span;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.textinputgroup.TextInputGroup.textInputGroup;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.icon.IconSets.fas.magnifyingGlass;
import static org.patternfly.icon.IconSets.fas.xmark;
import static org.patternfly.style.Classes.modifier;

/**
 * Toolbar with a search input and a result count label. Fires a search callback when the user presses Enter or clicks the
 * search icon, and a clear callback when the user clears the input.
 */
class CatalogToolbar implements IsElement<HTMLElement> {

    private final Toolbar toolbar;
    private final TextInputGroup searchInput;
    private final HTMLElement resultCount;

    CatalogToolbar(Consumer<String> onSearch, Runnable onClear) {
        searchInput = textInputGroup("catalog-search")
                .placeholder("Search catalog...")
                .onKeydown((event, component, value) -> {
                    if ("Enter".equals(((KeyboardEvent) event).key)) {
                        onSearch.accept(value);
                    }
                });

        resultCount = span().style("white-space", "nowrap").element();

        toolbar = toolbar().ouiaId(OuiaIds.PAGE_CATALOG_TOOLBAR)
                .addContent(toolbarContent()
                        .addGroup(toolbarGroup().css(modifier("filter-group"))
                                .addItem(toolbarItem().css(modifier("search-filter"))
                                        .add(searchInput)))
                        .addGroup(toolbarGroup().css(modifier("action-group"))
                                .addItem(toolbarItem()
                                        .add(button().primary().icon(magnifyingGlass()).text("Search")
                                                .onClick((e, c) -> onSearch.accept(searchInput.value()))))
                                .addItem(toolbarItem()
                                        .add(button().secondary().icon(xmark()).text("Clear")
                                                .onClick((e, c) -> {
                                                    searchInput.value("");
                                                    onClear.run();
                                                }))))
                        .addGroup(toolbarGroup().css(modifier("align-end"))
                                .addItem(toolbarItem().add(resultCount))));
    }

    @Override
    public HTMLElement element() {
        return toolbar.element();
    }

    void updateResultCount(int total, int limit) {
        if (total == 0) {
            resultCount.textContent = "No results";
        } else {
            resultCount.textContent = "Showing " + Math.min(total, limit) + " of " + total;
        }
    }
}

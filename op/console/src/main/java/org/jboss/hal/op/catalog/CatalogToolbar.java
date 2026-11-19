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

import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.resources.OuiaIds;
import org.patternfly.component.button.Button;
import org.patternfly.component.pagination.PageInfo;
import org.patternfly.component.pagination.Pagination;
import org.patternfly.component.textinputgroup.SearchInputGroup;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.core.Timeouts;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.clearTimeout;
import static elemental2.dom.DomGlobal.setTimeout;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Key.Enter;
import static org.jboss.hal.resources.OuiaIds.SEARCH_FILTER;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.pagination.Pagination.pagination;
import static org.patternfly.component.textinputgroup.SearchInputGroup.searchInputGroup;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarGroupType.actionGroup;
import static org.patternfly.component.toolbar.ToolbarGroupType.filterGroup;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.icon.IconSets.fas.magnifyingGlass;
import static org.patternfly.style.Classes.modifier;

/**
 * Toolbar with a search input and a result count label. Fires a search callback when the user presses Enter or clicks the
 * search icon, and a clear callback when the user clears the input.
 */
class CatalogToolbar implements IsElement<HTMLElement> {

    private final Toolbar toolbar;
    private final SearchInputGroup searchInput;
    private final Pagination pagination;
    private final Button verify;
    private double handle;

    CatalogToolbar(CatalogController controller) {
        searchInput = searchInputGroup(Id.unique())
                .ouiaId(SEARCH_FILTER)
                .icon(magnifyingGlass())
                .placeholder("Search model...")
                .onKeyup((event, textInputGroup, value) -> {
                    if (Enter.match(event)) {
                        controller.search(value);
                    }
                })
                .onClear((event, textInputGroup) -> controller.clearSearch());
        searchInput.input().apply(input -> input.autocomplete = "off");
        pagination = pagination(controller.state().pageInfo())
                .onChange((e, p, __, pi) -> controller.navigate(pi));
        toolbar = toolbar().ouiaId(OuiaIds.PAGE_CATALOG_TOOLBAR)
                .addContent(toolbarContent()
                        // .run(tc -> tc.containerDelegate().attributeStyleMap.set("align-items", "center"))
                        .addGroup(toolbarGroup(filterGroup)
                                .addItem(toolbarItem().css(modifier("search-filter"))
                                        .add(searchInput)))
                        .addDivider()
                        .addGroup(toolbarGroup(actionGroup)
                                .addItem(toolbarItem().add(button().secondary().text("Clear all filters")
                                        .onClick((e, c) -> controller.reset())))
                                .addItem(toolbarItem().add(verify = button().secondary()
                                        .text("Verify on server")
                                        .progress(false, "Verifying resources")
                                        .onClick((e, c) -> controller.verifyOnServer()))))
                        .addGroup(toolbarGroup().css(modifier("align-end"))
                                .addItem(toolbarItem().add(pagination))));
    }

    @Override
    public HTMLElement element() {
        return toolbar.element();
    }

    void startVerify() {
        handle = setTimeout(__ -> {
            verify.disabled();
            verify.startProgress();
        }, Timeouts.LOADING_TIMEOUT);
    }

    void verifyDone() {
        clearTimeout(handle);
        verify.stopProgress();
        verify.disabled(false);
    }

    void reset() {
        searchInput.value("", false);
        verify.stopProgress();
        verify.disabled();
        setVisible(pagination, false);
    }

    void update(PageInfo pageInfo) {
        verify.disabled(false);
        setVisible(pagination, true);
        pagination.pageInfo(pageInfo);
    }
}

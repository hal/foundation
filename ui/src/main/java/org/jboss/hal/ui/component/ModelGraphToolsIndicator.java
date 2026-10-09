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
package org.jboss.hal.ui.component;

import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.resources.Urls;
import org.patternfly.component.menu.MenuToggleType;
import org.patternfly.component.menu.SingleSelect;
import org.patternfly.core.OuiaSupport;
import org.patternfly.layout.flex.FlexItem;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.closest;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.icon.Icon.icon;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuGroup.menuGroup;
import static org.patternfly.component.menu.MenuToggle.menuToggle;
import static org.patternfly.component.menu.SingleSelect.singleSelect;
import static org.patternfly.component.menu.SingleSelectMenu.singleSelectMenu;
import static org.patternfly.icon.IconSets.rhUi.ban;
import static org.patternfly.icon.IconSets.rhUi.checkCircle;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.FlexShorthand._1;
import static org.patternfly.layout.flex.Gap.sm;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.menu;
import static org.patternfly.style.Classes.search;
import static org.patternfly.style.Placement.bottomEnd;
import static org.patternfly.style.Status.danger;
import static org.patternfly.style.Status.success;
import static org.patternfly.token.Token.globalIconColorDisabled;

/**
 * Masthead indicator that shows whether a matching model graph tools (MGT) sidecar container is running. Calls
 * {@link org.jboss.hal.core.mgt.ModelGraphTools#ping()} directly to check availability.
 */
public class ModelGraphToolsIndicator implements IsElement<HTMLElement>, OuiaSupport<HTMLElement, ModelGraphToolsIndicator> {

    // ------------------------------------------------------ factory

    private static ModelGraphToolsIndicator instance;

    public static ModelGraphToolsIndicator modelGraphToolsIndicator() {
        if (instance == null) {
            instance = new ModelGraphToolsIndicator();
        }
        return instance;
    }

    // ------------------------------------------------------ instance

    @SuppressWarnings("LineLength")
    private static final String MGT_SVG = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 192 192" width="1em" height="1em" stroke="currentColor" fill="currentColor" role="img" aria-hidden="true" data-icon-name="Model graph tools logo" style="transform: translateY(2px);">\
            <line x1="121.7" y1="69.3" x2="149.2" y2="41.9" stroke-width="13.5"/>\
            <line x1="92.1" y1="132.3" x2="90.0" y2="154.5" stroke-width="13.0"/>\
            <line x1="76.8" y1="63.6" x2="69.0" y2="50.2" stroke-width="12" stroke-linecap="round"/>\
            <line x1="61.5" y1="110.0" x2="50.5" y2="114.7" stroke-width="12" stroke-linecap="round"/>\
            <line x1="128.7" y1="111.9" x2="151.0" y2="123.0" stroke-width="13.5" stroke-linecap="round"/>\
            <circle cx="95.5" cy="95.5" r="37" fill="none" stroke-width="21"/>\
            <circle cx="164.0" cy="27.0" r="21" fill="none" stroke-width="12"/>\
            <circle cx="88.5" cy="170.5" r="16.1" fill="none" stroke-width="8.8"/>\
            <circle cx="47.5" cy="20.5" r="20.2"/>\
            <circle cx="20.0" cy="130.0" r="20.2"/>\
            <circle cx="184.5" cy="143.5" r="20.2"/>\
            </svg>""";

    private final HTMLElement iconElement;
    private final SingleSelect singleSelect;
    private final FlexItem statusIconContainer;
    private final FlexItem statusTextContainer;

    ModelGraphToolsIndicator() {
        String refreshId = Id.unique("model-graph-tools-indicator", "refresh");
        this.iconElement = span()
                .html(SafeHtmlUtils.fromSafeConstant(MGT_SVG))
                .element();
        this.singleSelect = singleSelect(menuToggle(MenuToggleType.default_).icon(iconElement))
                .stayOpen((e, mt, m) -> closest((HTMLElement) e.target, By.id(refreshId)) != null)
                .placement(bottomEnd)
                .noDefaultSelectHandler()
                .addMenu(singleSelectMenu()
                        .addContent(menuContent()
                                .addGroup(menuGroup("Model graph tools")
                                        .add(div().css(component(menu, search))
                                                .add(flex().gap(sm)
                                                        .addItem(statusIconContainer = flexItem())
                                                        .addItem(statusTextContainer = flexItem()))))
                                .addDivider()
                                .addGroup(menuGroup()
                                        .add(div().css(component(menu, search))
                                                .add(flex().gap(sm)
                                                        .addItem(flexItem().flex(_1)
                                                                .add(button("Refresh")
                                                                        .id(refreshId)
                                                                        .link().inline()
                                                                        .onClick((c, e) -> refresh())))
                                                        .addItem(flexItem()
                                                                .add(button("More info", Urls.MODEL_GRAPH_TOOLS_PAGE,
                                                                        "_blank").link().inline())))))));
        active(false);
        refresh();
    }

    @Override
    public String ouiaComponentType() {
        return "hal/ModelGraphToolsIndicator";
    }

    @Override
    public ModelGraphToolsIndicator that() {
        return this;
    }

    @Override
    public HTMLElement element() {
        return singleSelect.element();
    }

    // ------------------------------------------------------ api

    public void refresh() {
        uic().modelGraphTools().ping().then(available -> {
            active(available);
            return null;
        });
    }

    // ------------------------------------------------------ internal

    private void active(boolean available) {
        removeChildrenFrom(statusIconContainer);
        removeChildrenFrom(statusTextContainer);
        if (available) {
            iconElement.style.color = "unset";
            statusIconContainer.add(icon(checkCircle()).status(success));
            statusTextContainer.add(span().text("Model graph tools are available."));
        } else {
            iconElement.style.color = globalIconColorDisabled.var;
            statusIconContainer.add(icon(ban()).status(danger));
            statusTextContainer.add(span().text("Model graph tools are not available."));
        }
    }
}

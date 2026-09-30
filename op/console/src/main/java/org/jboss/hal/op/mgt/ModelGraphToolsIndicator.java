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
package org.jboss.hal.op.mgt;

import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.op.mgt.ModelGraphToolsEvents.Availability;
import org.jboss.hal.op.resources.Resources;
import org.jboss.hal.resources.Urls;
import org.patternfly.component.menu.MenuToggleType;
import org.patternfly.component.menu.SingleSelect;
import org.patternfly.core.OuiaSupport;
import org.patternfly.layout.flex.FlexItem;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.closest;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.span;
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
 * Masthead indicator that shows whether a matching model graph tools (MGT) sidecar container is running. Listens for
 * {@link ModelGraphToolsEvents.Availability} events and updates a status icon and dropdown accordingly.
 */
public class ModelGraphToolsIndicator implements IsElement<HTMLElement>, OuiaSupport<HTMLElement, ModelGraphToolsIndicator> {

    // ------------------------------------------------------ factory

    /** Creates a new model graph tools indicator. */
    public static ModelGraphToolsIndicator modelGraphToolsIndicator() {
        return new ModelGraphToolsIndicator();
    }

    // ------------------------------------------------------ instance

    private final HTMLElement icon;
    private final SingleSelect singleSelect;
    private final FlexItem statusIconContainer;
    private final FlexItem statusTextContainer;

    ModelGraphToolsIndicator() {
        String refreshId = Id.unique("model-graph-tools-indicator", "refresh");
        this.icon = span()
                .html(SafeHtmlUtils.fromSafeConstant(Resources.INSTANCE.mgt().getText()))
                .element();
        this.singleSelect = singleSelect(menuToggle(MenuToggleType.default_).icon(icon))
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
                                                                        .onClick((c, e) ->
                                                                                ModelGraphToolsEvents.Ping.dispatch(
                                                                                        e.element()))))
                                                        .addItem(flexItem()
                                                                .add(button("More info", Urls.MODEL_GRAPH_TOOLS_PAGE,
                                                                        "_blank").link().inline())))))
                        ));
        active(false);
        Availability.listen(document.body, details -> active(details.available));
    }

    @Override
    public String ouiaComponentType() {
        return "halOP/ModelGraphToolsIndicator";
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

    /** Updates the indicator icon and status text based on the given availability. */
    public void active(boolean available) {
        removeChildrenFrom(statusIconContainer);
        removeChildrenFrom(statusTextContainer);
        if (available) {
            icon.style.color = "unset";
            statusIconContainer.add(icon(checkCircle()).status(success));
            statusTextContainer.add(span().text("Model graph tools are available."));
        } else {
            icon.style.color = globalIconColorDisabled.var;
            statusIconContainer.add(icon(ban()).status(danger));
            statusTextContainer.add(span().text("Model graph tools are not available."));
        }
    }
}

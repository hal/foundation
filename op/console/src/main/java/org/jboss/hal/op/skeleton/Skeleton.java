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
package org.jboss.hal.op.skeleton;

import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.jboss.elemento.By;
import org.jboss.elemento.IsElement;
import org.jboss.hal.core.Notifications;
import org.jboss.hal.env.Environment;
import org.jboss.hal.op.endpoint.EndpointStorage;
import org.jboss.hal.op.notification.NotificationElements;
import org.jboss.hal.op.resources.Resources;
import org.jboss.hal.resources.Ids;
import org.patternfly.component.navigation.Navigation;
import org.patternfly.component.page.MastheadLogo;
import org.patternfly.component.page.Page;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.style.Classes;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.hal.op.endpoint.EndpointSelector.endpointSelector;
import static org.jboss.hal.op.notification.NotificationElements.notificationElements;
import static org.jboss.hal.op.skeleton.StabilityBanner.stabilityBanner;
import static org.patternfly.component.backtotop.BackToTop.backToTop;
import static org.patternfly.component.page.Masthead.masthead;
import static org.patternfly.component.page.MastheadBrand.mastheadBrand;
import static org.patternfly.component.page.MastheadContent.mastheadContent;
import static org.patternfly.component.page.MastheadLogo.mastheadLogo;
import static org.patternfly.component.page.MastheadMain.mastheadMain;
import static org.patternfly.component.page.Page.page;
import static org.patternfly.component.page.PageMain.pageMain;
import static org.patternfly.component.skiptocontent.SkipToContent.skipToContent;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.componentgroup.theme.ThemeSelector.themeSelector;
import static org.patternfly.layout.flex.Direction.column;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.FlexWrap.noWrap;
import static org.patternfly.layout.flex.SpaceItems.none;
import static org.patternfly.style.Breakpoint.default_;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.fullHeight;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.static_;
import static org.patternfly.style.Variable.componentVar;
import static org.patternfly.style.Variables.Height;

public class Skeleton implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static Skeleton skeleton(Environment environment, EndpointStorage endpointStorage,
            Notifications notifications, Navigation navigation) {
        return new Skeleton(environment, endpointStorage, notifications, navigation);
    }

    // ------------------------------------------------------ instance

    private static final String STABILITY_MARKER = "hal-stability-marker";
    private final HTMLElement root;
    private StabilityBanner stabilityBanner;

    Skeleton(Environment environment, EndpointStorage endpointStorage, Notifications notifications, Navigation navigation) {
        MastheadLogo logo = mastheadLogo("/")
                .style(componentVar(component(Classes.brand), Height).name, "36px")
                .apply(e -> e.innerHTML = SafeHtmlUtils.fromSafeConstant(
                        Resources.INSTANCE.logo().getText()).asString());

        NotificationElements notificationElements = notificationElements(notifications);

        Toolbar toolbar = toolbar().css(modifier(fullHeight), modifier(static_))
                .addContent(toolbarContent()
                        .add(toolbarItem().css(modifier("overflow-container"))
                                .add(navigation))
                        .addGroup(toolbarGroup().css(modifier("align-end"))
                                .addItem(toolbarItem().add(notificationElements.badge()))
                                .addItem(toolbarItem().add(themeSelector("hal")
                                        .withContrast()))
                                .addItem(toolbarItem().add(endpointSelector(endpointStorage)))));

        Page page = page()
                .addSkipToContent(skipToContent(Ids.MAIN_ID))
                .addMasthead(masthead()
                        .addMain(mastheadMain()
                                .addBrand(mastheadBrand()
                                        .addLogo(logo)))
                        .addContent(mastheadContent()
                                .addToolbar(toolbar)))
                .addNotificationDrawer(notificationElements.drawer())
                .addMain(pageMain(Ids.MAIN_ID).fill())
                .add(backToTop().scrollableSelector(By.id(Ids.MAIN_ID)));
        page.wire(notificationElements.badge(), notificationElements.drawer());

        if (environment.highlightStability()) {
            document.documentElement.classList.add(STABILITY_MARKER);
            root = flex()
                    .direction(column)
                    .flexWrap(noWrap)
                    .spaceItems(none)
                    .style("height", "100%")
                    .add(stabilityBanner = stabilityBanner(environment, this::dismiss))
                    .addItem(flexItem().grow(default_).style("min-height", 0)
                            .add(page))
                    .element();
        } else {
            root = page.element();
        }
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ internal

    private void dismiss() {
        failSafeRemoveFromParent(stabilityBanner);
        document.documentElement.classList.remove(STABILITY_MARKER);
    }
}

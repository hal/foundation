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
import org.jboss.hal.env.Environment;
import org.jboss.hal.op.bootstrap.BootstrapErrorElement;
import org.jboss.hal.op.resources.Assets;
import org.jboss.hal.resources.Ids;
import org.patternfly.component.navigation.Navigation;
import org.patternfly.component.page.MastheadLogo;
import org.patternfly.component.page.Page;
import org.patternfly.component.page.PageMain;
import org.patternfly.component.toolbar.ToolbarItem;
import org.patternfly.style.Classes;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.hal.op.skeleton.StabilityBanner.stabilityBanner;
import static org.patternfly.component.backtotop.BackToTop.backToTop;
import static org.patternfly.component.page.Masthead.masthead;
import static org.patternfly.component.page.MastheadBrand.mastheadBrand;
import static org.patternfly.component.page.MastheadContent.mastheadContent;
import static org.patternfly.component.page.MastheadMain.mastheadMain;
import static org.patternfly.component.page.Page.page;
import static org.patternfly.component.page.PageMain.pageMain;
import static org.patternfly.component.page.PageSection.pageSection;
import static org.patternfly.component.skiptocontent.SkipToContent.skipToContent;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
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
import static org.patternfly.token.Token.globalBackgroundColor100;

public class Skeleton implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static Skeleton skeleton(Environment environment) {
        return new Skeleton(environment);
    }

    public static Skeleton errorSkeleton() {
        return new Skeleton(null);
    }

    // ------------------------------------------------------ instance

    private static final String STABILITY_MARKER = "hal-stability-marker";
    private final Page page;
    private final PageMain pageMain;
    private final ToolbarItem toolbarItem;
    private HTMLElement root;

    Skeleton(Environment environment) {
        page = page()
                .addSkipToContent(skipToContent(Ids.MAIN_ID))
                .addMasthead(masthead()
                        .addMain(mastheadMain()
                                .addBrand(mastheadBrand()
                                        .addLogo(MastheadLogo.mastheadLogo("/")
                                                .style(componentVar(component(Classes.brand), Height).name, "36px")
                                                .apply(e -> e.innerHTML = SafeHtmlUtils.fromSafeConstant(
                                                        Assets.INSTANCE.logo().getText()).asString()))))
                        .addContent(mastheadContent()
                                .addToolbar(toolbar().css(modifier(fullHeight), modifier(static_))
                                        .addContent(toolbarContent()
                                                .add(toolbarItem = toolbarItem().css(modifier("overflow-container")))))))
                .addMain(pageMain = pageMain(Ids.MAIN_ID))
                .add(backToTop()
                        .scrollableSelector(By.id(Ids.MAIN_ID)));
        if (environment != null && environment.highlightStability()) {
            document.documentElement.classList.add(STABILITY_MARKER);
            root = flex()
                    .direction(column)
                    .flexWrap(noWrap)
                    .spaceItems(none)
                    .style("height", "100%")
                    .add(stabilityBanner(environment, this::dismiss))
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

    // ------------------------------------------------------ add

    public Skeleton add(Navigation navigation) {
        toolbarItem.add(navigation.element());
        return this;
    }

    public Skeleton add(BootstrapErrorElement bootstrapError) {
        pageMain.add(pageSection()
                .limitWidth()
                .add(div().style("background-color", globalBackgroundColor100.var)
                        .add(bootstrapError)));
        return this;
    }

    // ------------------------------------------------------ internal

    private void dismiss() {
        if (root != page.element()) {
            document.body.prepend(page.element());
            failSafeRemoveFromParent(root);
            root = page.element();
            document.documentElement.classList.remove(STABILITY_MARKER);
        }
    }
}

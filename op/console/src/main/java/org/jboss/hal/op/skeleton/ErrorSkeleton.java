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
import org.jboss.elemento.IsElement;
import org.jboss.hal.op.bootstrap.BootstrapErrorElement;
import org.jboss.hal.op.resources.Assets;
import org.jboss.hal.resources.Ids;
import org.patternfly.component.page.MastheadLogo;
import org.patternfly.component.page.Page;
import org.patternfly.component.page.PageMain;
import org.patternfly.style.Classes;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.patternfly.component.page.Masthead.masthead;
import static org.patternfly.component.page.MastheadBrand.mastheadBrand;
import static org.patternfly.component.page.MastheadMain.mastheadMain;
import static org.patternfly.component.page.Page.page;
import static org.patternfly.component.page.PageMain.pageMain;
import static org.patternfly.component.page.PageSection.pageSection;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Variable.componentVar;
import static org.patternfly.style.Variables.Height;
import static org.patternfly.token.Token.globalBackgroundColor100;

public class ErrorSkeleton implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static ErrorSkeleton errorSkeleton() {
        return new ErrorSkeleton();
    }

    // ------------------------------------------------------ instance

    private final Page page;
    private final PageMain pageMain;

    ErrorSkeleton() {
        page = page()
                .addMasthead(masthead()
                        .addMain(mastheadMain()
                                .addBrand(mastheadBrand()
                                        .addLogo(MastheadLogo.mastheadLogo("/")
                                                .style(componentVar(component(Classes.brand), Height).name, "36px")
                                                .apply(e -> e.innerHTML = SafeHtmlUtils.fromSafeConstant(
                                                        Assets.INSTANCE.logo().getText()).asString())))))
                .addMain(pageMain = pageMain(Ids.MAIN_ID));
    }

    @Override
    public HTMLElement element() {
        return page.element();
    }

    // ------------------------------------------------------ add

    public ErrorSkeleton add(BootstrapErrorElement bootstrapError) {
        pageMain.add(pageSection()
                .limitWidth()
                .add(div().style("background-color", globalBackgroundColor100.var)
                        .add(bootstrapError)));
        return this;
    }
}

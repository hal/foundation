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

import java.util.function.Supplier;

import org.jboss.elemento.Callback;
import org.jboss.elemento.IsElement;
import org.jboss.hal.env.Stability;
import org.jboss.hal.resources.OuiaIds;
import org.patternfly.core.OuiaSupport;
import org.patternfly.icon.PredefinedIcon;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.strong;
import static org.jboss.hal.resources.Urls.STABILITY_LEVELS;
import static org.jboss.hal.resources.Urls.replaceVersion;
import static org.jboss.hal.ui.brick.StabilityBricks.stabilityIconSupplier;
import static org.jboss.hal.ui.brick.StabilityBricks.stabilityStatus;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.banner.Banner.banner;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.divider.Divider.divider;
import static org.patternfly.component.divider.DividerType.hr;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.FlexWrap.noWrap;
import static org.patternfly.layout.flex.JustifyContent.center;
import static org.patternfly.layout.flex.SpaceItems.none;
import static org.patternfly.layout.flex.SpaceItems.sm;
import static org.patternfly.style.Orientation.vertical;

/**
 * A dismissible banner displayed at the top of the console when the WildFly server has been started with a non-default
 * stability level (e.g., experimental or preview). Shows the current stability level and provides a link to the
 * documentation.
 */
public class StabilityBanner implements IsElement<HTMLElement>, OuiaSupport<HTMLElement, StabilityBanner> {

    // ------------------------------------------------------ factory

    /** Creates a new stability banner with the given dismiss callback. */
    public static StabilityBanner stabilityBanner(Callback gotIt) {
        return new StabilityBanner(gotIt);
    }

    // ------------------------------------------------------ instance

    private final HTMLElement root;

    StabilityBanner(Callback gotIt) {
        Stability serverStability = uic().environment().serverStability();
        Supplier<PredefinedIcon> icon = stabilityIconSupplier(serverStability);

        root = banner()
                .status(stabilityStatus(serverStability))
                .screenReader("The server has been started with stability level " + serverStability.label)
                .add(flex().spaceItems(none)
                        .justifyContent(center)
                        .flexWrap(noWrap)
                        .add(flex().spaceItems(sm)
                                .addItem(flexItem().add(icon.get()))
                                .addItem(flexItem()
                                        .add("The server has been started with stability level ")
                                        .add(strong().text(serverStability.label)))
                                .addItem(flexItem().add(icon.get())))
                        .add(flex().spaceItems(sm).style("position:fixed;right:var(--pf-t--global--spacer--lg)")
                                .add(button("Got it").link().inline()
                                        .ouiaId(OuiaIds.STABILITY_DISMISS_BTN)
                                        .onClick((event, component) -> gotIt.call()))
                                .add(divider(hr).orientation(vertical))
                                .add(a(replaceVersion(STABILITY_LEVELS, uic().environment().productVersionLink()), "_blank")
                                        .text("More info"))))
                .element();
        initOuia();
    }

    @Override
    public String ouiaComponentType() {
        return OuiaIds.TYPE_STABILITY_BANNER;
    }

    @Override
    public StabilityBanner that() {
        return this;
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}

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
import org.jboss.hal.env.Environment;
import org.jboss.hal.env.Stability;
import org.patternfly.icon.PredefinedIcon;
import org.patternfly.style.Color;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.strong;
import static org.jboss.hal.ui.BuildingBlocks.stabilityColor;
import static org.jboss.hal.ui.BuildingBlocks.stabilityIconSupplier;
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

public class StabilityBanner implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static StabilityBanner stabilityBanner(Environment environment, Callback gotIt) {
        return new StabilityBanner(environment, gotIt);
    }

    // ------------------------------------------------------ instance

    private final HTMLElement root;

    StabilityBanner(Environment environment, Callback gotIt) {
        Stability serverStability = environment.serverStability();
        Color color = stabilityColor(serverStability);
        Supplier<PredefinedIcon> icon = stabilityIconSupplier(serverStability);
        String moreInfo = "https://docs.wildfly.org/" + environment.productVersionLink() + "/Admin_Guide.html#Feature_stability_levels";

        root = banner(color)
                .sticky()
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
                        .add(flex().spaceItems(sm).style("position:fixed;right:var(--pf-v5-global--spacer--lg)")
                                .add(button("Got it").link().inline().onClick((event, component) -> gotIt.call()))
                                .add(divider(hr).orientation(vertical))
                                .add(a(moreInfo, "_blank").text("More info"))))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}

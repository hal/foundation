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
package org.jboss.hal.op.dashboard;

import java.util.function.Consumer;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.AttributeDescriptions;
import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.component.list.DescriptionListDescription;
import org.patternfly.component.list.DescriptionListGroup;
import org.patternfly.icon.PredefinedIcon;

import static org.jboss.hal.core.LabelBuilder.labelBuilder;
import static org.jboss.hal.ui.BuildingBlocks.attributeDescriptionPopover;
import static org.jboss.hal.ui.BuildingBlocks.AttributeDescriptionContent.descriptionOnly;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;
import static org.patternfly.popper.Placement.auto;
import static org.patternfly.style.Size.xs;

class Dashboard {

    static EmptyState dashboardEmptyState() {
        return emptyState().size(xs);
    }

    static DescriptionListGroup dlg(AttributeDescriptions ad, ModelNode modelNode, String attribute) {
        return dlg(ad, attribute, null, dld -> dld.text(modelNode.get(attribute).asString()));
    }

    static DescriptionListGroup dlg(AttributeDescriptions ad, ModelNode modelNode, String attribute, PredefinedIcon icon) {
        return dlg(ad, attribute, icon, dld -> dld.text(modelNode.get(attribute).asString()));
    }

    static DescriptionListGroup dlg(AttributeDescriptions ad, String attribute, Consumer<DescriptionListDescription> withDld) {
        return dlg(ad, attribute, null, withDld);
    }

    static DescriptionListGroup dlg(AttributeDescriptions ad, String attribute, PredefinedIcon icon,
            Consumer<DescriptionListDescription> withDld) {
        String label = labelBuilder(attribute);
        return descriptionListGroup(attribute)
                .addTerm(descriptionListTerm(label)
                        .run(dlt -> {
                            if (icon != null) {
                                dlt.icon(icon);
                            }
                        })
                        .help(attributeDescriptionPopover(label, ad.get(attribute), descriptionOnly)
                                .placement(auto)))
                .addDescription(descriptionListDescription().run(withDld));
    }
}

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

import static org.jboss.hal.core.Humanize.sentenceCase;
import static org.jboss.hal.ui.brick.AttributeBricks.attributeDescriptionPopover;
import static org.jboss.hal.ui.brick.DescriptionBricks.AttributeDescriptionContent.descriptionOnly;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;
import static org.patternfly.style.Size.xs;

/** Shared utility methods for building dashboard card content such as empty states and description list groups. */
class Dashboard {

    /** Creates an extra-small empty state for use in dashboard cards. */
    static EmptyState dashboardEmptyState() {
        return emptyState().size(xs);
    }

    /** Creates a description list group for the given attribute, reading its value as a string from the model node. */
    static DescriptionListGroup dlg(AttributeDescriptions ad, ModelNode modelNode, String attribute) {
        return dlg(ad, attribute, null, dld -> dld.text(modelNode.get(attribute).asString()));
    }

    /** Creates a description list group with an icon for the given attribute. */
    static DescriptionListGroup dlg(AttributeDescriptions ad, ModelNode modelNode, String attribute, PredefinedIcon icon) {
        return dlg(ad, attribute, icon, dld -> dld.text(modelNode.get(attribute).asString()));
    }

    /** Creates a description list group with custom description content for the given attribute. */
    static DescriptionListGroup dlg(AttributeDescriptions ad, String attribute, Consumer<DescriptionListDescription> withDld) {
        return dlg(ad, attribute, null, withDld);
    }

    /** Creates a description list group with an optional icon and custom description content. */
    static DescriptionListGroup dlg(AttributeDescriptions ad, String attribute, PredefinedIcon icon,
            Consumer<DescriptionListDescription> withDld) {
        String label = sentenceCase(attribute);
        return descriptionListGroup(attribute)
                .addTerm(descriptionListTerm(label)
                        .run(dlt -> {
                            if (icon != null) {
                                dlt.icon(icon);
                            }
                        })
                        .help(attributeDescriptionPopover(label, ad.get(attribute), descriptionOnly)))
                .addDescription(descriptionListDescription().run(withDld));
    }
}

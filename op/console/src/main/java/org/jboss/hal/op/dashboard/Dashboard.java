package org.jboss.hal.op.dashboard;

import java.util.function.Consumer;

import org.jboss.hal.core.LabelBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.AttributeDescriptions;
import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.component.list.DescriptionListDescription;
import org.patternfly.component.list.DescriptionListGroup;
import org.patternfly.icon.PredefinedIcon;

import static org.jboss.hal.ui.BuildingBlocks.AttributeDescriptionContent.descriptionOnly;
import static org.jboss.hal.ui.BuildingBlocks.attributeDescriptionPopover;
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
        String label = new LabelBuilder().label(attribute);
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

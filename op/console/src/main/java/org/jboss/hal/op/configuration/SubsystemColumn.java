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
package org.jboss.hal.op.configuration;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.elemento.Id;
import org.jboss.elemento.router.PlaceManager;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.op.finder.ColumnProvider;
import org.patternfly.extension.finder.FinderColumn;

import static org.jboss.hal.core.Humanize.capitalCase;
import static org.jboss.hal.op.configuration.ConfigurationRoutes.RESOURCE_ROUTE;
import static org.jboss.hal.ui.brick.FinderBricks.stackPreview;
import static org.jboss.hal.ui.resource.finder.FinderSupport.childResources;
import static org.jboss.hal.ui.resource.finder.FinderSupport.metadataPreview;
import static org.jboss.hal.ui.resource.finder.FinderSupport.itemAddress;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.extension.finder.FinderColumn.finderColumn;
import static org.patternfly.extension.finder.FinderColumnActions.finderColumnActions;
import static org.patternfly.extension.finder.FinderColumnHeader.finderColumnHeader;
import static org.patternfly.extension.finder.FinderItem.finderItem;
import static org.patternfly.extension.finder.FinderItemActions.finderItemActions;
import static org.patternfly.icon.IconSets.fas.arrowUpRightFromSquare;
import static org.patternfly.icon.IconSets.fas.rotateRight;
import static org.patternfly.layout.stack.StackItem.stackItem;

/**
 * Finder column that lists all WildFly subsystems. Each item displays the subsystem name in capital case, provides a "view"
 * action that navigates to the {@link ConfigurationResourcePage}, and shows the subsystem's resource description in the preview
 * panel.
 */
@Dependent
public class SubsystemColumn implements ColumnProvider {

    /** Column identifier used for registration and OUIA test IDs. */
    public static final String ID = "subsystem-column";
    private static final AddressTemplate TEMPLATE = AddressTemplate.ofTrusted("subsystem=*");
    private final PlaceManager placeManager;

    @Inject
    public SubsystemColumn(PlaceManager placeManager) {
        this.placeManager = placeManager;
    }

    @Override
    public String identifier() {
        return ID;
    }

    @Override
    public FinderColumn get() {
        FinderColumn column = finderColumn(ID);
        return column.addHeader(finderColumnHeader("Subsystem").addActions(finderColumnActions()
                        .addButton(button(rotateRight()).plain().small().onClick((e, b) -> column.reload()))))
                .addItems(childResources(__ -> TEMPLATE, node -> finderItem(Id.build(node.asString()))
                        .text(capitalCase(node.asString()))
                        .run(item -> item.addActions(finderItemActions()
                                .addButton(button(arrowUpRightFromSquare()).plain().small().onClick((e, b) ->
                                        placeManager.goTo(RESOURCE_ROUTE, itemAddress(item))))))))
                .onPreview(metadataPreview((name, metadata, preview) ->
                        stackPreview(preview, capitalCase(name), stack -> stack.addItem(stackItem()
                                .add(content(p).editorial().text(metadata.resourceDescription().description()))))));
    }
}

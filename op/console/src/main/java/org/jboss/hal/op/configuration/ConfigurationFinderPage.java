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

import org.jboss.elemento.router.PlaceManager;
import org.jboss.elemento.router.Route;
import org.jboss.hal.op.finder.ColumnRegistry;
import org.jboss.hal.ui.resource.finder.FinderPage;
import org.patternfly.component.navigation.Navigation;
import org.patternfly.extension.finder.Finder;

import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.OuiaIds.PAGE_CONFIGURATION;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.h1;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.extension.finder.FinderPreview.finderPreview;
import static org.patternfly.layout.stack.Stack.stack;
import static org.patternfly.layout.stack.StackItem.stackItem;

/**
 * Configuration finder page for halOP. Displays subsystems and global resources (interfaces, socket bindings, paths, system
 * properties) using a {@link Finder} with columns registered via the {@link ColumnRegistry}.
 */
@Dependent
@Route("/configuration/:finderPath?")
public class ConfigurationFinderPage extends FinderPage {

    private final ColumnRegistry columnRegistry;

    @Inject
    public ConfigurationFinderPage(ColumnRegistry columnRegistry, PlaceManager placeManager, Navigation navigation) {
        super(placeManager, navigation, PAGE_CONFIGURATION);
        this.columnRegistry = columnRegistry;
    }

    @Override
    protected void configureFinder(Finder finder) {
        finder.addItem(columnRegistry.column(ConfigurationColumn.ID).get())
                .addPreview(finderPreview().css(halComponent("finder", "preview"))
                        .add(stack().gutter()
                                .addItem(stackItem().add(content(h1).text("Configuration")))
                                .addItem(stackItem()
                                        .add(content(p).editorial().html(ConfigurationDescriptions.CONFIGURATION_DESCRIPTION_1))
                                        .add(content(p).editorial().html(ConfigurationDescriptions.CONFIGURATION_DESCRIPTION_2)))));
    }
}

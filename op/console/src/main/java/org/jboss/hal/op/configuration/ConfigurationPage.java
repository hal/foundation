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

import org.gwtproject.safehtml.shared.SafeHtml;
import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.jboss.elemento.router.LoadedData;
import org.jboss.elemento.router.Page;
import org.jboss.elemento.router.Parameter;
import org.jboss.elemento.router.Place;
import org.jboss.elemento.router.Route;
import org.jboss.hal.op.finder.ColumnRegistry;
import org.patternfly.extension.finder.Finder;
import org.patternfly.style.Classes;

import elemental2.dom.HTMLElement;

import static java.util.Collections.singletonList;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.h1;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.extension.finder.Finder.finder;
import static org.patternfly.extension.finder.FinderPreview.finderPreview;
import static org.patternfly.layout.stack.Stack.stack;
import static org.patternfly.layout.stack.StackItem.stackItem;
import static org.patternfly.style.Classes.util;

/**
 * Represents the configuration page of halOP, responsible for displaying and managing configurations for subsystems and global
 * resources. It provides an interface to view, modify, and interact with configuration options such as interfaces, socket
 * bindings, paths, and system properties.
 * <p>
 * The page creates a {@link Finder} component to display the configuration columns. The finder instance is registered with the
 * {@link ColumnRegistry}. An instance to the configuration finder can be obtained with
 * <p>
 * {@snippet :
 * Finder finder = componentRegistry().lookupComponent(ComponentType.Finder);
 *}
 */
@Dependent
@Route("/configuration")
public class ConfigurationPage implements Page {

    // language=html
    private static final SafeHtml DESCRIPTION_1 = SafeHtmlUtils.fromSafeConstant(
            "Configure subsystems and global resources such as interfaces, socket bindings, paths and system properties.");
    // language=html
    private static final SafeHtml DESCRIPTION_2 = SafeHtmlUtils.fromSafeConstant(
            "View and modify the configuration for each available subsystem. For example, add a data source, configure a messaging provider, or set up application security.");

    private final ColumnRegistry columnRegistry;

    @Inject
    public ConfigurationPage(ColumnRegistry columnRegistry) {
        this.columnRegistry = columnRegistry;
    }

    @Override
    public Iterable<HTMLElement> elements(Place place, Parameter parameter, LoadedData data) {
        // The finder is registered with the component registry.
        // Although there are different finders (configuration, runtime, ...),
        // only one finder is in the DOM at a time. So the singleton pattern
        // of the component registry is still satisfied.
        // If necessary, an instance to the finder can be obtained with
        // Finder finder = componentRegistry().lookupComponent(ComponentType.Finder);
        return singletonList(finder().registerComponent().css(util("h-100"))
                .addItem(columnRegistry.column(ConfigurationColumn.ID).get())
                .addPreview(finderPreview().css(halComponent("finder", "preview"))
                        .add(stack().gutter()
                                .addItem(stackItem().add(content(h1).text("Configuration")))
                                .addItem(stackItem()
                                        .add(content(p).editorial().html(DESCRIPTION_1))
                                        .add(content(p).editorial().html(DESCRIPTION_2)))))
                .element());
    }
}

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
package org.jboss.hal.op.catalog;

import org.jboss.elemento.IsElement;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.li;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.strong;
import static org.jboss.elemento.Elements.ul;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.h3;
import static org.patternfly.component.content.ContentType.h4;
import static org.patternfly.component.icon.Icon.icon;
import static org.patternfly.icon.IconSets.rhUi.warning;
import static org.patternfly.style.Status.warning;

class CatalogWelcome implements IsElement<HTMLElement> {

    private final HTMLElement root;

    CatalogWelcome() {
        root = content().editorial()
                .add(content(h3).text("Model catalog"))
                .add(p().text("The model catalog lets you explore the complete WildFly management model. "
                        + "It indexes all resources, attributes, operations, and capabilities across the entire "
                        + "management tree and makes them searchable with faceted filters."))

                .add(content(h4).text("Getting started"))
                .add(p().text("Select a type from the sidebar to browse a specific category, "
                        + "or enter a search term in the toolbar to find entries by name or address. "
                        + "You can also combine type selection with facet filters to narrow down results."))

                .add(content(h4).text("Types"))
                .add(ul()
                        .add(li().add(strong().text("Resources"))
                                .add(" — addressable nodes in the management tree. Each resource has a unique "
                                        + "address (e.g. /subsystem=datasources/data-source=ExampleDS) and can be a "
                                        + "singleton or a regular resource. Resources may declare capabilities that "
                                        + "other parts of the model can reference."))
                        .add(li().add(strong().text("Attributes"))
                                .add(" — configuration and runtime properties of resources. Attributes have a data "
                                        + "type (string, int, boolean, etc.), an access type (read-write, read-only, "
                                        + "or metric), and a storage mode (configuration or runtime). Some attributes "
                                        + "require a server restart when changed."))
                        .add(li().add(strong().text("Operations"))
                                .add(" — management actions that can be invoked on resources. Operations may accept "
                                        + "parameters and return values. Some operations are global (available on all "
                                        + "resources), while others are specific to a resource type."))
                        .add(li().add(strong().text("Capabilities"))
                                .add(" — named services or features that resources provide and other resources can "
                                        + "depend on. Capabilities form a dependency graph across the management model.")))

                .add(content(h4).text("Filters"))
                .add(p().text("Use the facet filters in the sidebar to refine your search. Available filters depend "
                        + "on the selected type. Common filters include stability level and deprecation status. "
                        + "Type-specific filters let you narrow by data type, access type, storage mode, "
                        + "restart requirements, and more."))

                .add(content(h4).text("Verify on server"))
                .add(p().text("The catalog draws its data from the model graph database, which describes the full "
                        + "management model independent of any running server. Click \"Verify on server\" "
                        + "in the toolbar to cross-reference catalog entries with the connected WildFly instance. "
                        + "This turns catalog links into live navigation:"))
                .add(ul()
                        .add(li().text("Resources that do not exist on the server are shown as plain text "
                                + "instead of clickable links."))
                        .add(li().text("Wildcard addresses are resolved against the server. When exactly one "
                                + "matching resource exists, the link points directly to it."))
                        .add(li().text("When multiple matches exist, the link opens a list that lets you "
                                + "choose which resource to navigate to.")))
                .add(p().add(icon(warning()).status(warning).inline())
                        .add(" This operation checks each resource address against the running server, "
                                + "which can involve a large number of management operations. Depending on the "
                                + "size of the result set, it may take a moment to complete."))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}

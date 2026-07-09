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
package org.jboss.hal.ui.resource;

import org.jboss.elemento.IsElement;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.patternfly.component.page.PageBreadcrumb.pageBreadcrumb;
import static org.patternfly.component.page.PageGroup.pageGroup;
import static org.patternfly.component.page.PageSection.pageSection;
import static org.patternfly.style.Sticky.top;

/**
 * Composable layout shell for WildFly management resource views.
 * <p>
 * A pure layout container that accepts optional child components — breadcrumb, header, and content (tabs or resource list) —
 * and arranges them in a consistent page layout. The shell itself has no behavior and no data loading; all intelligence lives
 * in the composed child components.
 * <p>
 * Usage examples:
 * <pre>
 * // Full resource view
 * resourceShell(template, metadata)
 *     .addBreadcrumb(resourceBreadcrumb(template, metadata))
 *     .addHeader(resourceHeader(template, metadata))
 *     .addTabs(resourceTabs(template, metadata))
 *
 * // Minimal — just tabs
 * resourceShell(template, metadata)
 *     .addTabs(resourceTabs(template, metadata))
 * </pre>
 */
public class ResourceShell implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    /** Creates a new resource shell for the given template and metadata. */
    public static ResourceShell resourceShell(AddressTemplate template, Metadata metadata) {
        return new ResourceShell(template, metadata);
    }

    // ------------------------------------------------------ instance

    private final HTMLElement stickyGroup;
    private final HTMLElement contentSection;
    private final HTMLElement root;

    ResourceShell(AddressTemplate template, Metadata metadata) {
        this.root = div()
                .add(stickyGroup = pageGroup().sticky(top).element())
                .add(contentSection = pageSection().element())
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ builder

    /** Adds CSS classes to the content section. */
    public ResourceShell contentCss(String... css) {
        for (String c : css) {
            contentSection.classList.add(c);
        }
        return this;
    }

    /** Adds a breadcrumb to the sticky header area. */
    public ResourceShell addBreadcrumb(ResourceBreadcrumb breadcrumb) {
        stickyGroup.appendChild(pageBreadcrumb().add(breadcrumb).element());
        return this;
    }

    /** Adds a header (name, stability label, description) to the sticky header area. */
    public ResourceShell addHeader(ResourceHeader header) {
        stickyGroup.appendChild(pageSection().add(header).element());
        return this;
    }

    /** Adds a tabbed resource view to the content area. */
    public ResourceShell addTabs(ResourceTabs tabs) {
        contentSection.appendChild(tabs.element());
        return this;
    }

    /** Adds a child resource list to the content area. */
    public ResourceShell addResourceList(ResourceList resourceList) {
        contentSection.appendChild(resourceList.element());
        return this;
    }
}

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
package org.jboss.hal.ui.resource.shell;

import org.jboss.elemento.ElementConsumerMethods;
import org.jboss.elemento.IsElement;
import org.jboss.hal.env.Stability;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.OuiaIds;
import org.patternfly.component.ComponentIcon;
import org.patternfly.component.label.Label;
import org.patternfly.componentgroup.pageheader.PageHeader;
import org.patternfly.core.OuiaSupport;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;

import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.brick.StabilityLabel.stabilityLabel;
import static org.patternfly.componentgroup.pageheader.PageHeader.pageHeader;

/**
 * Displays the name, stability label, and description of a WildFly management resource.
 * <p>
 * By default, the title is derived from the template's last segment value, the stability label is shown when the environment
 * requires highlighting, and the description is taken from the resource metadata.
 */
public class ResourceHeader implements
        ComponentIcon<HTMLElement, ResourceHeader>,
        ElementConsumerMethods<HTMLElement, ResourceHeader>,
        IsElement<HTMLElement>,
        OuiaSupport<HTMLElement, ResourceHeader> {

    // ------------------------------------------------------ factory

    public static ResourceHeader resourceHeader(AddressTemplate template, Metadata metadata) {
        return new ResourceHeader(template, metadata);
    }

    // ------------------------------------------------------ instance

    private final AddressTemplate template;
    private final Metadata metadata;
    private final PageHeader pageHeader;

    ResourceHeader(AddressTemplate template, Metadata metadata) {
        this.template = template;
        this.metadata = metadata;
        this.pageHeader = pageHeader();

        Stability stability = metadata.resourceDescription().stability();
        if (uic().environment().highlightStability(stability)) {
            pageHeader.addLabel(stabilityLabel(stability).element());
        }
        initOuia(OuiaIds.RESOURCE_HEADER);
    }

    @Override
    public String ouiaComponentType() {
        return "halOP/ResourceHeader";
    }

    @Override
    public HTMLElement element() {
        return pageHeader.element();
    }

    // ------------------------------------------------------ add

    public ResourceHeader addHeader(String header) {
        pageHeader.addHeader(header);
        return this;
    }

    public ResourceHeader addHeader(HTMLElement header) {
        pageHeader.addHeader(header);
        return this;
    }

    public ResourceHeader addDescription(String description) {
        pageHeader.addDescription(description);
        return this;
    }

    public ResourceHeader addDescription(HTMLElement description) {
        pageHeader.addDescription(description);
        return this;
    }

    public ResourceHeader addLabel(Label label) {
        pageHeader.addLabel(label);
        return this;
    }

    // ------------------------------------------------------ builder

    public ResourceHeader defaultHeader() {
        return addHeader(template.isEmpty() ? "Management Model" : template.last().value);
    }

    public ResourceHeader defaultDescription() {
        return addDescription(metadata.resourceDescription().description());
    }

    @Override
    public ResourceHeader icon(Element icon) {
        pageHeader.icon(icon);
        return this;
    }

    @Override
    public ResourceHeader removeIcon() {
        pageHeader.removeIcon();
        return this;
    }

    @Override
    public ResourceHeader that() {
        return this;
    }
}

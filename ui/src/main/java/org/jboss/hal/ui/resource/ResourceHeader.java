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
import org.jboss.hal.env.Stability;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.p;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.title.Title.title;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.style.Size._3xl;

/**
 * Displays the name, stability label, and description of a WildFly management resource.
 * <p>
 * Renders fully from metadata at construction time — no attach-time data loading. The stability label is only shown when the
 * resource's stability level requires highlighting (as determined by the current environment settings).
 */
public class ResourceHeader implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    /** Creates a new resource header for the given template and metadata. */
    public static ResourceHeader resourceHeader(AddressTemplate template, Metadata metadata) {
        return new ResourceHeader(template, metadata);
    }

    // ------------------------------------------------------ instance

    private final HTMLElement root;

    ResourceHeader(AddressTemplate template, Metadata metadata) {
        String name = template.isEmpty() ? "Management Model" : template.last().value;
        String description = metadata.resourceDescription().description();
        Stability stability = metadata.resourceDescription().stability();

        this.root = content()
                .add(flex().alignItems(center)
                        .addItem(flexItem().add(title(1, _3xl, name)))
                        .run(f -> {
                            if (uic().environment().highlightStability(stability)) {
                                f.addItem(flexItem().add(stabilityLabel(stability)));
                            }
                        }))
                .run(c -> {
                    if (description != null && !description.isEmpty()) {
                        c.add(p().text(description));
                    }
                })
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}

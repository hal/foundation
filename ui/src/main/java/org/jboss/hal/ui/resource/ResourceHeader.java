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
 * By default, the title is derived from the template's last segment value, the stability label is shown when the environment
 * requires highlighting, and the description is taken from the resource metadata. All of these can be customized via builder
 * methods:
 * <ul>
 * <li>{@link #customTitle(String)} — override the title text</li>
 * <li>{@link #customTitle(HTMLElement)} — override the title with rich HTML content</li>
 * <li>{@link #showStability(boolean)} — suppress or show the stability label</li>
 * <li>{@link #showDescription(boolean)} — suppress or show the description</li>
 * </ul>
 * The element is constructed lazily on the first call to {@link #element()}.
 */
public class ResourceHeader implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    /** Creates a new resource header for the given template and metadata. */
    public static ResourceHeader resourceHeader(AddressTemplate template, Metadata metadata) {
        return new ResourceHeader(template, metadata);
    }

    // ------------------------------------------------------ instance

    private final AddressTemplate template;
    private final Metadata metadata;
    private String titleText;
    private HTMLElement titleContent;
    private boolean showStability = true;
    private boolean showDescription = true;
    private HTMLElement root;

    ResourceHeader(AddressTemplate template, Metadata metadata) {
        this.template = template;
        this.metadata = metadata;
    }

    @Override
    public HTMLElement element() {
        if (root == null) {
            root = build();
        }
        return root;
    }

    // ------------------------------------------------------ builder

    /** Overrides the default title (template's last segment value) with the given text. */
    public ResourceHeader customTitle(String titleText) {
        this.titleText = titleText;
        return this;
    }

    /** Overrides the default title with rich HTML content (e.g. text with inline {@code <code>} elements). */
    public ResourceHeader customTitle(HTMLElement titleContent) {
        this.titleContent = titleContent;
        return this;
    }

    /** Controls whether the stability label is shown. Defaults to {@code true}. */
    public ResourceHeader showStability(boolean showStability) {
        this.showStability = showStability;
        return this;
    }

    /** Controls whether the resource description is shown. Defaults to {@code true}. */
    public ResourceHeader showDescription(boolean showDescription) {
        this.showDescription = showDescription;
        return this;
    }

    // ------------------------------------------------------ internal

    private HTMLElement build() {
        String name;
        if (titleText != null) {
            name = titleText;
        } else {
            name = template.isEmpty() ? "Management Model" : template.last().value;
        }

        Stability stability = metadata.resourceDescription().stability();
        String description = metadata.resourceDescription().description();

        return content()
                .add(flex().alignItems(center)
                        .run(f -> {
                            if (titleContent != null) {
                                f.addItem(flexItem().add(title(1, _3xl).add(titleContent)));
                            } else {
                                f.addItem(flexItem().add(title(1, _3xl, name)));
                            }
                        })
                        .run(f -> {
                            if (showStability && uic().environment().highlightStability(stability)) {
                                f.addItem(flexItem().add(stabilityLabel(stability)));
                            }
                        }))
                .run(c -> {
                    if (showDescription && description != null && !description.isEmpty()) {
                        c.add(p().text(description));
                    }
                })
                .element();
    }
}

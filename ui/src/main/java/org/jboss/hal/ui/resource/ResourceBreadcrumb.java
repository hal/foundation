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

import java.util.function.Consumer;

import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.Segment;
import org.patternfly.component.breadcrumb.BreadcrumbItem;
import org.patternfly.component.icon.Icon;
import org.patternfly.component.icon.IconSize;
import org.patternfly.component.tooltip.Tooltip;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.navigator;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.EventType.click;
import static org.patternfly.component.breadcrumb.Breadcrumb.breadcrumb;
import static org.patternfly.component.breadcrumb.BreadcrumbItem.breadcrumbItem;
import static org.patternfly.component.icon.Icon.icon;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.icon.IconSets.fas.copy;

/**
 * Clickable breadcrumb trail for a WildFly management resource address.
 * <p>
 * Each address segment is rendered as a breadcrumb item. Clicking a segment invokes the {@code onSegmentClick} callback with
 * the corresponding address template. The active (last) segment includes a copy-to-clipboard button for the full address.
 * <p>
 * Renders fully from the address template at construction time — no attach-time data loading.
 */
public class ResourceBreadcrumb implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    /** Creates a new breadcrumb for the given resource address. */
    public static ResourceBreadcrumb resourceBreadcrumb(AddressTemplate template, Metadata metadata) {
        return new ResourceBreadcrumb(template, metadata);
    }

    // ------------------------------------------------------ instance

    private final HTMLElement root;
    private Consumer<AddressTemplate> onSegmentClick;

    ResourceBreadcrumb(AddressTemplate template, Metadata metadata) {
        org.patternfly.component.breadcrumb.Breadcrumb bc = breadcrumb();
        if (template.isEmpty()) {
            bc.addItem(breadcrumbItem("root", "/"));
        } else {
            bc.addItem(breadcrumbItem("root", "/")
                    .onClick((event, item) -> {
                        event.preventDefault();
                        if (onSegmentClick != null) {
                            onSegmentClick.accept(AddressTemplate.root());
                        }
                    }));
            AddressTemplate current = AddressTemplate.root();
            for (Segment segment : template) {
                current = current.append(segment.key, segment.value);
                boolean last = current.last().equals(template.last());
                BreadcrumbItem item = breadcrumbItem(current.identifier(),
                        segment.key + "=" + segment.value);
                if (last) {
                    item.active(true);
                    item.add(copyToClipboard(current));
                } else {
                    final AddressTemplate target = current;
                    item.onClick((event, bi) -> {
                        event.preventDefault();
                        if (onSegmentClick != null) {
                            onSegmentClick.accept(target);
                        }
                    });
                }
                bc.addItem(item);
            }
        }
        this.root = bc.element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ builder

    /** Registers a callback invoked when a non-active breadcrumb segment is clicked. */
    public ResourceBreadcrumb onSegmentClick(Consumer<AddressTemplate> onSegmentClick) {
        this.onSegmentClick = onSegmentClick;
        return this;
    }

    // ------------------------------------------------------ internal

    private HTMLElement copyToClipboard(AddressTemplate template) {
        String id = Id.unique("address", "copy");
        String text = "Copy address to clipboard";
        Tooltip tp = tooltip(By.id(id), text)
                .onClose((e, t) -> t.text(text));
        Icon ico = icon(copy()).size(IconSize.sm).id(id).on(click, e -> {
            navigator.clipboard.writeText(template.toString());
            tp.text("Address copied");
        });
        return span()
                .add(ico)
                .add(tp)
                .element();
    }
}

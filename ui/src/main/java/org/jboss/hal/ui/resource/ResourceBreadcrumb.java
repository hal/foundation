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

import java.util.ArrayList;
import java.util.List;

import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.Segment;
import org.jboss.hal.resources.OuiaIds;
import org.patternfly.component.breadcrumb.BreadcrumbItem;
import org.patternfly.component.icon.Icon;
import org.patternfly.component.icon.IconSize;
import org.patternfly.component.tooltip.Tooltip;
import org.patternfly.core.OuiaSupport;

import elemental2.dom.Event;
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
 * Each address segment is rendered as a breadcrumb item. The active (last) segment includes a copy-to-clipboard button for the
 * full address. Non-active segments delegate clicks to the registered {@link SegmentHandler}, which receives the segment's
 * template, its depth (0-based position in the address), and the breadcrumb item. The root {@code /} is reported at depth
 * {@code -1}.
 */
public class ResourceBreadcrumb implements IsElement<HTMLElement>, OuiaSupport<HTMLElement, ResourceBreadcrumb> {

    // ------------------------------------------------------ factory

    /** Creates a new breadcrumb for the given resource address. */
    public static ResourceBreadcrumb resourceBreadcrumb(AddressTemplate template, Metadata metadata) {
        return new ResourceBreadcrumb(template, metadata);
    }

    // ------------------------------------------------------ callback

    /** Handler invoked when a non-active breadcrumb segment is clicked. */
    @FunctionalInterface
    public interface SegmentHandler {

        /**
         * @param item     the breadcrumb item that was clicked
         * @param template the address template up to and including the clicked segment
         * @param depth    the 0-based position of the segment in the template ({@code -1} for the root {@code /})
         */
        void onSegmentClick(BreadcrumbItem item, AddressTemplate template, int depth);
    }

    // ------------------------------------------------------ instance

    private final HTMLElement root;
    private final List<SegmentHandler> segmentHandler;

    ResourceBreadcrumb(AddressTemplate template, Metadata metadata) {
        this.segmentHandler = new ArrayList<>();

        org.patternfly.component.breadcrumb.Breadcrumb bc = breadcrumb();
        if (template.isEmpty()) {
            bc.addItem(breadcrumbItem("root", "/")
                    .onClick((event, item) -> {
                        event.preventDefault();
                        event.stopPropagation();
                        fireSegmentClick(event, item, AddressTemplate.root(), -1);
                    }));
        } else {
            bc.addItem(breadcrumbItem("root", "/")
                    .onClick((event, item) -> {
                        event.preventDefault();
                        event.stopPropagation();
                        fireSegmentClick(event, item, AddressTemplate.root(), -1);
                    }));
            AddressTemplate current = AddressTemplate.root();
            int depth = 0;
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
                    final int segmentDepth = depth;
                    item.onClick((event, bi) -> {
                        event.preventDefault();
                        event.stopPropagation();
                        fireSegmentClick(event, bi, target, segmentDepth);
                    });
                }
                bc.addItem(item);
                depth++;
            }
        }
        this.root = bc.element();
        initOuia(OuiaIds.RESOURCE_BREADCRUMB);
    }

    @Override
    public String ouiaComponentType() {
        return "halOP/ResourceBreadcrumb";
    }

    @Override
    public ResourceBreadcrumb that() {
        return this;
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ events

    /** Registers a handler invoked when a non-active breadcrumb segment is clicked. */
    public ResourceBreadcrumb onSegmentClick(SegmentHandler segmentHandler) {
        this.segmentHandler.add(segmentHandler);
        return this;
    }

    // ------------------------------------------------------ internal

    private void fireSegmentClick(Event event, BreadcrumbItem item, AddressTemplate template, int depth) {
        event.preventDefault();
        event.stopPropagation();
        for (SegmentHandler handler : segmentHandler) {
            handler.onSegmentClick(item, template, depth);
        }
    }

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

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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.jboss.elemento.IsElement;
import org.jboss.elemento.TypedBuilder;
import org.patternfly.component.HasItems;
import org.patternfly.component.list.DescriptionList;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.resources.HalClasses.view;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.style.Breakpoint._2xl;
import static org.patternfly.style.Breakpoint.lg;
import static org.patternfly.style.Breakpoint.md;
import static org.patternfly.style.Breakpoint.sm;
import static org.patternfly.style.Breakpoint.xl;
import static org.patternfly.style.Breakpoints.breakpoints;
import static org.patternfly.style.Orientation.horizontal;
import static org.patternfly.style.Orientation.vertical;

/** Element to view an existing resource */
class ResourceView implements
        TypedBuilder<HTMLElement, ResourceView>,
        IsElement<HTMLElement>,
        HasItems<HTMLElement, ResourceView, ViewItem> {

    private final DescriptionList dl;
    private final List<BiConsumer<ResourceView, ViewItem>> onAdd;
    private final List<BiConsumer<ResourceView, ViewItem>> onRemove;
    private final Map<String, ViewItem> items;

    ResourceView() {
        this.items = new LinkedHashMap<>();
        this.onAdd = new ArrayList<>();
        this.onRemove = new ArrayList<>();
        this.dl = descriptionList().css(halComponent(resource, view))
                .orientation(breakpoints(
                        sm, vertical,
                        md, horizontal,
                        lg, horizontal,
                        xl, horizontal,
                        _2xl, horizontal));
    }

    @Override
    public HTMLElement element() {
        return dl.element();
    }

    @Override
    public ResourceView that() {
        return this;
    }

    @Override
    public ResourceView add(ViewItem item) {
        items.put(item.identifier(), item);
        dl.addItem(item.descriptionListGroup);
        onAdd.forEach(bc -> bc.accept(this, item));
        return this;
    }

    @Override
    public Iterator<ViewItem> iterator() {
        return items.values().iterator();
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean contains(String identifier) {
        return items.containsKey(identifier);
    }

    @Override
    public ViewItem item(String identifier) {
        return items.get(identifier);
    }

    @Override
    public void removeItem(String identifier) {
        ViewItem item = items.remove(identifier);
        failSafeRemoveFromParent(item);
        if (item != null) {
            onRemove.forEach(bc -> bc.accept(this, item));
        }
    }

    @Override
    public void clear() {
        dl.clear();
        items.values().forEach(item -> onRemove.forEach(bc -> bc.accept(this, item)));
        items.clear();
    }

    // ------------------------------------------------------ events

    @Override
    public ResourceView onAdd(BiConsumer<ResourceView, ViewItem> onAdd) {
        this.onAdd.add(onAdd);
        return this;
    }

    @Override
    public ResourceView onRemove(BiConsumer<ResourceView, ViewItem> onRemove) {
        this.onRemove.add(onRemove);
        return this;
    }
}

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

import java.util.HashMap;
import java.util.Map;

import org.jboss.elemento.IsElement;
import org.jboss.elemento.TypedBuilder;
import org.patternfly.component.HasIdentifier;
import org.patternfly.component.list.DescriptionListGroup;
import org.patternfly.component.list.DescriptionListTerm;
import org.patternfly.core.ComponentContext;

import elemental2.dom.HTMLElement;

import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;

/** An item for a {@link ResourceView} based on a {@link DescriptionListGroup} */
class ViewItem implements
        ManagerItem<ViewItem>,
        TypedBuilder<HTMLElement, ViewItem>,
        IsElement<HTMLElement>,
        ComponentContext<HTMLElement, ViewItem>,
        HasIdentifier<HTMLElement, ViewItem> {

    final DescriptionListGroup descriptionListGroup;
    private final String identifier;
    private final Map<String, Object> data;

    ViewItem(String identifier, DescriptionListTerm descriptionListTerm, HTMLElement valueElement) {
        this.identifier = identifier;
        this.data = new HashMap<>();
        this.descriptionListGroup = descriptionListGroup(identifier)
                .addTerm(descriptionListTerm)
                .addDescription(descriptionListDescription()
                        .add(valueElement));
    }

    @Override
    public HTMLElement element() {
        return descriptionListGroup.element();
    }

    @Override
    public ViewItem that() {
        return this;
    }

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public <T> ViewItem store(String key, T value) {
        data.put(key, value);
        return this;
    }

    @Override
    public boolean has(String key) {
        return data.containsKey(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (data.containsKey(key)) {
            return (T) data.get(key);
        }
        return null;
    }
}

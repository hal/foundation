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
package org.jboss.hal.ui.resource.view;
import org.jboss.hal.ui.resource.ResourceAttribute;

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;

/**
 * Strategy interface for creating specialised {@link ViewItem} instances based on attribute template and metadata.
 * Implementations are registered in {@link ViewItemProviders#specialViewItems} and consulted by
 * {@link ViewItemFactory#viewItem(AddressTemplate, Metadata, ResourceAttribute)} before falling back to the default
 * rendering.
 */
public interface ViewItemProvider {

    /** Tests whether this provider handles the given attribute. Must be fast -- executed for every view item. */
    boolean test(AddressTemplate template, Metadata metadata, ResourceAttribute ra);

    /** Creates a specialised view item for the given attribute. Only called when {@link #test} returns {@code true}. */
    ViewItem viewItem(AddressTemplate template, Metadata metadata, ResourceAttribute ra);
}

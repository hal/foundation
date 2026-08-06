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
package org.jboss.hal.ui.resource.extension;

import java.util.Set;

import org.jboss.hal.env.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.ui.resource.shell.ResourceHeader;

import elemental2.promise.Promise;

/**
 * Extension point for providing custom resource headers. Implementations are discovered via CDI and registered in
 * {@link ResourceHeaderRegistry}.
 * <p>
 * Each provider declares:
 * <ul>
 *     <li>{@link #scopes()} — the address template patterns it applies to (supports wildcards, matched via best-prefix)</li>
 *     <li>{@link #appliesTo(Environment, AddressTemplate)} — runtime activation condition</li>
 *     <li>{@link #createHeader(AddressTemplate, Metadata, ResourceHeader)} — the custom header factory</li>
 * </ul>
 * <p>
 * Providers that need runtime data (attribute values) should inject the required services (e.g., {@code Dispatcher},
 * {@code StatementContext}) via CDI and load data themselves in
 * {@link #createHeader(AddressTemplate, Metadata, ResourceHeader)}.
 * <p>
 * Providers can fully replace the default header or augment it:
 * {@snippet :
 * // Full replacement — ignore defaultHeader
 * public Promise<ResourceHeader> createHeader(AddressTemplate template, Metadata metadata, ResourceHeader defaultHeader) {
 *     return Promise.resolve(resourceHeader(template, metadata));
 * }
 *}
 * {@snippet :
 * // Augmentation — add extra content after loading data
 * public Promise<ResourceHeader> createHeader(AddressTemplate template, Metadata metadata, ResourceHeader defaultHeader) {
 *     return Promise.resolve(defaultHeader.add(p().text("Extra information")));
 * }
 *}
 */
public interface ResourceHeaderProvider {

    /**
     * The address template patterns this provider applies to. Supports wildcards (e.g.,
     * {@code /subsystem=datasources/data-source=*}). Matched via best-prefix against the concrete template being rendered.
     */
    Set<AddressTemplate> scopes();

    /**
     * Additional runtime activation condition beyond structural {@link #scopes()} matching. While {@link #scopes()} defines the
     * address patterns this provider applies to, this method handles runtime conditions such as operation mode, stability level,
     * or product version. It can also inspect concrete template values such as the resource name in the last segment. Defaults
     * to {@code true}.
     *
     * @param environment the runtime environment (operation mode, stability, product version, etc.)
     * @param template    the concrete resource template being rendered
     */
    default boolean appliesTo(Environment environment, AddressTemplate template) {
        return true;
    }

    /**
     * Creates a custom header for the given resource. Returns a promise to allow providers to load runtime data asynchronously
     * before building the header.
     *
     * @param template      the concrete resource template being rendered
     * @param metadata      the resource's management model metadata
     * @param defaultHeader the default {@link ResourceHeader}, fully configured. The provider can use it for augmentation (wrap
     *                      with extra content) or ignore it for full replacement.
     */
    Promise<ResourceHeader> createHeader(AddressTemplate template, Metadata metadata, ResourceHeader defaultHeader);
}

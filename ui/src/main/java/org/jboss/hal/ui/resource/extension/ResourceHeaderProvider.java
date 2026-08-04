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

import org.jboss.elemento.IsElement;
import org.jboss.hal.env.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.ui.resource.ResourceHeader;

import elemental2.dom.HTMLElement;

/**
 * Extension point for providing custom resource headers. Implementations are discovered via CDI and registered in
 * {@link ResourceHeaderRegistry}.
 * <p>
 * Each provider declares:
 * <ul>
 *     <li>{@link #scope()} — the address template pattern it applies to (supports wildcards, matched via best-prefix)</li>
 *     <li>{@link #appliesTo(Environment, AddressTemplate)} — runtime activation condition</li>
 *     <li>{@link #createHeader(ResourceContext, ResourceHeader)} — the custom header factory</li>
 * </ul>
 * <p>
 * Providers can fully replace the default header or augment it by wrapping the passed {@code defaultHeader}:
 * <pre>
 * // Full replacement — ignore defaultHeader
 * public IsElement&lt;HTMLElement&gt; createHeader(ResourceContext context, ResourceHeader defaultHeader) {
 *     return content().add(title(1, _3xl, "Custom Title"));
 * }
 *
 * // Augmentation — wrap defaultHeader with extra content
 * public IsElement&lt;HTMLElement&gt; createHeader(ResourceContext context, ResourceHeader defaultHeader) {
 *     return div()
 *             .add(defaultHeader)
 *             .add(button("Test Connection"));
 * }
 * </pre>
 */
public interface ResourceHeaderProvider {

    /**
     * The address template pattern this provider applies to. Supports wildcards
     * (e.g., {@code /subsystem=datasources/data-source=*}). Matched via best-prefix against the concrete template
     * being rendered.
     */
    AddressTemplate scope();

    /**
     * Whether this provider applies for the given environment and concrete resource template. Called at lookup time
     * when the environment is fully populated. Defaults to {@code true}.
     *
     * @param environment the runtime environment (operation mode, stability, product version, etc.)
     * @param template    the concrete resource template being rendered
     */
    default boolean appliesTo(Environment environment, AddressTemplate template) {
        return true;
    }

    /**
     * Creates a custom header for the given resource.
     *
     * @param context       the resource context with template, metadata, and current attribute values
     * @param defaultHeader the default {@link ResourceHeader}, fully configured but lazily built. The provider can
     *                      use it for augmentation (wrap with extra content) or ignore it for full replacement
     *                      (no build cost if unused).
     */
    IsElement<HTMLElement> createHeader(ResourceContext context, ResourceHeader defaultHeader);
}

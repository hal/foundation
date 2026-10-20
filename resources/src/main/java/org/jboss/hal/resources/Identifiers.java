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
package org.jboss.hal.resources;

import org.jboss.elemento.Id;

/**
 * Static identifiers used by components that implement {@code HasIdentifier}. These identifiers are synchronized with the dave
 * test suite and serve as the contract for element identification using {@code [data-identifier="..."]} selectors.
 *
 * <p>
 * There are two kinds of identifiers:
 *
 * <ol>
 * <li><strong>Static identifiers</strong> — string constants defined in this interface (e.g. {@link #CONFIGURATION_COLUMN},
 * {@link #SUBSYSTEMS_ITEM}). Use these for components whose identity is known at compile time.</li>
 * <li><strong>Dynamic identifiers</strong> — composed at runtime via {@link #build(String, String...)} or
 * {@link #fromTemplate(String)}. Use these for components whose identity depends on runtime data (e.g. resource names from
 * DMR, address templates).</li>
 * </ol>
 *
 * @see OuiaIds
 */
public interface Identifiers {

    String ALL_TASKS = "all-tasks";
    String BREADCRUMB_ROOT = "root";
    String CONFIGURATION_COLUMN = "configuration-column";
    String INBOUND_ITEM = "inbound";
    String INTERFACE_COLUMN = "interface-column";
    String INTERFACES_ITEM = "interfaces-item";
    String OUTBOUND_LOCAL_ITEM = "outbound-local";
    String OUTBOUND_REMOTE_ITEM = "outbound-remote";
    String PATH_COLUMN = "path-column";
    String PATHS_ITEM = "paths-item";
    String SOCKET_BINDING_GROUP_COLUMN = "socket-binding-group-column";
    String SOCKET_BINDING_INBOUND = "socket-binding-inbound";
    String SOCKET_BINDING_OUTBOUND_LOCAL = "socket-binding-outbound-local";
    String SOCKET_BINDING_OUTBOUND_REMOTE = "socket-binding-outbound-remote";
    String SOCKET_BINDING_TYPE = "socket-binding-type";
    String SOCKET_BINDINGS_ITEM = "socket-bindings-item";
    String SUBSYSTEM_COLUMN = "subsystem-column";
    String SUBSYSTEMS_ITEM = "subsystems-item";
    String SYSTEM_PROPERTIES_ITEM = "system-properties-item";
    String SYSTEM_PROPERTY_COLUMN = "system-property-column";

    /**
     * Composes an identifier from segments, delegating to {@link Id#build(String, String...)}.
     *
     * @param first the first segment
     * @param rest  additional segments
     * @return a composite identifier like {@code first-rest[0]-rest[1]-...}
     */
    static String build(String first, String... rest) {
        return Id.build(first, rest);
    }

    /**
     * Produces an identifier from an address template string. Mirrors the logic of
     * {@code AddressTemplate.identifier()} by replacing special characters ({@code /}, {@code =}, {@code :}, {@code *}) with
     * safe delimiters before passing through {@link Id#build(String, String...)}.
     *
     * @param template the address template string (e.g. {@code "subsystem=datasources/data-source=*"})
     * @return a safe identifier derived from the template
     */
    static String fromTemplate(String template) {
        if (template == null || template.isEmpty()) {
            return "root";
        }
        String safeTemplate = template
                .replace("/", "-s-")
                .replace("=", "-e-")
                .replace(":", "-c-")
                .replace("*", "-w-");
        return Id.build(safeTemplate);
    }
}

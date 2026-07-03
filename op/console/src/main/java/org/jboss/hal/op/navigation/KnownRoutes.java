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
package org.jboss.hal.op.navigation;

/**
 * Constants for all well-known route strings in the halOP console. Each constant defines a URL pattern with named parameters
 * (prefixed by {@code :}) and optional trailing parameters (suffixed by {@code ?}).
 * <p>
 * These constants are referenced by {@link org.jboss.hal.ui.navigation.RouteBinding}s in the {@link RouteRegistryProducer} and
 * by {@link org.jboss.elemento.router.Route @Route} annotations on page classes.
 *
 * @see org.jboss.hal.ui.navigation.RouteBinding
 * @see org.jboss.hal.ui.navigation.RouteRegistry
 */
public interface KnownRoutes {

    // ------------------------------------------------------ fallback route

    String DEFAULT_ROUTE = "/management-model/:selection?";

    // ------------------------------------------------------ all known routes (a-z)

    String INTERFACE_ROUTE = "/configuration/interface/:name";
    String PATH_ROUTE = "/configuration/path/:name";
    String SOCKET_BINDING_GROUP_ROUTE = "/configuration/socket-binding-group/:name";
    String SOCKET_BINDING_ROUTE = "/configuration/socket-binding-group/:group/socket-binding/:name";
    String SUBSYSTEM_ROUTE = "/configuration/subsystem/:name/:selection?";
    String SYSTEM_PROPERTY_ROUTE = "/configuration/system-property/:name";
}

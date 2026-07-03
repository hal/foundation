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
package org.jboss.hal.op.configuration.socket;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.elemento.router.Route;
import org.jboss.hal.ui.navigation.RouteBindingPage;
import org.jboss.hal.ui.navigation.RouteRegistry;

import static org.jboss.hal.op.navigation.KnownRoutes.SOCKET_BINDING_ROUTE;

/** Detail page for a socket binding within a socket binding group. Displays the binding's attributes such as port, interface and multicast settings. */
@Dependent
@Route("/configuration/socket-binding-group/:group/socket-binding/:name")
public class SocketBindingPage extends RouteBindingPage {

    @Inject
    public SocketBindingPage(RouteRegistry registry) {
        super(registry, SOCKET_BINDING_ROUTE);
    }
}

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
package org.jboss.hal.op.configuration;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.elemento.router.Route;
import org.jboss.hal.ui.navigation.RouteBindingPage;
import org.jboss.hal.ui.navigation.RouteRegistry;

import static org.jboss.hal.op.navigation.KnownRoutes.SUBSYSTEM_ROUTE;

/** Detail page for viewing and editing a single subsystem resource. */
@Dependent
@Route("/configuration/subsystem/:name/:selection?")
public class SubsystemPage extends RouteBindingPage {

    @Inject
    public SubsystemPage(RouteRegistry registry) {
        super(registry, SUBSYSTEM_ROUTE);
    }
}

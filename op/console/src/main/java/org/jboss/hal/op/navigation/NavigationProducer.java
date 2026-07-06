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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.jboss.elemento.router.PlaceManager;
import org.jboss.hal.resources.OuiaIds;
import org.patternfly.component.navigation.Navigation;
import org.patternfly.component.navigation.NavigationType.Horizontal;

import static org.patternfly.component.navigation.NavigationItem.navigationItem;

/** CDI producer for the primary horizontal {@link Navigation} component used in the masthead. */
public class NavigationProducer {

    @Inject PlaceManager placeManager;

    /** Produces an application-scoped horizontal navigation bar with items for all top-level routes. */
    @Produces
    @ApplicationScoped
    public Navigation navigation() {
        return Navigation.navigation(Horizontal.primary).ouiaId(OuiaIds.NAV)
                // IDs must match the routes!
                .addItem(navigationItem("/", "Dashboard", placeManager.href("/"))
                        .ouiaId(OuiaIds.NAV_DASHBOARD))
                .addItem(navigationItem("/deployments", "Deployments", placeManager.href("/deployments"))
                        .ouiaId(OuiaIds.NAV_DEPLOYMENTS))
                .addItem(navigationItem("/configuration", "Configuration", placeManager.href("/configuration"))
                        .ouiaId(OuiaIds.NAV_CONFIGURATION))
                .addItem(navigationItem("/runtime", "Runtime", placeManager.href("/runtime"))
                        .ouiaId(OuiaIds.NAV_RUNTIME))
                .addItem(navigationItem("/tasks", "Tasks", placeManager.href("/tasks"))
                        .ouiaId(OuiaIds.NAV_TASKS))
                .addItem(navigationItem("/management-model", "Management model", placeManager.href("/management-model"))
                        .ouiaId(OuiaIds.NAV_MODEL_BROWSER));
    }
}

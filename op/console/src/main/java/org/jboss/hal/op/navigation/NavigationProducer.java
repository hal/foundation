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
import org.patternfly.component.navigation.Navigation;
import org.patternfly.component.navigation.NavigationType.Horizontal;

import static org.patternfly.component.navigation.NavigationItem.navigationItem;

public class NavigationProducer {

    @Inject PlaceManager placeManager;

    @Produces
    @ApplicationScoped
    public Navigation navigation() {
        return Navigation.navigation(Horizontal.primary)
                // IDs must match the routes!
                .addItem(navigationItem("/", "Dashboard", placeManager.href("/")))
                .addItem(navigationItem("/deployments", "Deployments", placeManager.href("/deployments")))
                .addItem(navigationItem("/configuration", "Configuration", placeManager.href("/configuration")))
                .addItem(navigationItem("/tasks", "Tasks", placeManager.href("/tasks")))
                .addItem(navigationItem("/runtime", "Runtime", placeManager.href("/runtime")))
                .addItem(navigationItem("/management-model", "Management model", placeManager.href("/management-model")));
    }
}

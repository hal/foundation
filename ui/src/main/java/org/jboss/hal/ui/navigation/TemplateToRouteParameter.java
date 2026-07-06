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
package org.jboss.hal.ui.navigation;

import org.jboss.hal.meta.AddressTemplate;

/**
 * Extracts route parameters from a resolved address template. This is the <strong>template → route</strong> direction: when
 * navigating to a resource (finder click, out-of-scope link, "go to resource"), a resolved {@link AddressTemplate} is converted
 * into the parameter array required by {@link org.jboss.elemento.router.PlaceManager#goTo(String, String...)}.
 *
 * @see RouteBinding
 */
@FunctionalInterface
public interface TemplateToRouteParameter {

    /**
     * Extracts the route parameters for the given address template and matched route.
     *
     * @param template the address template being navigated to
     * @param route    the route that was resolved for this template
     * @return the parameter array to pass to the place manager
     */
    String[] parameter(AddressTemplate template, String route);
}

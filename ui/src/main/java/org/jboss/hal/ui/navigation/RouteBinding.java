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

import org.jboss.elemento.router.Parameter;
import org.jboss.hal.meta.AddressTemplate;

/**
 * Co-locates the bidirectional mapping between a route and an address template.
 * <p>
 * Every resource page needs two conversions:
 * <dl>
 *     <dt><strong>Route → Template</strong> ({@link #toTemplate})</dt>
 *     <dd>When a page loads from a URL, its route parameters must be converted into a resolved {@link AddressTemplate} so the
 *     model browser knows which resource to display.</dd>
 *     <dt><strong>Template → Route</strong> ({@link #toRoute})</dt>
 *     <dd>When navigating to a resource (finder click, out-of-scope link, "go to resource"), a resolved
 *     {@link AddressTemplate} must be converted into route parameters for
 *     {@link org.jboss.elemento.router.PlaceManager#goTo(String, String...)}.</dd>
 * </dl>
 *
 * @param route      the route string (e.g., {@code /configuration/interface/:name})
 * @param template   the wildcard address template (e.g., {@code interface=*})
 * @param toTemplate resolves route parameters into a concrete template (route → template direction)
 * @param toRoute    extracts route parameters from a resolved template (template → route direction)
 */
public record RouteBinding(
        String route,
        AddressTemplate template,
        RouteParameterToTemplate toTemplate,
        TemplateToRouteParameter toRoute) {

    AddressTemplate resolve(Parameter parameter) {
        return toTemplate.resolve(template, parameter);
    }

    String[] routeParams(AddressTemplate template) {
        return toRoute.parameter(template, route);
    }
}

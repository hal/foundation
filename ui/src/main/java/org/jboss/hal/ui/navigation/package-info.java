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

/**
 * Route binding infrastructure for mapping between client-side routes and WildFly management address templates.
 * <p>
 * The central abstraction is the {@link org.jboss.hal.ui.navigation.RouteBinding RouteBinding} record, which co-locates the
 * bidirectional mapping between a route (e.g., {@code /configuration/interface/:name}) and an address template (e.g.,
 * {@code interface=*}). Every resource page needs both directions:
 * <dl>
 *     <dt><strong>Route → Template</strong> ({@link org.jboss.hal.ui.navigation.RouteParameterToTemplate
 *     RouteParameterToTemplate})</dt>
 *     <dd>When a page loads from a URL, route parameters are converted into a resolved
 *     {@link org.jboss.hal.meta.AddressTemplate AddressTemplate} so the model browser knows which resource to display.</dd>
 *     <dt><strong>Template → Route</strong> ({@link org.jboss.hal.ui.navigation.TemplateToRouteParameter
 *     TemplateToRouteParameter})</dt>
 *     <dd>When navigating to a resource (finder click, out-of-scope link, "go to resource"), a resolved address template is
 *     converted into route parameters for
 *     {@link org.jboss.elemento.router.PlaceManager#goTo(String, String...) PlaceManager.goTo()}.</dd>
 * </dl>
 *
 * <h2>Key Types</h2>
 * <ul>
 *     <li>{@link org.jboss.hal.ui.navigation.RouteBinding RouteBinding} — pairs a route with its template and both conversion
 *     functions</li>
 *     <li>{@link org.jboss.hal.ui.navigation.RouteRegistry RouteRegistry} — holds all bindings; provides lookup by route
 *     (exact) or by template (best-prefix match), plus {@code goTo()} for one-step navigation</li>
 *     <li>{@link org.jboss.hal.ui.navigation.RouteBindingPage RouteBindingPage} — base class for resource pages; subclasses
 *     only need a route string and the registry</li>
 * </ul>
 * <p>
 * This package is app-agnostic. App-specific route constants and CDI producers live in the application module (e.g.,
 * {@code org.jboss.hal.op.navigation} for halOP).
 *
 * @see org.jboss.hal.ui.navigation.RouteBinding
 * @see org.jboss.hal.ui.navigation.RouteRegistry
 */
package org.jboss.hal.ui.navigation;

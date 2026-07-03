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
 * Navigation infrastructure for the halOP console.
 * <p>
 * This package contains app-specific routing constants and CDI producers:
 * <ul>
 *     <li>{@link org.jboss.hal.op.navigation.KnownRoutes KnownRoutes} — route string constants for all halOP pages</li>
 *     <li>{@link org.jboss.hal.op.navigation.RouteRegistryProducer RouteRegistryProducer} — CDI producer that creates the
 *     {@link org.jboss.hal.ui.navigation.RouteRegistry RouteRegistry} and registers all halOP route bindings</li>
 * </ul>
 * <p>
 * The app-agnostic route binding infrastructure ({@link org.jboss.hal.ui.navigation.RouteBinding RouteBinding},
 * {@link org.jboss.hal.ui.navigation.RouteRegistry RouteRegistry},
 * {@link org.jboss.hal.ui.navigation.RouteBindingPage RouteBindingPage}) lives in the
 * {@link org.jboss.hal.ui.navigation} package in the UI module.
 * <p>
 * This package also provides CDI producers for the {@link org.jboss.elemento.router.PlaceManager PlaceManager} and the primary
 * {@link org.patternfly.component.navigation.Navigation Navigation} component, along with fallback pages for unresolved routes
 * ({@link org.jboss.hal.op.navigation.NotFound NotFound}) and missing data
 * ({@link org.jboss.hal.op.navigation.NoData NoData}).
 *
 * <h2>Adding a New Resource Page</h2>
 * <ol>
 *     <li>Add a route constant to {@link org.jboss.hal.op.navigation.KnownRoutes KnownRoutes}</li>
 *     <li>Register a {@link org.jboss.hal.ui.navigation.RouteBinding RouteBinding} in
 *     {@link org.jboss.hal.op.navigation.RouteRegistryProducer RouteRegistryProducer} with both conversion functions</li>
 *     <li>Create a page class extending {@link org.jboss.hal.ui.navigation.RouteBindingPage RouteBindingPage} with a
 *     {@code @Route} annotation and a constructor that passes the route constant and the registry</li>
 * </ol>
 */
package org.jboss.hal.op.navigation;

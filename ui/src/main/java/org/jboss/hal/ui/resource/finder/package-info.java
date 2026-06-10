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
 * Finder navigation support and resource page routing.
 * <p>
 * Contains utilities for building finder-style column navigation from WildFly management resources, including
 * asynchronous child resource loading and metadata-based preview generation.
 * <p>
 * Key components:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.finder.FinderPage}</dt>
 * <dd>Base page for top-level finder-based navigation with URL synchronisation and path restoration.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.finder.FinderSupport}</dt>
 * <dd>Helpers for child resource loading and metadata-based preview generation in finder columns.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.finder.ResourcePage}</dt>
 * <dd>Base router page component that extracts resource addresses from route parameters and displays a model browser.</dd>
 * </dl>
 */
package org.jboss.hal.ui.resource.finder;

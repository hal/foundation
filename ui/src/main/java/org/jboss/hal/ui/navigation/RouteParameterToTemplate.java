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
 * Resolves route parameters into a concrete address template. This is the <strong>route → template</strong> direction: when a
 * page loads, its route parameters (e.g., {@code :name}, {@code :group}) are used to replace wildcards in the base template.
 *
 * @see RouteBinding
 */
@FunctionalInterface
public interface RouteParameterToTemplate {

    /**
     * Resolves the given base template using the route parameters.
     *
     * @param template  the base address template with wildcards (e.g., {@code interface=*})
     * @param parameter the route parameters extracted from the URL
     * @return the resolved address template with wildcards replaced by parameter values
     */
    AddressTemplate resolve(AddressTemplate template, Parameter parameter);
}

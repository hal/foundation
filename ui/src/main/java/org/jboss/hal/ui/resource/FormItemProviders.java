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
package org.jboss.hal.ui.resource;

import java.util.ArrayList;
import java.util.List;

class FormItemProviders {

    static final List<FormItemProvider> specialFormItems = new ArrayList<>();

    static {
        // TODO Add support to create and return specific form items based on the address template and attribute.
        // For instance /subsystem=logging/pattern-formatter=*@color-map
        // Accepts a comma delimited list of colors to be used for different levels with a pattern formatter.
        // The format for the color mapping pattern is level-name:color-name.
        // Valid Levels: severe, fatal, error, warn, warning, info, debug, trace, config, fine, finer, finest
        // Valid Colors: black, green, red, yellow, blue, magenta, cyan, white, brightblack, brightred, brightgreen, brightblue,
        // brightyellow, brightmagenta, brightcyan, brightwhit
        // Another example is the 'credential-reference' complex attribute used all over the place.

        // The first provider wins!
        // specialViewItems.add(new FormItemProvider() {
        //     @Override
        //     public boolean test(AddressTemplate template, Metadata metadata, ResourceAttribute ra, FormItemFlags flags) {
        //         return ...;
        //     }
        //
        //     @Override
        //     public ViewItem formItem(AddressTemplate template, Metadata metadata, ResourceAttribute ra, FormItemFlags flags) {
        //         return ...;
        //     }
        // });
    }
}

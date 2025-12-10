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

class ViewItemProviders {

    static final List<ViewItemProvider> specialViewItems = new ArrayList<>();

    static {
        // TODO Add support to create and return specific view items based on the address template and/or attribute.
        // For example the 'credential-reference' complex attribute used all over the place.

        // The first provider wins!
        // specialViewItems.add(new ViewItemProvider() {
        //     @Override
        //     public boolean test(AddressTemplate template, Metadata metadata, ResourceAttribute ra) {
        //         return ...;
        //     }
        //
        //     @Override
        //     public ViewItem viewItem(AddressTemplate template, Metadata metadata, ResourceAttribute ra) {
        //         return ...;
        //     }
        // });
    }
}

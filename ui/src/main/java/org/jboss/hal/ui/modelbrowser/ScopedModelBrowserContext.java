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
package org.jboss.hal.ui.modelbrowser;

import org.jboss.hal.meta.AddressTemplate;

/**
 * Context for a {@link ModelBrowser} that displays a subtree of the management model. Templates starting with the root
 * template are in scope; out-of-scope navigation uses the {@code PlaceManager} to route to the target's configuration page.
 */
public class ScopedModelBrowserContext implements ModelBrowserContext {

    private final AddressTemplate rootTemplate;

    /** Creates a scoped model browser context with the given root template defining the scope boundary. */
    public ScopedModelBrowserContext(AddressTemplate rootTemplate) {
        this.rootTemplate = rootTemplate;
    }

    @Override
    public boolean inScope(AddressTemplate template) {
        return template.template.startsWith(rootTemplate.template);
    }

    @Override
    public void navigate(AddressTemplate template) {
        // TODO Use PlaceManager to navigate to the target's configuration page
        //  uic().placeManager().goTo(...)
    }
}

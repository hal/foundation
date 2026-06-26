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
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.SelectInTree;

/**
 * Context for a {@link ModelBrowser} that displays the full management model tree. Everything is always in scope, and
 * navigation dispatches a {@link SelectInTree} event on the browser's root element.
 */
public class FullModelBrowserContext implements ModelBrowserContext {

    private ModelBrowser modelBrowser;

    /** Creates a full model browser context. Call {@link #init(ModelBrowser)} before use. */
    public FullModelBrowserContext() {
    }

    /** Binds this context to the given model browser for event dispatch. */
    void init(ModelBrowser modelBrowser) {
        this.modelBrowser = modelBrowser;
    }

    @Override
    public boolean inScope(AddressTemplate template) {
        return true;
    }

    @Override
    public void navigate(AddressTemplate template) {
        if (modelBrowser != null) {
            SelectInTree.dispatch(modelBrowser.element(), template);
        }
    }
}

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
 * Strategy interface that governs scope checking and navigation for a {@link ModelBrowser}. Implementations decide
 * whether a given address template is within the browser's navigable scope and how to navigate to out-of-scope targets.
 */
public interface ModelBrowserContext {

    /** Whether the given template is within this browser's navigable scope. */
    boolean inScope(AddressTemplate template);

    /** Navigate to the given template. */
    void navigate(AddressTemplate template);
}

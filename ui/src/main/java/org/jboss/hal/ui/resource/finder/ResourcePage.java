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
package org.jboss.hal.ui.resource.finder;

import org.jboss.elemento.router.LoadedData;
import org.jboss.elemento.router.Page;
import org.jboss.elemento.router.Parameter;
import org.jboss.elemento.router.Place;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.ui.modelbrowser.ModelBrowser;
import org.jboss.hal.ui.modelbrowser.ScopedModelBrowserContext;

import elemental2.dom.HTMLElement;

import static java.util.Collections.singletonList;
import static org.jboss.hal.resources.OuiaIds.PAGE_MODEL_BROWSER;
import static org.jboss.hal.ui.modelbrowser.ModelBrowser.modelBrowser;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.page.PageSection.pageSection;

/**
 * Base page that displays a {@link ModelBrowser} for a URL-encoded resource address extracted from a route parameter. Subclasses
 * only need to provide the route annotation and pass the parameter name to the constructor.
 */
public class ResourcePage implements Page {

    private final String parameterName;

    /**
     * Creates a resource page that extracts the encoded address from the given route parameter name.
     *
     * @param parameterName the route parameter name containing the URI-encoded address template
     */
    protected ResourcePage(String parameterName) {
        this.parameterName = parameterName;
    }

    /**
     * Decodes the address template from the route parameter and renders a {@link ModelBrowser} for it. If the parameter
     * is missing, an error empty state is shown instead.
     */
    @Override
    public Iterable<HTMLElement> elements(Place place, Parameter parameter, LoadedData data) {
        String address = parameter.get(parameterName);
        if (address != null) {
            AddressTemplate template = AddressTemplate.ofTrusted(address);
            ModelBrowser browser = modelBrowser(template, new ScopedModelBrowserContext(template));
            return singletonList(pageSection().ouiaId(PAGE_MODEL_BROWSER)
                    .add(browser)
                    .element());
        } else {
            return singletonList(pageSection().ouiaId(PAGE_MODEL_BROWSER)
                    .add(emptyState()
                            .status(danger)
                            .text("No resource address provided"))
                    .element());
        }
    }
}

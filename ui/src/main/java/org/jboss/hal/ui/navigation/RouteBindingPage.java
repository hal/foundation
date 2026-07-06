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

import java.util.Optional;

import org.jboss.elemento.router.LoadedData;
import org.jboss.elemento.router.Page;
import org.jboss.elemento.router.Parameter;
import org.jboss.elemento.router.Place;
import org.jboss.hal.meta.AddressTemplate;

import elemental2.dom.HTMLElement;

import static java.util.Collections.singletonList;
import static org.jboss.hal.resources.OuiaIds.PAGE_MODEL_BROWSER;
import static org.jboss.hal.ui.modelbrowser.ModelBrowser.modelBrowser;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.page.PageSection.pageSection;

/**
 * Base class for resource pages backed by a {@link RouteBinding} from the {@link RouteRegistry}. The binding provides the
 * address template and the route-to-template resolver; the registry provides out-of-scope navigation via
 * {@link RouteRegistry#goTo(AddressTemplate)}.
 * <p>
 * Subclasses only need to supply the route string and the registry. The page automatically handles the {@code :selection?}
 * route parameter for pages that support deep-linking into the resource tree.
 * <p>
 * Example:
 * <pre>
 * &#64;Dependent
 * &#64;Route("/configuration/interface/:name")
 * public class InterfacePage extends RouteBindingPage {
 *     &#64;Inject
 *     public InterfacePage(RouteRegistry registry) {
 *         super(registry, INTERFACE_ROUTE);
 *     }
 * }
 * </pre>
 *
 * @see RouteBinding
 * @see RouteRegistry
 */
public abstract class RouteBindingPage implements Page {

    // ------------------------------------------------------ instance

    private final RouteRegistry registry;
    private final String route;

    protected RouteBindingPage(RouteRegistry registry, String route) {
        this.registry = registry;
        this.route = route;
    }

    @Override
    public Iterable<HTMLElement> elements(Place place, Parameter parameter, LoadedData data) {
        HTMLElement element = registry.byRoute(route)
                .map(binding -> {
                    Optional<AddressTemplate> selection = parameter.has("selection")
                            ? AddressTemplate.ofUntrusted(parameter.get("selection"))
                            : Optional.empty();
                    return modelBrowser(binding.resolve(parameter), selection.orElse(null))
                            .element();
                })
                .orElse(emptyState()
                        .status(danger)
                        .text("Resource not found")
                        .element());

        return singletonList(pageSection().ouiaId(PAGE_MODEL_BROWSER)
                .add(element)
                .element());
    }
}

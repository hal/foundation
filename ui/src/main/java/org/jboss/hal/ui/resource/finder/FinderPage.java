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
import org.jboss.elemento.router.PlaceManager;
import org.patternfly.extension.finder.Finder;
import org.patternfly.extension.finder.FinderPath;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.history;
import static java.util.Collections.singletonList;
import static org.patternfly.extension.finder.Finder.finder;
import static org.patternfly.style.Classes.util;

/**
 * Base page for top-level finder-based navigation. Handles the common setup shared by all finder pages: creating the
 * {@link Finder}, parsing the optional {@code :finderPath?} route parameter, synchronising selection changes with the browser
 * URL via {@code pushState}, and restoring the selection on initial load.
 * <p>
 * Subclasses provide the {@link org.jboss.elemento.router.Route @Route} annotation with a route ending in
 * {@code /:finderPath?}, CDI annotations, and an implementation of {@link #configureFinder(Finder)} that adds the root column
 * and initial preview.
 *
 * @see ResourcePage
 */
public abstract class FinderPage implements Page {

    private final PlaceManager placeManager;
    private final String ouiaId;

    /**
     * Creates a finder page.
     *
     * @param placeManager the place manager used for URL generation
     * @param ouiaId       the OUIA component ID applied to the finder element
     */
    protected FinderPage(PlaceManager placeManager, String ouiaId) {
        this.placeManager = placeManager;
        this.ouiaId = ouiaId;
    }

    @Override
    public Iterable<HTMLElement> elements(Place place, Parameter parameter, LoadedData data) {
        Finder finder = finder().ouiaId(ouiaId).registerComponent().css(util("h-100"))
                .onAdd((theFinder, column) ->
                        column.onSelect((event, item, selected) -> {
                            if (selected) {
                                FinderPath fp = theFinder.activePath().toFinderPath();
                                String url;
                                if (fp.isEmpty()) {
                                    url = placeManager.href(place.path());
                                } else {
                                    url = placeManager.href(place.route(), fp.toString());
                                }
                                history.pushState(url, "", url);
                            }
                        }))
                .run(this::configureFinder);

        FinderPath finderPath = parameter.has("finderPath")
                ? FinderPath.parse(parameter.get("finderPath"))
                : null;
        if (finderPath != null && !finderPath.isEmpty()) {
            finder.select(finderPath);
        }
        return singletonList(finder.element());
    }

    /**
     * Called after the finder is created and wired for URL synchronization. Subclasses must add the root column and initial
     * preview here.
     *
     * @param finder the finder to configure
     */
    protected abstract void configureFinder(Finder finder);
}

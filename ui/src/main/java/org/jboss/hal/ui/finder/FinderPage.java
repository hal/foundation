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
package org.jboss.hal.ui.finder;

import org.jboss.elemento.router.LoadedData;
import org.jboss.elemento.router.Page;
import org.jboss.elemento.router.Parameter;
import org.jboss.elemento.router.Place;
import org.jboss.elemento.router.PlaceManager;
import org.patternfly.component.navigation.Navigation;
import org.patternfly.component.navigation.NavigationItem;
import org.patternfly.extension.finder.Finder;
import org.patternfly.extension.finder.FinderPath;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.history;
import static java.util.Collections.singletonList;
import static org.patternfly.extension.finder.Finder.finder;
import static org.patternfly.style.Classes.util;

/**
 * Base page for top-level finder-based navigation. Handles the common setup shared by all finder pages: creating the
 * {@link Finder}, parsing the optional {@code :finderPath?} route parameter, synchronizing selection changes with the browser
 * URL via {@code pushState}, and restoring the selection on an initial load.
 * <p>
 * Subclasses provide the {@link org.jboss.elemento.router.Route @Route} annotation with a route ending in
 * {@code /:finderPath?}, CDI annotations, and an implementation of {@link #configureFinder(Finder)} that adds the root column
 * and initial preview.
 */
public abstract class FinderPage implements Page {

    private final PlaceManager placeManager;
    private final Navigation navigation;
    private final String ouiaId;

    /**
     * Creates a finder page.
     *
     * @param placeManager the place manager used for URL generation
     * @param navigation   the top-level navigation whose item href is kept in sync with finder selections
     * @param ouiaId       the OUIA component ID applied to the finder element
     */
    protected FinderPage(PlaceManager placeManager, Navigation navigation, String ouiaId) {
        this.placeManager = placeManager;
        this.navigation = navigation;
        this.ouiaId = ouiaId;
    }

    @Override
    public Iterable<HTMLElement> elements(Place place, Parameter parameter, LoadedData data) {
        // Tracks whether the finder is restoring a previous selection from the URL.
        // During restoration, onSelect fires for each intermediate column — those must
        // not push history entries or browser back/forward breaks with duplicates.
        boolean[] restoring = {false};

        Finder finder = finder().ouiaId(ouiaId).registerComponent().css(util("h-100"))
                .onAdd((fndr, column) ->
                        column.onSelect((event, item, selected) -> {
                            if (selected && !restoring[0]) {
                                FinderPath fp = fndr.activePath().toFinderPath();
                                String url;
                                if (fp.isEmpty()) {
                                    url = placeManager.href(place.path());
                                } else {
                                    url = placeManager.href(place.route(), fp.toString());
                                }
                                history.pushState(url, "", url);
                                syncNavigationItem(place, url);
                            }
                        }))
                .run(this::configureFinder);

        FinderPath finderPath = parameter.has("finderPath")
                ? FinderPath.parse(parameter.get("finderPath"))
                : null;
        if (finderPath != null && !finderPath.isEmpty()) {
            restoring[0] = true;
            finder.select(finderPath).then(__ -> {
                restoring[0] = false;
                syncNavigationItem(place, placeManager.href(place.route(), finderPath.toString()));
                return null;
            });
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

    // Keeps the top-level navigation item in sync with the current finder selection.
    // Without this, clicking the nav item (e.g., "Configuration") would always navigate
    // to the bare route without a finderPath, losing the user's last selection.
    private void syncNavigationItem(Place place, String url) {
        NavigationItem ni = navigation.item(place.path());
        if (ni != null) {
            ni.href(url);
        }
    }
}

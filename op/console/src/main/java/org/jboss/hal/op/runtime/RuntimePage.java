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
package org.jboss.hal.op.runtime;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.elemento.router.LoadedData;
import org.jboss.elemento.router.Page;
import org.jboss.elemento.router.Parameter;
import org.jboss.elemento.router.Place;
import org.jboss.elemento.router.PlaceManager;
import org.jboss.elemento.router.Route;

import elemental2.dom.HTMLElement;

import static java.util.Collections.singletonList;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.router.Link.link;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.page.PageSection.pageSection;
import static org.patternfly.component.title.Title.title;
import static org.patternfly.style.Size._3xl;

@Dependent
@Route("/runtime")
public class RuntimePage implements Page {

    private final PlaceManager placeManager;

    @Inject
    public RuntimePage(PlaceManager placeManager) {
        this.placeManager = placeManager;
    }

    @Override
    public Iterable<HTMLElement> elements(Place place, Parameter parameter, LoadedData data) {
        return singletonList(pageSection()
                .add(content()
                        .add(title(1, _3xl, "Runtime"))
                        .add(p().text("Not yet implemented!"))
                        .add(p().add(link(placeManager, "/").text("Home"))))
                .element());
    }
}

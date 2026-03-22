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
package org.jboss.hal.op.configuration;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.elemento.router.LoadedData;
import org.jboss.elemento.router.Page;
import org.jboss.elemento.router.Parameter;
import org.jboss.elemento.router.Place;
import org.jboss.elemento.router.Route;
import org.jboss.hal.op.finder.ColumnRegistry;

import elemental2.dom.HTMLElement;

import static java.util.Collections.singletonList;
import static org.patternfly.extension.finder.Finder.finder;
import static org.patternfly.extension.finder.FinderPreview.finderPreview;
import static org.patternfly.style.Classes.util;

@Dependent
@Route("/configuration")
public class ConfigurationPage implements Page {

    private final ColumnRegistry columnRegistry;

    @Inject
    public ConfigurationPage(ColumnRegistry columnRegistry) {
        this.columnRegistry = columnRegistry;
    }

    @Override
    public Iterable<HTMLElement> elements(Place place, Parameter parameter, LoadedData data) {
        return singletonList(finder().css(util("h-100"))
                .addItem(columnRegistry.column(ConfigurationColumn.ID))
                .addPreview(finderPreview().css(util("p-md")))
                .element());
    }
}

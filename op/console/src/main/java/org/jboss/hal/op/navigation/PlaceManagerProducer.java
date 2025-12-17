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
package org.jboss.hal.op.navigation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.jboss.elemento.By;
import org.jboss.elemento.router.AnnotatedPlaces;
import org.jboss.elemento.router.PlaceManager;
import org.jboss.hal.env.Environment;
import org.jboss.hal.resources.Ids;
import org.kie.j2cl.tools.di.core.BeanManager;
import org.patternfly.component.navigation.Navigation;

public class PlaceManagerProducer {

    @Inject BeanManager beanManager;
    @Inject Environment environment;
    @Inject Navigation navigation;

    @Produces
    @ApplicationScoped
    public PlaceManager placeManager() {
        return new PlaceManager()
                .base(environment.base())
                .root(By.id(Ids.MAIN_ID))
                .title(title -> "HAL • " + title)
                .notFound(NotFound::new)
                .noData(NoData::new)
                .register(new AnnotatedPlaces(beanManager))
                .afterPlace((placeManager, place) -> navigation.select(place.route));
    }
}

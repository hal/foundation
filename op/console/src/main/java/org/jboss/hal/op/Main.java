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
package org.jboss.hal.op;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.jboss.elemento.router.PlaceManager;
import org.jboss.hal.core.Notifications;
import org.jboss.hal.env.Environment;
import org.jboss.hal.op.bootstrap.Bootstrap;
import org.jboss.hal.op.bootstrap.BootstrapError;
import org.jboss.hal.op.endpoint.EndpointStorage;
import org.kie.j2cl.tools.di.annotation.Application;
import org.kie.j2cl.tools.processors.annotations.GWT3EntryPoint;
import org.patternfly.component.navigation.Navigation;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.insertFirst;
import static org.jboss.hal.op.bootstrap.BootstrapErrorElement.bootstrapError;
import static org.jboss.hal.op.skeleton.ErrorSkeleton.errorSkeleton;
import static org.jboss.hal.op.skeleton.Skeleton.skeleton;

@Application(packages = {"org.jboss.hal"})
public class Main {

    @Inject Bootstrap bootstrap;
    @Inject EndpointStorage endpointStorage;
    @Inject Environment environment;
    @Inject PlaceManager placeManager;
    @Inject Navigation navigation;
    @Inject Notifications notifications;

    @GWT3EntryPoint
    public void onModuleLoad() {
        new MainBootstrap(this).initialize();
    }

    @PostConstruct
    void init() {
        bootstrap.run().subscribe(context -> {
            if (context.isSuccessful()) {
                insertFirst(document.body, skeleton(environment, endpointStorage, notifications).add(navigation));
                placeManager.start();
            } else {
                BootstrapError error = context.pop(BootstrapError.UNKNOWN);
                insertFirst(document.body, errorSkeleton().add(bootstrapError(error)));
            }
        });
    }
}

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

import org.jboss.elemento.logger.Logger;
import org.jboss.elemento.router.PlaceManager;
import org.jboss.hal.op.bootstrap.Bootstrap;
import org.jboss.hal.op.bootstrap.BootstrapError;
import org.jboss.hal.op.discover.Discover;
import org.jboss.hal.op.endpoint.EndpointStorage;
import org.patternfly.component.navigation.Navigation;
import org.treblereel.j2cl.processors.annotations.GWT3EntryPoint;

import io.crysknife.annotation.Application;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.Elements.insertFirst;
import static org.jboss.hal.op.bootstrap.BootstrapErrorElement.bootstrapError;
import static org.jboss.hal.op.skeleton.ErrorSkeleton.errorSkeleton;
import static org.jboss.hal.op.skeleton.Skeleton.skeleton;

/**
 * Entry point of the halOP console application. Initializes CDI, runs the bootstrap sequence, and either sets up the main
 * skeleton with navigation or shows an error page if the bootstrap fails.
 */
@Application(packages = {"org.jboss.hal"})
public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    @Inject Bootstrap bootstrap;
    @Inject Discover discover;
    @Inject EndpointStorage endpointStorage;
    @Inject Navigation navigation;
    @Inject PlaceManager placeManager;

    @GWT3EntryPoint
    public void onModuleLoad() {
        new MainBootstrap(this).initialize();
    }

    @PostConstruct
    void init() {
        bootstrap.run().subscribe(context -> {
            if (context.isSuccessful()) {
                logger.debug("Bootstrap completed");
                insertFirst(document.body, skeleton(endpointStorage, navigation));
                placeManager.start();
                discover.run().subscribe(__ -> logger.debug("Discovery completed"));
            } else {
                logger.debug("Bootstrap failed");
                BootstrapError error = context.pop(BootstrapError.UNKNOWN);
                insertFirst(document.body, errorSkeleton().add(bootstrapError(error)));
            }
        });
    }
}

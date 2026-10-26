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
package org.jboss.hal.mgt;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.hal.env.Environment;
import org.jboss.hal.env.Version;

import static org.jboss.hal.resources.Urls.MODEL_GRAPH_API;
import static org.jboss.hal.resources.Urls.replaceVersion;

/**
 * Computes the base URL for the MGT REST API from the connected WildFly version. Shared by all ModelGraph service classes.
 */
@ApplicationScoped
public class ModelGraphEndpoint {

    private static final int HTTP_PORT_BASE = 7000;

    private final Environment environment;

    @Inject
    public ModelGraphEndpoint(Environment environment) {
        this.environment = environment;
    }

    /** Returns a new {@code StringBuilder} pre-filled with the MGT API base URL for the current WildFly version. */
    public StringBuilder api() {
        return new StringBuilder(replaceVersion(MODEL_GRAPH_API, String.valueOf(port(environment.productVersion()))));
    }

    private int port(Version version) {
        return HTTP_PORT_BASE + version.major() * 10 + version.minor();
    }
}

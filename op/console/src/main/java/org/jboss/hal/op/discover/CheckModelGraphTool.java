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
package org.jboss.hal.op.discover;

import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.env.Version;

import elemental2.dom.RequestInit;
import elemental2.promise.Promise;

import static elemental2.dom.DomGlobal.fetch;

/**
 * Discovery task that checks whether a matching Model Graph Tool (MGT) container is running. The MGT port is derived from the
 * WildFly product version: {@code 7000 + major * 10 + minor}. For example, WildFly 41.0 maps to port 7410.
 *
 * <p>The task sends a GET request to {@code http://localhost:{port}/api/identity} and considers the MGT available if the
 * response status is 200.
 */
class CheckModelGraphTool implements Task<FlowContext> {

    private static final int HTTP_PORT_BASE = 7000;
    private static final Logger logger = Logger.getLogger(CheckModelGraphTool.class.getName());

    private final Version productVersion;

    CheckModelGraphTool(Version productVersion) {
        this.productVersion = productVersion;
    }

    @Override
    public Promise<FlowContext> apply(FlowContext context) {
        int port = HTTP_PORT_BASE + productVersion.major() * 10 + productVersion.minor();
        String url = "http://localhost:" + port + "/api/identity";

        RequestInit init = RequestInit.create();
        init.setMethod("GET");
        init.setMode("cors");

        logger.debug("Checking MGT availability at %s", url);
        return fetch(url, init)
                .then(response -> {
                    if (response.ok) {
                        response.json()
                                .then(identity -> {
                                    logger.info("Model graph tools for WildFly %s available: %o", productVersion, identity);
                                    return context.resolve();
                                })
                                .catch_(error -> {
                                    logger.error("Failed to parse model graph tools  identity for WildFly %s: %s",
                                            productVersion, String.valueOf(error));
                                    return context.resolve();
                                });
                    } else {
                        logger.info("Model graph tools for WildFly %s not available: %d", productVersion, response.status);
                    }
                    // TODO Update MGT availability icon in the masthead
                    return context.resolve();
                })
                .catch_(error -> {
                    logger.info("Model graph tools for WildFly %s not available: %s", productVersion, error);
                    // TODO Update MGT availability icon in the masthead
                    return context.resolve();
                });
    }
}

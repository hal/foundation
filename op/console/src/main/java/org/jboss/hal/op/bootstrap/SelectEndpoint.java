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
package org.jboss.hal.op.bootstrap;

import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.env.Endpoints;

import elemental2.core.TypeError;
import elemental2.dom.URLSearchParams;
import elemental2.promise.Promise;

import static elemental2.dom.DomGlobal.location;
import static org.jboss.hal.op.bootstrap.BootstrapError.fail;
import static org.jboss.hal.op.bootstrap.BootstrapError.Failure.NETWORK_ERROR;
import static org.jboss.hal.op.bootstrap.BootstrapError.Failure.NOT_AN_ENDPOINT;
import static org.jboss.hal.op.bootstrap.BootstrapError.Failure.NO_ENDPOINT_FOUND;
import static org.jboss.hal.op.bootstrap.BootstrapError.Failure.NO_ENDPOINT_SPECIFIED;
import static org.jboss.hal.op.bootstrap.BootstrapError.Failure.UNKNOWN;

class SelectEndpoint implements Task<FlowContext> {

    private static final Logger logger = Logger.getLogger(SelectEndpoint.class.getName());
    private static final String CONNECT_PARAMETER = "connect";
    private final Endpoints endpoints;
    private final EndpointStorage endpointStorage;

    SelectEndpoint(Endpoints endpoints) {
        this.endpoints = endpoints;
        this.endpointStorage = new EndpointStorage();
    }

    @Override
    public Promise<FlowContext> apply(FlowContext context) {
        if (!location.search.isEmpty()) {
            URLSearchParams query = new URLSearchParams(location.search);
            if (query.has(CONNECT_PARAMETER)) {
                String connect = query.get(CONNECT_PARAMETER);
                if (connect != null) {
                    if (connect.contains("://")) {
                        return connect(context, connect, true);
                    } else {
                        Endpoint endpoint = endpointStorage.findByName(connect);
                        if (endpoint != null) {
                            return connect(context, endpoint.url, true);
                        } else {
                            return fail(context, NO_ENDPOINT_FOUND, connect);
                        }
                    }
                } else {
                    return fail(context, NO_ENDPOINT_SPECIFIED, CONNECT_PARAMETER);
                }
            } else {
                return connect(context, "", false);
            }
        } else {
            return connect(context, "", false);
        }
    }

    private Promise<FlowContext> connect(FlowContext context, String url, boolean failFast) {
        String failSafeUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return Endpoint.ping(failSafeUrl)
                .then(valid -> {
                    if (valid) {
                        endpoints.init(failSafeUrl);
                        if (!url.isEmpty()) {
                            logger.info("Endpoint: %s", url);
                        }
                        return context.resolve();
                    } else {
                        if (failFast) {
                            context.push(new BootstrapError(NOT_AN_ENDPOINT, url));
                            return context.reject("failed");
                        } else {
                            return new EndpointModal(endpointStorage).open()
                                    .then(endpoint -> connect(context, endpoint.url, true));
                        }
                    }
                })
                .catch_(error -> {
                    if (context.isStackEmpty()) {
                        if (error instanceof TypeError) {
                            return fail(context, NETWORK_ERROR, url);
                        } else {
                            return fail(context, UNKNOWN, String.valueOf(error));
                        }
                    } else {
                        // forward failure from above
                        return context.reject(error);
                    }
                });
    }

}

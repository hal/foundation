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
import org.jboss.hal.op.endpoint.Endpoint;
import org.jboss.hal.op.endpoint.EndpointStorage;

import elemental2.core.TypeError;
import elemental2.dom.URL;
import elemental2.dom.URLSearchParams;
import elemental2.promise.Promise;

import static elemental2.dom.DomGlobal.location;
import static org.jboss.hal.op.bootstrap.BootstrapError.fail;
import static org.jboss.hal.op.bootstrap.BootstrapError.Failure.NETWORK_ERROR;
import static org.jboss.hal.op.bootstrap.BootstrapError.Failure.NOT_AN_ENDPOINT;
import static org.jboss.hal.op.bootstrap.BootstrapError.Failure.NO_ENDPOINT_FOUND;
import static org.jboss.hal.op.bootstrap.BootstrapError.Failure.NO_ENDPOINT_SPECIFIED;
import static org.jboss.hal.op.bootstrap.BootstrapError.Failure.UNKNOWN;
import static org.jboss.hal.op.endpoint.Endpoint.CONNECT_PARAMETER;
import static org.jboss.hal.op.endpoint.EndpointModal.endpointModal;

class SelectEndpoint implements Task<FlowContext> {

    private static final Logger logger = Logger.getLogger(SelectEndpoint.class.getName());
    private final Endpoints endpoints;
    private final EndpointStorage endpointStorage;

    SelectEndpoint(Endpoints endpoints, EndpointStorage endpointStorage) {
        this.endpoints = endpoints;
        this.endpointStorage = endpointStorage;
    }

    @Override
    public Promise<FlowContext> apply(FlowContext context) {
        if (!location.search.isEmpty()) {
            URLSearchParams query = new URLSearchParams(location.search);
            if (query.has(CONNECT_PARAMETER)) {
                String connect = query.get(CONNECT_PARAMETER);
                if (connect != null) {
                    if (connect.contains("://")) {
                        if (URL.canParse(connect)) {
                            URL url = new URL(connect);
                            Endpoint endpoint = endpointStorage.findByName(url.host);
                            if (endpoint == null) {
                                endpoint = Endpoint.endpoint(url.host, connect);
                                endpointStorage.add(endpoint);
                            }
                            return connect(context, endpoint);
                        } else {
                            return fail(context, NOT_AN_ENDPOINT, connect);
                        }
                    } else {
                        Endpoint endpoint = endpointStorage.findByName(connect);
                        if (endpoint != null) {
                            return connect(context, endpoint);
                        } else {
                            return fail(context, NO_ENDPOINT_FOUND, connect);
                        }
                    }
                } else {
                    return fail(context, NO_ENDPOINT_SPECIFIED, CONNECT_PARAMETER);
                }
            } else {
                return endpointModal(endpointStorage, false).open()
                        .then(endpoint -> connect(context, endpoint));
            }
        } else {
            return endpointModal(endpointStorage, false).open()
                    .then(endpoint -> connect(context, endpoint));
        }
    }

    private Promise<FlowContext> connect(FlowContext context, Endpoint endpoint) {
        String failSafeUrl = endpoint.failSafeUrl();
        return Endpoint.ping(failSafeUrl)
                .then(valid -> {
                    if (valid) {
                        endpoints.init(failSafeUrl);
                        endpointStorage.connect(endpoint);
                        logger.info("Endpoint: %o", endpoint);
                        return context.resolve();
                    } else {
                        context.push(new BootstrapError(NOT_AN_ENDPOINT, endpoint.url));
                        return context.reject("failed");
                    }
                })
                .catch_(error -> {
                    if (context.isStackEmpty()) {
                        if (error instanceof TypeError) {
                            return fail(context, NETWORK_ERROR, endpoint.url);
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

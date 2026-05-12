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
package org.jboss.hal.dmr.dispatch;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.env.AccessControlProvider;
import org.jboss.hal.env.Endpoints;
import org.jboss.hal.env.Environment;
import org.jboss.hal.env.Settings;

import elemental2.dom.Headers;
import elemental2.dom.Request;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.IThenable.ThenOnFulfilledCallbackFn;
import elemental2.promise.Promise;
import elemental2.promise.Promise.CatchOnRejectedCallbackFn;

import static elemental2.dom.DomGlobal.fetch;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESPONSE_HEADERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.dispatch.DmrResponseProcessor.PARSE_ERROR;
import static org.jboss.hal.dmr.dispatch.HeaderValues.APPLICATION_DMR_ENCODED;
import static org.jboss.hal.dmr.dispatch.HeaderValues.HEADER_MANAGEMENT_CLIENT_VALUE;
import static org.jboss.hal.dmr.dispatch.HttpMethod.POST;
import static org.jboss.hal.dmr.dispatch.RequestHeader.ACCEPT;
import static org.jboss.hal.dmr.dispatch.RequestHeader.CONTENT_TYPE;
import static org.jboss.hal.dmr.dispatch.RequestHeader.X_MANAGEMENT_CLIENT_NAME;
import static org.jboss.hal.env.Settings.Key.RUN_AS;

/**
 * Central {@code @ApplicationScoped} service for executing DMR {@link Operation operations} and {@link Composite
 * composites} against the WildFly management endpoint via HTTP POST.
 *
 * <p>
 * The dispatcher encodes operations in base64-encoded DMR format, sends them to the management endpoint, and decodes the
 * responses back into {@link ModelNode} structures. It automatically processes custom DMR response headers via registered
 * {@link DmrHeaderProcessor} instances and supports RBAC run-as semantics when configured.
 *
 * <p>
 * Operations can be executed synchronously (returning a {@link Promise}) or asynchronously (with a success callback and
 * error handler). Failed operations trigger the {@link DispatcherErrorHandler}, which logs the error by default.
 */
@ApplicationScoped
public class Dispatcher {

    // ------------------------------------------------------ instance

    private static final Logger logger = Logger.getLogger(Dispatcher.class.getName());
    private final DispatcherErrorHandler defaultErrorHandler;

    private final Endpoints endpoints;
    private final Environment environment;
    private final Settings settings;
    private final Instance<DmrHeaderProcessor> dmrHeaderProcessors;

    @Inject
    public Dispatcher(Environment environment,
            Settings settings,
            Endpoints endpoints,
            Instance<DmrHeaderProcessor> dmrHeaderProcessors) {
        this.environment = environment;
        this.settings = settings;
        this.endpoints = endpoints;
        this.dmrHeaderProcessors = dmrHeaderProcessors;
        defaultErrorHandler = (operation, error) -> {
            logger.error("Error executing operation: %s: %s", operation.asCli(), error);
            // TODO Fire message event
        };
    }

    // ------------------------------------------------------ execute composite

    public void execute(Composite operations, Consumer<CompositeResult> success) {
        execute(operations, success, defaultErrorHandler);
    }

    public void execute(Composite operations, Consumer<CompositeResult> success, DispatcherErrorHandler errorHandler) {
        dmr(operations)
                .then(payload -> {
                    success.accept(compositeResult(payload));
                    return null;
                })
                .catch_(error -> {
                    if (errorHandler != null) {
                        errorHandler.onError(operations, String.valueOf(error));
                    }
                    return null;
                });
    }

    public Promise<CompositeResult> execute(Composite operations) {
        return dmr(operations).then(payload -> Promise.resolve(compositeResult(payload)));
    }

    // ------------------------------------------------------ execute operation

    public void execute(Operation operation, Consumer<ModelNode> success) {
        execute(operation, success, defaultErrorHandler);
    }

    public void execute(Operation operation, Consumer<ModelNode> success, DispatcherErrorHandler errorHandler) {
        dmr(operation)
                .then(payload -> {
                    success.accept(operationResult(payload));
                    return null;
                })
                .catch_(error -> {
                    if (errorHandler != null) {
                        errorHandler.onError(operation, String.valueOf(error));
                    }
                    return null;
                });
    }

    public Promise<ModelNode> execute(Operation operation) {
        return execute(operation, true);
    }

    public Promise<ModelNode> execute(Operation operation, boolean logError) {
        return dmr(operation, logError).then(payload -> Promise.resolve(operationResult(payload)));
    }

    // ------------------------------------------------------ dmr

    /**
     * Executes the operation and upon a successful result, returns the response result but doesn't retrieve the "result"
     * payload as the other execute methods does. You should use this method if the response node you want is not in the
     * "result" attribute.
     */
    public void dmr(Operation operation, Consumer<ModelNode> success, DispatcherErrorHandler errorHandler) {
        dmr(operation)
                .then(payload -> {
                    success.accept(payload);
                    return null;
                })
                .catch_(error -> {
                    if (errorHandler != null) {
                        errorHandler.onError(operation, String.valueOf(error));
                    }
                    return null;
                });
    }

    /**
     * Executes the operation and upon a successful result, returns the response result but doesn't retrieve the "result"
     * payload as the other execute methods does. You should use this method if the response node you want is not in the
     * "result" attribute.
     *
     * @param operation the {@link Operation} to be executed
     * @return a {@link Promise} of {@link ModelNode} - the result of the operation
     */
    public Promise<ModelNode> dmr(Operation operation) {
        return dmr(operation, true);
    }

    /**
     * Executes the operation and upon a successful result, returns the response result but doesn't retrieve the "result"
     * payload as the other execute methods does. You should use this method if the response node you want is not in the
     * "result" attribute.
     *
     * @param operation the {@link Operation} to be executed
     * @param logError  if true, logs any error that occurs during the operation execution
     * @return a {@link Promise} of {@link ModelNode} - the result of the operation
     */
    public Promise<ModelNode> dmr(Operation operation, boolean logError) {
        RequestInit init = requestInit(POST, true);
        init.setBody(runAs(operation).toBase64String());
        Request request = new Request(endpoints.dmr(), init);

        return fetch(request)
                .then(processResponse())
                .then(processText(operation, new OperationResponseProcessor(), true))
                .catch_(error -> {
                    if (logError) {
                        defaultErrorHandler.onError(operation, String.valueOf(error));
                    }
                    return Promise.reject(error);
                });
    }

    // ------------------------------------------------------ promise handlers

    ThenOnFulfilledCallbackFn<Response, String> processResponse() {
        return response -> {
            if (!response.ok && response.status != 500) {
                return Promise.reject(ResponseStatus.fromStatusCode(response.status).statusText());
            }
            String contentType = response.headers.get(CONTENT_TYPE.header());
            if (!contentType.startsWith(APPLICATION_DMR_ENCODED)) {
                return Promise.reject(PARSE_ERROR + contentType);
            }
            return response.text();
        };
    }

    ThenOnFulfilledCallbackFn<String, ModelNode> processText(Operation operation, DmrResponseProcessor payloadProcessor,
            boolean recordOperation) {
        return text -> {
            if (recordOperation) {
                // TODO Macro recording
            }
            logger.debug("Process text for DMR operation: %s", operation.asCli());
            ModelNode payload = payloadProcessor.processPayload(POST, APPLICATION_DMR_ENCODED, text);
            if (!payload.isFailure()) {
                if (payload.hasDefined(RESPONSE_HEADERS)) {
                    DmrHeader[] headers = environment.standalone()
                            ? DmrHeader.standalone(payload.get(RESPONSE_HEADERS))
                            : DmrHeader.domain(payload.get(RESPONSE_HEADERS));
                    for (DmrHeaderProcessor dmrHeaderProcessor : dmrHeaderProcessors) {
                        dmrHeaderProcessor.process(headers);
                    }
                }
                return Promise.resolve(payload);
            } else {
                return Promise.reject(payload.getFailureDescription());
            }
        };
    }

    CatchOnRejectedCallbackFn<ModelNode> rejectWithError() {
        return error -> {
            logger.error("Dispatcher error: %s", error);
            return Promise.reject(error);
        };
    }

    // ------------------------------------------------------ internal

    private CompositeResult compositeResult(ModelNode payload) {
        return new CompositeResult(payload.get(RESULT));
    }

    private ModelNode operationResult(ModelNode payload) {
        return payload.get(RESULT);
    }

    private RequestInit requestInit(HttpMethod method, boolean dmr) {
        Headers headers = new Headers();
        if (dmr) {
            headers.set(ACCEPT.header(), APPLICATION_DMR_ENCODED);
            headers.set(CONTENT_TYPE.header(), APPLICATION_DMR_ENCODED);
        }
        headers.set(X_MANAGEMENT_CLIENT_NAME.header(), HEADER_MANAGEMENT_CLIENT_VALUE);
        String bearerToken = token();
        if (bearerToken != null) {
            headers.set("Authorization", "Bearer " + bearerToken);
        }

        RequestInit init = RequestInit.create();
        init.setMethod(method.name());
        init.setHeaders(headers);
        init.setMode("cors");
        init.setCredentials("include");
        return init;
    }

    private Operation runAs(Operation operation) {
        if (environment.accessControlProvider() == AccessControlProvider.RBAC) {
            Set<String> runAs = settings.get(RUN_AS).asSet();
            if (!runAs.isEmpty()) {
                Set<String> difference = new HashSet<>(runAs);
                difference.removeAll(operation.getRoles());
                if (!difference.isEmpty()) {
                    return operation.runAs(runAs);
                }
            }
        }
        return operation;
    }

    private String token() {
        // TODO Implement SSO using Keycloak
        return null;
    }
}

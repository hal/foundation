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

import elemental2.promise.Promise;

/**
 * Represents an error that occurred during the bootstrap process. Carries a {@link Failure} type and optional context data
 * describing what went wrong.
 */
public class BootstrapError {

    /** Enumerates the possible bootstrap failure types. */
    public enum Failure {

        /** No management interface was specified in the request parameters. */
        NO_ENDPOINT_SPECIFIED,

        /** The named management interface was not found in storage. */
        NO_ENDPOINT_FOUND,

        /** The URL does not point to a valid WildFly management interface. */
        NOT_AN_ENDPOINT,

        /** A network error occurred while contacting the management interface. */
        NETWORK_ERROR,

        /** An unexpected error occurred. */
        UNKNOWN,
    }

    /** Sentinel instance for unknown errors with no additional data. */
    public static final BootstrapError UNKNOWN = new BootstrapError(Failure.UNKNOWN, null);

    /** Creates a rejected promise carrying a {@link BootstrapError} on the flow context stack. */
    static Promise<FlowContext> fail(FlowContext context, BootstrapError.Failure failure, String data) {
        context.push(new BootstrapError(failure, data));
        return context.reject(failure);
    }

    private final Failure failure;
    private final String data;

    BootstrapError(Failure failure, String data) {
        this.failure = failure;
        this.data = data;
    }

    /** Returns the failure type. */
    public Failure failure() {
        return failure;
    }

    /** Returns optional data associated with the failure, such as a URL or parameter name. */
    public String data() {
        return data;
    }
}

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

/**
 * HTTP request headers used when communicating with the WildFly management endpoint.
 *
 * <p>
 * These headers control content negotiation ({@code Accept}, {@code Content-Type}) and identify the client application
 * ({@code X-Management-Client-Name}) to the server. Only headers actually used by the HAL console are defined here.
 */
public enum RequestHeader {

    // only those which are used in HAL
    ACCEPT("Accept"),

    CONTENT_TYPE("Content-Type"),

    X_MANAGEMENT_CLIENT_NAME("X-Management-Client-Name");

    private final String header;

    RequestHeader(final String header) {
        this.header = header;
    }

    /** Returns the HTTP header name string. */
    public String header() {
        return header;
    }
}

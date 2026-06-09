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

/** HTTP response status codes and their user-facing messages, used for error reporting in the dispatcher. */
enum ResponseStatus {

    _0(0, "The response for could not be processed."),

    _401(401, "Unauthorized."),

    _403(403, "Forbidden."),

    _404(404, "Management interface not found."),

    _500(500, "Internal Server Error."),

    _503(503, "Service temporarily unavailable. Is the server still starting?"),

    UNKNOWN(-1, "Unexpected status code.");

    static ResponseStatus fromStatusText(String statusText) {
        for (ResponseStatus responseStatus : ResponseStatus.values()) {
            if (responseStatus.statusText.equals(statusText)) {
                return responseStatus;
            }
        }
        return UNKNOWN;
    }

    static ResponseStatus fromStatusCode(int statusCode) {
        for (ResponseStatus responseStatus : ResponseStatus.values()) {
            if (responseStatus.statusCode == statusCode) {
                return responseStatus;
            }
        }
        return UNKNOWN;
    }

    private final int statusCode;
    private final String statusText;

    ResponseStatus(final int statusCode, final String statusText) {
        this.statusCode = statusCode;
        this.statusText = statusText;
    }

    boolean notAllowed() {
        return statusCode == _401.statusCode || statusCode == _403.statusCode;
    }

    int statusCode() {
        return statusCode;
    }

    String statusText() {
        return statusText;
    }
}

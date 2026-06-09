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
package org.jboss.hal.env;

/**
 * The operation mode of the server (standalone or domain).
 */
public enum OperationMode {

    /** The server is running as a standalone instance. */
    STANDALONE,

    /** The server is part of a managed domain. */
    DOMAIN,

    /** The operation mode has not been determined yet. */
    UNDEFINED;

    /**
     * Parses an operation mode from its string name, defaulting to {@link #STANDALONE} if the value is {@code null} or
     * unrecognized.
     */
    public static OperationMode parse(final String value) {
        OperationMode operationMode = STANDALONE;
        if (value != null) {
            try {
                operationMode = OperationMode.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException ignore) {
                // ignore
            }
        }
        return operationMode;
    }
}

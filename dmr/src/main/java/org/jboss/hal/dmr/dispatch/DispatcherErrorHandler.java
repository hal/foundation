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

import org.jboss.hal.dmr.Operation;

/**
 * Callback invoked when a DMR operation fails.
 *
 * <p>
 * Implementations receive the failed operation and an error message describing what went wrong. The default error handler
 * in {@link Dispatcher} logs the error and the operation's CLI representation.
 */
@FunctionalInterface
public interface DispatcherErrorHandler {

    void onError(Operation operation, String error);
}

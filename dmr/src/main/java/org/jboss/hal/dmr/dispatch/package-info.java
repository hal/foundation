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

/**
 * HTTP dispatch layer for executing DMR operations against the WildFly management endpoint. Handles request formatting,
 * response parsing, header processing, and server process state tracking.
 *
 * <p>
 * This package provides the infrastructure for communicating with WildFly's management endpoint over HTTP. Operations are
 * encoded in DMR format, sent via HTTP POST, and the responses are decoded back into
 * {@link org.jboss.hal.dmr.ModelNode} structures. Custom response headers are processed to track server state changes.
 *
 * <h2>Core Components</h2>
 * <dl>
 * <dt>{@link org.jboss.hal.dmr.dispatch.Dispatcher}</dt>
 * <dd>Central service for executing operations and composites via HTTP</dd>
 *
 * <dt>{@link org.jboss.hal.dmr.dispatch.ProcessState} / {@link org.jboss.hal.dmr.dispatch.ProcessStateEvent}</dt>
 * <dd>Tracks whether the server needs reload or restart</dd>
 *
 * <dt>{@link org.jboss.hal.dmr.dispatch.DmrHeader} / {@link org.jboss.hal.dmr.dispatch.DmrHeaderProcessor}</dt>
 * <dd>Custom response header extraction and processing</dd>
 * </dl>
 *
 * @see org.jboss.hal.dmr
 */
package org.jboss.hal.dmr.dispatch;

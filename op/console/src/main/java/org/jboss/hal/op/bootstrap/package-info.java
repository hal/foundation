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
 * Bootstrap process for the halOP console. The {@link org.jboss.hal.op.bootstrap.Bootstrap} class orchestrates a sequential
 * flow of tasks that initialize logging, select the management endpoint, read the server environment, discover the domain
 * controller (in domain mode), read stability levels, load user settings, and set the browser title.
 *
 * <p>If any task fails, a {@link org.jboss.hal.op.bootstrap.BootstrapError} is produced and rendered by
 * {@link org.jboss.hal.op.bootstrap.BootstrapErrorElement}.
 */
package org.jboss.hal.op.bootstrap;

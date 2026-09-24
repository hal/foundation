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
 * Optional discovery tasks that run in parallel after the halOP console has fully loaded. Unlike the
 * {@link org.jboss.hal.op.bootstrap bootstrap} pipeline, which is sequential and required, discovery tasks are parallel and
 * fire-and-forget — failures are logged but never block the console.
 *
 * <p>The {@link org.jboss.hal.op.discover.Discover} class orchestrates the parallel execution. Individual tasks implement
 * {@link org.jboss.elemento.flow.Task} and probe the environment for optional capabilities (e.g. whether a matching
 * Model Graph Tool container is available).
 */
package org.jboss.hal.op.discover;

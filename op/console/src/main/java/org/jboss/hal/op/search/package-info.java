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
 * Universal search feature accessible via {@code Cmd+K} / {@code Ctrl+K}. The package separates concerns as follows:
 * <ul>
 *     <li>{@link org.jboss.hal.op.search.UniversalSearchBox} — view created fresh on each open, manages the modal, search
 *         input UI, wildcard resolution, and navigation.</li>
 *     <li>{@link org.jboss.hal.op.search.UniversalSearchAsyncItems} — compound delegator that routes search requests to the
 *         appropriate strategy based on input value and model graph tools availability.</li>
 *     <li>{@link org.jboss.hal.op.search.MgtSearchAsyncItems} — queries the model graph tools REST API for matching resources,
 *         attributes, operations, and capabilities.</li>
 * </ul>
 *
 * <p>The search data flow uses the {@link org.patternfly.async.AsyncItems} pattern for direct integration with the search
 * input's built-in menu. Model graph tools availability is checked via
 * {@link org.jboss.hal.core.mgt.ModelGraphTools#available()}.
 */
package org.jboss.hal.op.search;

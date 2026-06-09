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
package org.jboss.hal.op.dashboard;

import static elemental2.dom.DomGlobal.clearInterval;
import static elemental2.dom.DomGlobal.setInterval;

/**
 * Mixin interface for dashboard cards that periodically refresh their content. Implementations provide the refresh
 * {@linkplain #interval() interval} in milliseconds and the actual {@linkplain #autoRefresh() refresh logic}.
 */
interface AutoRefresh {

    /** Returns the auto-refresh interval in milliseconds. */
    double interval();

    /** Returns the interval handle used to cancel the auto-refresh timer. */
    double handle();

    /** Starts the auto-refresh timer and returns the interval handle. */
    default double startAutoRefresh() {
        return setInterval(__ -> autoRefresh(), interval());
    }

    /** Called on each auto-refresh tick to update the card content. */
    void autoRefresh();

    /** Stops the auto-refresh timer. */
    default void stopAutoRefresh() {
        clearInterval(handle());
    }
}

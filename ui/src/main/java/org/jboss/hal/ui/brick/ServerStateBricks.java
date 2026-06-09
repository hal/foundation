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
package org.jboss.hal.ui.brick;

import org.jboss.hal.model.RunningMode;
import org.jboss.hal.model.RunningState;
import org.jboss.hal.model.RuntimeConfigurationState;
import org.jboss.hal.model.SuspendState;
import org.patternfly.component.label.Label;

import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.Severity.success;
import static org.patternfly.component.Severity.warning;
import static org.patternfly.component.label.Label.label;
import static org.patternfly.style.Color.blue;
import static org.patternfly.style.Color.grey;

/**
 * Factory methods for rendering WildFly server and host runtime state values as colour-coded PatternFly labels. Each
 * method maps the possible enum values to appropriate colours and statuses:
 * <ul>
 *     <li>Green / success — healthy states ({@code OK}, {@code RUNNING}, {@code NORMAL})</li>
 *     <li>Blue — transitional states ({@code STARTING}, {@code STOPPING}, {@code PRE_SUSPEND}, {@code SUSPENDING})</li>
 *     <li>Yellow / warning — attention states ({@code RELOAD_REQUIRED}, {@code RESTART_REQUIRED})</li>
 *     <li>Grey — inactive states ({@code STOPPED}, {@code ADMIN_ONLY})</li>
 *     <li>Red / danger — undefined or error states</li>
 * </ul>
 */
public final class ServerStateBricks {

    /**
     * Creates a label for a {@link RuntimeConfigurationState} value using the colour scheme described in
     * {@link ServerStateBricks}.
     */
    public static Label runtimeConfigurationStateLabel(RuntimeConfigurationState value) {
        return switch (value) {
            case STARTING, STOPPING -> label(value.name(), blue);
            case OK -> label(value.name()).status(success);
            case RELOAD_REQUIRED, RESTART_REQUIRED -> label(value.name()).status(warning);
            case STOPPED -> label(value.name(), grey);
            case UNDEFINED -> label(RuntimeConfigurationState.UNDEFINED.name()).status(danger);
        };
    }

    /** Creates a label for a {@link RunningMode} value ({@code NORMAL} → green, {@code ADMIN_ONLY} → grey). */
    public static Label runningModeLabel(RunningMode value) {
        return switch (value) {
            case NORMAL -> label(value.name()).status(success);
            case ADMIN_ONLY -> label(value.name(), grey);
            default -> label(RunningMode.UNDEFINED.name()).status(danger);
        };
    }

    /** Creates a label for a {@link RunningState} value. */
    public static Label runningStateLabel(RunningState value) {
        return switch (value) {
            case STARTING -> label(value.name(), blue);
            case RUNNING -> label(value.name()).status(success);
            case STOPPED -> label(value.name(), grey);
            case RESTART_REQUIRED, RELOAD_REQUIRED -> label(value.name()).status(warning);
            case UNDEFINED -> label(RunningState.UNDEFINED.name()).status(danger);
        };
    }

    /** Creates a label for a {@link SuspendState} value. */
    public static Label suspendStateLabel(SuspendState value) {
        return switch (value) {
            case RUNNING -> label(value.name()).status(success);
            case PRE_SUSPEND, SUSPENDED, SUSPENDING -> label(value.name(), blue);
            case UNDEFINED -> label(SuspendState.UNDEFINED.name()).status(danger);
        };
    }

    private ServerStateBricks() {
    }
}

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

import org.jboss.hal.event.ApplicationEvent;

/**
 * CDI event fired when the {@link ProcessState} changes after executing a DMR operation.
 *
 * <p>
 * This event is fired by {@link ProcessStateProcessor} when response headers indicate that one or more servers require a
 * reload or restart. UI components can observe this event to display notifications or warnings to the user.
 */
public class ProcessStateEvent implements ApplicationEvent {

    public final ProcessState processState;

    ProcessStateEvent(ProcessState processState) {
        this.processState = processState;
    }
}

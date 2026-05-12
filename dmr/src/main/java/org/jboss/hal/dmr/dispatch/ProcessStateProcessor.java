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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import org.jboss.hal.dmr.dispatch.ServerState.State;

import static org.jboss.hal.dmr.ModelDescriptionConstants.PROCESS_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD_REQUIRED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESTART_REQUIRED;

/**
 * A {@link DmrHeaderProcessor} that detects server process state changes from DMR response headers and fires
 * {@link ProcessStateEvent} CDI events.
 *
 * <p>
 * This processor inspects the {@code process-state} header in operation responses. If any server reports
 * {@code reload-required} or {@code restart-required}, it aggregates them into a {@link ProcessState} and fires a
 * {@link ProcessStateEvent} to notify the application.
 */
@ApplicationScoped
public class ProcessStateProcessor implements DmrHeaderProcessor {

    @Inject Event<ProcessStateEvent> processStateEventEvent;

    @Override
    public void process(DmrHeader[] headers) {
        ProcessState processState = new ProcessState();
        for (DmrHeader header : headers) {
            if (header.getHeader().hasDefined(PROCESS_STATE)) {
                String processStateValue = header.getHeader().get(PROCESS_STATE).asString();
                if (RESTART_REQUIRED.equals(processStateValue)) {
                    ServerState state = new ServerState(header.getHost(), header.getServer(), State.RESTART_REQUIRED);
                    processState.add(state);

                } else if (RELOAD_REQUIRED.equals(processStateValue)) {
                    ServerState state = new ServerState(header.getHost(), header.getServer(), State.RELOAD_REQUIRED);
                    processState.add(state);
                }
            }
        }
        if (!processState.isEmpty()) {
            processStateEventEvent.fire(new ProcessStateEvent(processState));
        }
    }
}

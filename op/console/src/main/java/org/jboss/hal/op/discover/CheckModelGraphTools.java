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
package org.jboss.hal.op.discover;

import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.hal.op.mgt.ModelGraphTools;

import elemental2.promise.Promise;

/**
 * Discovery task that checks whether a matching model graph tools container is running. The port is derived from the WildFly
 * product version: {@code 7000 + major * 10 + minor}. For example, WildFly 41.0 maps to port 7410.
 */
class CheckModelGraphTools implements Task<FlowContext> {

    private final ModelGraphTools mgt;

    CheckModelGraphTools(ModelGraphTools mgt) {
        this.mgt = mgt;
    }

    @Override
    public Promise<FlowContext> apply(FlowContext context) {
        return mgt.ping()
                .then((__) -> context.resolve())
                .catch_(error -> context.resolve());
    }
}

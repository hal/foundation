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
package org.jboss.hal.op.bootstrap;

import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;

import elemental2.promise.Promise;

import static elemental2.dom.DomGlobal.location;

/** Bootstrap task that initializes the Elemento logger from the URL query parameters (e.g., {@code ?logLevel=debug}). */
class SetLogLevel implements Task<FlowContext> {

    @Override
    public Promise<FlowContext> apply(FlowContext context) {
        Logger.initFrom(location);
        return context.resolve();
    }
}

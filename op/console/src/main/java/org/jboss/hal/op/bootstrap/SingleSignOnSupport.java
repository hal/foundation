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

/** Bootstrap task placeholder for single sign-on support. Currently not implemented. */
class SingleSignOnSupport implements Task<FlowContext> {

    private static final Logger logger = Logger.getLogger(SingleSignOnSupport.class.getName());

    @Override
    public Promise<FlowContext> apply(FlowContext context) {
        logger.warn("Single sign on support is not yet implemented.");
        return context.resolve();
    }
}

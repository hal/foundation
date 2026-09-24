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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Subscription;
import org.jboss.hal.env.Environment;
import org.jboss.hal.op.mgt.ModelGraphTools;

import static java.util.Collections.singletonList;
import static org.jboss.elemento.flow.Flow.parallel;

/**
 * Orchestrates optional discovery tasks that run in parallel after the console has fully loaded. Unlike
 * {@link org.jboss.hal.op.bootstrap.Bootstrap}, failures here are silently logged and never block the console.
 */
@ApplicationScoped
public class Discover {

    @Inject Environment environment;
    @Inject ModelGraphTools mgt;

    /** Starts all discovery tasks in parallel and returns a subscription to observe the result. */
    public Subscription<FlowContext> run() {
        return parallel(new FlowContext(), singletonList(
                new CheckModelGraphTools(mgt)
        )).failFast(false);
    }
}

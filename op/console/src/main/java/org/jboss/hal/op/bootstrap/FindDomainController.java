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

import java.util.List;

import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Environment;
import org.jboss.hal.meta.Placeholder;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Keys;

import elemental2.promise.Promise;

import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.flow.Flow.sequential;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PRIMARY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

class FindDomainController implements Task<FlowContext> {

    private static final Logger logger = Logger.getLogger(FindDomainController.class.getName());

    private final Dispatcher dispatcher;
    private final Environment environment;
    private final StatementContext statementContext;

    FindDomainController(Dispatcher dispatcher, Environment environment, StatementContext statementContext) {
        this.dispatcher = dispatcher;
        this.environment = environment;
        this.statementContext = statementContext;
    }

    @Override
    public Promise<FlowContext> apply(final FlowContext context) {
        if (environment.domain()) {
            List<String> hosts = context.get(Keys.HOSTS);
            if (hosts == null) {
                return Promise.resolve(context);
            } else {
                List<Task<FlowContext>> hostTasks = hosts.stream()
                        .map(host -> {
                            ResourceAddress address = new ResourceAddress().add(HOST, host);
                            Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                                    .param(ATTRIBUTES_ONLY, true)
                                    .param(INCLUDE_RUNTIME, true)
                                    .build();
                            return (Task<FlowContext>) c -> dispatcher.execute(operation).then(result -> {
                                boolean primary = false;
                                if (result.hasDefined(PRIMARY)) {
                                    primary = result.get(PRIMARY).asBoolean();
                                }
                                if (primary) {
                                    String name = result.get(NAME).asString();
                                    environment.update(name);
                                    statementContext.assign(Placeholder.DOMAIN_CONTROLLER, name);
                                    logger.info("Domain controller: %s", name);
                                }
                                return Promise.resolve(c);
                            });
                        })
                        .collect(toList());
                return sequential(context, hostTasks).promise();
            }
        } else {
            return Promise.resolve(context);
        }
    }
}

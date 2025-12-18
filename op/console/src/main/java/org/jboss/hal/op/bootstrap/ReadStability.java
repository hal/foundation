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

import java.util.Arrays;
import java.util.Objects;

import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Environment;
import org.jboss.hal.env.Stability;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import elemental2.promise.Promise;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PERMISSIBLE_STABILITY_LEVELS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STABILITY;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;

class ReadStability implements Task<FlowContext> {

    private static final Logger logger = Logger.getLogger(ReadStability.class.getName());
    private final Dispatcher dispatcher;
    private final Environment environment;
    private final StatementContext statementContext;

    ReadStability(Dispatcher dispatcher, Environment environment, StatementContext statementContext) {
        this.dispatcher = dispatcher;
        this.environment = environment;
        this.statementContext = statementContext;
    }

    @Override
    public Promise<FlowContext> apply(FlowContext context) {
        AddressTemplate template = environment.standalone()
                ? AddressTemplate.of("core-service=server-environment")
                : AddressTemplate.of("{domain.controller}/core-service=host-environment");
        Operation operation = new Operation.Builder(template.resolve(statementContext), READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(ATTRIBUTES_ONLY, true)
                .build();
        return dispatcher.execute(operation)
                .then(result -> {
                    Stability stability = readStabilityLevel(result);
                    Stability[] permissibleStabilityLevels = readPermissibleStabilityLevels(result);
                    environment.update(stability, permissibleStabilityLevels);
                    logger.info("Stability: %s, permissible levels: %s",
                            stability, Arrays.toString(permissibleStabilityLevels));
                    return context.resolve();
                });
    }

    private Stability readStabilityLevel(ModelNode modelNode) {
        return asEnumValue(modelNode, STABILITY, Stability::valueOf, environment.builtInStability());
    }

    private Stability[] readPermissibleStabilityLevels(ModelNode modelNode) {
        if (modelNode.hasDefined(PERMISSIBLE_STABILITY_LEVELS)) {
            return modelNode.get(PERMISSIBLE_STABILITY_LEVELS).asList().stream()
                    .map(node -> asEnumValue(node, Stability::valueOf, null))
                    .filter(Objects::nonNull)
                    .toArray(Stability[]::new);
        }
        return new Stability[0];
    }
}
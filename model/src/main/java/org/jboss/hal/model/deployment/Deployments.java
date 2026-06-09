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
package org.jboss.hal.model.deployment;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.model.server.Server;

import elemental2.promise.Promise;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE_DEPTH;

/**
 * Service for reading deployment resources from the management model.
 */
@ApplicationScoped
public class Deployments {

    private final Dispatcher dispatcher;

    @Inject
    public Deployments(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /** Reads all deployments from a standalone server, including subsystem and subdeployment information. */
    public Promise<List<Deployment>> readStandaloneDeployments() {
        Operation operation = new Operation.Builder(ResourceAddress.root(),
                READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, DEPLOYMENT)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE_DEPTH, 2)
                .build();
        return dispatcher.execute(operation)
                .then(result -> Promise.resolve(result.asPropertyList().stream()
                        .map(property -> new Deployment(Server.standalone(), property.getValue()))
                        .collect(toList())));
    }
}

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

import java.util.ArrayList;
import java.util.List;

import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Environment;
import org.jboss.hal.env.OperationMode;
import org.jboss.hal.env.Version;
import org.jboss.hal.model.server.Server;
import org.jboss.hal.model.user.Role;
import org.jboss.hal.model.user.User;

import elemental2.promise.Promise;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DOMAIN_ORGANIZATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LAUNCH_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ORGANIZATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PRODUCT_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PRODUCT_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VERBOSE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WHOAMI_OPERATION;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;

/**
 * Bootstrap task that reads core environment information from the management root resource and the current user's identity via
 * {@code :whoami}. Populates the {@link Environment} with name, organisation, product name/version, management model version,
 * and operation mode. Also sets up the authenticated {@link User} with username and mapped roles.
 */
class ReadEnvironment implements Task<FlowContext> {

    private static final Logger logger = Logger.getLogger(ReadEnvironment.class.getName());
    private final Dispatcher dispatcher;
    private final Environment environment;
    private final User user;

    ReadEnvironment(Dispatcher dispatcher, Environment environment, User user) {
        this.dispatcher = dispatcher;
        this.environment = environment;
        this.user = user;
    }

    @Override
    public Promise<FlowContext> apply(FlowContext context) {
        List<Operation> operations = new ArrayList<>();
        operations.add(new Operation.Builder(ResourceAddress.root(), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build());
        operations.add(new Operation.Builder(ResourceAddress.root(), WHOAMI_OPERATION).param(VERBOSE, true).build());

        return dispatcher.execute(new Composite(operations))
                .then(result -> {
                    ModelNode root = result.step(0).get(RESULT);

                    // standalone or domain mode?
                    OperationMode operationMode = asEnumValue(root, LAUNCH_TYPE, OperationMode::valueOf,
                            OperationMode.UNDEFINED);

                    // name and organisation
                    String name = UNDEFINED;
                    if (root.hasDefined(NAME)) {
                        name = root.get(NAME).asString();
                    }
                    String organisation = UNDEFINED;
                    String attribute = operationMode == OperationMode.STANDALONE ? ORGANIZATION : DOMAIN_ORGANIZATION;
                    if (root.hasDefined(attribute)) {
                        organisation = root.get(attribute).asString();
                    }

                    // product name and version
                    String productName = UNDEFINED;
                    Version productVersion = Version.EMPTY_VERSION;
                    if (root.hasDefined(PRODUCT_NAME)) {
                        productName = root.get(PRODUCT_NAME).asString();
                    }
                    if (root.hasDefined(PRODUCT_VERSION)) {
                        productVersion = Version.parseVersion(root.get(PRODUCT_VERSION).asString());
                    }

                    // management model version
                    Version managementModelVersion = ModelNodeHelper.parseVersion(root);

                    environment.update(name, organisation, productName, productVersion, managementModelVersion, operationMode);
                    logger.info(
                            "Name: %s, organisation: %s, product-name: %s, product-info: %s, management-model-version: %s, operation-mode: %s",
                            environment.instanceName(), environment.instanceOrganization(), environment.productName(),
                            environment.productVersion(), environment.managementVersion(), environment.operationMode());
                    if (operationMode == OperationMode.STANDALONE) {
                        Server.standalone().addServerAttributes(root);
                    }

                    ModelNode whoami = result.step(1);
                    String username = whoami.get("identity").get("username").asString();
                    user.setName(username);
                    if (whoami.hasDefined("mapped-roles")) {
                        List<ModelNode> roles = whoami.get("mapped-roles").asList();
                        for (ModelNode role : roles) {
                            String roleName = role.asString();
                            user.addRole(new Role(roleName));
                        }
                    }
                    user.setAuthenticated(true);
                    logger.info("User: %s", user);

                    return context.resolve();
                });
    }
}

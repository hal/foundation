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
package org.jboss.hal.op;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.ResourceDefinition;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.SubsystemResourceRegistration;
import org.jboss.as.controller.capability.RuntimeCapability;
import org.jboss.as.controller.descriptions.ParentResourceDescriptionResolver;
import org.jboss.as.controller.descriptions.SubsystemResourceDescriptionResolver;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.server.mgmt.domain.ExtensibleHttpManagement;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoadException;
import org.wildfly.subsystem.resource.ManagementResourceRegistrar;
import org.wildfly.subsystem.resource.ManagementResourceRegistrationContext;
import org.wildfly.subsystem.resource.ResourceDescriptor;
import org.wildfly.subsystem.resource.SubsystemResourceDefinitionRegistrar;
import org.wildfly.subsystem.resource.operation.ResourceOperationRuntimeHandler;
import org.wildfly.subsystem.service.ResourceServiceConfigurator;
import org.wildfly.subsystem.service.ResourceServiceInstaller;
import org.wildfly.subsystem.service.ServiceDependency;
import org.wildfly.subsystem.service.ServiceInstaller;

import io.undertow.io.IoCallback;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

/**
 * Registrar for the HAL on premise subsystem. Installs a service that serves the console resources on the management HTTP
 * interface at {@code /halop}.
 */
class HalOpSubsystemRegistrar implements SubsystemResourceDefinitionRegistrar {

    static final String NAME = "halop";
    static final SubsystemResourceRegistration REGISTRATION = SubsystemResourceRegistration.of(NAME);
    static final ParentResourceDescriptionResolver RESOLVER =
            new SubsystemResourceDescriptionResolver(NAME, HalOpSubsystemRegistrar.class);

    static final String CONSOLE_MODULE = "org.jboss.hal.op.console";
    static final String CONTEXT_NAME = "halop";
    static final String RESOURCE_PREFIX = "console";

    private static final Logger log = Logger.getLogger(HalOpSubsystemRegistrar.class);
    private static final String EXTENSIBLE_HTTP_CAPABILITY = "org.wildfly.management.http.extensible";
    private static final RuntimeCapability<Void> HALOP_CAPABILITY = RuntimeCapability.Builder.of("org.jboss.hal.op")
            .addRequirements(EXTENSIBLE_HTTP_CAPABILITY)
            .build();

    @Override
    public ManagementResourceRegistration register(SubsystemRegistration parent,
            ManagementResourceRegistrationContext context) {
        parent.setHostCapable();

        ManagementResourceRegistration registration = parent.registerSubsystemModel(
                ResourceDefinition.builder(REGISTRATION, RESOLVER).build());

        ResourceDescriptor descriptor = ResourceDescriptor.builder(RESOLVER)
                .addCapability(HALOP_CAPABILITY)
                .withRuntimeHandler(ResourceOperationRuntimeHandler.configureService(new HalOpServiceConfigurator()))
                .build();
        ManagementResourceRegistrar.of(descriptor).register(registration);

        return registration;
    }

    /**
     * Configures the service that registers the HAL console static context on the management HTTP interface.
     */
    private static class HalOpServiceConfigurator implements ResourceServiceConfigurator {

        @Override
        public ResourceServiceInstaller configure(OperationContext context, ModelNode model) {
            ServiceDependency<ExtensibleHttpManagement> httpManagement =
                    ServiceDependency.on(EXTENSIBLE_HTTP_CAPABILITY, ExtensibleHttpManagement.class);

            return ServiceInstaller.builder(httpManagement)
                    .onStart(mgmt -> {
                        try {
                            Module consoleModule = Module.getCallerModuleLoader().loadModule(CONSOLE_MODULE);
                            ResourceManager resourceManager = new ClassPathResourceManager(
                                    consoleModule.getClassLoader(), RESOURCE_PREFIX);
                            mgmt.addManagementHandler(CONTEXT_NAME, true, spaHandler(resourceManager));
                            log.infof("HAL on premise console available at /%s", CONTEXT_NAME);
                        } catch (ModuleLoadException e) {
                            throw new RuntimeException("Failed to load HAL console module: " + CONSOLE_MODULE, e);
                        }
                    })
                    .onStop(mgmt -> {
                        mgmt.removeContext(CONTEXT_NAME);
                        log.infof("HAL on premise console removed from /%s", CONTEXT_NAME);
                    })
                    .build();
        }

        private static HttpHandler spaHandler(ResourceManager resourceManager) {
            ResourceHandler resourceHandler = new ResourceHandler(resourceManager);
            return exchange -> {
                String path = exchange.getRelativePath();
                if (path.isEmpty() || path.startsWith("/index.html")) {
                    exchange.setStatusCode(StatusCodes.FOUND);
                    exchange.getResponseHeaders().put(Headers.LOCATION, "/" + CONTEXT_NAME + "/");
                    exchange.endExchange();
                } else {
                    if (clientRoute(path)) {
                        Resource index = resourceManager.getResource("/index.html");
                        index.serve(exchange.getResponseSender(), exchange, IoCallback.END_EXCHANGE);
                    } else {
                        resourceHandler.handleRequest(exchange);
                    }
                }
            };
        }

        private static boolean clientRoute(String path) {
            return !path.equals("/") && !path.contains(".");
        }

    }
}

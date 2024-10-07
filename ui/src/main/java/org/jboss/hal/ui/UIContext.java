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
package org.jboss.hal.ui;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Environment;
import org.jboss.hal.env.Settings;
import org.jboss.hal.meta.CapabilityRegistry;
import org.jboss.hal.meta.MetadataRepository;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.tree.ModelTree;

/** Holds common classes needed by UI elements. Use this class to keep the method signatures clean and tidy. */
@Startup
@ApplicationScoped
public class UIContext {

    // ------------------------------------------------------ singleton

    // Hacky way to make the UIContext statically available.
    // Use this only if there's no other (CDI-compliant) way!
    private static UIContext instance;

    public static UIContext uic() {
        if (instance == null) {
            logger.error("UIContext has not yet been initialized. Static access is not possible!");
        }
        return instance;
    }

    // ------------------------------------------------------ instance

    private static final Logger logger = Logger.getLogger(UIContext.class.getName());
    private final Environment environment;
    private final Settings settings;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final ModelTree modelTree;
    private final CapabilityRegistry capabilityRegistry;
    private final MetadataRepository metadataRepository;

    @Inject
    public UIContext(Environment environment,
            Settings settings,
            Dispatcher dispatcher,
            StatementContext statementContext,
            ModelTree modelTree,
            CapabilityRegistry capabilityRegistry,
            MetadataRepository metadataRepository) {
        this.environment = environment;
        this.settings = settings;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.modelTree = modelTree;
        this.capabilityRegistry = capabilityRegistry;
        this.metadataRepository = metadataRepository;
    }

    @PostConstruct
    void init() {
        UIContext.instance = this;
    }

    public Environment environment() {
        return environment;
    }

    public Settings settings() {
        return settings;
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public StatementContext statementContext() {
        return statementContext;
    }

    public ModelTree modelTree() {
        return modelTree;
    }

    public CapabilityRegistry capabilityRegistry() {
        return capabilityRegistry;
    }

    public MetadataRepository metadataRepository() {
        return metadataRepository;
    }
}

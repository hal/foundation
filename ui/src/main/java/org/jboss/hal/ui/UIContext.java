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
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.Notifications;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Environment;
import org.jboss.hal.env.Settings;
import org.jboss.hal.meta.CapabilityRegistry;
import org.jboss.hal.meta.MetadataRepository;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.tree.ModelTree;

/**
 * Holds common classes often needed in UI elements.
 * <p>
 * In most cases UI classes take not part in CDI. In case they need one of the classes returned by the methods of this class,
 * use {@link #uic()} to get a reference.
 */
@Startup
@ApplicationScoped
public class UIContext {

    // ------------------------------------------------------ singleton

    private static UIContext instance;

    public static UIContext uic() {
        if (instance == null) {
            logger.error("UIContext has not yet been initialized. Static access is not possible!");
        }
        return instance;
    }

    // ------------------------------------------------------ instance

    private static final Logger logger = Logger.getLogger(UIContext.class.getName());

    private final CapabilityRegistry capabilityRegistry;
    private final CrudOperations crud;
    private final Dispatcher dispatcher;
    private final Environment environment;
    private final MetadataRepository metadataRepository;
    private final ModelTree modelTree;
    private final Notifications notifications;
    private final Settings settings;
    private final StatementContext statementContext;

    @Inject
    public UIContext(
            CapabilityRegistry capabilityRegistry,
            CrudOperations crud,
            Dispatcher dispatcher,
            Environment environment,
            MetadataRepository metadataRepository,
            ModelTree modelTree,
            Notifications notifications,
            Settings settings,
            StatementContext statementContext
    ) {
        this.capabilityRegistry = capabilityRegistry;
        this.crud = crud;
        this.dispatcher = dispatcher;
        this.environment = environment;
        this.metadataRepository = metadataRepository;
        this.modelTree = modelTree;
        this.notifications = notifications;
        this.settings = settings;
        this.statementContext = statementContext;
    }

    @PostConstruct
    void init() {
        UIContext.instance = this;
    }

    public CapabilityRegistry capabilityRegistry() {
        return capabilityRegistry;
    }

    public CrudOperations crud() {
        return crud;
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public Environment environment() {
        return environment;
    }

    public MetadataRepository metadataRepository() {
        return metadataRepository;
    }

    public ModelTree modelTree() {
        return modelTree;
    }

    public Notifications notifications() {
        return notifications;
    }

    public Settings settings() {
        return settings;
    }

    public StatementContext statementContext() {
        return statementContext;
    }
}

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
import org.jboss.elemento.router.PlaceManager;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.Notifications;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Endpoints;
import org.jboss.hal.env.Environment;
import org.jboss.hal.env.Settings;
import org.jboss.hal.meta.CapabilityRegistry;
import org.jboss.hal.meta.MetadataRepository;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.tree.ModelTree;
import org.jboss.hal.ui.navigation.RouteRegistry;

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

    /** Returns the singleton instance of the UI context. Must only be called after CDI initialization. */
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
    private final Endpoints endpoints;
    private final Environment environment;
    private final MetadataRepository metadataRepository;
    private final ModelTree modelTree;
    private final Notifications notifications;
    private final PlaceManager placeManager;
    private final RouteRegistry routeRegistry;
    private final Settings settings;
    private final StatementContext statementContext;

    /** Creates a new UI context. All parameters are injected by the CDI container. */
    @Inject
    public UIContext(
            CapabilityRegistry capabilityRegistry,
            CrudOperations crud,
            Dispatcher dispatcher,
            Endpoints endpoints,
            Environment environment,
            MetadataRepository metadataRepository,
            ModelTree modelTree,
            Notifications notifications,
            PlaceManager placeManager,
            RouteRegistry routeRegistry,
            Settings settings,
            StatementContext statementContext
    ) {
        this.capabilityRegistry = capabilityRegistry;
        this.crud = crud;
        this.dispatcher = dispatcher;
        this.endpoints = endpoints;
        this.environment = environment;
        this.metadataRepository = metadataRepository;
        this.modelTree = modelTree;
        this.notifications = notifications;
        this.placeManager = placeManager;
        this.routeRegistry = routeRegistry;
        this.settings = settings;
        this.statementContext = statementContext;
    }

    @PostConstruct
    void init() {
        UIContext.instance = this;
    }

    /** Returns the capability registry for resolving WildFly capabilities. */
    public CapabilityRegistry capabilityRegistry() {
        return capabilityRegistry;
    }

    /** Returns the CRUD operations facade for creating, reading, updating, and deleting management resources. */
    public CrudOperations crud() {
        return crud;
    }

    /** Returns the dispatcher for executing DMR operations against WildFly. */
    public Dispatcher dispatcher() {
        return dispatcher;
    }

    /** Returns the endpoint configuration for the management interface. */
    public Endpoints endpoints() {
        return endpoints;
    }

    /** Returns the runtime environment with server and console metadata. */
    public Environment environment() {
        return environment;
    }

    /** Returns the metadata repository for accessing WildFly management model metadata. */
    public MetadataRepository metadataRepository() {
        return metadataRepository;
    }

    /** Returns the model tree representing the WildFly management resource hierarchy. */
    public ModelTree modelTree() {
        return modelTree;
    }

    /** Returns the place manager for navigating the application. */
    public PlaceManager placeManager() {
        return placeManager;
    }

    /** Returns the route registry for navigating to routes by address template. */
    public RouteRegistry routeRegistry() {
        return routeRegistry;
    }

    /** Returns the notification service for displaying messages to the user. */
    public Notifications notifications() {
        return notifications;
    }

    /** Returns the user settings such as locale, page size, and other preferences. */
    public Settings settings() {
        return settings;
    }

    /** Returns the statement context for resolving placeholders in resource addresses. */
    public StatementContext statementContext() {
        return statementContext;
    }
}

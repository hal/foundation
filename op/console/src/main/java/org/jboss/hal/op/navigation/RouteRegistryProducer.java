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
package org.jboss.hal.op.navigation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.jboss.elemento.router.PlaceManager;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Segment;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.StatementContextResolver;
import org.jboss.hal.meta.WildcardResolver;
import org.jboss.hal.ui.navigation.RouteBinding;
import org.jboss.hal.ui.navigation.RouteRegistry;

import static org.jboss.hal.meta.WildcardResolver.Direction.LTR;
import static org.jboss.hal.op.navigation.KnownRoutes.DEFAULT_ROUTE;
import static org.jboss.hal.op.navigation.KnownRoutes.INTERFACE_ROUTE;
import static org.jboss.hal.op.navigation.KnownRoutes.PATH_ROUTE;
import static org.jboss.hal.op.navigation.KnownRoutes.SOCKET_BINDING_GROUP_ROUTE;
import static org.jboss.hal.op.navigation.KnownRoutes.SOCKET_BINDING_ROUTE;
import static org.jboss.hal.op.navigation.KnownRoutes.SUBSYSTEM_ROUTE;
import static org.jboss.hal.op.navigation.KnownRoutes.SYSTEM_PROPERTY_ROUTE;

/**
 * CDI producer that creates the {@link RouteRegistry} and registers all known {@link RouteBinding}s. Each binding pairs a route
 * from {@link KnownRoutes} with its address template and both conversion functions.
 * <p>
 * When adding a new resource page, add its binding here. The route constant should already exist in {@link KnownRoutes}.
 *
 * @see RouteBinding
 * @see RouteRegistry
 */
public class RouteRegistryProducer {

    @Inject PlaceManager placeManager;
    @Inject StatementContext statementContext;

    @Produces
    @ApplicationScoped
    public RouteRegistry routeRegistry() {
        RouteRegistry registry = new RouteRegistry(placeManager, statementContext, DEFAULT_ROUTE);
        registerBindings(registry);
        return registry;
    }

    private void registerBindings(RouteRegistry registry) {
        // interface=*  <->  /configuration/interface/:name
        registry.register(new RouteBinding(
                INTERFACE_ROUTE,
                AddressTemplate.ofTrusted("interface=*"),
                (template, parameter) -> template.apply(new WildcardResolver(LTR, parameter.get("name"))),
                (template, route) -> new String[]{template.last().value}));

        // path=*  <->  /configuration/path/:name
        registry.register(new RouteBinding(
                PATH_ROUTE,
                AddressTemplate.ofTrusted("path=*"),
                (template, parameter) -> template.apply(new WildcardResolver(LTR, parameter.get("name"))),
                (template, route) -> new String[]{template.last().value}));

        // socket-binding-group=*  <->  /configuration/socket-binding-group/:name
        registry.register(new RouteBinding(
                SOCKET_BINDING_GROUP_ROUTE,
                AddressTemplate.ofTrusted("socket-binding-group=*"),
                (template, parameter) -> template.apply(new WildcardResolver(LTR, parameter.get("name"))),
                (template, route) -> new String[]{template.last().value}));

        // socket-binding-group=*/socket-binding=*  <->  /configuration/socket-binding-group/:group/socket-binding/:name
        registry.register(new RouteBinding(
                SOCKET_BINDING_ROUTE,
                AddressTemplate.ofTrusted("socket-binding-group=*/socket-binding=*"),
                (template, parameter) -> template.apply(
                        new WildcardResolver(LTR, parameter.get("group"), parameter.get("name"))),
                (template, route) -> socketBindingParams(template)));

        // subsystem=*  <->  /configuration/subsystem/:name/:selection?
        registry.register(new RouteBinding(
                SUBSYSTEM_ROUTE,
                AddressTemplate.ofTrusted("subsystem=*"),
                (template, parameter) -> template.apply(new WildcardResolver(LTR, parameter.get("name"))),
                (template, route) -> subsystemParams(template)));

        // system-property=*  <->  /configuration/system-property/:name
        registry.register(new RouteBinding(
                SYSTEM_PROPERTY_ROUTE,
                AddressTemplate.ofTrusted("system-property=*"),
                (template, parameter) -> template.apply(new WildcardResolver(LTR, parameter.get("name"))),
                (template, route) -> new String[]{template.last().value}));
    }

    // ------------------------------------------------------ parameter extractors

    private String[] subsystemParams(AddressTemplate template) {
        AddressTemplate resolved = new StatementContextResolver(statementContext).resolve(template);
        String name = null;
        String selection = null;
        for (Segment segment : resolved) {
            if ("subsystem".equals(segment.key)) {
                name = segment.value;
                break;
            }
        }
        if (!"subsystem".equals(resolved.last().key)) {
            selection = resolved.template;
        }
        if (name != null && selection != null) {
            return new String[]{name, selection};
        } else if (name != null) {
            return new String[]{name};
        }
        return null;
    }

    private String[] socketBindingParams(AddressTemplate template) {
        String group = null;
        String name = null;
        for (Segment segment : template) {
            if ("socket-binding-group".equals(segment.key)) {
                group = segment.value;
            } else if ("socket-binding".equals(segment.key)) {
                name = segment.value;
            }
        }
        if (group != null && name != null) {
            return new String[]{group, name};
        }
        return null;
    }
}

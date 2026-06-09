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
package org.jboss.hal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.model.server.Server;

import static java.util.stream.Collectors.toList;

/**
 * Abstract base for model nodes that aggregate {@link Server} instances (such as hosts and server groups).
 */
public abstract class HasServersNode extends NamedNode {

    private final List<Server> servers;

    /** Creates a new instance from the given name and model node. */
    public HasServersNode(String name, ModelNode node) {
        super(name, node);
        this.servers = new ArrayList<>();
    }

    /** Creates a new instance from the given DMR property. */
    public HasServersNode(Property property) {
        super(property);
        this.servers = new ArrayList<>();
    }

    /** Returns {@code true} if this node has at least one server. */
    public boolean hasServers() {
        return !servers.isEmpty();
    }

    /** Returns {@code true} if any server matches the given predicate. */
    public boolean hasServers(Predicate<Server> predicate) {
        return servers.stream().anyMatch(predicate);
    }

    /** Adds a server to this node. */
    public void addServer(Server server) {
        servers.add(server);
    }

    /** Returns all servers associated with this node. */
    public List<Server> getServers() {
        return servers;
    }

    /** Returns servers matching the given predicate. */
    public List<Server> getServers(Predicate<Server> predicate) {
        return servers.stream().filter(predicate).collect(toList());
    }
}

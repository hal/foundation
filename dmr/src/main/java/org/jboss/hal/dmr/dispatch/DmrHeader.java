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
package org.jboss.hal.dmr.dispatch;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESPONSE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESPONSE_HEADERS;

/**
 * Represents a custom DMR response header, consisting of an operation name and its associated {@link ModelNode} value.
 *
 * <p>
 * WildFly management operations can return custom headers in their responses. In standalone mode, there is a single header
 * for the server. In domain mode, headers are returned per server, identified by server group, host, and server name. This
 * class encapsulates those headers and provides factory methods to extract them from operation response payloads.
 */
public class DmrHeader {

    // ------------------------------------------------------ factory

    static DmrHeader[] standalone(ModelNode modelNode) {
        return new DmrHeader[]{new DmrHeader(null, null, Names.STANDALONE_SERVER, modelNode)};
    }

    static DmrHeader[] domain(ModelNode modelNode) {
        List<DmrHeader> headers = new ArrayList<>();
        for (Property serverGroup : modelNode.asPropertyList()) {
            ModelNode serverGroupValue = serverGroup.getValue();
            if (serverGroupValue.hasDefined(HOST)) {
                List<Property> hosts = serverGroupValue.get(HOST).asPropertyList();
                for (Property host : hosts) {
                    ModelNode hostValue = host.getValue();
                    List<Property> servers = hostValue.asPropertyList();
                    for (Property server : servers) {
                        ModelNode serverResponse = server.getValue().get(RESPONSE);
                        if (serverResponse.hasDefined(RESPONSE_HEADERS)) {
                            headers.add(new DmrHeader(serverGroup.getName(), host.getName(), server.getName(),
                                    serverResponse.get(RESPONSE_HEADERS)));
                        }
                    }
                }
            }
        }
        return headers.toArray(new DmrHeader[0]);
    }

    // ------------------------------------------------------ instance

    private final String serverGroup;
    private final String host;
    private final String server;
    private final ModelNode header;

    public DmrHeader(String serverGroup, String host, String server, ModelNode header) {
        this.serverGroup = serverGroup;
        this.host = host;
        this.server = server;
        this.header = header;
    }

    public String getServerGroup() {
        return serverGroup;
    }

    public String getHost() {
        return host;
    }

    public String getServer() {
        return server;
    }

    public ModelNode getHeader() {
        return header;
    }
}

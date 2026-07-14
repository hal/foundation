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
package org.jboss.hal.op.configuration.socket;

import org.jboss.hal.meta.AddressTemplate;
import org.patternfly.extension.finder.FinderColumn;
import org.patternfly.extension.finder.ResolvedFinderSegment;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING_GROUP;
import static org.jboss.hal.ui.finder.FinderBricks.crudColumn;
import static org.jboss.hal.resources.Keys.RESOURCE_NAME;

/**
 * Shared factory for socket binding finder columns. Builds a CRUD column that resolves its address template from the selected
 * socket binding group and binding type in the finder path.
 */
class SocketBindingColumns {

    /**
     * Creates a finder column for socket bindings. The column resolves its {@link AddressTemplate} by reading the selected
     * socket binding group and binding type from the current finder path.
     *
     * @param id     the column identifier
     * @param header the column header text
     * @return a configured {@link FinderColumn} for the given socket binding type
     */
    static FinderColumn socketBindingColumn(String id, String header) {
        return crudColumn(id, header, asList("bound", "bound-address", "bound-port", "fixed-port", "interface",
                        "multicast-address", "multicast-port", "port"),
                path -> {
                    ResolvedFinderSegment groupSegment = path.findColumn(SocketBindingGroupColumn.ID);
                    ResolvedFinderSegment typeSegment = path.findColumn(SocketBindingTypeColumn.ID);
                    if (groupSegment != null && typeSegment != null) {
                        String group = groupSegment.item.get(RESOURCE_NAME);
                        String type = typeSegment.item.get(RESOURCE_NAME);
                        if (group != null && type != null) {
                            return AddressTemplate.of(SOCKET_BINDING_GROUP, group).append(type, "*");
                        }
                    }
                    return null;
                },
                null);
    }
}

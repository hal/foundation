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

import org.jboss.elemento.Id;
import org.jboss.hal.meta.AddressTemplate;
import org.patternfly.extension.finder.FinderColumn;
import org.patternfly.extension.finder.FinderSegment;

import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING_GROUP;
import static org.jboss.hal.op.finder.Columns.RESOURCE_NAME_KEY;
import static org.jboss.hal.op.finder.Columns.childResources;
import static org.jboss.hal.op.finder.Columns.resourceColumn;
import static org.patternfly.extension.finder.FinderItem.finderItem;

class SocketBindingColumns {

    static FinderColumn socketBindingColumn(String id, String header) {
        return resourceColumn(id, header)
                .defaultSearch()
                .toggleSearch(column -> column.items().size() > 5)
                .addItems(childResources(
                        path -> {
                            FinderSegment groupSegment = path.findColumn(SocketBindingGroupColumn.ID);
                            FinderSegment typeSegment = path.findColumn(SocketBindingTypeColumn.ID);
                            if (groupSegment != null && typeSegment != null) {
                                String group = groupSegment.item.get(RESOURCE_NAME_KEY);
                                String type = typeSegment.item.get(RESOURCE_NAME_KEY);
                                if (group != null && type != null) {
                                    return AddressTemplate.root()
                                            .append(SOCKET_BINDING_GROUP, group)
                                            .append(type, "*");
                                }
                            }
                            return null;
                        },
                        node -> finderItem(Id.build(node.asString()))
                                .text(node.asString())));
    }
}

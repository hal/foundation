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

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.hal.op.finder.ColumnProvider;
import org.jboss.hal.op.finder.ColumnRegistry;
import org.patternfly.extension.finder.FinderColumn;
import org.patternfly.icon.IconSets.far;
import org.patternfly.icon.IconSets.fas;

import static org.jboss.hal.dmr.ModelDescriptionConstants.LOCAL_DESTINATION_OUTBOUND_SOCKET_BINDING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOTE_DESTINATION_OUTBOUND_SOCKET_BINDING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING;
import static org.jboss.hal.op.finder.Columns.RESOURCE_NAME_KEY;
import static org.patternfly.extension.finder.FinderColumn.finderColumn;
import static org.patternfly.extension.finder.FinderColumnHeader.finderColumnHeader;
import static org.patternfly.extension.finder.FinderItem.finderItem;

@Dependent
public class SocketBindingTypeColumn implements ColumnProvider {

    public static final String ID = "socket-binding-type";
    private final Instance<ColumnRegistry> registry;

    @Inject
    public SocketBindingTypeColumn(Instance<ColumnRegistry> registry) {
        this.registry = registry;
    }

    @Override
    public String identifier() {
        return ID;
    }

    @Override
    public FinderColumn get() {
        return finderColumn(ID)
                .addHeader(finderColumnHeader("Type"))
                .addItem(finderItem("inbound", "Inbound")
                        .icon(far.arrowAltCircleLeft())
                        .store(RESOURCE_NAME_KEY, SOCKET_BINDING)
                        .nextColumn(registry.get().column(InboundColumn.ID)))
                .addItem(finderItem("outbound-local", "Outbound Local")
                        .store(RESOURCE_NAME_KEY, LOCAL_DESTINATION_OUTBOUND_SOCKET_BINDING)
                        .icon(far.arrowAltCircleRight())
                        .nextColumn(registry.get().column(OutboundLocalColumn.ID)))
                .addItem(finderItem("outbound-remote", "Outbound Remote")
                        .store(RESOURCE_NAME_KEY, REMOTE_DESTINATION_OUTBOUND_SOCKET_BINDING)
                        .icon(fas.arrowCircleRight())
                        .nextColumn(registry.get().column(OutboundRemoteColumn.ID)));
    }
}

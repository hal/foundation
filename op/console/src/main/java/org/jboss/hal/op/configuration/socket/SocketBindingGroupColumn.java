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

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.op.finder.ColumnProvider;
import org.jboss.hal.op.finder.ColumnRegistry;
import org.patternfly.extension.finder.FinderColumn;

import static java.util.Arrays.asList;
import static org.jboss.hal.ui.BuildingBlocks.crudColumn;

@Dependent
public class SocketBindingGroupColumn implements ColumnProvider {

    public static final String ID = "socket-binding-group-column";
    private static final AddressTemplate TEMPLATE = AddressTemplate.of("socket-binding-group=*");

    private final Instance<ColumnRegistry> registry;

    @Inject
    public SocketBindingGroupColumn(Instance<ColumnRegistry> registry) {
        this.registry = registry;
    }

    @Override
    public String identifier() {
        return ID;
    }

    @Override
    public FinderColumn get() {
        return crudColumn(ID, "Socket Binding Group",
                asList("default-interface", "port-offset", "local-destination-outbound-socket-binding",
                        "remote-destination-outbound-socket-binding"),
                __ -> TEMPLATE,
                registry.get().column(SocketBindingTypeColumn.ID));
    }
}

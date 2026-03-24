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
import jakarta.inject.Inject;

import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.op.finder.ColumnProvider;
import org.patternfly.extension.finder.FinderColumn;

import static org.jboss.hal.op.configuration.socket.SocketBindingColumns.socketBindingColumn;

@Dependent
public class OutboundLocalColumn implements ColumnProvider {

    public static final String ID = "socket-binding-outbound-local";
    private final CrudOperations crud;

    @Inject
    public OutboundLocalColumn(CrudOperations crud) {
        this.crud = crud;
    }

    @Override
    public String identifier() {
        return ID;
    }

    @Override
    public FinderColumn get() {
        return socketBindingColumn(crud, ID, "Outbound Local");
    }
}

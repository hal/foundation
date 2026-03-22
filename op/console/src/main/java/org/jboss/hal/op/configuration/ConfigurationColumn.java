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
package org.jboss.hal.op.configuration;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.hal.op.finder.ColumnProvider;
import org.jboss.hal.op.finder.ColumnRegistry;
import org.patternfly.extension.finder.FinderColumn;

import static org.patternfly.extension.finder.FinderColumn.finderColumn;
import static org.patternfly.extension.finder.FinderColumnHeader.finderColumnHeader;
import static org.patternfly.extension.finder.FinderItem.finderItem;

@Dependent
public class ConfigurationColumn implements ColumnProvider {

    public static final String ID = "configuration-column";
    private final Instance<ColumnRegistry> registry;

    @Inject
    public ConfigurationColumn(Instance<ColumnRegistry> registry) {
        this.registry = registry;
    }

    @Override
    public String identifier() {
        return ID;
    }

    @Override
    public FinderColumn get() {
        return finderColumn(ID)
                .addHeader(finderColumnHeader("Configuration"))
                .addItem(finderItem("subsystems-item", "Subsystems")
                        .nextColumn(registry.get().column(SubsystemsColumn.ID)))
                .addItem(finderItem("interfaces-item", "Interfaces"))
                .addItem(finderItem("socket-bindings-item", "Socket bindings"))
                .addItem(finderItem("paths-item", "Paths"))
                .addItem(finderItem("system-properties-item", "System properties"));
    }
}

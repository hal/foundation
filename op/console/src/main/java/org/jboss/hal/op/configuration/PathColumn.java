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

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.op.finder.ColumnProvider;
import org.patternfly.extension.finder.FinderColumn;

import static java.util.Arrays.asList;
import static org.jboss.hal.ui.BuildingBlocks.crudColumn;

@Dependent
public class PathColumn implements ColumnProvider {

    public static final String ID = "path-column";
    private static final AddressTemplate TEMPLATE = AddressTemplate.of("path=*");

    @Override
    public String identifier() {
        return ID;
    }

    @Override
    public FinderColumn get() {
        return crudColumn(ID, "Path", asList("path", "read-only", "relative-to"), __ -> TEMPLATE, null);
    }
}

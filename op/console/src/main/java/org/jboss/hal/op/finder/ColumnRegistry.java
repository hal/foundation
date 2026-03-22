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
package org.jboss.hal.op.finder;

import java.util.HashMap;
import java.util.Map;

import jakarta.ejb.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.patternfly.extension.finder.FinderColumn;

@Startup
@ApplicationScoped
public class ColumnRegistry {

    private final Map<String, ColumnProvider> columns;

    @Inject
    public ColumnRegistry(Instance<ColumnProvider> providers) {
        this.columns = new HashMap<>();
        for (ColumnProvider provider : providers) {
            String identifier = provider.identifier();
            columns.put(identifier, provider);
        }
    }

    public FinderColumn column(String identifier) {
        // TODO Error handling
        return columns.get(identifier).get();
    }
}

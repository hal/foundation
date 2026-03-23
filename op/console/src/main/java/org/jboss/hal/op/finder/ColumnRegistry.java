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
import java.util.function.Supplier;

import jakarta.ejb.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.elemento.logger.Logger;
import org.patternfly.extension.finder.FinderColumn;

/**
 * A registry for managing and accessing {@link ColumnProvider} instances by their identifiers. This class collects all
 * available {@link ColumnProvider} implementations during application startup and makes them accessible via their unique
 * identifiers.
 * <p>
 * <b>Key Responsibilities:</b>
 * - Registers all {@link ColumnProvider} implementations available at runtime. - Provides a mechanism to retrieve a
 * {@link FinderColumn} supplier based on a given identifier.
 * <p>
 * <b>Lifecycle and Scope:</b>
 * - The class is annotated with {@link Startup} and {@link ApplicationScoped}, indicating it is initialized during application
 * startup and shared across the application's lifecycle.
 * <p>
 * <b>CDI Injection:</b>
 * - The {@link Instance} of {@link ColumnProvider} is injected into the constructor to dynamically gather all column provider
 * implementations.
 */
@Startup
@ApplicationScoped
public class ColumnRegistry {

    private static final Logger logger = Logger.getLogger(ColumnRegistry.class.getName());
    private final Map<String, ColumnProvider> columns;

    @Inject
    public ColumnRegistry(Instance<ColumnProvider> providers) {
        this.columns = new HashMap<>();
        for (ColumnProvider provider : providers) {
            String identifier = provider.identifier();
            columns.put(identifier, provider);
        }
    }

    /**
     * Retrieves a {@link Supplier} of {@link FinderColumn} based on the provided identifier. If no {@link ColumnProvider} is
     * found for the specified identifier, this method logs an error and returns {@code null}.
     *
     * @param identifier the unique identifier of the {@link ColumnProvider} to locate
     * @return a {@link Supplier} of {@link FinderColumn} associated with the given identifier, or {@code null} if no matching
     * {@link ColumnProvider} is found
     */
    public Supplier<FinderColumn> column(String identifier) {
        ColumnProvider provider = columns.get(identifier);
        if (provider == null) {
            logger.error("No column provider found for identifier: " + identifier);
            return null;
        }
        return provider;
    }
}

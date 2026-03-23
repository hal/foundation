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

import java.util.function.Supplier;

import org.patternfly.extension.finder.FinderColumn;

/**
 * A provider for finder columns. Implementations supply a {@link FinderColumn} instance and are registered in the
 * {@link ColumnRegistry} by their {@linkplain #identifier() identifier}.
 *
 * <p><b>Important:</b> If a column provider needs access to the {@link ColumnRegistry} (e.g., to resolve next columns),
 * it <em>must</em> inject a CDI {@link jakarta.enterprise.inject.Instance Instance&lt;ColumnRegistry&gt;} instead of injecting
 * the {@link ColumnRegistry} directly. This avoids circular dependency issues during CDI initialization.
 * <p>
 * {@snippet :
 * public class FooColumn implements ColumnProvider {
 *
 *     private final Instance<ColumnRegistry> registry;
 *
 *     @Inject
 *     public FooColumn(Instance<ColumnRegistry> registry) {
 *         this.registry = registry;
 *     }
 *
 *     @Override
 *     public FinderColumn get() {
 *         return finderColumn("foo-column", "Foo")
 *                 .addItem(finderItem("bar-item", "Bar")
 *                         .nextColumn(registry.get().column("bar-column")));
 *     }
 * }
 *}
 */
public interface ColumnProvider extends Supplier<FinderColumn> {

    String identifier();
}

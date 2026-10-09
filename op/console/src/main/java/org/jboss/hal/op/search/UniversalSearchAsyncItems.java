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
package org.jboss.hal.op.search;

import org.jboss.hal.core.mgt.ModelGraphTools;
import org.jboss.hal.ui.component.ResourceAddressAsyncItems;
import org.patternfly.async.AsyncItems;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MenuList;
import org.patternfly.core.HasValue;

import elemental2.promise.Promise;

import static java.util.Collections.emptyList;

/**
 * Compound async items implementation that delegates to the appropriate search strategy based on the current input value and
 * model graph tools (MGT) availability.
 *
 * <p>This is the single {@link AsyncItems} instance registered on the search input's {@link MenuList}. It routes requests to:
 * <ul>
 *     <li>{@link ResourceAddressAsyncItems} — when the input starts with {@code /} (resource address typeahead against the live
 *         WildFly server)</li>
 *     <li>{@link MgtSearchAsyncItems} — when MGT is available and the input is a free-text query</li>
 *     <li>An empty result — when MGT is not available and the input is not a resource address</li>
 * </ul>
 *
 * <p>The separation of concerns is preserved through delegation: this class contains no search logic, only routing. The actual
 * search implementations are independent and interchangeable.
 *
 * @see ResourceAddressAsyncItems
 * @see MgtSearchAsyncItems
 */
class UniversalSearchAsyncItems implements AsyncItems<MenuList, MenuItem> {

    private final HasValue<String> valueProvider;
    private final ResourceAddressAsyncItems addressItems;
    private final MgtSearchAsyncItems mgtItems;
    private boolean mgtAvailable;

    UniversalSearchAsyncItems(HasValue<String> valueProvider, ModelGraphTools modelGraphTools) {
        this.valueProvider = valueProvider;
        this.addressItems = new ResourceAddressAsyncItems(valueProvider);
        this.mgtItems = new MgtSearchAsyncItems(valueProvider, modelGraphTools);
        this.mgtAvailable = false;
    }

    @Override
    public Promise<Iterable<MenuItem>> apply(MenuList menuList) {
        String value = valueProvider.value();
        if (value == null || value.trim().isEmpty()) {
            return Promise.resolve(emptyList());
        }
        if (MgtSearchAsyncItems.hasTypeFilter(value)) {
            if (mgtAvailable) {
                return mgtItems.apply(menuList);
            }
            return Promise.resolve(emptyList());
        }
        if (value.startsWith("/")) {
            return addressItems.apply(menuList);
        }
        if (mgtAvailable) {
            return mgtItems.apply(menuList);
        }
        return Promise.resolve(emptyList());
    }

    /** Updates the MGT availability status. */
    void mgtAvailable(boolean available) {
        this.mgtAvailable = available;
    }

    /** Returns whether MGT is currently available. */
    boolean isMgtAvailable() {
        return mgtAvailable;
    }
}

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
package org.jboss.hal.ui.brick;

import org.jboss.elemento.IsElement;
import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.elemento.Elements.setVisible;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateActions.emptyStateActions;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.icon.IconSets.fas.ban;
import static org.patternfly.icon.IconSets.fas.magnifyingGlass;

/**
 * Factory methods for common empty-state components and toggle utilities.
 * <p>
 * Provides reusable empty states for "no results" (search/filter misses), "no match" (filter misses with a clear button), and
 * "no items" (empty collections). All factory methods return {@link EmptyState}, which is a builder — callers can chain
 * {@code .addFooter()}, {@code .status()}, etc. for further customization.
 */
public final class EmptyStateBricks {

    /**
     * Creates a "no results found" empty state with a search icon, used when a filter or search yields no matches.
     *
     * @param body the body text explaining the empty result
     * @return an empty state component that can be further customized via the builder pattern
     */
    public static EmptyState noResults(String body) {
        return emptyState()
                .icon(magnifyingGlass())
                .text("No results found")
                .addBody(emptyStateBody().text(body));
    }

    /**
     * Creates a "no match" empty state with a search icon and a "Clear all filters" button that resets the given filter.
     *
     * @param filter the active filter whose state will be reset when the user clicks "Clear all filters"
     * @return an empty state component that can be further customized via the builder pattern
     */
    public static <T> EmptyState noMatch(Filter<T> filter) {
        return noResults("No results match the filter criteria. Clear all filters and try again.")
                .addFooter(emptyStateFooter().addActions(emptyStateActions().add(
                        button("Clear all filters").link().onClick((event, component) -> filter.resetAll()))));
    }

    /**
     * Creates a "no items" empty state with a ban icon, used when a collection is genuinely empty (not filtered).
     *
     * @param title the heading text (e.g. "No attributes", "No capabilities")
     * @param body  the body text explaining why no items exist
     * @return an empty state component that can be further customized via the builder pattern
     */
    public static EmptyState noItems(String title, String body) {
        return emptyState()
                .icon(ban())
                .text(title)
                .addBody(emptyStateBody().text(body));
    }

    /**
     * Creates an error empty state with danger status, used for operation failures and missing resources.
     *
     * @param title the heading text (e.g. "Operation failed", "Resource not found")
     * @param body  the body text explaining the error
     * @return an empty state component that can be further customized via the builder pattern
     */
    public static EmptyState error(String title, String body) {
        return emptyState()
                .status(danger)
                .text(title)
                .addBody(emptyStateBody().text(body));
    }

    /**
     * Shows or hides an element within a container. Appends the element on first show; removes it on hide.
     *
     * @param element   the element to show or hide
     * @param container the container element
     * @param show      {@code true} to show, {@code false} to hide
     */
    public static void toggle(IsElement<HTMLElement> element, HTMLElement container, boolean show) {
        if (show) {
            if (container.contains(element.element())) {
                setVisible(element, true);
            } else {
                container.appendChild(element.element());
            }
        } else {
            failSafeRemoveFromParent(element);
        }
    }

    private EmptyStateBricks() {
    }
}

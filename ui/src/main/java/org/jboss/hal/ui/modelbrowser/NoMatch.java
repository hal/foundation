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
package org.jboss.hal.ui.modelbrowser;

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
import static org.patternfly.component.emptystate.EmptyStateHeader.emptyStateHeader;
import static org.patternfly.icon.IconSets.fas.search;

public class NoMatch<T> implements IsElement<HTMLElement> {

    private final EmptyState emptyState;

    public NoMatch(Filter<T> filter) {
        this.emptyState = emptyState()
                .addHeader(emptyStateHeader()
                        .icon(search())
                        .text("No results found"))
                .addBody(emptyStateBody()
                        .text("No results match the filter criteria. Clear all filters and try again."))
                .addFooter(emptyStateFooter()
                        .addActions(emptyStateActions()
                                .add(button("Clear all filters").link()
                                        .onClick((event, component) -> filter.resetAll()))));
    }

    @Override
    public HTMLElement element() {
        return emptyState.element();
    }

    public void toggle(HTMLElement container, boolean show) {
        if (show) {
            if (container.contains(element())) {
                setVisible(this, true);
            } else {
                container.appendChild(element());
            }
        } else {
            failSafeRemoveFromParent(this);
        }
    }
}

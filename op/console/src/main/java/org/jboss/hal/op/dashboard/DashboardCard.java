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
package org.jboss.hal.op.dashboard;

import org.jboss.elemento.IsElement;
import org.patternfly.component.card.CardActions;

import elemental2.dom.HTMLElement;

import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.card.CardActions.cardActions;
import static org.patternfly.icon.IconSets.fas.rotateRight;

/** Base interface for dashboard cards. Each card renders as an HTML element and supports manual refresh. */
interface DashboardCard extends IsElement<HTMLElement> {

    /** Refreshes the card content by fetching current data from the management interface. */
    void refresh();

    /** Returns card actions containing a refresh button. */
    default CardActions refreshActions() {
        return cardActions()
                .add(button().plain().icon(rotateRight()).onClick((e, c) -> refresh()));
    }
}

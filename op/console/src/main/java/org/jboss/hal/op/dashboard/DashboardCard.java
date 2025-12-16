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
import static org.patternfly.icon.IconSets.fas.redo;

interface DashboardCard extends IsElement<HTMLElement> {

    void refresh();

    default CardActions refreshActions() {
        return cardActions()
                .add(button().plain().icon(redo()).onClick((e, c) -> refresh()));
    }
}

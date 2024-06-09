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
package org.jboss.hal.op.skeleton;

import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.style.Size;

import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateHeader.emptyStateHeader;
import static org.patternfly.icon.IconSets.fas.exclamationCircle;

public class EmptyStates {

    public static EmptyState domainModeNotSupported(Size size) {
        return nyi(size, "Domain mode", "Domain mode is not supported yet.");
    }

    public static EmptyState nyi(Size size, String header, String text) {
        return emptyState().size(size)
                .addHeader(emptyStateHeader()
                        .icon(exclamationCircle())
                        .text(header))
                .addBody(emptyStateBody().textContent(text));
    }
}

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
package org.jboss.hal.op.bootstrap;

import org.jboss.elemento.IsElement;
import org.patternfly.component.content.Content;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.location;
import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.br;
import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.pre;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateActions.emptyStateActions;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;

public class BootstrapErrorElement implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static BootstrapErrorElement bootstrapError(BootstrapError error) {
        return new BootstrapErrorElement(error);
    }

    // ------------------------------------------------------ instance

    private final HTMLElement root;

    BootstrapErrorElement(BootstrapError error) {
        String selectUrl = location.origin + location.pathname;
        this.root = emptyState()
                .status(danger)
                .text(header(error))
                .addBody(emptyStateBody()
                        .add(details(error)))
                .addFooter(emptyStateFooter()
                        .addActions(emptyStateActions()
                                .add(button("Select management interface", selectUrl).primary())))
                .element();
    }

    private String header(BootstrapError error) {
        switch (error.failure()) {
            case NO_ENDPOINT_SPECIFIED:
                return "No management interface specified";
            case NO_ENDPOINT_FOUND:
                return "Management interface not found";
            case NOT_AN_ENDPOINT:
                return "Not an valid management interface";
            case NETWORK_ERROR:
                return "Network Error";
            case UNKNOWN:
                return "Unknown error";
        }
        return "Unknown error";
    }

    private HTMLElement details(BootstrapError error) {
        Content textContent = content();
        switch (error.failure()) {
            case NO_ENDPOINT_SPECIFIED:
                textContent.add(p()
                        .add("You have not specified a value for parameter ")
                        .add(code().text(error.data()))
                        .add(".")
                        .add(br())
                        .add("You must specify a URL of a management interface or the name of a saved management interface."));
                break;
            case NO_ENDPOINT_FOUND:
                textContent.add(p()
                        .add("The management interface ")
                        .add(code().text(error.data()))
                        .add(" was not found."));
                break;
            case NOT_AN_ENDPOINT:
                textContent.add(p()
                        .add("The management interface ")
                        .add(a(error.data(), "_blank").text(error.data()))
                        .add(" is not a valid management interface."));
                break;
            case NETWORK_ERROR:
                textContent.add(p()
                        .add("A network error occurred while accessing ")
                        .add(a(error.data(), "_blank").text(error.data()))
                        .add("."));
                break;
            case UNKNOWN:
                textContent
                        .add(p().text("An unknown error occurred."))
                        .add(pre().text(error.data()));
                break;
        }
        return textContent.element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}

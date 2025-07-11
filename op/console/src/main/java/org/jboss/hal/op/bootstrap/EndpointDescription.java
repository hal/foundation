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

import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.location;
import static org.jboss.elemento.Elements.br;
import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.small;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.patternfly.component.codeblock.CodeBlock.codeBlock;
import static org.patternfly.component.expandable.ExpandableSection.expandableSection;
import static org.patternfly.component.expandable.ExpandableSectionContent.expandableSectionContent;
import static org.patternfly.component.expandable.ExpandableSectionToggle.expandableSectionToggle;
import static org.patternfly.component.popover.Popover.popover;
import static org.patternfly.style.Classes.helpText;
import static org.patternfly.style.Classes.util;

class EndpointDescription implements IsElement<HTMLElement> {

    private final HTMLElement root;

    EndpointDescription() {
        String allowedOriginId = Id.unique();
        this.root = div()
                .add(p()
                        .add("HAL runs in standalone mode. To continue, you must connect to the management interface of a WildFly server. This management interface must have an ")
                        .add(span().css(halModifier(helpText))
                                .id(allowedOriginId)
                                .text("allowed origin"))
                        .add(" for ")
                        .add(span().text(location.origin))
                        .add("."))
                .add(popover()
                        .trigger(By.id(allowedOriginId))
                        .addHeader("Allowed origin")
                        .addBody(
                                "An allowed origin is a trusted origin for sending Cross-Origin Resource Sharing (CORS) requests on the management API once the user is authenticated."))
                .add(expandableSection()
                        .addToggle(expandableSectionToggle("Show more", "Show less"))
                        .addContent(expandableSectionContent()
                                .add(p().css(util("mb-sm"))
                                        .add("Use the following CLI commands to add an allowed origin for ")
                                        .add(span().text(location.origin))
                                        .add(".")
                                        .add(br())
                                        .add(small()
                                                .add("For domain mode prepend the first command with ")
                                                .add(code().text("/host=primary"))
                                                .add(" and use ")
                                                .add(code().text("reload --host=primary"))
                                                .add(" to reload the domain.")))
                                .add(codeBlock()
                                        .code("/core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=" + location.origin + ")\nreload"))))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}

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
package org.jboss.hal.op.task;

import jakarta.enterprise.context.Dependent;

import org.jboss.hal.task.Task;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;

import static org.jboss.hal.core.Notification.nyi;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.icon.IconSets.fas.server;

@Dependent
public class ReverseProxyTask implements Task {

    public static final String TASK_ID = ReverseProxyTask.class.getName();

    @Override
    public String id() {
        return TASK_ID;
    }

    @Override
    public String title() {
        return "Reverse proxy";
    }

    @Override
    public Element icon() {
        return server().element();
    }

    @Override
    public HTMLElement summary() {
        return content(p)
                .add("Create and configure a reverse proxy in the Undertow subsystem.")
                .element();
    }

    @Override
    public Iterable<HTMLElement> elements() {
        return null;
    }

    @Override
    public void run() {
        uic().notifications().send(nyi());
    }

    @Override
    public boolean enabled() {
        return false;
    }
}

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

import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.p;
import static org.jboss.hal.core.Notification.nyi;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.icon.IconSets.fas.shieldAlt;

@Dependent
public class ManagementSSLTask implements Task {

    public static final String TASK_ID = ManagementSSLTask.class.getName();

    @Override
    public String id() {
        return TASK_ID;
    }

    @Override
    public String title() {
        return "Management SSL";
    }

    @Override
    public Element icon() {
        return shieldAlt().element();
    }

    @Override
    public HTMLElement summary() {
        return content(p)
                .add("Enable / disable SSL for a given management interface. By default the ")
                .add(code("http-interface"))
                .add(" is used, but other management interface can be targeted as well.")
                .element();
    }

    @Override
    public HTMLElement moreInfo() {
        return content()
                .add(p().text("This task lets you manage SSL for a management interface."))
                .element();
    }

    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public void run() {
        uic().notifications().send(nyi());
    }
}

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
package org.jboss.hal.task;

import org.patternfly.component.icon.Icon;
import org.patternfly.component.page.PageSection;
import org.patternfly.component.title.Title;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;

import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.component.page.PageSection.pageSection;
import static org.patternfly.style.Size._3xl;

/**
 * Represents a task that can be executed within the console. Each task provides details such as an identifier, title, icon, and
 * summary, and defines the operation it performs.
 */
public interface Task {

    String id();

    String title();

    Element icon();

    HTMLElement summary();

    Iterable<HTMLElement> elements();

    void run();

    default boolean enabled() {
        return true;
    }

    default PageSection header() {
        return pageSection()
                .add(content()
                        .add(Title.title(1, _3xl)
                                .add(Icon.icon(icon()).inline())
                                .add(" " + title())))
                .add(content(p).editorial().add(summary()));
    }
}

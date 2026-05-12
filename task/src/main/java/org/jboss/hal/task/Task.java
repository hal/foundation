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
 * Represents a guided, executable task in the HAL management console. Tasks appear on the console's tasks page and provide a
 * structured way for users to perform multi-step operations such as configuring a datasource, enabling SSL, or setting up a
 * reverse proxy.
 *
 * <p>
 * Each task declares display metadata — {@link #id() id}, {@link #title() title}, {@link #icon() icon}, and
 * {@link #summary() summary} — along with UI {@link #elements() elements} for user interaction and a {@link #run() run} method
 * that executes the operation. Tasks can be conditionally shown or hidden by overriding {@link #enabled()}.
 *
 * <p>
 * Implementations should be CDI beans (typically {@code @Dependent} scoped) so the console can discover and display them
 * automatically. The default {@link #header()} method renders a standard page header from the task's title, icon, and summary.
 */
public interface Task {

    /** Returns a unique identifier for this task, typically the fully qualified class name. */
    String id();

    /** Returns the human-readable title displayed on the tasks page and in the task header. */
    String title();

    /** Returns the icon element displayed alongside the task title. */
    Element icon();

    /** Returns a short description of what this task does, displayed below the title on the tasks page. */
    HTMLElement summary();

    /** Returns the UI elements that make up the task's interactive content, displayed below the {@link #header()}. */
    Iterable<HTMLElement> elements();

    /** Executes the task's operation. Called when the user triggers the task after interacting with its {@link #elements()}. */
    void run();

    /**
     * Returns whether this task is available to the user. Disabled tasks are not shown on the tasks page. Defaults to
     * {@code true}.
     */
    default boolean enabled() {
        return true;
    }

    /**
     * Renders a standard page header containing the task's {@link #icon()}, {@link #title()}, and {@link #summary()}. Override
     * this method to provide a custom header layout.
     */
    default PageSection header() {
        return pageSection()
                .add(content()
                        .add(Title.title(1, _3xl)
                                .add(Icon.icon(icon()).inline())
                                .add(" " + title())))
                .add(content(p).editorial().add(summary()));
    }
}

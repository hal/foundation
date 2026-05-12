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

/**
 * Provides the {@link org.jboss.hal.task.Task} interface for defining executable tasks in the HAL management console.
 *
 * <h2>Key Concepts</h2>
 * <p>
 * A task represents a guided, multi-step operation that users can launch from the console's task page. Each task declares its
 * identity ({@link org.jboss.hal.task.Task#id() id}), display metadata ({@link org.jboss.hal.task.Task#title() title},
 * {@link org.jboss.hal.task.Task#icon() icon}, {@link org.jboss.hal.task.Task#summary() summary}), and the UI elements and
 * logic to execute it ({@link org.jboss.hal.task.Task#elements() elements}, {@link org.jboss.hal.task.Task#run() run}). Tasks
 * can be conditionally enabled via {@link org.jboss.hal.task.Task#enabled() enabled}.
 *
 * <h2>Usage</h2>
 * <p>
 * Implement the {@link org.jboss.hal.task.Task} interface and register it as a CDI bean. The console discovers tasks
 * automatically and displays them on the tasks page.
 * {@snippet :
 * @Dependent
 * public class MyTask implements Task {
 *
 *     public String id() {
 *         return MyTask.class.getName();
 *     }
 *
 *     public String title() {
 *         return "Configure Logging";
 *     }
 *
 *     public Element icon() {
 *         return clipboard().element();
 *     }
 *
 *     public HTMLElement summary() {
 *         return content(p).add("Configures the logging subsystem.").element();
 *     }
 *
 *     public Iterable<HTMLElement> elements() {
 *         return List.of();
 *     }
 *
 *     public void run() {
 *         // execute the task logic
 *     }
 * }
 *}
 */
package org.jboss.hal.task;

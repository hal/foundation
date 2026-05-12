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
 * Defines the base event interfaces for the HAL management console.
 *
 * <h2>Key Concepts</h2>
 * <p>
 * HAL uses two distinct event mechanisms:
 * <dl>
 *     <dt>{@link org.jboss.hal.event.UIEvent}</dt>
 *     <dd>Browser-level events dispatched as {@link elemental2.dom.CustomEvent}s on HTML elements. Use for UI-local
 *     communication within a component subtree. Implementations provide static factory methods to create typed custom events
 *     and use {@link org.jboss.hal.event.UIEvent#type(String, String...) UIEvent.type()} to build namespaced event type
 *     strings.</dd>
 *
 *     <dt>{@link org.jboss.hal.event.ApplicationEvent}</dt>
 *     <dd>Application-level events managed by CDI. Use for cross-component communication that is not tied to the DOM
 *     hierarchy. Fire with {@code @Inject Event<T>} and observe with {@code @Observes}.</dd>
 * </dl>
 *
 * <h2>Usage</h2>
 * <p>
 * Define a UI event by extending {@link org.jboss.hal.event.UIEvent}:
 * {@snippet :
 *     interface SelectResource extends UIEvent {
 *         String TYPE = UIEvent.type("browser", "select");
 *
 *         static CustomEvent<String> event(String resourceAddress) {
 *             CustomEventInit<String> init = CustomEventInit.create();
 *             init.setDetail(resourceAddress);
 *             return new CustomEvent<>(TYPE, init);
 *         }
 *     }
 * }
 * <p>
 * Define an application event by implementing {@link org.jboss.hal.event.ApplicationEvent}:
 * {@snippet :
 *     public class SettingsChangedEvent implements ApplicationEvent {
 *         public final String key;
 *         public SettingsChangedEvent(String key) {
 *             this.key = key;
 *         }
 *     }
 * }
 */
package org.jboss.hal.event;

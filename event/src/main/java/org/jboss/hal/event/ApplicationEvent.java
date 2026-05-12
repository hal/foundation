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
package org.jboss.hal.event;

/**
 * Marker interface for HAL application events managed and dispatched by CDI. Unlike {@link UIEvent}, which uses browser DOM
 * custom events, application events are fired with {@code @Inject Event<T>} and observed with {@code @Observes}, enabling
 * cross-component communication independent of the DOM hierarchy.
 */
public interface ApplicationEvent {
}

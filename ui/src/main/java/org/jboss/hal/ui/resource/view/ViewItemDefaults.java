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
package org.jboss.hal.ui.resource.view;

/**
 * Provides default constants and configurations for {@code ViewItem} implementations. This interface defines values that are
 * commonly used across various types of view items to ensure consistent behavior and appearance in UI components.
 */
interface ViewItemDefaults {

    /**
     * Defines the default number of labels to display in components utilizing label groups. This constant is used to control
     * the maximum number of labels rendered for specific UI elements, ensuring that the display remains compact and
     * manageable.
     */
    int NUM_LABELS = 5;
}

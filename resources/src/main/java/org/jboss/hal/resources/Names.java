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
package org.jboss.hal.resources;

/**
 * Common names and technical terms which are not meant to be translated.
 */
public interface Names {

    /** Default browser tab title pattern; {@code %n} is replaced with the instance name. */
    String BROWSER_DEFAULT_TITLE = "%n | Management Console";

    /** Fallback browser tab title used when the instance name is unknown. */
    String BROWSER_FALLBACK_TITLE = "HAL Management Console";

    /** Placeholder text for values that are not available. */
    String NOT_AVAILABLE = "n/a";

    /** Display name for the standalone server in the navigation and UI. */
    String STANDALONE_SERVER = "Standalone Server";
}

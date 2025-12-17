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
package org.jboss.hal.ui;

import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

/**
 * J2CL mapping for the DOMPurify HTML sanitizer library.
 *
 * @see <a href="https://github.com/cure53/DOMPurify">https://github.com/cure53/DOMPurify</a>
 */
@JsType(isNative = true, namespace = GLOBAL)
public class DOMPurify {

    /**
     * Sanitizes dirty HTML string and returns sanitized HTML.
     *
     * @param dirty the HTML string to sanitize
     * @return the sanitized HTML string
     */
    public static native String sanitize(String dirty);

    /**
     * Sanitizes dirty HTML string with configuration options and returns sanitized HTML.
     *
     * @param dirty  the HTML string to sanitize
     * @param config the configuration options for sanitization
     * @return the sanitized HTML string
     */
    public static native String sanitize(String dirty, DOMPurifyConfig config);
}

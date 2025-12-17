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

import jsinterop.annotations.JsMethod;

/**
 * J2CL mapping for the marked Markdown parser library.
 *
 * @see <a href="https://marked.js.org/">https://marked.js.org/</a>
 */
public class Marked {

    /**
     * Parses a Markdown string into HTML and sanitizes the resulting HTML using {@link DOMPurify#sanitize(String)}.
     *
     * @param markdown the Markdown string to be parsed and sanitized
     * @return the sanitized HTML string
     */
    public static String parseAndPurify(String markdown) {
        return DOMPurify.sanitize(parse(markdown));
    }

    /**
     * Parses a Markdown string into HTML and sanitizes the resulting HTML using {@link DOMPurify#sanitize(String)}.
     *
     * @param markdown the Markdown string to be parsed and sanitized
     * @param options  the options for parsing
     * @return the sanitized HTML string
     */
    public static String parseAndPurify(String markdown, MarkedOptions options) {
        return DOMPurify.sanitize(parse(markdown, options));
    }

    /**
     * Parses a Markdown string inline into HTML and sanitizes the resulting HTML using {@link DOMPurify#sanitize(String)}.
     *
     * @param markdown the Markdown string to be parsed and sanitized
     * @return the sanitized HTML string
     */
    public static String parseInlineAndPurify(String markdown) {
        return DOMPurify.sanitize(parseInline(markdown));
    }

    /**
     * Parses a Markdown string inline into HTML and sanitizes the resulting HTML using {@link DOMPurify#sanitize(String)}.
     *
     * @param markdown the Markdown string to be parsed and sanitized
     * @param options  the options for parsing
     * @return the sanitized HTML string
     */
    public static String parseInlineAndPurify(String markdown, MarkedOptions options) {
        return DOMPurify.sanitize(parseInline(markdown, options));
    }

    /**
     * Parses Markdown string and returns HTML.
     *
     * @param markdown the Markdown string to parse
     * @return the parsed HTML string
     */
    @JsMethod(namespace = "marked", name = "parse")
    public static native String parse(String markdown);

    /**
     * Parses Markdown string with options and returns HTML.
     *
     * @param markdown the Markdown string to parse
     * @param options  the options for parsing
     * @return the parsed HTML string
     */
    @JsMethod(namespace = "marked", name = "parse")
    public static native String parse(String markdown, MarkedOptions options);

    /**
     * Parses Markdown string and returns HTML without the surrounding {@code <p>} tag.
     *
     * @param markdown the Markdown string to parse
     * @return the parsed HTML string
     */
    @JsMethod(namespace = "marked", name = "parseInline")
    public static native String parseInline(String markdown);

    /**
     * Parses Markdown string with options and returns HTML without the surrounding {@code <p>} tag.
     *
     * @param markdown the Markdown string to parse
     * @param options  the options for parsing
     * @return the parsed HTML string
     */
    @JsMethod(namespace = "marked", name = "parseInline")
    public static native String parseInline(String markdown, MarkedOptions options);
}

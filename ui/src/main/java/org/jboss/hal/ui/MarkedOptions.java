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

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Options for configuring the marked markdown parser.
 *
 * @see <a href="https://marked.js.org/using_advanced#options">https://marked.js.org/using_advanced#options</a>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class MarkedOptions {

    @JsOverlay
    public static MarkedOptions markedOptions() {
        return new MarkedOptions();
    }

    /**
     * Enable asynchronous parsing.
     */
    public boolean async;

    /**
     * A prefix URL for any relative link.
     */
    public String baseUrl;

    /**
     * If true, add {@code <br>} on a single line break (copies GitHub behavior on comments, but not on rendered markdown files).
     * Requires {@code gfm} to be {@code true}.
     */
    public boolean breaks;

    /**
     * Enable GitHub Flavored Markdown.
     */
    public boolean gfm;

    /**
     * Include an id attribute when emitting headings (h1, h2, h3, etc).
     */
    public boolean headerIds;

    /**
     * A string to prefix the id attribute when emitting headings (h1, h2, h3, etc).
     */
    public String headerPrefix;

    /**
     * A prefix for code block {@code class} attribute.
     */
    public String langPrefix;

    /**
     * If true, conform to the original markdown.pl as much as possible. Don't fix original markdown bugs or behavior. Turns off and overrides {@code gfm}.
     */
    public boolean pedantic;

    /**
     * If true, the parser does not throw any exception.
     */
    public boolean silent;

    /**
     * If true, use "smart" typographic punctuation for things like quotes and dashes.
     */
    public boolean smartypants;

    @JsOverlay
    public final MarkedOptions async(boolean async) {
        this.async = async;
        return this;
    }

    @JsOverlay
    public final MarkedOptions baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    @JsOverlay
    public final MarkedOptions breaks(boolean breaks) {
        this.breaks = breaks;
        return this;
    }

    @JsOverlay
    public final MarkedOptions gfm(boolean gfm) {
        this.gfm = gfm;
        return this;
    }

    @JsOverlay
    public final MarkedOptions headerIds(boolean headerIds) {
        this.headerIds = headerIds;
        return this;
    }

    @JsOverlay
    public final MarkedOptions headerPrefix(String headerPrefix) {
        this.headerPrefix = headerPrefix;
        return this;
    }

    @JsOverlay
    public final MarkedOptions langPrefix(String langPrefix) {
        this.langPrefix = langPrefix;
        return this;
    }

    @JsOverlay
    public final MarkedOptions pedantic(boolean pedantic) {
        this.pedantic = pedantic;
        return this;
    }

    @JsOverlay
    public final MarkedOptions silent(boolean silent) {
        this.silent = silent;
        return this;
    }

    @JsOverlay
    public final MarkedOptions smartypants(boolean smartypants) {
        this.smartypants = smartypants;
        return this;
    }
}

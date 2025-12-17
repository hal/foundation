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
import jsinterop.base.JsArrayLike;

/**
 * Configuration options for DOMPurify.
 *
 * @see <a href="https://github.com/cure53/DOMPurify#can-i-configure-dompurify">https://github.com/cure53/DOMPurify#can-i-configure-dompurify</a>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class DOMPurifyConfig {

    @JsOverlay
    public static DOMPurifyConfig domPurifyConfig() {
        return new DOMPurifyConfig();
    }

    /**
     * Array of allowed tags.
     */
    public JsArrayLike<String> ALLOWED_TAGS;

    /**
     * Array of allowed attributes.
     */
    public JsArrayLike<String> ALLOWED_ATTR;

    /**
     * Array of forbidden tags (overrides ALLOWED_TAGS).
     */
    public JsArrayLike<String> FORBID_TAGS;

    /**
     * Array of forbidden attributes (overrides ALLOWED_ATTR).
     */
    public JsArrayLike<String> FORBID_ATTR;

    /**
     * Allow ARIA attributes.
     */
    public boolean ALLOW_ARIA_ATTR;

    /**
     * Allow custom data attributes.
     */
    public boolean ALLOW_DATA_ATTR;

    /**
     * Allow unknown protocols.
     */
    public boolean ALLOW_UNKNOWN_PROTOCOLS;

    /**
     * Output should be safe for common template engines.
     */
    public boolean SAFE_FOR_TEMPLATES;

    /**
     * Return DOM instead of HTML string.
     */
    public boolean RETURN_DOM;

    /**
     * Return DOM fragment instead of HTML string.
     */
    public boolean RETURN_DOM_FRAGMENT;

    /**
     * Return entire document instead of fragment.
     */
    public boolean WHOLE_DOCUMENT;

    /**
     * Keep content when removing element.
     */
    public boolean KEEP_CONTENT;

    /**
     * Perform sanitization in-place.
     */
    public boolean IN_PLACE;

    /**
     * Sanitize DOM to prevent clobbering attacks.
     */
    public boolean SANITIZE_DOM;

    @JsOverlay
    public final DOMPurifyConfig allowedTags(JsArrayLike<String> allowedTags) {
        this.ALLOWED_TAGS = allowedTags;
        return this;
    }

    @JsOverlay
    public final DOMPurifyConfig allowedAttr(JsArrayLike<String> allowedAttr) {
        this.ALLOWED_ATTR = allowedAttr;
        return this;
    }

    @JsOverlay
    public final DOMPurifyConfig forbidTags(JsArrayLike<String> forbidTags) {
        this.FORBID_TAGS = forbidTags;
        return this;
    }

    @JsOverlay
    public final DOMPurifyConfig forbidAttr(JsArrayLike<String> forbidAttr) {
        this.FORBID_ATTR = forbidAttr;
        return this;
    }

    @JsOverlay
    public final DOMPurifyConfig allowAriaAttr(boolean allowAriaAttr) {
        this.ALLOW_ARIA_ATTR = allowAriaAttr;
        return this;
    }

    @JsOverlay
    public final DOMPurifyConfig allowDataAttr(boolean allowDataAttr) {
        this.ALLOW_DATA_ATTR = allowDataAttr;
        return this;
    }

    @JsOverlay
    public final DOMPurifyConfig allowUnknownProtocols(boolean allowUnknownProtocols) {
        this.ALLOW_UNKNOWN_PROTOCOLS = allowUnknownProtocols;
        return this;
    }

    @JsOverlay
    public final DOMPurifyConfig safeForTemplates(boolean safeForTemplates) {
        this.SAFE_FOR_TEMPLATES = safeForTemplates;
        return this;
    }

    @JsOverlay
    public final DOMPurifyConfig returnDom(boolean returnDom) {
        this.RETURN_DOM = returnDom;
        return this;
    }

    @JsOverlay
    public final DOMPurifyConfig returnDomFragment(boolean returnDomFragment) {
        this.RETURN_DOM_FRAGMENT = returnDomFragment;
        return this;
    }

    @JsOverlay
    public final DOMPurifyConfig wholeDocument(boolean wholeDocument) {
        this.WHOLE_DOCUMENT = wholeDocument;
        return this;
    }

    @JsOverlay
    public final DOMPurifyConfig keepContent(boolean keepContent) {
        this.KEEP_CONTENT = keepContent;
        return this;
    }

    @JsOverlay
    public final DOMPurifyConfig inPlace(boolean inPlace) {
        this.IN_PLACE = inPlace;
        return this;
    }

    @JsOverlay
    public final DOMPurifyConfig sanitizeDom(boolean sanitizeDom) {
        this.SANITIZE_DOM = sanitizeDom;
        return this;
    }
}

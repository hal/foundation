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
package org.jboss.hal.env;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

/**
 * JsInterop binding for the <a href="https://github.com/js-cookie/js-cookie">js-cookie</a> library. Provides typed access to
 * browser cookies from Java code compiled with J2CL.
 */
@JsType(isNative = true, namespace = GLOBAL, name = "Cookies")
class Cookies {

    /** Returns the value of the cookie with the given name, or {@code null} if not set. */
    static native String get(String name);

    /** Sets a session cookie (cleared when the browser closes). */
    static native void set(String name, String value);

    /** Sets a persistent cookie that expires after the given number of days. */
    @JsOverlay
    static void set(String name, String value, int expires) {
        CookieOptions options = new CookieOptions();
        options.expires = expires;
        set(name, value, options);
    }

    /** Sets a cookie with the given options. */
    static native void set(String name, String value, CookieOptions options);

    /** Removes the cookie with the given name. */
    static native void remove(String name);

    /** Native JavaScript object passed to {@code Cookies.set()} to configure cookie attributes. */
    @JsType(isNative = true, namespace = GLOBAL, name = "Object")
    static class CookieOptions {

        /** Number of days until the cookie expires. */
        int expires;
    }
}

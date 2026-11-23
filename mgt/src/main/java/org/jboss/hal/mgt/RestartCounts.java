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
package org.jboss.hal.mgt;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/** Facet counts for the {@code restart} facet: all-services, no-services, resource-services, and jvm. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class RestartCounts {

    public int jvm;

    // hyphenated names cannot use @JsProperty — J2CL rejects non-identifier names
    @JsOverlay
    public final int noServices() {
        JsPropertyMap<Object> map = Js.asPropertyMap(this);
        return map.has("no-services") ? map.getAsAny("no-services").asInt() : 0;
    }

    @JsOverlay
    public final int allServices() {
        JsPropertyMap<Object> map = Js.asPropertyMap(this);
        return map.has("all-services") ? map.getAsAny("all-services").asInt() : 0;
    }

    @JsOverlay
    public final int resourceServices() {
        JsPropertyMap<Object> map = Js.asPropertyMap(this);
        return map.has("resource-services") ? map.getAsAny("resource-services").asInt() : 0;
    }
}

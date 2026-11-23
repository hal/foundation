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

/** Facet counts for the {@code dataType} facet. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class DataTypeCounts {

    public int string;
    public int object;
    public int map;
    public int bytes;
    public int property;

    // @JsOverlay is required for all names that are not common JS identifiers — see package-info.java
    @JsOverlay
    public final int boolean_() {
        JsPropertyMap<Object> map = Js.asPropertyMap(this);
        return map.has("boolean") ? map.getAsAny("boolean").asInt() : 0;
    }

    @JsOverlay
    public final int int_() {
        JsPropertyMap<Object> map = Js.asPropertyMap(this);
        return map.has("int") ? map.getAsAny("int").asInt() : 0;
    }

    @JsOverlay
    public final int long_() {
        JsPropertyMap<Object> map = Js.asPropertyMap(this);
        return map.has("long") ? map.getAsAny("long").asInt() : 0;
    }

    @JsOverlay
    public final int double_() {
        JsPropertyMap<Object> map = Js.asPropertyMap(this);
        return map.has("double") ? map.getAsAny("double").asInt() : 0;
    }

    @JsOverlay
    public final int bigDecimal() {
        JsPropertyMap<Object> map = Js.asPropertyMap(this);
        return map.has("big-decimal") ? map.getAsAny("big-decimal").asInt() : 0;
    }

    @JsOverlay
    public final int listSimple() {
        JsPropertyMap<Object> map = Js.asPropertyMap(this);
        return map.has("list-simple") ? map.getAsAny("list-simple").asInt() : 0;
    }

    @JsOverlay
    public final int listObject() {
        JsPropertyMap<Object> map = Js.asPropertyMap(this);
        return map.has("list-object") ? map.getAsAny("list-object").asInt() : 0;
    }
}

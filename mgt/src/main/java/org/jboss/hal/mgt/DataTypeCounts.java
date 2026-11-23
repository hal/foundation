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
        return Js.asPropertyMap(this).getAsAny("boolean").asInt();
    }

    @JsOverlay
    public final int int_() {
        return Js.asPropertyMap(this).getAsAny("int").asInt();
    }

    @JsOverlay
    public final int long_() {
        return Js.asPropertyMap(this).getAsAny("long").asInt();
    }

    @JsOverlay
    public final int double_() {
        return Js.asPropertyMap(this).getAsAny("double").asInt();
    }

    @JsOverlay
    public final int bigDecimal() {
        return Js.asPropertyMap(this).getAsAny("big-decimal").asInt();
    }

    @JsOverlay
    public final int listSimple() {
        return Js.asPropertyMap(this).getAsAny("list-simple").asInt();
    }

    @JsOverlay
    public final int listObject() {
        return Js.asPropertyMap(this).getAsAny("list-object").asInt();
    }
}

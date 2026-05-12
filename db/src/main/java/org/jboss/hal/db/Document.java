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
package org.jboss.hal.db;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import static jsinterop.annotations.JsPackage.GLOBAL;

/** JsInterop interface representing a PouchDB document backed by a native JavaScript object. */
@JsType(isNative = true, namespace = GLOBAL, name = "Object")
public interface Document extends JsPropertyMap<Object> {

    /** Creates a new document with the given {@code _id} property. */
    @JsOverlay
    static Document of(String id) {
        Document document = Js.cast(JsPropertyMap.of());
        document.set("_id", id);
        return document;
    }

    /** Returns the document's {@code _id} property. */
    @JsOverlay
    default String id() {
        return getAsAny("_id").asString();
    }
}

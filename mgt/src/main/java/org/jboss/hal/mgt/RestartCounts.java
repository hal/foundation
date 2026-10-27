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

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/** Facet counts for the {@code restart} facet: all-services, no-services, resource-services, and jvm. */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class RestartCounts {

    @JsProperty(name = "all-services") public int allServices;
    @JsProperty(name = "no-services") public int noServices;
    @JsProperty(name = "resource-services") public int resourceServices;
    public int jvm;
}

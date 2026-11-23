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

/**
 * Model Graph Tools (MGT) integration. Contains DTOs, REST clients, and events for interacting with the MGT Neo4j-backed
 * service that indexes the WildFly management model.
 *
 * <h2>JsInterop Mapping Strategies</h2>
 * <p>
 * The DTO classes in this package use {@code @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")} to map
 * JSON objects returned by the MGT REST API. Two mapping strategies are used depending on the JS property name:
 *
 * <dl>
 * <dt>Plain field</dt>
 * <dd>When the JS property name is a valid Java identifier and is common enough to appear in Closure Compiler's built-in externs
 * (e.g. DOM/JS API types): {@code public int string;}. The Java field name must match the JSON key exactly. Property names like
 * {@code type}, {@code name}, {@code description}, {@code string}, {@code object}, {@code map} are safe because they appear in
 * built-in externs and are never renamed.</dd>
 *
 * <dt>{@code @JsOverlay} with {@code Js.asPropertyMap()}</dt>
 * <dd>When the JS property name is a Java reserved word ({@code boolean}, {@code int}, {@code long}, {@code double},
 * {@code default}, {@code true}, {@code false}), contains characters illegal in JS identifiers (hyphens), or when the set of keys
 * is open-ended. The overlay method uses bracket access ({@code Js.asPropertyMap(this).getAsAny("boolean")}), which Closure
 * Compiler never renames regardless of type information.</dd>
 * </dl>
 *
 * <h2>Why not {@code @JsProperty(name = "...")}?</h2>
 * <p>
 * {@code @JsProperty(name = "...")} generates dot-access in the transpiled JS (e.g. {@code obj.boolean}). Because these types use
 * {@code name = "Object"}, J2CL emits all variables as {@code /** Object * /} in the generated JS. Closure Compiler's
 * type-based property disambiguation then fails to connect dot-access on a generic {@code Object} to the custom type declarations
 * in the externs file. Only properties that happen to exist in Closure's built-in externs (common DOM/JS API names) survive;
 * uncommon names like {@code int}, {@code boolean}, {@code true}, {@code false} are renamed away. Additionally, J2CL rejects
 * {@code @JsProperty} names containing hyphens since they are not valid JS identifiers.
 *
 * <h2>Externs</h2>
 * <p>
 * All JS property names are declared in the Closure Compiler externs file at {@code META-INF/externs/mgt.externs.js} using
 * {@code @record} (structural typing) rather than {@code @constructor}, since these types represent plain JSON objects from
 * {@code fetch().json()}, not class instances. The externs protect plain-field properties; {@code @JsOverlay} bracket access is
 * inherently safe and does not depend on externs.
 */
package org.jboss.hal.mgt;

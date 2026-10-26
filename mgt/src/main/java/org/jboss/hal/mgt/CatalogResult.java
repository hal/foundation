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

/**
 * JsInterop type for a single MGT catalog result. Mirrors {@code org.wildfly.modelgraph.api.CatalogResult}.
 *
 * <p>The {@link #type} field is one of {@code "resource"}, {@code "attribute"}, {@code "operation"}, or
 * {@code "capability"}. The {@link #properties} field contains type-specific properties (may be {@code null} for
 * capabilities).
 *
 * @see <a href="https://github.com/model-graph-tools">Model Graph Tools</a>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class CatalogResult {

    /** The result type: {@code "resource"}, {@code "attribute"}, {@code "operation"}, or {@code "capability"}. */
    public String type;

    /** The name of the resource, attribute, operation, or capability. */
    public String name;

    /** A human-readable description (may be {@code null}). */
    public String description;

    /** The full management model address (may be {@code null} for some capabilities). */
    public String address;

    /** The parent resource address without the attribute/operation name (may be {@code null}). */
    public String resourceAddress;

    /** The stability level (may be {@code null}). */
    public String stability;

    /** Whether this element is deprecated. */
    public boolean deprecated;

    /** Type-specific properties (may be {@code null} for capabilities). */
    public CatalogProperties properties;

    @JsOverlay
    public final ModelGraphType type() {
        return ModelGraphType.of(type);
    }
}

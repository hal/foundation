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
 * JsInterop type for a single MGT search result. Mirrors {@code org.wildfly.modelgraph.api.SearchResult}.
 *
 * <p>The {@link #type} field is one of {@code "resource"}, {@code "attribute"}, {@code "operation"}, or
 * {@code "capability"}. The {@link #address} field contains the management model address where the item is defined (may be
 * {@code null} for some capabilities). The {@link #providedBy} array lists resources that declare a capability.
 *
 * @see <a href="https://github.com/model-graph-tools">Model Graph Tools</a>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SearchResult {

    /** The result type: {@code "resource"}, {@code "attribute"}, {@code "operation"}, or {@code "capability"}. */
    public String type;

    /** The name of the resource, attribute, operation, or capability. */
    public String name;

    /** A human-readable description (may be {@code null}). */
    public String description;

    /** The management model address where this item is defined (may be {@code null} for some capabilities). */
    public String address;

    /** Whether the resource (or the parent resource of an attribute/operation) is a singleton (may be {@code null}). */
    public Boolean singleton;

    /** Resources that declare this capability (only present for capabilities, may be {@code null}). */
    public ResourceRef[] providedBy;

    @JsOverlay
    public final ModelGraphType type() {
        return ModelGraphType.of(type);
    }
}

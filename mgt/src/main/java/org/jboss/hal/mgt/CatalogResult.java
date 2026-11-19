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

import org.jboss.elemento.Id;
import org.jboss.hal.env.Stability;
import org.jboss.hal.resources.Identifiers;

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

    /** A human-readable description. Present for resources, attributes, and operations; {@code null} for capabilities. */
    public String description;

    /** The management model address. For resources this is the full address; for attributes and operations it is the parent
     *  resource address. {@code null} for capabilities. */
    public String resourceAddress;

    /** The stability level. Always {@code null} for capabilities; might be {@code null} for other types. */
    public String stability;

    /** Whether this element is deprecated. */
    public boolean deprecated;

    /** Type-specific properties. Always present for all types. */
    public CatalogProperties properties;

    @JsOverlay
    public final String id() {
        return Id.build(type, name, Identifiers.fromTemplate(resourceAddress));
    }

    @JsOverlay
    public final ModelGraphType type() {
        return ModelGraphType.of(type);
    }

    @JsOverlay
    public final Stability stability(Stability defaultStability) {
        return Stability.parse(stability, defaultStability);
    }
}

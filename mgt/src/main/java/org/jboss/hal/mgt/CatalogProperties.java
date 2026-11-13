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
 * JsInterop type for the type-specific properties of a catalog result. The JSON arrives as a flat object with all possible
 * fields; which fields are present depends on the result type. {@code @JsOverlay} methods provide null-safe access to boolean
 * fields.
 *
 * <p>Resource fields: {@link #singleton}, {@link #capabilities}, {@link #definesAttrGroups}.
 *
 * <p>Attribute fields: {@link #dataType}, {@link #accessType}, {@link #storage}, {@link #restart}, {@link #required},
 * {@link #nillable}, {@link #expressions}, {@link #sensitive}, {@link #unit}, {@link #capabilityReference}.
 *
 * <p>Operation fields: {@link #readOnly}, {@link #runtimeOnly}, {@link #global}, {@link #hasParams},
 * {@link #hasReturnValue}.
 *
 * @see <a href="https://github.com/model-graph-tools">Model Graph Tools</a>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class CatalogProperties {

    // ------------------------------------------------------ resource properties

    /** Whether the resource defines attribute groups (may be {@code null}). */
    public Boolean definesAttrGroups;

    /** Whether the resource is a singleton (may be {@code null}). */
    public Boolean singleton;

    /** Names of capabilities declared by this resource (empty array if none, {@code null} for non-resource types). */
    public String[] capabilities;

    // ------------------------------------------------------ attribute properties

    /** Whether expressions are allowed (may be {@code null}). */
    public Boolean expressions;

    /** Whether the attribute is nillable (may be {@code null}). */
    public Boolean nillable;

    /** Whether the attribute is required (may be {@code null}). */
    public Boolean required;

    /** Whether the attribute has an IS_SENSITIVE relationship (may be {@code null}). */
    public Boolean sensitive;

    /** The access type of the attribute (may be {@code null}). */
    public String accessType;

    /** The name of the referenced capability, or {@code null} if the attribute does not reference a capability. */
    public String capabilityReference;

    /** The data type of the attribute (may be {@code null}). */
    public String dataType;

    /** The restart policy for the attribute (may be {@code null}). */
    public String restart;

    /** The storage type of the attribute (may be {@code null}). */
    public String storage;

    /** The measurement unit of the attribute (may be {@code null}). */
    public String unit;

    // ------------------------------------------------------ operation properties

    /** Whether the operation is global (may be {@code null}). */
    public Boolean global;

    /** Whether the operation has parameters (may be {@code null}). */
    public Boolean hasParams;

    /** Whether the operation has a return value (may be {@code null}). */
    public Boolean hasReturnValue;

    /** Whether the operation is read-only (may be {@code null}). */
    public Boolean readOnly;

    /** Whether the operation is runtime-only (may be {@code null}). */
    public Boolean runtimeOnly;

    // ------------------------------------------------------ overlay accessors

    @JsOverlay
    public final boolean isRequired() {
        return required != null && required;
    }

    @JsOverlay
    public final boolean isNillable() {
        return nillable != null && nillable;
    }

    @JsOverlay
    public final boolean isExpressions() {
        return expressions != null && expressions;
    }

    @JsOverlay
    public final boolean isSensitive() {
        return sensitive != null && sensitive;
    }

    @JsOverlay
    public final boolean isReadOnly() {
        return readOnly != null && readOnly;
    }

    @JsOverlay
    public final boolean isRuntimeOnly() {
        return runtimeOnly != null && runtimeOnly;
    }

    @JsOverlay
    public final boolean isGlobal() {
        return global != null && global;
    }

    @JsOverlay
    public final boolean isHasParams() {
        return hasParams != null && hasParams;
    }

    @JsOverlay
    public final boolean isHasReturnValue() {
        return hasReturnValue != null && hasReturnValue;
    }

    @JsOverlay
    public final boolean isSingleton() {
        return singleton != null && singleton;
    }

    @JsOverlay
    public final boolean isDeclaresCap() {
        return capabilities != null && capabilities.length > 0;
    }
}

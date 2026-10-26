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
 * JsInterop type for the type-specific properties of a catalog result. The JSON arrives as a flat object with all
 * possible fields; which fields are present depends on the result type. {@code @JsOverlay} methods provide null-safe
 * access to boolean fields.
 *
 * <p>Attribute fields: {@link #dataType}, {@link #accessType}, {@link #storage}, {@link #restart}, {@link #required},
 * {@link #nillable}, {@link #expressions}, {@link #sensitive}, {@link #unit}.
 *
 * <p>Operation fields: {@link #readOnly}, {@link #runtimeOnly}, {@link #global}, {@link #hasParams},
 * {@link #hasReturnValue}.
 *
 * <p>Resource fields: {@link #singleton}, {@link #declaresCap}, {@link #definesAttrGroups}.
 *
 * @see <a href="https://github.com/model-graph-tools">Model Graph Tools</a>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class CatalogProperties {

    // ------------------------------------------------------ attribute properties

    public String dataType;
    public String accessType;
    public String storage;
    public String restart;
    public Boolean required;
    public Boolean nillable;
    public Boolean expressions;
    public Boolean sensitive;
    public String unit;

    // ------------------------------------------------------ operation properties

    public Boolean readOnly;
    public Boolean runtimeOnly;
    public Boolean global;
    public Boolean hasParams;
    public Boolean hasReturnValue;

    // ------------------------------------------------------ resource properties

    public Boolean singleton;
    public Boolean declaresCap;
    public Boolean definesAttrGroups;

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
        return declaresCap != null && declaresCap;
    }

    @JsOverlay
    public final boolean isDefinesAttrGroups() {
        return definesAttrGroups != null && definesAttrGroups;
    }
}

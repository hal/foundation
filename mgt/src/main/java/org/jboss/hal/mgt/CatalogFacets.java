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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * JsInterop type for the facet counts in a catalog API response. Each field corresponds to a facet and holds the count of
 * results per facet value. Fields are {@code null} when the API contextually omits them (e.g., attribute-specific facets are
 * omitted when {@code type=operation}).
 *
 * <p>The {@code unit} facet is open-ended (values depend on the WildFly model), so it is accessed via the
 * {@link #unitCounts()} overlay method instead of a typed field.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class CatalogFacets {

    // ------------------------------------------------------ general

    public TypeCounts type;
    public StabilityCounts stability;
    public BooleanCounts deprecated;

    // ------------------------------------------------------ resource

    public BooleanCounts singleton;
    public BooleanCounts declaresCap;
    public BooleanCounts definesAttrGroups;

    // ------------------------------------------------------ attribute

    public DataTypeCounts dataType;
    public AccessTypeCounts accessType;
    public StorageCounts storage;
    public RestartCounts restart;
    public BooleanCounts required;
    public BooleanCounts nillable;
    public BooleanCounts expressions;
    public BooleanCounts sensitive;
    public BooleanCounts hasAllowed;
    public BooleanCounts hasRelationships;
    public BooleanCounts refsCap;

    // ------------------------------------------------------ operation

    public BooleanCounts readOnly;
    public BooleanCounts runtimeOnly;
    public BooleanCounts global;
    public BooleanCounts hasParams;
    public BooleanCounts hasReturnValue;

    // ------------------------------------------------------ unit (dynamic)

    @JsOverlay
    public final Map<String, Integer> unitCounts() {
        JsPropertyMap<Object> self = Js.asPropertyMap(this);
        if (!self.has("unit")) {
            return Collections.emptyMap();
        }
        JsPropertyMap<Double> raw = Js.uncheckedCast(self.get("unit"));
        if (raw == null) {
            return Collections.emptyMap();
        }
        Map<String, Integer> result = new LinkedHashMap<>();
        raw.forEach(key -> result.put(key, (int) (double) raw.get(key)));
        return result;
    }
}

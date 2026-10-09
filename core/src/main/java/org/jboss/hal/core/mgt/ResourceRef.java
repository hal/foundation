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
package org.jboss.hal.core.mgt;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * JsInterop type for a resource reference in an MGT search result. Mirrors {@code org.wildfly.modelgraph.api.ResourceRef}.
 *
 * @see <a href="https://github.com/model-graph-tools">Model Graph Tools</a>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ResourceRef {

    /** The resource name. */
    public String name;

    /** The management model address of the resource. */
    public String address;
}

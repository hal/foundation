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
package org.jboss.hal.op.task;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jboss.hal.op.resources.Resources;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import static elemental2.core.Global.JSON;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class TaskDefinition {

    @JsOverlay
    public static List<TaskDefinition> taskDefinitions() {
        JsPropertyMap<TaskDefinition> taskDefinitions = Js.cast(JSON.parse(Resources.INSTANCE.tasks().getText()));
        SortedMap<String, TaskDefinition> result = new TreeMap<>();
        taskDefinitions.forEach(key -> result.put(key, taskDefinitions.get(key)));
        return new ArrayList<>(result.values());
    }

    public String title;
    public String icon;
    public String summary;
}

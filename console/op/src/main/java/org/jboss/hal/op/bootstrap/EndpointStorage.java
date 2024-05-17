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
package org.jboss.hal.op.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.hal.resources.Ids;

import elemental2.core.JsArray;
import elemental2.webstorage.Storage;
import elemental2.webstorage.WebStorageWindow;

import static elemental2.core.Global.JSON;
import static elemental2.dom.DomGlobal.window;
import static java.util.Comparator.comparing;

class EndpointStorage {

    private final Map<String, Endpoint> endpoints;
    private final Storage storage;

    EndpointStorage() {
        this.endpoints = new HashMap<>();
        this.storage = WebStorageWindow.of(window).localStorage;
        String payload = storage.getItem(Ids.ENDPOINTS);
        if (payload != null && !payload.isEmpty()) {
            //noinspection unchecked
            JsArray<Endpoint> eps = (JsArray<Endpoint>) JSON.parse(payload);
            for (int i = 0; i < eps.length; i++) {
                endpoints.put(eps.getAt(i).id, eps.getAt(i));
            }
        }
    }

    void add(Endpoint endpoint) {
        endpoints.put(endpoint.id, endpoint);
        save();
    }

    void remove(String id) {
        endpoints.remove(id);
        save();
    }

    Endpoint get(String id) {
        return endpoints.get(id);
    }

    Endpoint findByName(String name) {
        for (Endpoint endpoint : endpoints.values()) {
            if (endpoint.name.equals(name)) {
                return endpoint;
            }
        }
        return null;
    }

    boolean isEmpty() {
        return endpoints.isEmpty();
    }

    List<Endpoint> endpoints() {
        List<Endpoint> sorted = new ArrayList<>(endpoints.values());
        //noinspection Java8ListSort
        Collections.sort(sorted, comparing(endpoint -> endpoint.name));
        return sorted;
    }

    private void save() {
        JsArray<Endpoint> eps = new JsArray<>();
        endpoints.values().forEach(eps::push);
        storage.setItem(Ids.ENDPOINTS, JSON.stringify(eps));
    }
}

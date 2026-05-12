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
package org.jboss.hal.model.filter;

import java.util.List;

/**
 * Represents an access type filter value with a display label.
 */
public class AccessTypeValue {

    public static List<AccessTypeValue> accessTypeValues() {
        return List.of(new AccessTypeValue(AccessTypeAttribute.NAME, "Read-write", "read-write"),
                new AccessTypeValue(AccessTypeAttribute.NAME, "Read-only", "read-only"),
                new AccessTypeValue(AccessTypeAttribute.NAME, "Metric", "metric"));
    }

    public final String identifier;
    public final String text;
    public final String value;

    public AccessTypeValue(String filterAttribute, String text, String value) {
        this.identifier = filterAttribute + "-" + value;
        this.text = text;
        this.value = value;
    }
}

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

    /** Returns all predefined access type filter values. */
    public static List<AccessTypeValue> accessTypeValues() {
        return List.of(new AccessTypeValue(AccessTypeAttribute.NAME, "Read-write", "read-write"),
                new AccessTypeValue(AccessTypeAttribute.NAME, "Read-only", "read-only"),
                new AccessTypeValue(AccessTypeAttribute.NAME, "Metric", "metric"));
    }

    /** Unique identifier combining the filter attribute name and the value. */
    public final String identifier;

    /** Human-readable display text. */
    public final String text;

    /** DMR value used for matching. */
    public final String value;

    /** Creates a new access type value for the given filter attribute. */
    public AccessTypeValue(String filterAttribute, String text, String value) {
        this.identifier = filterAttribute + "-" + value;
        this.text = text;
        this.value = value;
    }
}

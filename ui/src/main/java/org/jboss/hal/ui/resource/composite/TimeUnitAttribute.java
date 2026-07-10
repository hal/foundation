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
package org.jboss.hal.ui.resource.composite;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.description.AttributeDescription;

import static org.jboss.hal.dmr.ModelDescriptionConstants.TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNIT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;

/**
 * {@link CompositeAttribute} implementation that matches keepalive-time attributes by their structure: an OBJECT with a
 * value-type containing {@code time} (LONG) and {@code unit} (STRING) sub-attributes.
 * <p>
 * This structural match covers the {@code keepalive-time} attribute used across thread pool resources in:
 * <ul>
 *     <li>{@code batch-jberet} — thread pools</li>
 *     <li>{@code ee} — managed executor/scheduled executor services</li>
 *     <li>{@code ejb3} — thread pools</li>
 *     <li>{@code infinispan} — cache container thread pools</li>
 *     <li>{@code jca} — workmanager thread pools</li>
 *     <li>{@code jgroups} — transport thread pools</li>
 * </ul>
 *
 * @see CompositeAttributes
 */
public class TimeUnitAttribute implements CompositeAttribute {

    /** Returns the time value from a keepalive-time model node, or -1 if undefined. */
    public static long time(ModelNode value) {
        if (value.isDefined() && value.hasDefined(TIME)) {
            return value.get(TIME).asLong();
        }
        return -1;
    }

    /** Returns the unit value from a keepalive-time model node, or {@code null} if undefined. */
    public static String unit(ModelNode value) {
        if (value.isDefined() && value.hasDefined(UNIT)) {
            return value.get(UNIT).asString();
        }
        return null;
    }

    @Override
    public boolean matches(AttributeDescription description) {
        try {
            ModelType type = description.get(TYPE).asType();
            if (type != ModelType.OBJECT) {
                return false;
            }
            if (!description.hasDefined(VALUE_TYPE)) {
                return false;
            }
            if (description.get(VALUE_TYPE).getType() != ModelType.OBJECT) {
                return false;
            }
            ModelNode valueType = description.get(VALUE_TYPE);
            return valueType.has(TIME) && valueType.has(UNIT);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

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
package org.jboss.hal.ui.resource.data;

import java.util.List;
import java.util.Map;

import org.jboss.hal.ui.resource.ResourceAttribute;

/**
 * Strategy for grouping resource attributes into named sections. Implementations produce a map from group name to the
 * attributes in that group, consumed by {@link ResourceData} to render expandable sections.
 */
@FunctionalInterface
public interface GroupingStrategy {

    /**
     * Groups the given attributes into named sections.
     *
     * @return a map from group name to the attributes in that group, in display order
     */
    Map<String, List<ResourceAttribute>> group(List<ResourceAttribute> attributes);
}

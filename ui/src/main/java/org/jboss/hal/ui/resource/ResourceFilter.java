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
package org.jboss.hal.ui.resource;

import org.jboss.hal.model.filter.AccessTypeAttribute;
import org.jboss.hal.model.filter.DefinedAttribute;
import org.jboss.hal.model.filter.DeprecatedAttribute;
import org.jboss.hal.model.filter.ExpressionAttribute;
import org.jboss.hal.model.filter.NameAttribute;
import org.jboss.hal.model.filter.RequiredAttribute;
import org.jboss.hal.model.filter.StorageAttribute;
import org.jboss.hal.model.filter.TypesAttribute;
import org.patternfly.filter.Filter;
import org.patternfly.filter.FilterOperator;

class ResourceFilter extends Filter<ResourceAttribute> {

    ResourceFilter() {
        super(FilterOperator.AND);
        add(new NameAttribute<>(ra -> ra.name));
        add(new TypesAttribute<>(ra -> ra.description));
        add(new DefinedAttribute<>(ra -> ra.value));
        add(new RequiredAttribute<>(ra -> ra.description));
        add(new DeprecatedAttribute<>(ra -> ra.description));
        add(new StorageAttribute<>(ra -> ra.description));
        add(new AccessTypeAttribute<>(ra -> ra.description));
        add(new ExpressionAttribute<>(ra -> ra.description));
    }
}

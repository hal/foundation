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
package org.jboss.hal.ui.modelbrowser;

import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.model.filter.AccessTypeAttribute;
import org.jboss.hal.model.filter.DeprecatedAttribute;
import org.jboss.hal.model.filter.ExpressionAttribute;
import org.jboss.hal.model.filter.NameAttribute;
import org.jboss.hal.model.filter.RequiredAttribute;
import org.jboss.hal.model.filter.StorageAttribute;
import org.jboss.hal.model.filter.TypesAttribute;
import org.patternfly.filter.Filter;
import org.patternfly.filter.FilterOperator;

public class AttributesFilter extends Filter<AttributeDescription> {

    public AttributesFilter() {
        super(FilterOperator.AND);
        add(new NameAttribute<>(NamedNode::name));
        add(new TypesAttribute<>(ad -> ad));
        add(new RequiredAttribute<>(ad -> ad));
        add(new DeprecatedAttribute<>(ad -> ad));
        add(new StorageAttribute<>(ad -> ad));
        add(new AccessTypeAttribute<>(ad -> ad));
        add(new ExpressionAttribute<>(ad -> ad));
    }
}

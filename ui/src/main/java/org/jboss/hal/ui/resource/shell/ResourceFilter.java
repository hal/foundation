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
package org.jboss.hal.ui.resource.shell;

import org.jboss.hal.model.filter.AccessTypeAttribute;
import org.jboss.hal.model.filter.DefinedAttribute;
import org.jboss.hal.model.filter.DeprecatedAttribute;
import org.jboss.hal.model.filter.ExpressionAttribute;
import org.jboss.hal.model.filter.NameAttribute;
import org.jboss.hal.model.filter.RequiredAttribute;
import org.jboss.hal.model.filter.StorageAttribute;
import org.jboss.hal.model.filter.TypesAttribute;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.patternfly.filter.Filter;
import org.patternfly.filter.FilterOperator;

/** Multi-criteria attribute filter supporting name search, type, status, storage, access type, and expression filtering. */
public class ResourceFilter extends Filter<ResolvedAttribute> {

    ResourceFilter() {
        super(FilterOperator.AND);
        add(new NameAttribute<>(ResolvedAttribute::name));
        add(new TypesAttribute<>(ResolvedAttribute::description));
        add(new DefinedAttribute<>(ResolvedAttribute::value));
        add(new RequiredAttribute<>(ResolvedAttribute::description));
        add(new DeprecatedAttribute<>(ResolvedAttribute::description));
        add(new StorageAttribute<>(ResolvedAttribute::description));
        add(new AccessTypeAttribute<>(ResolvedAttribute::description));
        add(new ExpressionAttribute<>(ResolvedAttribute::description));
    }
}

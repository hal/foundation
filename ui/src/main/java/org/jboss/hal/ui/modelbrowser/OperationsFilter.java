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
import org.jboss.hal.meta.description.OperationDescription;
import org.jboss.hal.model.filter.DeprecatedAttribute;
import org.jboss.hal.model.filter.GlobalOperationsAttribute;
import org.jboss.hal.model.filter.NameAttribute;
import org.jboss.hal.model.filter.ParametersAttribute;
import org.jboss.hal.model.filter.ReturnValueAttribute;
import org.patternfly.filter.Filter;
import org.patternfly.filter.FilterOperator;

public class OperationsFilter extends Filter<OperationDescription> {

    private final boolean showGlobalOperations;

    public OperationsFilter(boolean showGlobalOperations) {
        super(FilterOperator.AND);
        this.showGlobalOperations = showGlobalOperations;
        add(new NameAttribute<>(NamedNode::name));
        add(new ParametersAttribute<>());
        add(new ReturnValueAttribute<>());
        add(new DeprecatedAttribute<>(od -> od));
        add(new GlobalOperationsAttribute<>());
    }

    @Override
    public void resetAll(String origin) {
        super.resetAll(origin);
        // respect user setting!
        set(GlobalOperationsAttribute.NAME, showGlobalOperations);
    }
}

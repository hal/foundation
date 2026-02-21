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
package org.jboss.hal.op.task.statistics;

import java.util.List;
import java.util.Objects;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.jboss.hal.dmr.Expression.extractExpressions;
import static org.jboss.hal.dmr.ModelType.EXPRESSION;

class ResourceData {

    final AddressTemplate template;
    final ModelNode value;
    boolean expressionsAllowed;

    ResourceData(AddressTemplate template, ModelNode value) {
        this.template = template;
        this.value = value;
        this.expressionsAllowed = false;
    }

    private ResourceData(AddressTemplate template, ModelNode value, boolean expressionsAllowed) {
        this.template = template;
        this.value = value;
        this.expressionsAllowed = expressionsAllowed;
    }

    ResourceData copy(ModelNode value) {
        return new ResourceData(template, value, expressionsAllowed);
    }

    boolean isExpression() {
        return value.isDefined() && value.getType() == EXPRESSION;
    }

    List<String> expressions() {
        if (isExpression()) {
            String[] expressions = extractExpressions(value.asString());
            if (expressions != null) {
                return asList(expressions);
            }
        }
        return emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {return false;}
        ResourceData that = (ResourceData) o;
        return Objects.equals(template, that.template);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(template);
    }
}

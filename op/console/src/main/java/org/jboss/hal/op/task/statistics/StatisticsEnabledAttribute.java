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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.patternfly.filter.FilterAttribute;

import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;

class StatisticsEnabledAttribute extends FilterAttribute<ModelNode, StatisticsEnabledValue> {

    static final String NAME = STATISTICS_ENABLED + "-attribute";

    StatisticsEnabledAttribute() {
        super(NAME, (modelNode, sea) -> {
            ModelNode sen = modelNode.get(STATISTICS_ENABLED);
            return switch (sea) {
                case true_ -> sen.getType() == ModelType.BOOLEAN && sen.asBoolean();
                case false_ -> sen.getType() == ModelType.BOOLEAN && !sen.asBoolean();
                case expression -> sen.getType() == ModelType.EXPRESSION;
                case noExpression -> sen.getType() != ModelType.EXPRESSION;
            };
        });
    }
}

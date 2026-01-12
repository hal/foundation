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
package org.jboss.hal.op.task;

import java.util.EnumSet;
import java.util.Set;

import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.tree.ModelTree;
import org.jboss.hal.meta.tree.TraverseContinuation;
import org.jboss.hal.meta.tree.TraverseOperation;
import org.jboss.hal.meta.tree.TraverseType;

import elemental2.promise.Promise;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;

class FindStatisticsEnabledAttributes {

    private static final Logger logger = Logger.getLogger(FindStatisticsEnabledAttributes.class.getName());

    private final Dispatcher dispatcher;
    private final ModelTree modelTree;

    FindStatisticsEnabledAttributes(Dispatcher dispatcher, ModelTree modelTree) {
        this.dispatcher = dispatcher;
        this.modelTree = modelTree;
    }

    void find(AddressTemplate root) {
        TraverseContinuation continuation = new TraverseContinuation();
        TraverseOperation<Set<String>> readAttributes = (template, context) -> {
            if (template.fullyQualified()) {
                return dispatcher.execute(new Operation.Builder(template.resolve(context), READ_RESOURCE_OPERATION)
                                .param(ATTRIBUTES_ONLY, true)
                                .param(INCLUDE_RUNTIME, true)
                                .build())
                        .then(result -> {
                            Set<String> collect = result.asPropertyList().stream()
                                    .map(Property::getName)
                                    .collect(toSet());
                            return Promise.resolve(collect);
                        })
                        .catch_(error -> {
                            logger.error("Failed to read attributes of %s: %s", template, error);
                            return Promise.resolve(emptySet());
                        });
            } else {
                return Promise.resolve(emptySet());
            }
        };
        modelTree.traverse(continuation, root, singleton("/core-service"),
                        EnumSet.noneOf(TraverseType.class), readAttributes,
                        (template, attributes, context) -> {
                            if (template.fullyQualified() && attributes.contains(STATISTICS_ENABLED)) {
                                logger.info("Found statistics enabled attribute for %s", template);
                            }
                        })
                .then(context -> {
                    logger.info("Traversal completed for %s", root);
                    return null;
                });
    }
}

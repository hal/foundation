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
package org.jboss.hal.ui.resource.form;

import java.util.Collections;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;

import static java.util.Collections.singletonList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

/**
 * Strategy for producing DMR operations from a form item's current state. Injected into {@link StandardFormItem} at
 * construction time. Composite form items like {@link PathRelativeToFormItem} use the {@link #writeOrUndefine} utility
 * directly.
 * <p>
 * Most items use the default {@link #WRITE_ATTRIBUTE} strategy, which produces a single {@code write-attribute} or
 * {@code undefine-attribute} operation. Custom implementations like {@link MapOperationStrategy} produce granular operations
 * (e.g. {@code map-put}/{@code map-remove}) for attribute types that require per-entry operations.
 *
 * @see StandardFormItem
 * @see PathRelativeToFormItem
 * @see MapOperationStrategy
 */
@FunctionalInterface
public interface OperationStrategy {

    /** Standard strategy: single {@code write-attribute} or {@code undefine-attribute} operation. */
    OperationStrategy WRITE_ATTRIBUTE = (item, address) -> {
        if (!item.isModified()) {
            return Collections.emptyList();
        }
        return singletonList(writeOrUndefine(address, item.attribute().fqn(), item.modelNode()));
    };

    /** Builds a {@code write-attribute} or {@code undefine-attribute} operation depending on whether the value is defined. */
    static Operation writeOrUndefine(ResourceAddress address, String name, ModelNode value) {
        if (value.isDefined()) {
            return new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                    .param(NAME, name)
                    .param(VALUE, value)
                    .build();
        } else {
            return new Operation.Builder(address, UNDEFINE_ATTRIBUTE_OPERATION)
                    .param(NAME, name)
                    .build();
        }
    }

    /** Produces the DMR operations needed to persist the form item's current value. */
    List<Operation> operations(FormItem item, ResourceAddress address);
}

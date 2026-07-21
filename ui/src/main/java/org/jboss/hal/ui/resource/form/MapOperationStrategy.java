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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.patternfly.component.textinputgroup.FilterInput;

import static org.jboss.hal.dmr.ModelDescriptionConstants.KEY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAP_PUT_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAP_REMOVE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;

/**
 * {@link OperationStrategy} that produces granular {@code map-put} and {@code map-remove} DMR operations for individual
 * changed/removed entries in a map attribute.
 */
public final class MapOperationStrategy implements OperationStrategy {

    public static final MapOperationStrategy INSTANCE = new MapOperationStrategy();

    private MapOperationStrategy() {
    }

    @Override
    public List<Operation> operations(FormItem item, ResourceAddress address) {
        if (!item.isModified()) {
            return Collections.emptyList();
        }
        EditableControl<?> ec = item.editableControl();
        if (ec == null || !(ec.nativeControl() instanceof MapControl)) {
            return Collections.emptyList();
        }
        MapControl mapControl = (MapControl) ec.nativeControl();
        @SuppressWarnings("unchecked")
        FilterInput control = ((EditableControl<FilterInput>) ec).control();
        Map<String, String> original = mapControl.originalEntries();
        Map<String, String> current = mapControl.currentEntries(control);
        String attributeName = item.attribute().name();
        List<Operation> operations = new ArrayList<>();

        for (String key : original.keySet()) {
            if (!current.containsKey(key)) {
                operations.add(new Operation.Builder(address, MAP_REMOVE_OPERATION)
                        .param(NAME, attributeName)
                        .param(KEY, key)
                        .build());
            }
        }

        for (Map.Entry<String, String> entry : current.entrySet()) {
            String originalValue = original.get(entry.getKey());
            if (originalValue == null || !originalValue.equals(entry.getValue())) {
                operations.add(new Operation.Builder(address, MAP_PUT_OPERATION)
                        .param(NAME, attributeName)
                        .param(KEY, entry.getKey())
                        .param(VALUE, entry.getValue())
                        .build());
            }
        }

        return operations;
    }
}

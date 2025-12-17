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
package org.jboss.hal.meta;

import java.util.ArrayList;
import java.util.List;

import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Settings;

import elemental2.promise.Promise;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.flow.Flow.sequential;
import static org.jboss.elemento.logger.Level.DEBUG;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ACCESS_CONTROL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.COMBINED_DESCRIPTIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LOCALE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_DESCRIPTION_OPERATION;
import static org.jboss.hal.meta.RrdParser.parseComposite;
import static org.jboss.hal.meta.RrdParser.parseSingle;

/** Creates, executes and parses the {@code read-resource-description} operations to read metadata. */
class RrdTask implements Task<ProcessingContext> {

    private static final Logger logger = Logger.getLogger(RrdTask.class.getName());
    private static final int BATCH_SIZE = 3;

    private final Dispatcher dispatcher;
    private final Settings settings;

    RrdTask(Settings settings, Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.settings = settings;
    }

    @Override
    public Promise<ProcessingContext> apply(ProcessingContext context) {
        List<Task<ProcessingContext>> tasks = new ArrayList<>();

        // create and partition operations
        List<Operation> operations = createRrd(context);
        if (operations.size() == 1) {
            Operation operation = operations.get(0);
            logger.debug("About to execute one rrd operation: %s", operation.asCli());
            tasks.add((ProcessingContext pc) -> dispatcher.execute(operation).then(result -> {
                parseSingle(operation.getAddress(), result, context.rrdResult);
                return Promise.resolve(pc);
            }));

        } else if (operations.size() <= BATCH_SIZE) {
            Composite composite = new Composite(operations);
            logger.debug("About to execute one composite rrd operation: %s", composite.asCli());
            tasks.add((ProcessingContext pc) -> dispatcher.execute(composite).then(result -> {
                parseComposite(composite, result, context.rrdResult);
                return Promise.resolve(pc);
            }));

        } else {
            List<List<Operation>> piles = partition(operations);
            List<Composite> composites = piles.stream().map(Composite::new).collect(toList());
            if (logger.isEnabled(DEBUG)) {
                String ops = composites.stream().map(Composite::asCli).collect(joining(", "));
                logger.debug("About to execute %d composite rrd operations: %s", composites.size(), ops);
            }
            for (Composite composite : composites) {
                tasks.add((ProcessingContext pc) -> dispatcher.execute(composite).then(result -> {
                    parseComposite(composite, result, context.rrdResult);
                    return Promise.resolve(pc);
                }));
            }
        }

        if (!tasks.isEmpty()) {
            return sequential(context, tasks).promise();
        } else {
            logger.debug("No rrd operations necessary");
            return Promise.resolve(context);
        }
    }

    private List<Operation> createRrd(ProcessingContext context) {
        List<Operation> operations = new ArrayList<>();
        String locale = settings.get(Settings.Key.LOCALE).value();
        for (String address : context.addresses) {
            ResourceAddress resourceAddress = AddressTemplate.of(address).resolve(); // to get the encoding right
            operations.add(new Operation.Builder(resourceAddress, READ_RESOURCE_DESCRIPTION_OPERATION)
                    .param(OPERATIONS, true)
                    .param(ACCESS_CONTROL, COMBINED_DESCRIPTIONS)
                    .param(LOCALE, locale)
                    .build());
        }
        return operations;
    }

    private List<List<Operation>> partition(List<Operation> operations) {
        List<List<Operation>> piles = new ArrayList<>();
        for (int i = 0; i < operations.size(); i += BATCH_SIZE) {
            piles.add(new ArrayList<>(operations.subList(i, Math.min(i + BATCH_SIZE, operations.size()))));
        }
        return piles;
    }
}

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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;

import elemental2.promise.Promise;

public class ExecuteOperationDialog {

    // ------------------------------------------------------ factory

    public static ExecuteOperationDialog executeOperation(AddressTemplate template, String operation) {
        return new ExecuteOperationDialog(template, operation);
    }

    // ------------------------------------------------------ instance

    private final AddressTemplate template;
    private final String operation;

    ExecuteOperationDialog(AddressTemplate template, String operation) {
        this.template = template;
        this.operation = operation;
    }

    // ------------------------------------------------------ api

    public Promise<ModelNode> execute() {
        return new Promise<ModelNode>((resolve, reject) -> {

        });
    }
}

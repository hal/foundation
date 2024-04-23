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
package org.jboss.hal.dmr.dispatch;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;

import elemental2.promise.Promise;

@ApplicationScoped
public class Dispatcher {

    public Promise<ModelNode> execute(Operation operation) {
        return Promise.resolve(new ModelNode());
    }
}

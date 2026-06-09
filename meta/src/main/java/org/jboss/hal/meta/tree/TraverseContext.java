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
package org.jboss.hal.meta.tree;

import java.util.HashMap;
import java.util.Map;

import org.jboss.hal.dmr.Operation;

/**
 * Class representing the context used during the traversal of a management model tree. This context keeps track of resources
 * processed, accepted and failed during traversal.
 */
public class TraverseContext {

    private int processed;
    private int accepted;
    private Map<String, Operation> failed;

    public TraverseContext() {
        processed = 0;
        accepted = 0;
        failed = new HashMap<>();
    }

    /** @return the total number of child resources discovered during traversal */
    public int processed() {
        return processed;
    }

    /** @return a map of resource addresses to their failed operations */
    public Map<String, Operation> failed() {
        return failed;
    }

    /** @return the number of resources accepted by the traverse operation */
    public int accepted() {
        return accepted;
    }

    // ------------------------------------------------------ internal

    void recordProgress(int size) {
        processed += size;
    }

    void recordAccepted() {
        accepted++;
    }

    void recordFailed(String address, Operation operation) {
        failed.put(address, operation);
    }
}

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

/**
 * Class representing the control mechanism for managing the traversal process. The {@code TraverseContinuation} object can be
 * used to control the traversal by stopping the traversal operation.
 */
public class TraverseContinuation {

    boolean running;

    public TraverseContinuation() {
        running = false;
    }

    /** Signals the traversal to stop at the next opportunity. */
    public void stop() {
        running = false;
    }

    /** @return whether the traversal is currently running */
    public boolean running() {
        return running;
    }
}

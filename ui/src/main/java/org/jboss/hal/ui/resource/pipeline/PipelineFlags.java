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
package org.jboss.hal.ui.resource.pipeline;

/**
 * Configuration flags for the pipeline, controlling scope (new vs. existing resource) and placeholder behavior. Used by both
 * view and form item creation.
 */
public record PipelineFlags(Scope scope, Placeholder placeholder) {

    /** Whether the pipeline is operating on a new resource or an existing one. */
    public enum Scope {
        /** Creating a new resource (add operation). */
        NEW_RESOURCE,
        /** Editing an already persisted resource. */
        EXISTING_RESOURCE
    }

    /** Controls what placeholder text appears in inputs when no value is set. */
    public enum Placeholder {
        /** No placeholder text. */
        NONE,
        /** Show "undefined" as a placeholder. */
        UNDEFINED,
        /** Show the attribute's default value as a placeholder. */
        DEFAULT_VALUE
    }
}

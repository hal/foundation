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

/** Configuration flags passed to {@link FormItem} instances, controlling scope (new vs. existing resource) and placeholder behavior. */
public class FormItemFlags {

    /** Whether the form item is editing a new resource or an existing one. */
    public enum Scope {
        /** The form is creating a new resource. */
        NEW_RESOURCE,
        /** The form is editing an already persisted resource. */
        EXISTING_RESOURCE
    }

    /** Controls what placeholder text appears in the input when no value is set. */
    public enum Placeholder {
        /** No placeholder text. */
        NONE,
        /** Show "undefined" as a placeholder. */
        UNDEFINED,
        /** Show the attribute's default value as a placeholder. */
        DEFAULT_VALUE
    }

    final Scope scope;
    final Placeholder placeholder;

    /** Creates new flags with the given scope and placeholder policy. */
    public FormItemFlags(Scope scope, Placeholder placeholder) {
        this.scope = scope;
        this.placeholder = placeholder;
    }
}

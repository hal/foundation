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
package org.jboss.hal.resources;

/**
 * Context constants for dynamic OUIA ID composition. Used as the first argument to {@link OuiaIds#ouia(String, String...)}.
 * <p>
 * Every context used in {@code OuiaIds.ouia()} calls must be defined here — raw string literals should be avoided.
 * Additional contexts will be added as call sites are migrated from {@link Ids} to {@link OuiaIds}.
 *
 * @see OuiaIds
 * @see OuiaSuffixes
 */
public interface OuiaContexts {

    String ROOT = "root";
    String OPERATION = "operation";
}

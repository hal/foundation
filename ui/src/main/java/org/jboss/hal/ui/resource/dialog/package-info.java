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

/**
 * Modal dialogs for WildFly management resource CRUD operations.
 * <p>
 * Provides factory methods for creating, resetting, and removing management resources, as well as executing management
 * operations with parameter input. All dialogs return promises for asynchronous handling.
 * <p>
 * Key components:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.dialog.ResourceDialogs}</dt>
 * <dd>Factory methods for add, reset, remove, and execute operation modals/wizards.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.dialog.ExecuteOperationDialog}</dt>
 * <dd>Wrapper for operation execution dialogs.</dd>
 * </dl>
 */
package org.jboss.hal.ui.resource.dialog;

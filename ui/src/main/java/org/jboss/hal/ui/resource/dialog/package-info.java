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
 * Modal dialogs for WildFly management resource operations.
 * <p>
 * Provides dialogs for adding, deleting, and executing operations on management resources. All dialogs return promises for
 * asynchronous handling.
 * <p>
 * Key components:
 * <dl>
 * <dt>{@link org.jboss.hal.ui.resource.dialog.DialogBricks}</dt>
 * <dd>Brick class with static entry points for all resource dialogs (see {@linkplain org.jboss.hal.ui brick pattern}).</dd>
 * <dt>{@link org.jboss.hal.ui.resource.dialog.AddResourceDialogs}</dt>
 * <dd>Add resource wizard and modal dialogs.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.dialog.DeleteResourceDialog}</dt>
 * <dd>Delete confirmation dialog.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.dialog.ExecuteOperationDialogs}</dt>
 * <dd>Execute operation dialog with parameter inputs and result display.</dd>
 * <dt>{@link org.jboss.hal.ui.resource.dialog.ExecuteOperationDialog}</dt>
 * <dd>Builder-style wrapper for executing a management operation (stub, not yet wired up).</dd>
 * </dl>
 */
package org.jboss.hal.ui.resource.dialog;

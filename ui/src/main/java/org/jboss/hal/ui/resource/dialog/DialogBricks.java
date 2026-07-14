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
package org.jboss.hal.ui.resource.dialog;

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;

import elemental2.promise.Promise;

/**
 * Reusable UI fragments ("bricks") for resource management dialogs. Following the
 * {@linkplain org.jboss.hal.ui brick pattern}, this class provides static factory methods for opening add, delete, and
 * execute-operation dialogs. Each method delegates to a dedicated dialog class:
 * <ul>
 * <li>{@link AddResourceDialogs} — add wizard and add modal</li>
 * <li>{@link DeleteResourceDialog} — delete confirmation</li>
 * <li>{@link ExecuteOperationDialogs} — execute operation modal</li>
 * </ul>
 * All dialogs return promises that resolve to {@link ModelNode} values or undefined (empty {@code ModelNode}) if the operation
 * was canceled.
 */
public final class DialogBricks {

    private DialogBricks() {
    }

    // ------------------------------------------------------ add

    /**
     * Opens a multi-step wizard for adding a resource when multiple template locations are possible.
     *
     * @param templates the possible locations where the resource can be added
     * @param resource  the name of the resource to be added
     * @return a {@link Promise} that resolves to a {@link ModelNode} representing the added resource, or to an undefined
     * {@link ModelNode} if canceled
     */
    public static Promise<ModelNode> addResourceWizard(List<AddressTemplate> templates, String resource) {
        return AddResourceDialogs.addResourceWizard(templates, resource);
    }

    /**
     * Opens a modal dialog for adding a resource at a known location.
     *
     * @param template  the address location where the resource will be added
     * @param resource  the name of the resource to be added
     * @param singleton whether the resource is a singleton
     * @return a {@link Promise} that resolves to a {@link ModelNode} representing the added resource, or to an undefined
     * {@link ModelNode} if canceled or rejected
     */
    public static Promise<ModelNode> addResourceModal(AddressTemplate template, String resource, boolean singleton) {
        return AddResourceDialogs.addResourceModal(template, resource, singleton);
    }

    // ------------------------------------------------------ execute operation

    /**
     * Opens a modal dialog to execute a management operation on the resource identified by the template.
     *
     * @param template  the address of the resource
     * @param operation the name of the operation to execute
     */
    public static void executeOperationModal(AddressTemplate template, String operation) {
        ExecuteOperationDialogs.executeOperationModal(template, operation);
    }

    // ------------------------------------------------------ delete

    /**
     * Opens a confirmation dialog for deleting the resource identified by the template.
     *
     * @param template the address of the resource to delete
     * @return a {@link Promise} that resolves to a {@link ModelNode} with value {@code true} on success, or to an undefined
     * {@link ModelNode} if canceled
     */
    public static Promise<ModelNode> deleteResourceModal(AddressTemplate template) {
        return DeleteResourceDialog.deleteResourceModal(template);
    }
}

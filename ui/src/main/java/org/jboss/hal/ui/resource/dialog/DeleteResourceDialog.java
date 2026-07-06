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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContextResolver;
import org.jboss.hal.resources.OuiaIds;

import elemental2.promise.Promise;

import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.core.Humanize.capitalCase;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.modal.Modal.modal;
import static org.patternfly.component.modal.ModalBody.modalBody;
import static org.patternfly.component.modal.ModalFooter.modalFooter;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Size.sm;

/**
 * Confirmation dialog for deleting a management resource.
 * <p>
 * Opens a small modal asking the user to confirm deletion of the resource identified by the given address template. Resolves
 * the promise to a {@link ModelNode} with value {@code true} on success, or to an undefined {@link ModelNode} if canceled.
 */
class DeleteResourceDialog {

    static Promise<ModelNode> deleteResourceModal(AddressTemplate template) {
        AddressTemplate resolvedTemplate = new StatementContextResolver(uic().statementContext()).resolve(template);
        String name = capitalCase(resolvedTemplate.last().value);
        return new Promise<>((resolve, reject) -> modal().size(sm)
                .ouiaId(OuiaIds.DELETE_MODAL)
                .addHeader("Delete resource")
                .addBody(modalBody()
                        .add("Do you really want to delete ")
                        .add(span().css(util("font-weight-bold")).text(name))
                        .add("?"))
                .addFooter(modalFooter()
                        .addButton(button("Delete").primary()
                                .ouiaId(OuiaIds.DELETE_BTN), (__, m) -> uic().crud().delete(resolvedTemplate)
                                .then(result -> {
                                    m.close();
                                    ModelNode success = new ModelNode();
                                    success.set(true);
                                    resolve.onInvoke(success);
                                    return null;
                                })
                                .catch_(error -> {
                                    m.close();
                                    reject.onInvoke(error);
                                    return null;
                                }))
                        .addButton(button("Cancel").link()
                                .ouiaId(OuiaIds.CANCEL_BTN), (__, m) -> {
                            m.close();
                            resolve.onInvoke(new ModelNode());
                        }))
                .appendToBody()
                .open());
    }
}

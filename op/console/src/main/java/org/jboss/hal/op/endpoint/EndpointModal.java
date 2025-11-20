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
package org.jboss.hal.op.endpoint;

import java.util.List;

import org.jboss.elemento.logger.Logger;
import org.patternfly.component.button.Button;
import org.patternfly.component.modal.Modal;

import elemental2.promise.Promise;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.RejectCallbackFn;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.ResolveCallbackFn;

import static java.util.Collections.emptyList;
import static org.jboss.elemento.Elements.isVisible;
import static org.jboss.elemento.Elements.setVisible;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.list.ActionList.actionList;
import static org.patternfly.component.list.ActionListGroup.actionListGroup;
import static org.patternfly.component.list.ActionListItem.actionListItem;
import static org.patternfly.component.modal.Modal.modal;
import static org.patternfly.component.modal.ModalBody.modalBody;
import static org.patternfly.component.modal.ModalFooter.modalFooter;
import static org.patternfly.component.modal.ModalHeader.modalHeader;
import static org.patternfly.component.modal.ModalHeaderDescription.modalHeaderDescription;
import static org.patternfly.style.Size.md;

public class EndpointModal {

    private static final Logger logger = Logger.getLogger(EndpointModal.class.getName());

    public static EndpointModal endpointModal(EndpointStorage storage, boolean closable) {
        return new EndpointModal(storage, closable);
    }

    private final EndpointStorage storage;
    private final Button add;
    private final Button ok;
    private final Button cancel;
    private final EndpointForm form;
    private final Modal modal;
    private final EndpointTable table;
    private final boolean closable;
    private Endpoint endpoint;
    private ResolveCallbackFn<Endpoint> resolve;
    private RejectCallbackFn reject;

    EndpointModal(EndpointStorage storage, boolean closable) {
        this.storage = storage;
        this.closable = closable;
        this.endpoint = null;

        add = button("Add")
                .secondary()
                .onClick((event, component) -> newEndpoint());
        ok = button("Connect")
                .primary()
                .disabled()
                .onClick((event, component) -> saveOrConnect());
        cancel = button("Cancel")
                .link()
                .onClick((event, component) -> cancel());

        form = new EndpointForm(storage);
        table = new EndpointTable(this::newEndpoint, this::edit, this::remove)
                .onSelect((e, tr, selected) -> {
                    ok.disabled(!selected);
                    if (selected) {
                        endpoint = storage.get(tr.identifier());
                    }
                });

        modal = modal()
                .size(md)
                .hideClose()
                .autoClose(false)
                .closeOnEsc(closable)
                .addHeader(modalHeader()
                        .addTitle("Connect to WildFly")
                        .addDescription(modalHeaderDescription()
                                .add(new EndpointDescription())))
                .addBody(modalBody()
                        .add(form)
                        .add(table))
                .addFooter(modalFooter()
                        .add(actionList()
                                .addItem(actionListGroup()
                                        .addItem(actionListItem().add(ok))
                                        .addItem(actionListItem().add(add))
                                        .addItem(actionListItem().add(cancel)))))
                .appendToBody();

        setVisible(form, false);
        setVisible(table, false);
    }

    public Promise<Endpoint> open() {
        return new Promise<>((resolve, reject) -> {
            this.resolve = resolve;
            this.reject = reject;
            showEndpoints();
            modal.open();
        });
    }

    // ------------------------------------------------------ internal

    private void showEndpoints() {
        if (storage.isEmpty()) {
            noEndpoints();
        } else {
            existingEndpoints(storage.endpoints());
        }
    }

    private void noEndpoints() {
        table.show(emptyList());
        ok.text("Connect").disabled(true);

        setVisible(form, false);
        setVisible(table, true);
        setVisible(add, false);
        setVisible(cancel, closable);
    }

    private void existingEndpoints(List<Endpoint> endpoints) {
        table.show(endpoints);
        ok.text("Connect").disabled(true); // will be enabled on endpoint selection

        setVisible(form, false);
        setVisible(table, true);
        setVisible(add, true);
        setVisible(cancel, closable);
    }

    void newEndpoint() {
        form.reset();
        form.show(null);
        ok.text("Add").disabled(false);

        setVisible(form, true);
        setVisible(table, false);
        setVisible(add, false);
        setVisible(cancel, true);
    }

    private void edit(Endpoint endpoint) {
        form.reset();
        form.show(endpoint);
        ok.text("Save").disabled(false);

        setVisible(form, true);
        setVisible(table, false);
        setVisible(add, false);
        setVisible(cancel, true);
    }

    private void saveOrConnect() {
        if (isVisible(form)) {
            if (form.isValid()) {
                Endpoint endpoint = form.endpoint();
                storage.add(endpoint);
                showEndpoints();
                table.select(endpoint.id);
            }
        } else {
            if (endpoint != null) {
                modal.close();
                resolve.onInvoke(endpoint);
            } else {
                modal.close();
                String error = "Cannot resolve endpoint modal. No selected endpoint!";
                logger.error(error);
                reject.onInvoke(error);
            }
        }
    }

    private void remove(Endpoint endpoint) {
        storage.remove(endpoint.id);
        showEndpoints();
    }

    private void cancel() {
        if (isVisible(form)) {
            showEndpoints();
        } else if (closable) {
            modal.close();
        }
    }
}

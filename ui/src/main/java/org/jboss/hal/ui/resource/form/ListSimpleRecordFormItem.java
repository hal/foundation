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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.AttributeDescriptions;
import org.jboss.hal.ui.resource.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.Pipeline;
import org.patternfly.style.Classes;

import elemental2.dom.HTMLElement;

import static java.util.Collections.singletonList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.hal.core.Humanize.sentenceCase;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.recordTable;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.ui.resource.form.OperationStrategy.writeOrUndefine;
import static org.jboss.hal.ui.resource.pipeline.ListSimpleRecordHandler.addCells;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.modal.Modal.modal;
import static org.patternfly.component.modal.ModalBody.modalBody;
import static org.patternfly.component.modal.ModalFooter.modalFooter;
import static org.patternfly.component.modal.ModalHeader.modalHeader;
import static org.patternfly.component.modal.ModalHeaderTitle.modalHeaderTitle;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.table.Tbody.tbody;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.Th.th;
import static org.patternfly.component.table.Thead.thead;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.icon.IconSets.rhUi.add;
import static org.patternfly.icon.IconSets.rhUi.close;
import static org.patternfly.icon.IconSets.rhUi.edit;
import static org.patternfly.style.Classes.action;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.fitContent;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Size.lg;

/**
 * Custom form item for LIST attributes with simple-record value-type. Renders an editable compact table with per-row Edit and
 * Delete actions and an Add action in the header. Add/Edit open a modal with pipeline-produced typed form items for each
 * sub-attribute.
 */
public class ListSimpleRecordFormItem implements FormItem {

    private final String identifier;
    private final PipelineContext context;
    private final ResolvedAttribute attribute;
    private final List<AttributeDescription> columns;
    private final List<ModelNode> originalRecords;
    private final List<ModelNode> currentRecords;
    private final boolean writable;
    private final HTMLElement tableContainer;
    private final HTMLElement root;

    public ListSimpleRecordFormItem(PipelineContext context, String identifier, ResolvedAttribute attribute) {
        this.identifier = identifier;
        this.context = context;
        this.attribute = attribute;

        AttributeDescriptions descriptions = attribute.description().valueTypeAttributeDescriptions();
        this.columns = new ArrayList<>();
        for (AttributeDescription ad : descriptions) {
            columns.add(ad);
        }

        this.originalRecords = cloneList(attribute.value());
        this.currentRecords = cloneList(attribute.value());
        this.writable = attribute.writable() && !attribute.readOnly();
        this.tableContainer = div().element();
        renderTable();

        this.root = formGroup(identifier).css(halComponent(resource, recordTable))
                .addLabel(FormItemBricks.label(context, identifier, attribute.description()))
                .addControl(formGroupControl().add(tableContainer))
                .element();
    }

    // ------------------------------------------------------ form item

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public ResolvedAttribute attribute() {
        return attribute;
    }

    @Override
    public void contributeToPayload(ModelNode payload) {
        ModelNode list = new ModelNode();
        list.setEmptyList();
        for (ModelNode record : currentRecords) {
            list.add(record);
        }
        payload.get(attribute.fqn()).set(list);
    }

    @Override
    public boolean isModified() {
        return !originalRecords.toString().equals(currentRecords.toString());
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public void resetValidation() {
    }

    @Override
    public void disable() {
    }

    @Override
    public List<Operation> operations(ResourceAddress address) {
        if (!isModified()) {
            return Collections.emptyList();
        }
        ModelNode listValue;
        if (currentRecords.isEmpty() && attribute.description().nillable()) {
            listValue = new ModelNode();
        } else {
            listValue = new ModelNode();
            listValue.setEmptyList();
            for (ModelNode record : currentRecords) {
                listValue.add(record);
            }
        }
        return singletonList(writeOrUndefine(address, attribute.fqn(), listValue));
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ table rendering

    private void renderTable() {
        removeChildrenFrom(tableContainer);

        var headRow = tr(identifier + "-head");
        for (AttributeDescription col : columns) {
            headRow.add(th(col.name()).css(modifier(fitContent)).text(sentenceCase(col.name())));
        }
        if (writable) {
            headRow.add(th("actions").css(component(Classes.table, action))
                    .add(button().plain().icon(add()).onClick((e, b) -> openModal(-1, "Add"))));
        }

        var body = tbody();
        int index = 0;
        for (ModelNode record : currentRecords) {
            var row = tr(identifier + "-" + index);
            addCells(context, attribute.listEntry(record), columns, row);
            if (writable) {
                int rowIndex = index;
                row.add(td("actions").actions()
                        .add(button().plain().icon(edit())
                                .onClick((e, b) -> openModal(rowIndex, "Save")))
                        .add(button().plain().icon(close())
                                .onClick((e, b) -> {
                                    currentRecords.remove(rowIndex);
                                    renderTable();
                                })));
            }
            body.addRow(row);
            index++;
        }

        tableContainer.appendChild(table().compact().noBorders()
                .addHead(thead().addRow(headRow))
                .addBody(body)
                .element());
    }

    // ------------------------------------------------------ modal

    private void openModal(int index, String primaryAction) {
        boolean adding = index == -1;
        String title = adding
                ? "Add " + sentenceCase(attribute.name())
                : "Edit " + sentenceCase(attribute.name());

        ModelNode recordValue = adding ? new ModelNode() : currentRecords.get(index).clone();

        List<FormItem> childItems = new ArrayList<>();
        for (AttributeDescription col : columns) {
            ResolvedAttribute entry = attribute.listEntry(recordValue);
            ResolvedAttribute child = entry.child(col.name()).detachFromParent();
            FormItem childItem = Pipeline.instance().formItem(context, child);
            if (childItem != null) {
                childItems.add(childItem);
            }
        }

        ResourceForm resourceForm = new ResourceForm();
        for (FormItem item : childItems) {
            resourceForm.addItem(item);
        }

        modal().size(lg).top()
                .addHeader(modalHeader()
                        .addTitle(modalHeaderTitle().text(title)))
                .addBody(modalBody()
                        .add(div().css(halComponent(resource))
                                .add(resourceForm)))
                .addFooter(modalFooter()
                        .addButton(button(primaryAction).primary(), (__, m) -> {
                            resourceForm.resetValidation();
                            if (resourceForm.validate()) {
                                ModelNode record = collectRecord(childItems);
                                if (adding) {
                                    currentRecords.add(record);
                                } else {
                                    currentRecords.set(index, record);
                                }
                                renderTable();
                                m.close();
                            }
                        })
                        .addButton(button("Cancel").link(), (__, m) -> m.close()))
                .appendToBody()
                .open();
    }

    private ModelNode collectRecord(List<FormItem> childItems) {
        ModelNode record = new ModelNode();
        for (FormItem item : childItems) {
            item.contributeToPayload(record);
        }
        return record;
    }

    // ------------------------------------------------------ internal

    private List<ModelNode> cloneList(ModelNode value) {
        List<ModelNode> cloned = new ArrayList<>();
        if (value.isDefined()) {
            for (ModelNode item : value.asList()) {
                cloned.add(item.clone());
            }
        }
        return cloned;
    }
}

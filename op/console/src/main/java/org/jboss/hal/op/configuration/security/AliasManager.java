package org.jboss.hal.op.configuration.security;

import java.util.List;

import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.OperationDescription;
import org.jboss.hal.model.filter.NameAttribute;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.resources.OuiaIds;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.form.ResourceForm;
import org.jboss.hal.ui.resource.form.StringControl;
import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.component.list.DataList;
import org.patternfly.component.list.DataListItem;
import org.patternfly.component.modal.Modal;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.core.ObservableValue;
import org.patternfly.filter.Filter;
import org.patternfly.filter.FilterOperator;
import org.patternfly.handler.ComponentHandler;
import org.patternfly.layout.stack.StackItem;
import org.patternfly.style.Classes;
import org.patternfly.style.Variable;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.isAttached;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.core.Notification.error;
import static org.jboss.hal.core.Notification.success;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD_ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE_ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SET_SECRET;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.brick.CodeBricks.errorCode;
import static org.jboss.hal.ui.brick.DomBricks.toggle;
import static org.jboss.hal.ui.brick.EmptyStateBricks.noItems;
import static org.jboss.hal.ui.brick.EmptyStateBricks.noMatch;
import static org.jboss.hal.ui.filter.ItemCount.itemCount;
import static org.jboss.hal.ui.filter.NameSearchInput.nameSearchInput;
import static org.jboss.hal.ui.resource.dialog.ExecuteOperationDialogs.operationForm;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.alert.Alert.alert;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.emptystate.EmptyStateActions.emptyStateActions;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;
import static org.patternfly.component.list.DataList.dataList;
import static org.patternfly.component.list.DataListAction.dataListAction;
import static org.patternfly.component.list.DataListCell.dataListCell;
import static org.patternfly.component.list.DataListItem.dataListItem;
import static org.patternfly.component.modal.Modal.modal;
import static org.patternfly.component.modal.ModalBody.modalBody;
import static org.patternfly.component.modal.ModalFooter.modalFooter;
import static org.patternfly.component.modal.ModalHeader.modalHeader;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarGroupType.actionGroupPlain;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.component.toolbar.ToolbarItemType.searchFilter;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.core.ObservableValue.ov;
import static org.patternfly.icon.IconSets.fas.plus;
import static org.patternfly.icon.IconSets.fas.rotateRight;
import static org.patternfly.layout.stack.Stack.stack;
import static org.patternfly.layout.stack.StackItem.stackItem;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.filtered;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Size.lg;
import static org.patternfly.style.Size.sm;
import static org.patternfly.style.Variable.componentVar;

class AliasManager implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    static AliasManager aliasManager(Dispatcher dispatcher, AddressTemplate template, Metadata metadata) {
        return new AliasManager(dispatcher, template, metadata);
    }

    // ------------------------------------------------------ instance

    private final Dispatcher dispatcher;
    private final AddressTemplate template;
    private final Metadata metadata;
    private final boolean supportsSetSecret;
    private final ObservableValue<Integer> visible;
    private final ObservableValue<Integer> total;
    private final Toolbar toolbar;
    private final EmptyState noMatch;
    private final HTMLElement dataListContainer;
    private final HTMLElement root;
    private DataList dataList;

    AliasManager(Dispatcher dispatcher, AddressTemplate template, Metadata metadata) {
        this.dispatcher = dispatcher;
        this.template = template;
        this.metadata = metadata;
        this.supportsSetSecret = "credential-store".equals(template.last().key);
        this.visible = ov(0);
        this.total = ov(0);

        Filter<String> filter = new Filter<String>(FilterOperator.AND)
                .add(new NameAttribute<>(alias -> alias))
                .onChange(this::onFilterChanged);
        noMatch = noMatch(filter);

        String addId = Id.unique("add");
        String refreshId = Id.unique("refresh");
        Variable spacer = componentVar(component(Classes.toolbar), "spacer");
        Variable filterGroupSpacer = componentVar(component(Classes.toolbar, Classes.group), "m-filter-group", "spacer");
        toolbar = toolbar().css(util("pt-xs"))
                .addContent(toolbarContent()
                        .addItem(toolbarItem(searchFilter)
                                .style(spacer.name, filterGroupSpacer.asVar())
                                .add(nameSearchInput(filter)))
                        .addItem(toolbarItem()
                                .style("align-self", "center")
                                .add(itemCount(visible, total, "alias", "aliases")))
                        .addGroup(toolbarGroup(actionGroupPlain).css(modifier("align-right"))
                                .addItem(toolbarItem().id(addId)
                                        .add(button().plain().icon(plus()).onClick((e, b) -> add()))
                                        .add(tooltip(By.id(addId), "Add")))
                                .addItem(toolbarItem().id(refreshId)
                                        .add(button().plain().icon(rotateRight()).onClick((e, b) -> load()))
                                        .add(tooltip(By.id(refreshId), "Refresh")))));
        setVisible(toolbar, false);

        root = div()
                .add(toolbar)
                .add(dataListContainer = div().element())
                .element();
        load();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ data

    private void load() {
        Operation operation = new Operation.Builder(template.resolve(), "read-aliases").build();
        dispatcher.execute(operation, result -> {
            List<String> aliases = result.asList().stream().map(ModelNode::asString).sorted().collect(toList());
            if (aliases.isEmpty()) {
                empty();
            } else {
                visible.set(aliases.size());
                total.set(aliases.size());
                show(aliases);
            }
        });
    }

    private void show(List<String> aliases) {
        setVisible(toolbar, true);
        removeChildrenFrom(dataListContainer);

        if (dataList == null) {
            dataList = dataList();
        }
        dataList.clear();
        for (String alias : aliases) {
            String aliasId = Id.build(alias);
            dataList.addItem(dataListItem(aliasId)
                    .addCell(dataListCell().add(span().id(aliasId).text(alias)))
                    .addAction(dataListAction()
                            .run(action -> {
                                if (supportsSetSecret) {
                                    action.add(button("Set secret").tertiary().onClick((e, b) -> setSecret(alias)));
                                }
                            })
                            .add(button("Remove").tertiary().onClick((e, b) -> remove(alias)))));
        }
        if (!isAttached(dataList)) {
            dataListContainer.appendChild(dataList.element());
        }
    }

    private void empty() {
        setVisible(toolbar, false);
        removeChildrenFrom(dataListContainer);

        dataListContainer.appendChild(noItems("No aliases", "This credential store doesn't contain aliases.")
                .addFooter(emptyStateFooter()
                        .addActions(emptyStateActions()
                                .add(button("Add").link().onClick((e, b) -> add()))
                                .add(button("Refresh").link().onClick((e, b) -> load()))))
                .element());
    }

    // ------------------------------------------------------ filter

    private void onFilterChanged(Filter<String> filter, String origin) {
        if (dataList != null) {
            int matchingItems = filter.defined() ? applyFilter(filter) : clearFilter();
            visible.set(matchingItems);
        }
    }

    private int applyFilter(Filter<String> filter) {
        int matches = 0;
        for (DataListItem item : dataList.items()) {
            String text = item.element().textContent;
            boolean match = filter.match(text);
            item.classList().toggle(modifier(filtered), !match);
            if (match) {
                matches++;
            }
        }
        toggle(noMatch, dataListContainer, matches == 0);
        return matches;
    }

    private int clearFilter() {
        toggle(noMatch, dataListContainer, false);
        dataList.items().forEach(dli -> dli.classList().remove(modifier(filtered)));
        return total.get();
    }

    // ------------------------------------------------------ crud

    private void add() {
        OperationDescription operationDescription = metadata.resourceDescription().operations().get(ADD_ALIAS);
        if (operationDescription.isDefined()) {
            StackItem resultContainer = stackItem();
            ResourceForm form = operationForm(template, metadata, operationDescription);
            prepareOperation("Add alias", operationDescription.description(), "Add", form, resultContainer,
                    (__, m) -> executeAdd(m, form, resultContainer));
        } else {
            uic().notifications().send(error("Operation failed", "No operation definition found for " + ADD_ALIAS));
        }
    }

    private void executeAdd(Modal modal, ResourceForm form, StackItem resultContainer) {
        ModelNode payload = form.modelNode();
        String alias = payload.get(ALIAS).asString();
        Operation operation = new Operation.Builder(template.resolve(), ADD_ALIAS)
                .payload(payload)
                .build();
        executeOperation("Alias added", "Alias " + alias + " has been successfully added.",
                modal, form, resultContainer, operation);
    }

    private void setSecret(String alias) {
        OperationDescription operationDescription = metadata.resourceDescription().operations().get(SET_SECRET);
        if (operationDescription.isDefined()) {
            StackItem resultContainer = stackItem();
            ResourceForm form = operationForm(template, metadata, operationDescription);
            FormItem aliasItem = form.item(ALIAS);
            if (aliasItem != null) {
                aliasItem.disable();
                StringControl control = (StringControl) aliasItem.editableControl().nativeControl();
                control.textInput().value(alias);
            }
            prepareOperation("set secret", operationDescription.description(), "Set secret", form, resultContainer,
                    (__, m) -> executeSetSecret(m, form, resultContainer));
        } else {
            uic().notifications().send(error("Operation failed", "No operation definition found for " + SET_SECRET));
        }
    }

    private void executeSetSecret(Modal modal, ResourceForm form, StackItem resultContainer) {
        ModelNode payload = form.modelNode();
        String alias = payload.get(ALIAS).asString();
        Operation operation = new Operation.Builder(template.resolve(), SET_SECRET)
                .payload(payload)
                .build();
        executeOperation("Secret set", "Secret for " + alias + " has been successfully set.",
                modal, form, resultContainer, operation);
    }

    private void remove(String alias) {
        modal().size(sm)
                .ouiaId(OuiaIds.DELETE_MODAL)
                .addHeader("Delete alias")
                .addBody(modalBody()
                        .add("Do you really want to delete ")
                        .add(span().css(util("font-weight-bold")).text(alias))
                        .add("?"))
                .addFooter(modalFooter()
                        .addButton(button("Delete").primary()
                                .ouiaId(OuiaIds.DELETE_BTN), (__, m) -> executeRemove(m, alias))
                        .addButton(button("Cancel").link().ouiaId(OuiaIds.CANCEL_BTN), (__, m) -> m.close()))
                .appendToBody()
                .open();
    }

    private void executeRemove(Modal modal, String alias) {
        Operation operation = new Operation.Builder(template.resolve(), REMOVE_ALIAS)
                .param(ALIAS, alias)
                .build();
        uic().dispatcher().execute(operation)
                .then(result -> {
                    uic().notifications().send(success("Alias removed", "Alias " + alias + " has been successfully removed."));
                    modal.close();
                    load();
                    return null;
                })
                .catch_(err -> {
                    uic().notifications().send(error("Operation failed", "Alias " + alias + " could not be removed."));
                    modal.close();
                    return null;
                });
    }

    // ------------------------------------------------------ internal

    private void prepareOperation(String title, String description, String primaryText,
            ResourceForm form, StackItem resultContainer, ComponentHandler<Modal> primaryHandler) {
        modal().size(lg).top()
                .ouiaId(OuiaIds.EXECUTE_MODAL)
                .addHeader(modalHeader()
                        .addTitle(title)
                        .addDescription(description))
                .addBody(modalBody()
                        .add(stack().gutter()
                                .addItem(stackItem().fill(true)
                                        .add(div().css(halComponent(HalClasses.resource))
                                                .add(form)))
                                .addItem(resultContainer)))
                .addFooter(modalFooter()
                        .addButton(button(primaryText).primary()
                                .ouiaId(OuiaIds.EXECUTE_BTN), primaryHandler)
                        .addButton(button("Close").link()
                                .ouiaId(OuiaIds.CLOSE_BTN), (__, m) -> m.close()))
                .appendToBody()
                .open();
    }

    private void executeOperation(String successTitle, String successDescription,
            Modal modal, ResourceForm form, StackItem resultContainer, Operation operation) {
        boolean execute = true;
        form.resetValidation();
        removeChildrenFrom(resultContainer);
        if (!form.validate()) {
            execute = false;
            form.validationAlert("Operation failed");
        }

        if (execute) {
            uic().dispatcher().execute(operation)
                    .then(result -> {
                        uic().notifications().send(success(successTitle, successDescription));
                        modal.close();
                        load();
                        return null;
                    })
                    .catch_(err -> {
                        form.addAlert(alert(danger, "Operation failed").inline());
                        resultContainer.add(errorCode(String.valueOf(err), 7));
                        return null;
                    });
        }
    }
}

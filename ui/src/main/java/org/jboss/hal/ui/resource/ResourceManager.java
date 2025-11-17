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
package org.jboss.hal.ui.resource;

import java.util.ArrayList;
import java.util.List;

import org.jboss.elemento.Attachable;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.TypedBuilder;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.ui.modelbrowser.NoMatch;
import org.jboss.hal.ui.resource.FormItemFlags.Placeholder;
import org.jboss.hal.ui.resource.FormItemFlags.Scope;
import org.patternfly.component.HasItems;
import org.patternfly.core.ObservableValue;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;
import elemental2.dom.MutationRecord;

import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.isAttached;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.hal.core.Notification.nyi;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.resources.HalClasses.body;
import static org.jboss.hal.resources.HalClasses.filtered;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.ui.BuildingBlocks.errorCode;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.resource.FormItemFactory.formItem;
import static org.jboss.hal.ui.resource.ResourceAttribute.includes;
import static org.jboss.hal.ui.resource.ResourceAttribute.resourceAttributes;
import static org.jboss.hal.ui.resource.ResourceManager.State.EDIT;
import static org.jboss.hal.ui.resource.ResourceManager.State.ERROR;
import static org.jboss.hal.ui.resource.ResourceManager.State.NO_ATTRIBUTES;
import static org.jboss.hal.ui.resource.ResourceManager.State.VIEW;
import static org.jboss.hal.ui.resource.ResourceToolbar.resourceToolbar;
import static org.jboss.hal.ui.resource.ViewItemFactory.viewItem;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.alert.Alert.alert;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateActions.emptyStateActions;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;
import static org.patternfly.core.ObservableValue.ov;
import static org.patternfly.icon.IconSets.fas.ban;

/**
 * Combines a {@link ResourceFilter} and {@link ResourceToolbar} with a {@link ResourceView} and {@link ResourceForm}.
 */
public class ResourceManager implements TypedBuilder<HTMLElement, ResourceManager>, IsElement<HTMLElement>, Attachable {

    // ------------------------------------------------------ factory

    public static ResourceManager resourceManager(AddressTemplate template, Metadata metadata) {
        return new ResourceManager(template, metadata);
    }

    // ------------------------------------------------------ instance

    enum State {
        VIEW, EDIT, NO_ATTRIBUTES, ERROR
    }

    private static final Logger logger = Logger.getLogger(ResourceManager.class.getName());

    private final AddressTemplate template;
    private final Metadata metadata;
    private final List<String> attributes;
    private final ObservableValue<Integer> visible;
    private final ObservableValue<Integer> total;
    private final Filter<ResourceAttribute> filter;
    private final NoMatch<ResourceAttribute> noMatch;
    private final ResourceToolbar toolbar;
    private final HTMLElement rootContainer;
    private final HTMLElement root;
    private boolean inlineEdit;
    private State state;
    private Operation operation;
    private HasItems<HTMLElement, ?, ? extends ManagerItem<?>> items;
    private ResourceForm resourceForm;

    ResourceManager(AddressTemplate template, Metadata metadata) {
        this.template = template;
        this.metadata = metadata;
        this.attributes = new ArrayList<>();
        this.visible = ov(0);
        this.total = ov(0);
        this.filter = new ResourceFilter().onChange(this::onFilterChanged);
        this.noMatch = new NoMatch<>(filter);
        this.inlineEdit = false;
        this.state = null;
        this.operation = new Operation.Builder(template.resolve(), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        this.root = div().css(halComponent(resource))
                .add(toolbar = resourceToolbar(this, filter, visible, total))
                .add(rootContainer = div().css(halComponent(resource, body))
                        .element())
                .element();

        setVisible(toolbar, false);
        Attachable.register(this, this);
    }

    @Override
    public void attach(MutationRecord mutationRecord) {
        load(VIEW);
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ builder

    public ResourceManager inlineEdit() {
        return inlineEdit(true);
    }

    public ResourceManager inlineEdit(boolean inlineEdit) {
        this.inlineEdit = inlineEdit;
        return this;
    }

    public ResourceManager operation(Operation operation) {
        if (operation != null) {
            this.operation = operation;
        } else {
            logger.error("Operation is null!");
        }
        return this;
    }

    public ResourceManager attributes(Iterable<String> attributes) {
        for (String attribute : attributes) {
            this.attributes.add(attribute);
        }
        return this;
    }

    @Override
    public ResourceManager that() {
        return this;
    }

    // ------------------------------------------------------ status

    void load(State state) {
        changeState(state);
        if (metadata.isDefined()) {
            uic().dispatcher().execute(operation, resource -> {
                if (valid(resource)) {
                    List<ResourceAttribute> resourceAttributes = resourceAttributes(resource, metadata, includes(attributes));

                    if (state == VIEW) {
                        ResourceView resourceView = new ResourceView();
                        for (ResourceAttribute ra : resourceAttributes) {
                            resourceView.addItem(viewItem(template, metadata, ra));
                        }
                        items = resourceView;

                    } else if (state == EDIT) {
                        resourceForm = new ResourceForm(template);
                        for (ResourceAttribute ra : resourceAttributes) {
                            resourceForm.addItem(formItem(template, metadata, ra,
                                    new FormItemFlags(Scope.EXISTING_RESOURCE, Placeholder.UNDEFINED)));
                        }
                        items = resourceForm;
                    }

                    if (state == VIEW || state == EDIT) {
                        total.set(resourceAttributes.size());
                        if (filter.defined()) {
                            onFilterChanged(filter, null);
                        } else {
                            visible.set(resourceAttributes.size());
                        }
                        toolbar.adjust(state, metadata.securityContext());
                        setVisible(toolbar, true);
                        rootContainer.append(items.element());
                    }
                } else {
                    noAttributes();
                }
            }, (op, error) -> operationError(op.asCli(), error));
        } else {
            metadataError();
        }
    }

    private void noAttributes() {
        changeState(NO_ATTRIBUTES);
        rootContainer.append(emptyState()
                .icon(ban())
                .text("No attributes")
                .addBody(emptyStateBody()
                        .text("This resource contains no attributes."))
                .element());
    }

    private void operationError(String operation, String error) {
        changeState(ERROR);
        rootContainer.append(emptyState()
                .status(danger)
                .text("Operation failed")
                .addBody(emptyStateBody()
                        .add("Unable to view resource. Operation ")
                        .add(code().text(operation))
                        .add(" failed:")
                        .add(errorCode(error)))
                .addFooter(emptyStateFooter()
                        .addActions(emptyStateActions()
                                .add(button("Try again").link().onClick((e, b) -> refresh()))))
                .element());
    }

    private void metadataError() {
        changeState(ERROR);
        rootContainer.append(emptyState()
                .status(danger)
                .text("No metadata")
                .addBody(emptyStateBody()
                        .text("Unable to view resource: No metadata found!"))
                .element());
    }

    // ------------------------------------------------------ filter

    private void onFilterChanged(Filter<ResourceAttribute> filter, String origin) {
        if ((state == VIEW || state == EDIT) && items != null && isAttached(element())) {
            logger.debug("Filter attributes: %s", filter);
            int matchingItems;
            if (filter.defined()) {
                matchingItems = 0;
                for (ManagerItem<?> item : items) {
                    ResourceAttribute ra = item.resourceAttribute();
                    if (ra != null) {
                        boolean match = filter.match(ra);
                        item.element().classList.toggle(halModifier(filtered), !match);
                        if (match) {
                            matchingItems++;
                        }
                    }
                }
                noMatch.toggle(rootContainer, matchingItems == 0);
            } else {
                matchingItems = total.get();
                noMatch.toggle(rootContainer, false);
                items.items().forEach(item -> item.element().classList.remove(halModifier(filtered)));
            }
            visible.set(matchingItems);
        }
    }

    // ------------------------------------------------------ actions

    void refresh() {
        if (state == VIEW) {
            removeChildrenFrom(rootContainer);
            load(VIEW);
        }
    }

    void reset() {
        if (state == VIEW) {
            // TODO Implement me!
            uic().notifications().send(nyi());
        }
    }

    void save() {
        if (state == EDIT && resourceForm != null) {
            resourceForm.resetValidation();
            if (resourceForm.validate()) {
                uic().crud().update(template, resourceForm.attributeOperations())
                        .then(__ -> {
                            load(VIEW);
                            return null;
                        })
                        .catch_(error -> {
                            resourceForm.addAlert(alert(danger, "Update failed").inline()
                                    .addDescription(String.valueOf(error)));
                            return null;
                        });
            } else {
                resourceForm.validationAlert("Update failed");
            }
        }
    }

    void cancel() {
        if (state == EDIT) {
            load(VIEW);
        }
    }

    // ------------------------------------------------------ internal

    private void changeState(State state) {
        boolean stateChange = state != this.state;
        boolean viewOrEdit = (state == VIEW || state == EDIT) && (this.state == VIEW || this.state == EDIT);
        this.state = state;
        if (stateChange) {
            removeChildrenFrom(rootContainer);
            // only hide the toolbar if there's a change from VIEW|EDIT to some other state or vice versa
            setVisible(toolbar, viewOrEdit);
        }
    }

    private boolean valid(ModelNode resource) {
        return resource != null && resource.isDefined() && !resource.asPropertyList().isEmpty();
    }
}

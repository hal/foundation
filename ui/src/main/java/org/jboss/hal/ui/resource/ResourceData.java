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
import java.util.stream.Collectors;

import org.jboss.elemento.Attachable;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.TypedBuilder;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.form.ResourceForm;
import org.jboss.hal.ui.resource.grouping.AutoGrouping;
import org.jboss.hal.ui.resource.grouping.GroupingSupport;
import org.jboss.hal.ui.resource.pipeline.Pipeline;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags.Placeholder;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags.Scope;
import org.jboss.hal.ui.resource.view.ResourceView;
import org.jboss.hal.ui.resource.view.ViewItem;
import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.core.ObservableValue;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLDivElement;
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
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.brick.CodeBricks.errorCode;
import static org.jboss.hal.ui.brick.EmptyStateBricks.error;
import static org.jboss.hal.ui.brick.EmptyStateBricks.noItems;
import static org.jboss.hal.ui.brick.EmptyStateBricks.noMatch;
import static org.jboss.hal.ui.brick.DomBricks.toggle;
import static org.jboss.hal.ui.resource.ResourceData.State.EDIT;
import static org.jboss.hal.ui.resource.ResourceData.State.ERROR;
import static org.jboss.hal.ui.resource.ResourceData.State.NO_ATTRIBUTES;
import static org.jboss.hal.ui.resource.ResourceData.State.VIEW;
import static org.jboss.hal.ui.resource.ResourceDataToolbar.resourceDataToolbar;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.alert.Alert.alert;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateActions.emptyStateActions;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;
import static org.patternfly.core.ObservableValue.ov;

/**
 * Central state machine that orchestrates viewing and editing of WildFly management resource attributes. Uses the pipeline to
 * produce view and form items from resource metadata, and delegates filtering and grouping to
 * {@link org.jboss.hal.ui.resource.view.ResourceView} and {@link ResourceForm}.
 *
 * @see org.jboss.hal.ui.resource.ResourceItem
 * @see GroupingSupport
 */
public class ResourceData implements TypedBuilder<HTMLElement, ResourceData>, IsElement<HTMLElement>, Attachable {

    // ------------------------------------------------------ factory

    public static ResourceData resourceData(AddressTemplate template, Metadata metadata) {
        return new ResourceData(template, metadata);
    }

    // ------------------------------------------------------ instance

    public enum State {
        VIEW, EDIT, NO_ATTRIBUTES, ERROR
    }

    private static final Logger logger = Logger.getLogger(ResourceData.class.getName());

    private final AddressTemplate template;
    private final Metadata metadata;
    private final ObservableValue<Integer> visible;
    private final ObservableValue<Integer> total;
    private final Filter<ResolvedAttribute> filter;
    private final List<String> attributes;
    private final EmptyState noMatch;
    private final ResourceDataToolbar toolbar;
    private final HTMLContainerBuilder<HTMLDivElement> rootContainer;
    private final HTMLElement root;
    private final Pipeline pipeline;
    private final List<ViewItem> viewItems;
    private final List<FormItem> formItems;

    private boolean grouped;
    private boolean supportsGrouping;
    private State state;
    private Operation operation;
    private ResourceView resourceView;
    private ResourceForm resourceForm;

    ResourceData(AddressTemplate template, Metadata metadata) {
        this.template = template;
        this.metadata = metadata;
        this.visible = ov(0);
        this.total = ov(0);
        this.filter = new ResourceFilter().onChange(this::onFilterChanged);
        this.attributes = new ArrayList<>();
        this.noMatch = noMatch(filter);
        this.grouped = false;
        this.supportsGrouping = false;
        this.state = null;
        this.operation = new Operation.Builder(template.resolve(), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        this.pipeline = Pipeline.DEFAULT;
        this.viewItems = new ArrayList<>();
        this.formItems = new ArrayList<>();
        this.root = div().css(halComponent(resource))
                .add(toolbar = resourceDataToolbar(this, filter, visible, total))
                .add(rootContainer = div().css(halComponent(resource, body)))
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

    public ResourceData operation(Operation operation) {
        if (operation != null) {
            this.operation = operation;
        } else {
            logger.error("Operation is null!");
        }
        return this;
    }

    public ResourceData attributes(Iterable<String> attributes) {
        for (String attribute : attributes) {
            this.attributes.add(attribute);
        }
        return this;
    }

    @Override
    public ResourceData that() {
        return this;
    }

    // ------------------------------------------------------ status

    void load(State state) {
        changeState(state);
        if (metadata.isDefined()) {
            uic().dispatcher().execute(operation, resource -> {
                if (valid(resource)) {
                    PipelineContext context = new PipelineContext(template, metadata, resource,
                            new PipelineFlags(Scope.EXISTING_RESOURCE, Placeholder.UNDEFINED));
                    viewItems.clear();
                    formItems.clear();
                    HTMLElement rootElement = null;

                    if (state == VIEW) {
                        List<ViewItem> items = pipeline.viewItems(context);
                        items = filterByIncludes(items);
                        viewItems.addAll(items);
                        supportsGrouping = GroupingSupport.hasGroups(items)
                                || items.size() >= AutoGrouping.AUTO_GROUPING_THRESHOLD;
                        resourceView = new ResourceView();
                        rootElement = resourceView.build(items, grouped && supportsGrouping);

                    } else if (state == EDIT) {
                        List<FormItem> items = pipeline.formItems(context);
                        items = filterByIncludes(items);
                        formItems.addAll(items);
                        supportsGrouping = GroupingSupport.hasGroups(items)
                                || items.size() >= AutoGrouping.AUTO_GROUPING_THRESHOLD;
                        resourceForm = new ResourceForm();
                        resourceForm.addItems(items, grouped && supportsGrouping);
                        rootElement = resourceForm.element();
                    }

                    if (state == VIEW || state == EDIT) {
                        int itemCount = state == VIEW ? viewItems.size() : formItems.size();
                        total.set(itemCount);
                        if (filter.defined()) {
                            onFilterChanged(filter, null);
                        } else {
                            visible.set(itemCount);
                        }
                        toolbar.adjust(state, metadata.securityContext());
                        setVisible(toolbar, true);
                        rootContainer.add(rootElement);
                    }
                } else {
                    noAttributes();
                }
            }, (op, error) -> operationError(op.asCli(), error));
        } else {
            metadataError();
        }
    }

    // ------------------------------------------------------ filtering

    private <T extends ResourceItem> List<T> filterByIncludes(List<T> items) {
        if (attributes.isEmpty()) {
            return items;
        }
        return items.stream()
                .filter(item -> attributes.contains(item.attribute().fqn()))
                .collect(Collectors.toList());
    }

    private void onFilterChanged(Filter<ResolvedAttribute> filter, String origin) {
        if ((state == VIEW || state == EDIT) && isAttached(element())) {
            logger.debug("Filter attributes: %s", filter);
            int matchingItems;
            if (filter.defined()) {
                matchingItems = state == VIEW
                        ? resourceView.applyFilter(filter)
                        : resourceForm.applyFilter(filter);
                toggle(noMatch, rootContainer.element(), matchingItems == 0);
            } else {
                matchingItems = total.get();
                toggle(noMatch, rootContainer.element(), false);
                if (state == VIEW) {
                    resourceView.clearFilter();
                } else {
                    resourceForm.clearFilter();
                }
            }
            visible.set(matchingItems);
        }
    }

    // ------------------------------------------------------ error states

    private void noAttributes() {
        changeState(NO_ATTRIBUTES);
        rootContainer.add(noItems("No attributes", "This resource contains no attributes."));
    }

    private void operationError(String operation, String error) {
        changeState(ERROR);
        rootContainer.add(emptyState()
                .status(danger)
                .text("Operation failed")
                .addBody(emptyStateBody()
                        .add("Unable to view resource. Operation ")
                        .add(code().text(operation))
                        .add(" failed:")
                        .add(errorCode(error)))
                .addFooter(emptyStateFooter()
                        .addActions(emptyStateActions()
                                .add(button("Try again").link().onClick((e, b) -> refresh())))));
    }

    private void metadataError() {
        changeState(ERROR);
        rootContainer.add(error("No metadata", "Unable to view resource: No metadata found!"));
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
            uic().notifications().send(nyi());
        }
    }

    void save() {
        if (state == EDIT && resourceForm != null) {
            resourceForm.resetValidation();
            if (resourceForm.validate()) {
                List<Operation> ops = resourceForm.operations(template.resolve());
                uic().crud().update(template, ops)
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

    void toggleGrouped() {
        grouped = !grouped;
        removeChildrenFrom(rootContainer);
        load(state);
    }

    boolean isGrouped() {
        return grouped;
    }

    boolean supportsGrouping() {
        return supportsGrouping;
    }

    // ------------------------------------------------------ internal

    private void changeState(State state) {
        boolean stateChange = state != this.state;
        boolean viewOrEdit = (state == VIEW || state == EDIT) && (this.state == VIEW || this.state == EDIT);
        this.state = state;
        if (stateChange) {
            removeChildrenFrom(rootContainer);
            setVisible(toolbar, viewOrEdit);
        }
    }

    private boolean valid(ModelNode resource) {
        return resource != null && resource.isDefined() && !resource.asPropertyList().isEmpty();
    }
}

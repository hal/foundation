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
package org.jboss.hal.ui.resource.data;
import org.jboss.hal.ui.resource.ResourceAttribute;
import org.jboss.hal.ui.resource.ResourceItem;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.form.FormItemFlags.Placeholder;
import org.jboss.hal.ui.resource.form.FormItemFlags.Scope;
import org.jboss.hal.ui.resource.form.FormItemFlags;
import org.jboss.hal.ui.resource.view.ViewItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.elemento.Attachable;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.TypedBuilder;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.HalClasses;
import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.component.expandable.ExpandableSection;
import org.patternfly.component.form.FormFieldGroup;
import org.patternfly.component.form.FormFieldGroupBody;
import org.patternfly.component.list.DescriptionList;
import org.jboss.hal.ui.resource.form.ResourceForm;
import org.jboss.hal.ui.resource.view.ResourceView;
import org.patternfly.component.HasItems;
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
import static org.jboss.hal.core.Humanize.capitalCase;
import static org.jboss.hal.core.Notification.nyi;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.resources.HalClasses.body;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.ui.brick.CodeBricks.errorCode;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.resource.form.FormItemFactory.formItem;
import static org.jboss.hal.ui.resource.ResourceAttribute.grouped;
import static org.jboss.hal.ui.resource.ResourceAttribute.hasGroups;
import static org.jboss.hal.ui.resource.ResourceAttribute.includes;
import static org.jboss.hal.ui.resource.ResourceAttribute.resourceAttributes;
import static org.jboss.hal.ui.resource.view.ViewItemFactory.viewItem;
import static org.jboss.hal.ui.resource.data.ResourceData.State.EDIT;
import static org.jboss.hal.ui.resource.data.ResourceData.State.ERROR;
import static org.jboss.hal.ui.resource.data.ResourceData.State.NO_ATTRIBUTES;
import static org.jboss.hal.ui.resource.data.ResourceData.State.VIEW;
import static org.jboss.hal.ui.resource.data.ResourceDataToolbar.resourceDataToolbar;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.alert.Alert.alert;
import static org.patternfly.component.button.Button.button;
import static org.jboss.hal.ui.brick.EmptyStateBricks.error;
import static org.jboss.hal.ui.brick.EmptyStateBricks.noItems;
import static org.jboss.hal.ui.brick.EmptyStateBricks.noMatch;
import static org.jboss.hal.ui.brick.EmptyStateBricks.toggle;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateActions.emptyStateActions;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;
import static org.patternfly.component.expandable.ExpandableSection.expandableSection;
import static org.patternfly.component.expandable.ExpandableSectionContent.expandableSectionContent;
import static org.patternfly.component.expandable.ExpandableSectionToggle.expandableSectionToggle;
import static org.patternfly.component.form.FormFieldGroup.formFieldGroup;
import static org.patternfly.component.form.FormFieldGroupBody.formFieldGroupBody;
import static org.patternfly.component.form.FormFieldGroupHeader.formFieldGroupHeader;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.core.ObservableValue.ov;
import static org.patternfly.icon.IconSets.fas.ban;
import static org.patternfly.style.Breakpoint._2xl;
import static org.patternfly.style.Breakpoint.lg;
import static org.patternfly.style.Breakpoint.md;
import static org.patternfly.style.Breakpoint.sm;
import static org.patternfly.style.Breakpoint.xl;
import static org.patternfly.style.Breakpoints.breakpoints;
import static org.patternfly.style.Classes.filtered;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Orientation.horizontal;
import static org.patternfly.style.Orientation.vertical;

/**
 * Central state machine that orchestrates viewing and editing of WildFly management resource attributes. Combines a
 * {@link ResourceFilter} and {@link ResourceDataToolbar} with a {@link ResourceView} and
 * {@link org.jboss.hal.ui.resource.form.ResourceForm}.
 * <p>
 * Manages transitions between {@link State#VIEW}, {@link State#EDIT}, {@link State#NO_ATTRIBUTES}, and
 * {@link State#ERROR} states. On attach, it loads the resource from the management endpoint and populates either a
 * read-only view or an editable form depending on the requested state.
 */
public class ResourceData implements TypedBuilder<HTMLElement, ResourceData>, IsElement<HTMLElement>, Attachable {

    // ------------------------------------------------------ factory

    /** Creates a new resource data component for the given address template and metadata. */
    public static ResourceData resourceData(AddressTemplate template, Metadata metadata) {
        return new ResourceData(template, metadata);
    }

    // ------------------------------------------------------ instance

    /** The lifecycle states of the resource data component. */
    public enum State {
        /** Read-only display of resource attributes. */
        VIEW,
        /** Editable form for modifying resource attributes. */
        EDIT,
        /** The resource has no attributes to display. */
        NO_ATTRIBUTES,
        /** An error occurred while loading the resource or its metadata. */
        ERROR
    }

    private static final Logger logger = Logger.getLogger(ResourceData.class.getName());

    private final AddressTemplate template;
    private final Metadata metadata;
    private final List<String> attributes;
    private final ObservableValue<Integer> visible;
    private final ObservableValue<Integer> total;
    private final Filter<ResourceAttribute> filter;
    private final EmptyState noMatch;
    private final ResourceDataToolbar toolbar;
    private final HTMLContainerBuilder<HTMLDivElement> rootContainer;
    private final HTMLElement root;
    private boolean inlineEdit;
    private boolean grouped;
    private boolean supportsGrouping;
    private List<ResourceItem<?>> allItems;
    private List<HTMLElement> groupContainers;
    private State state;
    private Operation operation;
    private HasItems<HTMLElement, ?, ? extends ResourceItem<?>> items;
    private ResourceForm resourceForm;

    ResourceData(AddressTemplate template, Metadata metadata) {
        this.template = template;
        this.metadata = metadata;
        this.attributes = new ArrayList<>();
        this.visible = ov(0);
        this.total = ov(0);
        this.filter = new ResourceFilter().onChange(this::onFilterChanged);
        this.noMatch = noMatch(filter);
        this.inlineEdit = false;
        this.grouped = false;
        this.supportsGrouping = false;
        this.allItems = new ArrayList<>();
        this.groupContainers = new ArrayList<>();
        this.state = null;
        this.operation = new Operation.Builder(template.resolve(), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
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

    /** Enables inline editing for this resource data component. */
    public ResourceData inlineEdit() {
        return inlineEdit(true);
    }

    /** Controls whether inline editing is enabled. */
    public ResourceData inlineEdit(boolean inlineEdit) {
        this.inlineEdit = inlineEdit;
        return this;
    }

    /** Overrides the default {@code read-resource} operation used to load the resource. */
    public ResourceData operation(Operation operation) {
        if (operation != null) {
            this.operation = operation;
        } else {
            logger.error("Operation is null!");
        }
        return this;
    }

    /** Restricts the displayed attributes to the given fully qualified names. An empty iterable shows all attributes. */
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
                    List<ResourceAttribute> resourceAttributes = resourceAttributes(resource, metadata, includes(attributes));

                    if (state == VIEW) {
                        allItems.clear();
                        groupContainers.clear();
                        supportsGrouping = hasGroups(resourceAttributes);
                        if (grouped && supportsGrouping) {
                            HTMLContainerBuilder<HTMLDivElement> viewContainer = div();
                            Map<String, List<ResourceAttribute>> groups = grouped(resourceAttributes);
                            for (Map.Entry<String, List<ResourceAttribute>> entry : groups.entrySet()) {
                                String groupName = entry.getKey();
                                List<ResourceAttribute> groupAttributes = entry.getValue();
                                DescriptionList dl = descriptionList()
                                        .css(halComponent(HalClasses.resource, HalClasses.view))
                                        .orientation(breakpoints(
                                                sm, vertical,
                                                md, horizontal,
                                                lg, horizontal,
                                                xl, horizontal,
                                                _2xl, horizontal));
                                for (ResourceAttribute ra : groupAttributes) {
                                    ViewItem vi = viewItem(template, metadata, ra);
                                    dl.addItem(vi.descriptionListGroup);
                                    allItems.add(vi);
                                }
                                if ("ungrouped".equals(groupName)) {
                                    viewContainer.add(dl);
                                } else {
                                    ExpandableSection es = expandableSection()
                                            .addToggle(expandableSectionToggle(capitalCase(groupName)))
                                            .addContent(expandableSectionContent().add(dl));
                                    es.expand();
                                    viewContainer.add(es);
                                    groupContainers.add(es.element());
                                }
                            }
                            items = null;
                            rootContainer.add(viewContainer);
                        } else {
                            ResourceView resourceView = new ResourceView();
                            for (ResourceAttribute ra : resourceAttributes) {
                                resourceView.addItem(viewItem(template, metadata, ra));
                            }
                            items = resourceView;
                        }

                    } else if (state == EDIT) {
                        allItems.clear();
                        groupContainers.clear();
                        supportsGrouping = hasGroups(resourceAttributes);
                        resourceForm = new ResourceForm(template);
                        if (grouped && supportsGrouping) {
                            Map<String, List<ResourceAttribute>> groups = grouped(resourceAttributes);
                            for (Map.Entry<String, List<ResourceAttribute>> entry : groups.entrySet()) {
                                String groupName = entry.getKey();
                                List<ResourceAttribute> groupAttributes = entry.getValue();
                                if ("ungrouped".equals(groupName)) {
                                    for (ResourceAttribute ra : groupAttributes) {
                                        FormItem fi = formItem(template, metadata, ra,
                                                new FormItemFlags(Scope.EXISTING_RESOURCE, Placeholder.UNDEFINED));
                                        resourceForm.addItem(fi);
                                        allItems.add(fi);
                                    }
                                } else {
                                    FormFieldGroupBody ffgBody = formFieldGroupBody();
                                    for (ResourceAttribute ra : groupAttributes) {
                                        FormItem fi = formItem(template, metadata, ra,
                                                new FormItemFlags(Scope.EXISTING_RESOURCE, Placeholder.UNDEFINED));
                                        resourceForm.addItem(fi);
                                        ffgBody.addGroup(fi.formGroup);
                                        allItems.add(fi);
                                    }
                                    FormFieldGroup ffg = formFieldGroup(true)
                                            .addHeader(formFieldGroupHeader().title(capitalCase(groupName)))
                                            .addBody(ffgBody);
                                    resourceForm.addFieldGroup(ffg);
                                    groupContainers.add(ffg.element());
                                }
                            }
                        } else {
                            for (ResourceAttribute ra : resourceAttributes) {
                                resourceForm.addItem(formItem(template, metadata, ra,
                                        new FormItemFlags(Scope.EXISTING_RESOURCE, Placeholder.UNDEFINED)));
                            }
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
                        if (items != null) {
                            rootContainer.add(items);
                        }
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

    // ------------------------------------------------------ filter

    private void onFilterChanged(Filter<ResourceAttribute> filter, String origin) {
        if ((state == VIEW || state == EDIT) && isAttached(element())) {
            logger.debug("Filter attributes: %s", filter);
            int matchingItems;
            Iterable<? extends ResourceItem<?>> filterItems;
            if (grouped && !allItems.isEmpty()) {
                filterItems = allItems;
            } else if (items != null) {
                filterItems = items;
            } else {
                filterItems = java.util.Collections.emptyList();
            }

            if (filter.defined()) {
                matchingItems = 0;
                for (ResourceItem<?> item : filterItems) {
                    ResourceAttribute ra = item.resourceAttribute();
                    if (ra != null) {
                        boolean match = filter.match(ra);
                        item.element().classList.toggle(modifier(filtered), !match);
                        if (match) {
                            matchingItems++;
                        }
                    }
                }
                // Hide group containers where all items are filtered out
                for (HTMLElement container : groupContainers) {
                    boolean hasVisibleItem = false;
                    for (ResourceItem<?> item : filterItems) {
                        if (container.contains(item.element())
                                && !item.element().classList.contains(modifier(filtered))) {
                            hasVisibleItem = true;
                            break;
                        }
                    }
                    setVisible(container, hasVisibleItem);
                }
                toggle(noMatch, rootContainer.element(), matchingItems == 0);
            } else {
                matchingItems = total.get();
                toggle(noMatch, rootContainer.element(), false);
                for (ResourceItem<?> item : filterItems) {
                    item.element().classList.remove(modifier(filtered));
                }
                for (HTMLElement container : groupContainers) {
                    setVisible(container, true);
                }
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
            allItems.clear();
            groupContainers.clear();
            // only hide the toolbar if there's a change from VIEW|EDIT to some other state or vice versa
            setVisible(toolbar, viewOrEdit);
        }
    }

    private boolean valid(ModelNode resource) {
        return resource != null && resource.isDefined() && !resource.asPropertyList().isEmpty();
    }
}

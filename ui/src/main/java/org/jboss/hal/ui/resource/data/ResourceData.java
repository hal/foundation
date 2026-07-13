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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.ui.resource.form.FormItem;
import org.jboss.hal.ui.resource.pipeline.Pipeline;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags.Placeholder;
import org.jboss.hal.ui.resource.pipeline.PipelineFlags.Scope;
import org.jboss.hal.ui.resource.pipeline.ResolvedAttribute;
import org.jboss.hal.ui.resource.view.ViewItem;
import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.component.expandable.ExpandableSection;
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
import static org.jboss.hal.resources.HalClasses.groupBody;
import static org.jboss.hal.resources.HalClasses.groups;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.brick.CodeBricks.errorCode;
import static org.jboss.hal.ui.brick.EmptyStateBricks.error;
import static org.jboss.hal.ui.brick.EmptyStateBricks.noItems;
import static org.jboss.hal.ui.brick.EmptyStateBricks.noMatch;
import static org.jboss.hal.ui.brick.EmptyStateBricks.toggle;
import static org.jboss.hal.ui.resource.data.ResourceData.State.EDIT;
import static org.jboss.hal.ui.resource.data.ResourceData.State.ERROR;
import static org.jboss.hal.ui.resource.data.ResourceData.State.NO_ATTRIBUTES;
import static org.jboss.hal.ui.resource.data.ResourceData.State.VIEW;
import static org.jboss.hal.ui.resource.data.ResourceDataToolbar.resourceDataToolbar;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.alert.Alert.alert;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateActions.emptyStateActions;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;
import static org.patternfly.component.expandable.ExpandableSection.expandableSection;
import static org.patternfly.component.expandable.ExpandableSectionContent.expandableSectionContent;
import static org.patternfly.component.expandable.ExpandableSectionToggle.expandableSectionToggle;
import static org.patternfly.component.form.Form.form;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.core.ObservableValue.ov;
import static org.patternfly.style.Classes.filtered;
import static org.patternfly.style.Classes.group;
import static org.patternfly.style.Classes.modifier;

/**
 * Central state machine that orchestrates viewing and editing of WildFly management resource attributes. Uses the pipeline to
 * produce view and form items from resource metadata.
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

    private static final String UNGROUPED = "ungrouped";
    private static final int AUTO_GROUPING_THRESHOLD = 20;
    private static final int TARGET_GROUP_SIZE = 10;
    private static final Logger logger = Logger.getLogger(ResourceData.class.getName());

    private final AddressTemplate template;
    private final Metadata metadata;
    private final ObservableValue<Integer> visible;
    private final ObservableValue<Integer> total;
    private final Filter<ResolvedAttribute> filter;
    private final List<String> attributes;
    private final List<HTMLElement> groupContainers;
    private final EmptyState noMatch;
    private final ResourceDataToolbar toolbar;
    private final HTMLContainerBuilder<HTMLDivElement> rootContainer;
    private final HTMLElement root;

    private boolean inlineEdit;
    private boolean grouped;
    private boolean supportsGrouping;
    private State state;
    private Operation operation;
    private Pipeline pipeline;

    // current items — one of these is populated depending on state
    private List<ViewItem> viewItems;
    private List<FormItem> formItems;
    private org.patternfly.component.form.Form currentForm;

    ResourceData(AddressTemplate template, Metadata metadata) {
        this.template = template;
        this.metadata = metadata;
        this.visible = ov(0);
        this.total = ov(0);
        this.filter = new ResourceFilter().onChange(this::onFilterChanged);
        this.attributes = new ArrayList<>();
        this.groupContainers = new ArrayList<>();
        this.noMatch = noMatch(filter);
        this.inlineEdit = false;
        this.grouped = false;
        this.supportsGrouping = false;
        this.state = null;
        this.operation = new Operation.Builder(template.resolve(), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        this.pipeline = Pipeline.create();
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

    public ResourceData inlineEdit() {
        return inlineEdit(true);
    }

    public ResourceData inlineEdit(boolean inlineEdit) {
        this.inlineEdit = inlineEdit;
        return this;
    }

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
                    groupContainers.clear();
                    HTMLElement rootElement = null;

                    if (state == VIEW) {
                        List<ViewItem> items = pipeline.viewItems(context);
                        items = filterByIncludes(items);
                        viewItems.addAll(items);
                        supportsGrouping = hasGroups(items);
                        rootElement = buildViewElement(items);

                    } else if (state == EDIT) {
                        List<FormItem> items = pipeline.formItems(context);
                        items = filterByIncludes(items);
                        formItems.addAll(items);
                        supportsGrouping = hasGroups(formItems);
                        rootElement = buildFormElement(items);
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

    // ------------------------------------------------------ build view

    private HTMLElement buildViewElement(List<ViewItem> items) {
        if (grouped && supportsGrouping) {
            HTMLContainerBuilder<HTMLDivElement> container = div().css(halComponent(HalClasses.resource, groups));
            Map<String, List<ViewItem>> itemGroups = groupByMetadata(items);
            for (Map.Entry<String, List<ViewItem>> entry : itemGroups.entrySet()) {
                String groupName = entry.getKey();
                List<ViewItem> groupItems = entry.getValue();
                HTMLElement dl = descriptionList().css(halComponent(HalClasses.resource, HalClasses.view)).element();
                for (ViewItem item : groupItems) {
                    dl.appendChild(item.element());
                }
                if (UNGROUPED.equals(groupName)) {
                    container.add(dl);
                } else {
                    ExpandableSection es = expandableSection()
                            .css(halComponent(HalClasses.resource, group))
                            .addToggle(expandableSectionToggle(capitalCase(groupName)))
                            .addContent(expandableSectionContent().add(dl));
                    container.add(es);
                    groupContainers.add(es.element());
                }
            }
            return container.element();
        } else {
            HTMLElement dl = descriptionList().css(halComponent(HalClasses.resource, HalClasses.view)).element();
            for (ViewItem item : items) {
                dl.appendChild(item.element());
            }
            return dl;
        }
    }

    // ------------------------------------------------------ build form

    private HTMLElement buildFormElement(List<FormItem> items) {
        currentForm = form().css(halComponent(resource, HalClasses.form)).horizontal();
        if (grouped && supportsGrouping) {
            Map<String, List<FormItem>> itemGroups = groupByMetadata(items);
            for (Map.Entry<String, List<FormItem>> entry : itemGroups.entrySet()) {
                String groupName = entry.getKey();
                List<FormItem> groupItems = entry.getValue();
                if (UNGROUPED.equals(groupName)) {
                    for (FormItem item : groupItems) {
                        currentForm.add(item.element());
                    }
                } else {
                    HTMLContainerBuilder<HTMLDivElement> groupContent = div()
                            .css(halComponent(HalClasses.resource, groupBody));
                    for (FormItem item : groupItems) {
                        groupContent.add(item.element());
                    }
                    ExpandableSection es = expandableSection()
                            .css(halComponent(HalClasses.resource, group))
                            .addToggle(expandableSectionToggle(capitalCase(groupName)))
                            .addContent(expandableSectionContent().add(groupContent));
                    currentForm.add(es);
                    groupContainers.add(es.element());
                }
            }
        } else {
            for (FormItem item : items) {
                currentForm.add(item.element());
            }
        }
        return currentForm.element();
    }

    // ------------------------------------------------------ grouping

    private <T extends IsElement<HTMLElement>> boolean hasGroups(List<T> items) {
        if (items.isEmpty()) {
            return false;
        }
        // check if there are items which implement ViewItem or FormItem with a non-null group
        for (T item : items) {
            String grp = itemGroup(item);
            if (grp != null) {
                return true;
            }
        }
        return false;
    }

    private <T extends IsElement<HTMLElement>> Map<String, List<T>> groupByMetadata(List<T> items) {
        List<T> ungrouped = new ArrayList<>();
        TreeMap<String, List<T>> namedGroups = new TreeMap<>();
        for (T item : items) {
            String grp = itemGroup(item);
            if (grp == null) {
                ungrouped.add(item);
            } else {
                namedGroups.computeIfAbsent(grp, k -> new ArrayList<>()).add(item);
            }
        }
        LinkedHashMap<String, List<T>> result = new LinkedHashMap<>();
        if (!ungrouped.isEmpty()) {
            result.put(UNGROUPED, ungrouped);
        }
        result.putAll(namedGroups);
        return result;
    }

    private <T> String itemGroup(T item) {
        if (item instanceof ViewItem) {
            return ((ViewItem) item).attribute().description().group();
        } else if (item instanceof FormItem) {
            return ((FormItem) item).attribute().description().group();
        }
        return null;
    }

    // ------------------------------------------------------ filtering

    private <T extends IsElement<HTMLElement>> List<T> filterByIncludes(List<T> items) {
        if (attributes.isEmpty()) {
            return items;
        }
        return items.stream().filter(item -> {
            ResolvedAttribute ra = itemAttribute(item);
            return ra != null && attributes.contains(ra.fqn());
        }).collect(Collectors.toList());
    }

    private <T> ResolvedAttribute itemAttribute(T item) {
        if (item instanceof ViewItem) {
            return ((ViewItem) item).attribute();
        } else if (item instanceof FormItem) {
            return ((FormItem) item).attribute();
        }
        return null;
    }

    private void onFilterChanged(Filter<ResolvedAttribute> filter, String origin) {
        if ((state == VIEW || state == EDIT) && isAttached(element())) {
            logger.debug("Filter attributes: %s", filter);
            int matchingItems = 0;

            List<? extends IsElement<HTMLElement>> items = state == VIEW ? viewItems : formItems;

            if (filter.defined()) {
                for (IsElement<HTMLElement> item : items) {
                    ResolvedAttribute ra = itemAttribute(item);
                    if (ra != null) {
                        boolean match = filter.match(ra);
                        item.element().classList.toggle(modifier(filtered), !match);
                        if (match) {
                            matchingItems++;
                        }
                    }
                }
                for (HTMLElement container : groupContainers) {
                    boolean hasVisibleItem = false;
                    for (IsElement<HTMLElement> item : items) {
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
                for (IsElement<HTMLElement> item : items) {
                    item.element().classList.remove(modifier(filtered));
                }
                for (HTMLElement container : groupContainers) {
                    setVisible(container, true);
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
        if (state == EDIT && !formItems.isEmpty()) {
            formItems.forEach(FormItem::resetValidation);
            boolean valid = formItems.stream().allMatch(FormItem::validate);
            if (valid) {
                List<Operation> ops = formItems.stream()
                        .filter(FormItem::isModified)
                        .flatMap(fi -> fi.operations(template.resolve()).stream())
                        .collect(Collectors.toList());
                uic().crud().update(template, ops)
                        .then(__ -> {
                            load(VIEW);
                            return null;
                        })
                        .catch_(error -> {
                            if (currentForm != null) {
                                currentForm.add(alert(danger, "Update failed").inline()
                                        .addDescription(String.valueOf(error)));
                            }
                            return null;
                        });
            } else {
                if (currentForm != null) {
                    currentForm.add(alert(danger, "Update failed").inline()
                            .addDescription("Please fix the validation errors before saving."));
                }
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
            groupContainers.clear();
            setVisible(toolbar, viewOrEdit);
        }
    }

    private boolean valid(ModelNode resource) {
        return resource != null && resource.isDefined() && !resource.asPropertyList().isEmpty();
    }
}

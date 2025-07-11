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
package org.jboss.hal.ui.modelbrowser;

import java.util.EnumSet;
import java.util.Set;

import org.jboss.elemento.Id;
import org.jboss.elemento.Key;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.tree.TraverseContext;
import org.jboss.hal.meta.tree.TraverseContinuation;
import org.jboss.hal.meta.tree.TraverseType;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.SelectInTree;
import org.patternfly.component.button.Button;
import org.patternfly.component.form.Form;
import org.patternfly.component.form.FormGroup;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.Radio;
import org.patternfly.component.form.TextArea;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.list.DescriptionList;
import org.patternfly.component.list.List;
import org.patternfly.component.list.ListItem;
import org.patternfly.component.modal.Modal;
import org.patternfly.component.popover.Popover;
import org.patternfly.core.Timeouts;
import org.patternfly.icon.IconSets;
import org.patternfly.layout.flex.FlexItem;
import org.patternfly.layout.flex.Gap;
import org.patternfly.style.Breakpoint;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.clearTimeout;
import static elemental2.dom.DomGlobal.setTimeout;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.strong;
import static org.jboss.elemento.EventType.keydown;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.modelBrowser;
import static org.jboss.hal.resources.HalClasses.results;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.divider.Divider.divider;
import static org.patternfly.component.divider.DividerType.hr;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateHeader.emptyStateHeader;
import static org.patternfly.component.form.Form.form;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.FormGroupLabel.formGroupLabel;
import static org.patternfly.component.form.FormGroupRole.radiogroup;
import static org.patternfly.component.form.Radio.radio;
import static org.patternfly.component.form.TextArea.textArea;
import static org.patternfly.component.form.TextAreaResize.vertical;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.help.HelperText.helperText;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.component.modal.Modal.modal;
import static org.patternfly.component.modal.ModalBody.modalBody;
import static org.patternfly.component.modal.ModalFooter.modalFooter;
import static org.patternfly.component.popover.Popover.popover;
import static org.patternfly.component.popover.PopoverBody.popoverBody;
import static org.patternfly.component.popover.PopoverHeader.popoverHeader;
import static org.patternfly.layout.flex.Direction.column;
import static org.patternfly.layout.flex.Display.inlineFlex;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.Gap.sm;
import static org.patternfly.layout.grid.Grid.grid;
import static org.patternfly.layout.grid.GridItem.gridItem;
import static org.patternfly.popper.Placement.auto;
import static org.patternfly.style.Breakpoints.breakpoints;
import static org.patternfly.style.Classes.search;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Size.lg;
import static org.patternfly.token.Token.globalTextColorDisabled;

class FindResource {

    private final HTMLElement trigger;
    private final TraverseContinuation continuation;
    private final Modal searchModal;
    private final FlexItem searchResults;
    private final FlexItem status;
    private final FlexItem noResults;
    private final List matchingResources;
    private final FormGroupControl nameControl;
    private final TextInput nameInput;
    private final TextInput rootInput;
    private final TextArea excludeTextArea;
    private final Radio scopeAddressRadio;
    private final Radio scopeTypeRadio;
    private final Radio scopeNameRadio;
    private final Radio comparisonContainsRadio;
    private final Button searchButton;
    private double timeout;

    // ------------------------------------------------------ ui

    FindResource(HTMLElement trigger, String rootAddress) {
        this.trigger = trigger;
        this.continuation = new TraverseContinuation();

        String baseId = "search-resource";
        String nameId = Id.build(baseId, "name");
        String rootId = Id.build(baseId, "root");
        String excludeId = Id.build(baseId, "exclude");
        String scopeId = Id.build(baseId, "scope");
        String comparisonId = Id.build(baseId, "comparison");

        DescriptionList scopeDescription = descriptionList().horizontal().compact()
                .addItem(descriptionListGroup(Id.build(scopeId, "address-help"))
                        .addTerm(descriptionListTerm("Address"))
                        .addDescription(descriptionListDescription()
                                .add(div().text(
                                        "Search anywhere in the resource address"))
                                .add(flex().display(inlineFlex).columnGap(sm)
                                        .add(span().add(strong().text("data")))
                                        .add(span().text("→"))
                                        .add(span()
                                                .add("/subsystem=")
                                                .add(strong().text("data"))
                                                .add("sources/")
                                                .add(strong().text("data"))
                                                .add("-source=ExampleDS")))))
                .addItem(descriptionListGroup(Id.build(scopeId, "type-help"))
                        .addTerm(descriptionListTerm("Type"))
                        .addDescription(descriptionListDescription()
                                .add(div().text("Search in the resource type"))
                                .add(flex().display(inlineFlex).columnGap(sm)
                                        .add(span().add(strong().text("data")))
                                        .add(span().text("→"))
                                        .add(span()
                                                .add("/subsystem=datasources/")
                                                .add(strong().text("data"))
                                                .add("-source=ExampleDS")))))
                .addItem(descriptionListGroup(Id.build(scopeId, "name-help"))
                        .addTerm(descriptionListTerm("Name"))
                        .addDescription(descriptionListDescription()
                                .add(div().text("Search in the resource name"))
                                .add(flex().display(inlineFlex).columnGap(sm)
                                        .add(span().add(strong().text("example")))
                                        .add(span().text("→"))
                                        .add(span()
                                                .add("/subsystem=datasources/data-source=")
                                                .add(strong().text("Example"))
                                                .add("DS")))));

        DescriptionList comparisonDescription = descriptionList().horizontal().compact()
                .addItem(descriptionListGroup(Id.build(comparisonId, "contains-help"))
                        .addTerm(descriptionListTerm("contains"))
                        .addDescription(descriptionListDescription()
                                .add(div()
                                        .text("The search result must contain the search term"))
                                .add(flex().display(inlineFlex).columnGap(sm)
                                        .add(span().add(strong().text("example")))
                                        .add(span().text("→"))
                                        .add(span()
                                                .add("/subsystem=datasources/data-source=")
                                                .add(strong().text("Example"))
                                                .add("DS")))))
                .addItem(descriptionListGroup(Id.build(comparisonId, "equals-help"))
                        .addTerm(descriptionListTerm("equals"))
                        .addDescription(descriptionListDescription()
                                .add(div().text("The search result must match the search term"))
                                .add(flex().display(inlineFlex).columnGap(sm)
                                        .add(span().add(strong().text("exampleds")))
                                        .add(span().text("→"))
                                        .add(span()
                                                .add("/subsystem=datasources/data-source=")
                                                .add(strong().text("ExampleDS"))))));

        Popover scopeInfo = popover()
                .autoWidth()
                .placement(auto)
                .addHeader(popoverHeader().text("Where to search"))
                .addBody(popoverBody()
                        .add(flex().direction(column).rowGap(Gap.md)
                                .addItem(flexItem().add(div().text("Given comparison is 'contains':")))
                                .addItem(flexItem().add(scopeDescription))
                                .addItem(flexItem().add(div().text("The search is case insensitive by default.")))));

        Popover comparisonInfo = popover()
                .autoWidth()
                .placement(auto)
                .addHeader(popoverHeader().text("How to search"))
                .addBody(popoverBody()
                        .add(flex().direction(column).rowGap(Gap.md)
                                .addItem(flexItem().add(div().text("Given scope is 'Name':")))
                                .addItem(flexItem().add(comparisonDescription))
                                .addItem(flexItem().add(div().text("The search is case insensitive by default.")))));

        FormGroup nameFormGroup = formGroup(nameId).required()
                .addLabel(formGroupLabel("Name"))
                .addControl(nameControl = formGroupControl()
                        .addControl(nameInput = textInput(nameId)
                                .on(keydown, e -> {
                                    if (Key.Enter.match(e)) {
                                        search();
                                    }
                                })));

        FormGroup rootFormGroup = formGroup(rootId)
                .addLabel(formGroupLabel("Root"))
                .addControl(formGroupControl()
                        .addControl(rootInput = textInput(rootId).value(rootAddress)
                                .on(keydown, e -> {
                                    if (Key.Enter.match(e)) {
                                        search();
                                    }
                                }))
                        .addHelperText(helperText("Leave empty to search the whole model.")));

        FormGroup excludeFormGroup = formGroup(excludeId)
                .addLabel(formGroupLabel("Exclude"))
                .addControl(formGroupControl()
                        .addControl(excludeTextArea = textArea(excludeId)
                                .autoResize()
                                .resize(vertical)
                                .value("/core-service"))
                        .addHelperText(helperText("Enter resource addresses to exclude (separated by line breaks)")));

        FormGroup scopeFormGroup = formGroup(scopeId).role(radiogroup)
                .addLabel(formGroupLabel("Scope").noPaddingTop().help("Where to search", scopeInfo))
                .addControl(formGroupControl().inline()
                        .addRadio(scopeAddressRadio = radio(Id.build(scopeId, "address"), scopeId, "Address"))
                        .addRadio(scopeTypeRadio = radio(Id.build(scopeId, "type"), scopeId, "Type"))
                        .addRadio(scopeNameRadio = radio(Id.build(scopeId, "name"), scopeId, "Name")
                                .value(true, false)));

        FormGroup comparisonFormGroup = formGroup(comparisonId).role(radiogroup)
                .addLabel(formGroupLabel("Comparison").noPaddingTop().help("How to search", comparisonInfo))
                .addControl(formGroupControl().inline()
                        .addRadio(comparisonContainsRadio = radio(Id.build(comparisonId, "contains"),
                                comparisonId,
                                "contains")
                                .value(true, false))
                        .addRadio(radio(Id.build(comparisonId, "equals"), comparisonId, "equals")));

        Form searchForm = form().horizontal()
                .add(grid().gutter().columns(breakpoints(Breakpoint.md, 6))
                        .addItem(gridItem().span(12).add(nameFormGroup))
                        .addItem(gridItem().span(12).add(rootFormGroup))
                        .addItem(gridItem().span(12).add(excludeFormGroup))
                        .addItem(gridItem().span(6).add(scopeFormGroup))
                        .addItem(gridItem().span(6).add(comparisonFormGroup)));

        searchResults = flexItem()
                .add(flex().direction(column).rowGap(Gap.md)
                        .addItem(status = flexItem().css(util("text-truncate")))
                        .addItem(flexItem()
                                .add(matchingResources = list().css(halComponent(modelBrowser, search, results))
                                        .plain())));

        noResults = flexItem()
                .add(emptyState()
                        .addHeader(emptyStateHeader()
                                .icon(IconSets.fas.search())
                                .text("No results found"))
                        .addBody(emptyStateBody()
                                .text("No resources match the search criteria.")));

        searchModal = modal().css(halComponent(modelBrowser, search))
                .top()
                .size(lg)
                .addHeader("Find a resource")
                .addBody(modalBody()
                        .add(flex().direction(column).rowGap(Gap.md)
                                .addItem(flexItem().add(searchForm))
                                .addItem(flexItem().add(divider(hr)))
                                .addItem(searchResults)
                                .addItem(noResults)))
                .addFooter(modalFooter()
                        .addButton(searchButton = button("Search").primary()
                                .progress(false, "Search is in progress")
                                .onClick((e, b) -> search()))
                        .addButton(button("Cancel").link().onClick((e, b) -> close())))
                .appendToBody();
        setVisible(searchResults, false);
        setVisible(noResults, false);
    }

    // ------------------------------------------------------ state

    void open() {
        searchModal.open();
        nameInput.input().element().focus();
    }

    private void close() {
        endSearch();
        searchModal.close();
    }

    private void search() {
        if (continuation.isRunning()) {
            continuation.stop();
        } else {
            if (nameInput.value().isEmpty()) {
                nameControl.addHelperText(helperText("Must not be empty", error));
                nameInput.validated(error);
            } else {
                nameControl.removeHelperText();
                nameInput.resetValidation();
                matchingResources.clear();
                removeChildrenFrom(status);
                status.style("color", globalTextColorDisabled.var);
                setVisible(searchResults, true);
                setVisible(noResults, false);

                String name = nameInput.value();
                Set<String> exclude = stream(excludeTextArea.value().split("\\r?\\n"))
                        .filter(s -> !s.trim().isEmpty())
                        .collect(toSet());
                boolean contains = comparisonContainsRadio.value();
                AddressTemplate rootTemplate = AddressTemplate.of(rootInput.value());
                timeout = setTimeout(__ -> searchButton.text("Stop").startProgress(), Timeouts.LOADING_TIMEOUT);
                uic().modelTree().traverse(continuation, rootTemplate, exclude, EnumSet.noneOf(TraverseType.class),
                                (template, traverseContext) -> {
                                    status.text("Process " + template.toString());
                                    String argument = "";
                                    if (scopeAddressRadio.value()) {
                                        argument = template.template;
                                    } else if (scopeTypeRadio.value()) {
                                        argument = template.last().key;
                                    } else if (scopeNameRadio.value()) {
                                        argument = template.last().value;
                                    }
                                    boolean match = contains
                                            ? argument.toLowerCase().contains(name.toLowerCase())
                                            : argument.equalsIgnoreCase(name);
                                    if (match) {
                                        ListItem listItem = listItem()
                                                .add(button().link().inline().text(template.toString())
                                                        .onClick((e, b) -> {
                                                            SelectInTree.dispatch(trigger, template);
                                                            close();
                                                        }));
                                        matchingResources.addItem(listItem);
                                        listItem.element().scrollIntoView();
                                    }
                                })
                        .then(context -> {
                            results(context);
                            return null;
                        })
                        .catch_(error -> {
                            error(String.valueOf(error));
                            return null;
                        });
            }
        }
    }

    private void results(TraverseContext context) {
        if (matchingResources.isEmpty()) {
            setVisible(searchResults, false);
            setVisible(noResults, true);
        } else {
            removeChildrenFrom(status);
            status.style("color", "inherit")
                    .add("Found ")
                    .add(strong().text(String.valueOf(matchingResources.size())))
                    .add(" matches in ")
                    .add(strong().text(String.valueOf(context.processed())))
                    .add(" resources.");
        }
        endSearch();
    }

    private void error(String reason) {
        String failSafeReason = reason != null && !reason.isEmpty() ? "Unknown error" : reason;
        status.text("Error while searching: " + failSafeReason);
        endSearch();
    }

    private void endSearch() {
        clearTimeout(timeout);
        searchButton.text("Search").stopProgress();
    }
}

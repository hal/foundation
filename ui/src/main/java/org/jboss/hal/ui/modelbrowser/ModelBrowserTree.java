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

import java.util.ArrayList;
import java.util.List;

import org.jboss.elemento.IsElement;
import org.jboss.elemento.flow.Flow;
import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Segment;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.resources.Keys;
import org.patternfly.component.button.Button;
import org.patternfly.component.tooltip.Tooltip;
import org.patternfly.component.tree.TreeView;
import org.patternfly.component.tree.TreeViewItem;
import org.patternfly.style.Sticky;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.tree;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserEngine.mbn2tvi;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.page.PageSection.pageSection;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.component.tree.TreeView.treeView;
import static org.patternfly.component.tree.TreeViewType.selectableItems;
import static org.patternfly.core.AsyncStatus.pending;
import static org.patternfly.icon.IconSets.far.minusSquare;
import static org.patternfly.icon.IconSets.fas.arrowLeft;
import static org.patternfly.icon.IconSets.fas.arrowRight;
import static org.patternfly.icon.IconSets.fas.home;
import static org.patternfly.icon.IconSets.fas.redo;
import static org.patternfly.icon.IconSets.fas.search;
import static org.patternfly.popper.Placement.bottom;
import static org.patternfly.popper.Placement.bottomStart;
import static org.patternfly.style.Classes.insetNone;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Padding.noPadding;

class ModelBrowserTree implements IsElement<HTMLElement> {

    // ------------------------------------------------------ instance

    private static final Logger logger = Logger.getLogger(ModelBrowserTree.class.getName());
    private final ModelBrowser modelBrowser;
    private final History<TreeViewItem> history;
    private final Button backButton;
    private final Button forwardButton;
    private final TreeView treeView;
    private final HTMLElement root;
    private Tooltip backTooltip;
    private Tooltip forwardTooltip;

    ModelBrowserTree(ModelBrowser modelBrowser) {
        this.modelBrowser = modelBrowser;
        this.history = new History<>();

        treeView = treeView(selectableItems).guides()
                .onSelect((event, treeViewItem, selected) -> navigate(treeViewItem, true));
        backButton = button().plain().icon(arrowLeft()).disabled().onClick((event, component) -> back());
        forwardButton = button().plain().icon(arrowRight()).disabled().onClick((event, component) -> forward());
        Button refreshButton = button().plain().icon(redo()).onClick((e, b) -> reload());
        Button homeButton = button().plain().icon(home()).onClick((e, b) -> modelBrowser.home());
        Button findResource = button().plain()
                .icon(search())
                .onClick((e, b) -> new FindResource(b.element(), selectedAddress()).open());
        GotoResource gotoResource = new GotoResource();
        Button collapseButton = button().plain().icon(minusSquare()).onClick((e, b) -> treeView.collapse());

        tooltip(refreshButton.element(), "Refresh").placement(bottom).appendToBody();
        tooltip(homeButton.element(), "Home").placement(bottom).appendToBody();
        tooltip(findResource.element(), "Find a resource").placement(bottom).appendToBody();
        tooltip(gotoResource.element(), "Go to resource").placement(bottom).appendToBody();
        tooltip(collapseButton.element(), "Collapse all").placement(bottom).appendToBody();

        root = div().css(halComponent(HalClasses.modelBrowser, tree))
                .add(pageSection().sticky(Sticky.top).padding(noPadding)
                        .add(toolbar().css(modifier(insetNone))
                                .addContent(toolbarContent()
                                        .addItem(toolbarItem().css(modifier("spacer-none")).add(backButton))
                                        .addItem(toolbarItem().css(modifier("spacer-none")).add(forwardButton))
                                        .addItem(toolbarItem().css(modifier("spacer-none")).add(refreshButton))
                                        .addItem(toolbarItem().css(modifier("spacer-none")).add(homeButton))
                                        .addItem(toolbarItem().css(modifier("spacer-none")).add(findResource))
                                        .addItem(toolbarItem().css(modifier("spacer-none")).add(gotoResource))
                                        .addItem(toolbarItem().css(modifier("spacer-none")).add(collapseButton)))))
                .add(pageSection().padding(noPadding).add(treeView))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ load

    void load(List<ModelBrowserNode> nodes) {
        treeView.clear();
        treeView.addItems(nodes, mbn2tvi(uic().dispatcher()));
    }

    void reload() {
        if (!treeView.selectedItems().isEmpty()) {
            treeView.selectedItems().get(0).reload();
        } else {
            // no selection → load root
            modelBrowser.load();
        }
    }

    // ------------------------------------------------------ select

    void select(String identifier) {
        treeView.select(identifier);
    }

    void select(String parentIdentifier, String childIdentifier) {
        TreeViewItem parentItem = treeView.findItem(parentIdentifier);
        if (parentItem != null) {
            if (!parentItem.expanded() && parentItem.status() == pending) {
                parentItem.load().then(__ -> {
                    treeView.select(childIdentifier);
                    return null;
                });
            } else if (!parentItem.contains(childIdentifier)) {
                // child might have been added externally in CLI or other management tools
                parentItem.reload().then(__ -> {
                    treeView.select(childIdentifier);
                    return null;
                });
            } else {
                treeView.select(childIdentifier);
            }
        } else {
            treeView.select(childIdentifier);
        }
    }

    void select(AddressTemplate template) {
        if (!template.isEmpty() && template.fullyQualified()) {
            TreeViewItem item = treeView.findItem(template.identifier());
            if (item != null) {
                treeView.select(item);
            } else {
                Flow.sequential(new FlowContext(), selectTasks(template)).subscribe(context -> {
                    if (context.isSuccessful()) {
                        // The template might contain invalid segments or no longer exist.
                        // Build a template up to the last valid segment.
                        AddressTemplate current = AddressTemplate.root();
                        for (Segment segment : template) {
                            if (treeView.findItem(current.append(segment.key, segment.value).identifier()) == null) {
                                break;
                            }
                            current = current.append(segment.key, segment.value);
                        }
                        treeView.select(current.identifier());
                    } else {
                        logger.error("Unable to select template %s: %s", template, context.failure());
                    }
                });
            }
        } else {
            logger.error("Unable to select %s: Template is empty or not fully qualified.", template);
        }
    }

    void unselect() {
        treeView.unselect(false);
    }

    String selectedAddress() {
        if (!treeView.selectedItems().isEmpty()) {
            TreeViewItem tvi = treeView.selectedItems().get(0);
            ModelBrowserNode mbn = tvi.get(Keys.MODEL_BROWSER_NODE);
            if (mbn != null) {
                return mbn.template.toString();
            }
        }
        return "";
    }

    private List<Task<FlowContext>> selectTasks(AddressTemplate template) {
        /*
         Relation between the template and the tree view item IDs:

         # Template with 3 segments → 6 item IDs
         subsystem=infinispan/cache-container=web/local-cache=sso
         s-subsystem-e-w
         s-subsystem-e-infinispan
         s-subsystem-e-infinispan-s-cache-container-e-w
         s-subsystem-e-infinispan-s-cache-container-e-web
         s-subsystem-e-infinispan-s-cache-container-e-web-s-local-cache-e-w
         s-subsystem-e-infinispan-s-cache-container-e-web-s-local-cache-e-sso

         # Template with 5 segments → 10 item IDs
         /core-service=management/access=authorization/constraint=application-classification/type=core/classification=deployment
         s-core-service-e-w
         s-core-service-e-management
         s-core-service-e-management-s-access-e-w
         s-core-service-e-management-s-access-e-authorization
         s-core-service-e-management-s-access-e-authorization-s-constraint-e-w
         s-core-service-e-management-s-access-e-authorization-s-constraint-e-application-classification
         s-core-service-e-management-s-access-e-authorization-s-constraint-e-application-classification-s-type-e-w
         s-core-service-e-management-s-access-e-authorization-s-constraint-e-application-classification-s-type-e-core
         s-core-service-e-management-s-access-e-authorization-s-constraint-e-application-classification-s-type-e-core-s-classification-e-w
         s-core-service-e-management-s-access-e-authorization-s-constraint-e-application-classification-s-type-e-core-s-classification-e-deployment
         */
        List<Task<FlowContext>> tasks = new ArrayList<>();
        AddressTemplate current = AddressTemplate.root();
        for (Segment segment : template) {
            String wildcardItemId = current.append(segment.key, "*").identifier();
            String valueItemId = current.append(segment.key, segment.value).identifier();
            tasks.add(context -> treeView.load(wildcardItemId).then(items -> context.resolve()));
            tasks.add(context -> treeView.load(valueItemId).then(items -> context.resolve()));
            current = current.append(segment.key, segment.value);
        }
        return tasks;
    }

    // ------------------------------------------------------ navigation

    private void back() {
        if (history.canGoBack()) {
            TreeViewItem treeViewItem = history.back();
            treeView.select(treeViewItem, true, false);
            navigate(treeViewItem, false);
        }
    }

    private void forward() {
        if (history.canGoForward()) {
            TreeViewItem treeViewItem = history.forward();
            treeView.select(treeViewItem, true, false);
            navigate(treeViewItem, false);
        }
    }

    private void navigate(TreeViewItem treeViewItem, boolean updateHistory) {
        if (updateHistory) {
            history.navigate(treeViewItem);
        }
        updateNavigationButtons();
        ModelBrowserNode node = treeViewItem.get(Keys.MODEL_BROWSER_NODE);
        if (node != null) {
            modelBrowser.detail.show(node);
        }
    }

    private void updateNavigationButtons() {
        backButton.disabled(!history.canGoBack());
        forwardButton.disabled(!history.canGoForward());

        if (history.canGoBack()) {
            ModelBrowserNode node = null;
            TreeViewItem treeViewItem = history.peekBack();
            if (treeViewItem != null) {
                node = treeViewItem.get(Keys.MODEL_BROWSER_NODE);
            }
            if (backTooltip == null) {
                backTooltip = tooltip(backButton.element(), "")
                        .placement(bottomStart)
                        .appendToBody();
            }
            if (node != null) {
                backTooltip.text("Go back to " + node.template);
            }
        } else {
            failSafeRemoveFromParent(backTooltip);
            backTooltip = null;
        }

        if (history.canGoForward()) {
            ModelBrowserNode node = null;
            TreeViewItem treeViewItem = history.peekForward();
            if (treeViewItem != null) {
                node = treeViewItem.get(Keys.MODEL_BROWSER_NODE);
            }
            if (forwardTooltip == null) {
                forwardTooltip = tooltip(forwardButton.element(), "")
                        .placement(bottomStart)
                        .appendToBody();
            }
            if (node != null) {
                forwardTooltip.text("Go forward to " + node.template);
            }
        } else {
            failSafeRemoveFromParent(forwardTooltip);
            forwardTooltip = null;
        }
    }
}

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

import org.jboss.elemento.IsElement;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.core.Notifications;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.AddResource;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.DeleteResource;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.SelectInTree;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_SINGLETONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_TYPES_OPERATION;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.modelBrowser;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserEngine.parseChildren;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.RESOURCE;
import static org.jboss.hal.ui.resource.ResourceDialogs.addResource;
import static org.jboss.hal.ui.resource.ResourceDialogs.deleteResource;
import static org.patternfly.layout.grid.Grid.grid;
import static org.patternfly.layout.grid.GridItem.gridItem;

public class ModelBrowser implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static ModelBrowser modelBrowser(AddressTemplate template) {
        return new ModelBrowser(template);
    }

    // ------------------------------------------------------ instance

    private static final Logger logger = Logger.getLogger(ModelBrowser.class.getName());
    final ModelBrowserTree tree;
    final ModelBrowserDetail detail;
    private final AddressTemplate template;
    private final HTMLElement root;
    private ModelBrowserNode rootMbn;

    public ModelBrowser(AddressTemplate template) {
        this.template = template;
        this.tree = new ModelBrowserTree(this);
        this.detail = new ModelBrowserDetail(this);
        this.root = grid().span(12)
                .css(halComponent(modelBrowser))
                // TODO Implement a resize handle which modifies the CSS variables
                //  --hal-c-model-browser-tree-columns: span 3;
                //  --hal-c-model-browser-detail-columns: span 9;
                /*
                                .addItem(gridItem()
                                        .add(flex().spaceItems(none)
                                                .addItem(flexItem().flex(_1)
                                                        .add(tree))
                                                .addItem(flexItem().alignSelf(stretch)
                                                        .add(div().css(halComponent(modelBrowser, resize))
                                                                .add(div().css(halComponent(modelBrowser, resize, handle))))))
                                        .add(detail))
                */
                .addItem(gridItem().span(3).add(tree))
                .addItem(gridItem().span(9).add(detail))
                .element();

        AddResource.listen(root, this::add);
        DeleteResource.listen(root, this::delete);
        SelectInTree.listen(root, this::select);
        load();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ internal

    void home() {
        if (rootMbn != null) {
            tree.unselect();
            detail.show(rootMbn);
        }
    }

    void load() {
        if (template.fullyQualified()) {
            uic().metadataRepository().lookup(template, metadata -> {
                ResourceAddress address = template.resolve(uic().statementContext());
                Operation operation = new Operation.Builder(address, READ_CHILDREN_TYPES_OPERATION)
                        .param(INCLUDE_SINGLETONS, true)
                        .build();
                uic().dispatcher().execute(operation, result -> {
                    String name = template.isEmpty() ? "Management Model" : template.last().value;
                    rootMbn = new ModelBrowserNode(template, name, RESOURCE);
                    tree.load(parseChildren(rootMbn, result, true));
                    detail.show(rootMbn);
                });
            });
        } else {
            // TODO Add error empty state
            logger.error("Illegal address: %s. Please specify a fully qualified address not ending with '*'", template);
        }
    }

    private void add(AddResource.Details details) {
        addResource(details.parent, details.child, details.singleton).then(__ -> {
            tree.select(details.parent.identifier());
            tree.reload();
            return null;
        });
    }

    private void delete(DeleteResource.Details details) {
        deleteResource(details.template)
                .then(__ -> {
                    tree.select(details.template.anonymiseLast().identifier());
                    tree.reload();
                    return null;
                })
                .catch_(error -> {
                    Notifications.error("Failed to delete resource", String.valueOf(error));
                    return null;
                });
    }

    private void select(SelectInTree.Details details) {
        if (rootMbn != null) {
            if (details.template != null) {
                // select by address template
                if (details.template.template.startsWith(rootMbn.template.template)) {
                    tree.select(details.template);
                } else {
                    logger.error("Unable to select %s: %s is not a sub-template of %s",
                            details.template, details.template, rootMbn.template);
                }
            } else if (details.identifier != null) {
                if (details.parentIdentifier != null) {
                    // select by parent/child identifier
                    tree.select(details.parentIdentifier, details.identifier);
                } else {
                    // select by identifier
                    tree.select(details.identifier);
                }
            }
        } else {
            logger.error("Unable to select %s: Root template is null!", template);
        }
    }
}
